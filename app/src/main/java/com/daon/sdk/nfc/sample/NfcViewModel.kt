package com.daon.sdk.nfc.sample

import android.app.Application
import android.content.Intent
import android.nfc.NfcAdapter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.daon.sdk.nfc.NFCReader
import com.daon.sdk.nfc.TagData
import com.daon.sdk.nfc.TagError
import com.daon.sdk.nfc.TagReaderCallback
import com.daon.sdk.nfc.TagReaderInfo
import com.daon.sdk.nfc.reader.passport.PassportParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Represents the current state of the NFC scanning dialog. */
enum class NfcScanState {
    IN_PROGRESS, SUCCESS, FAIL
}

/**
 * Describes whether NFC is usable on this device at the moment [NfcViewModel.startScan]
 * is called, as reported by the SDK's own [com.daon.sdk.nfc.NFCReader.isSupported] and
 * [com.daon.sdk.nfc.NFCReader.isEnabled] APIs.
 */
enum class NfcAvailability {
    /** The device has no NFC hardware ([NFCReader.isSupported] returned false). */
    NOT_SUPPORTED,

    /** NFC hardware is present but switched off in settings ([NFCReader.isEnabled] returned false). */
    DISABLED,
}

/**
 * Holds the data surfaced to the UI during and after an NFC scan.
 *
 * @param scanState Current scan state.
 * @param message Human-readable status or phase name; shown below the NFC indicator.
 * @param progress Normalised read progress in `[0.0, 1.0]`, or `null` when no progress
 *   is available yet (indeterminate). Drives the [LinearProgressIndicator] in the dialog.
 * @param result Full [TagData] returned on a successful read, or `null` otherwise.
 */
data class NfcUiState(
    val scanState: NfcScanState = NfcScanState.IN_PROGRESS,
    val message: String = "",
    val progress: Float? = null,
    val result: TagData? = null,
)

/**
 * ViewModel that owns the [NFCReader] instance and bridges its callbacks into
 * Compose-observable [StateFlow]s.
 *
 * @param application Application context used for localized scan status messages.
 */
class NfcViewModel(application: Application) : AndroidViewModel(application) {

    private val _dialogVisible = MutableStateFlow(false)
    /** Whether the NFC scanning dialog should be shown. */
    val dialogVisible: StateFlow<Boolean> = _dialogVisible.asStateFlow()

    private val _uiState = MutableStateFlow(NfcUiState())
    /** Current scan state exposed to the UI. */
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    /** Whether the result screen should be shown instead of the scan screen. */
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    private val _nfcAvailability = MutableStateFlow<NfcAvailability?>(null)
    /**
     * Non-null when the SDK reported NFC as absent or disabled at the time [startScan] was
     * called.  The UI should present an error dialog and call [dismissNfcUnavailable] to
     * clear it.
     */
    val nfcAvailability: StateFlow<NfcAvailability?> = _nfcAvailability.asStateFlow()

    // `internal` visibility is intentional: the property must remain private to callers
    // outside this module but needs to be injectable in unit tests (where constructing a
    // real NFCReader is not possible because it requires a live FragmentActivity).
    internal var nfcReader: NFCReader? = null
    private var pendingParams: PassportParameters? = null

    private var isActivityResumed = false
    // The published SDK cannot report whether enable() registered dispatch.
    private var readerNeedsDisable = false
    private var isReadingTag = false
    private var scanGeneration = 0
    private var pendingTagIntent: Intent? = null

    private fun syncReader() {
        val shouldEnable = isActivityResumed && _dialogVisible.value &&
            _uiState.value.scanState == NfcScanState.IN_PROGRESS
        val reader = nfcReader
        if (shouldEnable) {
            if (reader != null) {
                readerNeedsDisable = true
                reader.enable()
            }
        } else if (readerNeedsDisable) {
            reader?.disable()
            readerNeedsDisable = false
        }
    }

    private fun tagReaderCallback(generation: Int) = object : TagReaderCallback {
        override fun tagReaderDidFinish(data: TagData) {
            if (generation != scanGeneration) return
            scanGeneration++
            isReadingTag = false
            Log.d("DAON", "tagReaderDidFinish")
            _uiState.value = NfcUiState(
                scanState = NfcScanState.SUCCESS,
                message = getApplication<Application>().getString(R.string.nfc_status_scan_complete),
                result = data,
            )
            syncReader()
        }

        override fun tagReaderFailed(code: Short, message: String?) {
            if (generation != scanGeneration) return
            scanGeneration++
            isReadingTag = false
            Log.d("DAON", "tagReaderFailed: $message")
            val errorMessage = when (code) {
                TagError.ERROR_NO_ACCESS -> getApplication<Application>().getString(R.string.nfc_error_authentication_failed)
                TagError.ERROR_CONNECTION_LOST -> getApplication<Application>().getString(R.string.nfc_error_connection_lost)
                TagError.ERROR_TAG_NOT_SUPPORTED -> getApplication<Application>().getString(R.string.nfc_error_tag_not_supported)
                TagError.ERROR_PACE_NOT_SUPPORTED -> getApplication<Application>().getString(R.string.nfc_error_pace_not_supported)
                TagError.ERROR_NO_DATA,
                TagError.ERROR_NOT_IMPLEMENTED -> getApplication<Application>().getString(R.string.nfc_error_unable_to_read)
                TagError.ERROR_LICENSE -> getApplication<Application>().getString(R.string.nfc_error_license, message.orEmpty())
                else -> getApplication<Application>().getString(R.string.nfc_error_read_failed)
            }
            _uiState.value = NfcUiState(scanState = NfcScanState.FAIL, message = errorMessage)
            syncReader()
        }

        override fun tagReaderInformation(info: TagReaderInfo) {
            if (generation != scanGeneration) return
            _uiState.value = NfcUiState(
                scanState = NfcScanState.IN_PROGRESS,
                message = info.message ?: "",
                progress = (info.percent / 100f).takeIf { info.percent > 0 },
            )
        }

        override fun onAnalyticsDataReady(name: String, analyticsData: String) {
            // Implement if analytics data from the SDK is required.
        }
    }

    /**
     * Creates (or re-creates) the [NFCReader] bound to the given [activity].
     * Must be called on every [MainActivity.onCreate] — including after a
     * configuration change — so that the reader always holds a reference to
     * the current, live activity instance.
     *
     * Keeping a stale reference from a previous (destroyed) activity causes
     * [NfcAdapter.enableForegroundDispatch] to throw [IllegalStateException]
     * on Android 11–14, because the platform verifies that the activity passed
     * to that API is the currently resumed foreground activity.
     *
     * @param activity The hosting [androidx.fragment.app.FragmentActivity].
     */
    fun initReader(activity: androidx.fragment.app.FragmentActivity) {
        nfcReader?.disable()
        readerNeedsDisable = false
        nfcReader = NFCReader(activity, "license.txt", false)
        Log.d("DAON", "NFCReader initialised — SDK version: ${NFCReader.version()}")
    }

    /**
     * Validates form input and starts a scan if valid. Returns the field requiring correction.
     */
    fun startScan(input: ScanInput): ScanInputError? {
        val error = input.validate()
        if (error != null) return error
        startScan(input.toParameters())
        return null
    }

    /**
     * Enables NFC foreground dispatch and shows the scanning dialog.
     *
     * Before opening the dialog the method queries the SDK — [NFCReader.isSupported] and
     * [NFCReader.isEnabled] — to verify that NFC is usable.  If either check fails,
     * [nfcAvailability] is updated with the reason and the method returns early without
     * opening the dialog.  The UI should observe [nfcAvailability] and call
     * [dismissNfcUnavailable] once the user has dismissed the error.
     *
     * @param params Passport parameters derived from the user's input.
     */
    fun startScan(params: PassportParameters) {
        val reader = nfcReader
        if (reader != null) {
            when {
                !reader.isSupported() -> {
                    _nfcAvailability.value = NfcAvailability.NOT_SUPPORTED
                    return
                }
                !reader.isEnabled() -> {
                    _nfcAvailability.value = NfcAvailability.DISABLED
                    return
                }
            }
        }
        if (isReadingTag) {
            nfcReader?.disable()
            readerNeedsDisable = false
        }
        pendingTagIntent = null
        scanGeneration++
        isReadingTag = false
        pendingParams = params
        _uiState.value = NfcUiState(scanState = NfcScanState.IN_PROGRESS, message = getApplication<Application>().getString(R.string.nfc_status_hold_near_document))
        _dialogVisible.value = true

        syncReader()
    }

    /**
     * Dismisses the NFC scanning dialog and disables foreground dispatch.
     * Should be called when the user cancels the dialog.
     */
    fun cancelScan() {
        pendingTagIntent = null
        scanGeneration++
        isReadingTag = false
        _dialogVisible.value = false
        _showResult.value = false
        _uiState.value = NfcUiState()
        syncReader()
    }

    /**
     * Clears the [nfcAvailability] error state.
     * Call from the UI after the user dismisses the NFC unavailable / disabled dialog.
     */
    fun dismissNfcUnavailable() {
        _nfcAvailability.value = null
    }

    /**
     * Resets a failed scan to [NfcScanState.IN_PROGRESS] using the previously stored
     * [pendingParams]. Dispatch is re-enabled only when the activity is resumed.
     */
    fun retryScan() {
        if (pendingParams == null || !_dialogVisible.value || _uiState.value.scanState != NfcScanState.FAIL) return
        pendingTagIntent = null
        scanGeneration++
        isReadingTag = false
        _uiState.value = NfcUiState(
            scanState = NfcScanState.IN_PROGRESS,
            message = getApplication<Application>().getString(R.string.nfc_status_hold_near_document),
        )
        syncReader()
    }

    /**
     * Hides the scanning dialog and transitions to the result screen.
     * Should be called when the user taps "Done" after a successful scan.
     */
    fun showResults() {
        _dialogVisible.value = false
        _showResult.value = true
        syncReader()
    }

    /**
     * Returns to the scan screen and resets all state.
     * Should be called when the user taps "Scan Again" from the result screen.
     */
    fun resetResult() {
        _showResult.value = false
        _uiState.value = NfcUiState()
    }

    /**
     * Cancels an active read and disables NFC foreground dispatch on pause.
     */
    fun disableReader() {
        isActivityResumed = false
        pendingTagIntent = null
        if (isReadingTag) {
            scanGeneration++
            isReadingTag = false
            _uiState.value = NfcUiState(
                scanState = NfcScanState.FAIL,
                message = getApplication<Application>().getString(R.string.nfc_error_read_failed),
            )
        }
        syncReader()
    }

    /** Discards a tag delivered during a pause if the activity actually enters the background. */
    fun stopReader() {
        pendingTagIntent = null
    }

    /**
     * Re-enables NFC foreground dispatch only for an unfinished scan.
     * Call from the hosting activity's `ON_RESUME` lifecycle event.
     */
    fun resumeReader() {
        isActivityResumed = true
        syncReader()
        pendingTagIntent?.let { intent ->
            pendingTagIntent = null
            onNewIntent(intent)
        }
    }

    /**
     * Forwards a new NFC [Intent] to the reader.
     * Call from [MainActivity.onNewIntent].
     *
     * @param intent The intent received from the NFC system.
     */
    fun onNewIntent(intent: Intent?) {
        val params = pendingParams ?: return
        val reader = nfcReader ?: return
        if (!_dialogVisible.value || isReadingTag ||
            _uiState.value.scanState != NfcScanState.IN_PROGRESS ||
            intent?.action != NfcAdapter.ACTION_TECH_DISCOVERED) {
            return
        }
        if (!isActivityResumed) {
            pendingTagIntent = intent
            return
        }
        isReadingTag = true
        reader.read(intent, params, tagReaderCallback(scanGeneration))
    }
}
