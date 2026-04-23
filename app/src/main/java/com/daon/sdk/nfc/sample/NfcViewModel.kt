package com.daon.sdk.nfc.sample

import android.app.Application
import android.content.Intent
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
 * @param application Application context required to construct [NFCReader].
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

    // TagReaderCallback implementation forwarded from the Activity's onNewIntent.
    private val tagReaderCallback = object : TagReaderCallback {
        override fun tagReaderDidFinish(data: TagData) {
            Log.d("DAON", "tagReaderDidFinish: ${data.toJSON()}")
            nfcReader?.disable()
            _uiState.value = NfcUiState(
                scanState = NfcScanState.SUCCESS,
                message = getApplication<Application>().getString(R.string.nfc_status_scan_complete),
                result = data,
            )
        }

        override fun tagReaderFailed(code: Short, message: String?) {
            Log.d("DAON", "tagReaderFailed: $message")
            if (code == TagError.ERROR_CANCELLED) {
                // Scan was interrupted (e.g. app backgrounded). Keep the dialog visible
                // and show the failure state so the user sees it on return to foreground.
                if (_dialogVisible.value) {
                    nfcReader?.disable()
                    _uiState.value = NfcUiState(
                        scanState = NfcScanState.FAIL,
                        message = getApplication<Application>().getString(R.string.nfc_error_read_failed),
                    )
                }
                return
            }
            val errorMessage = when (code) {
                TagError.ERROR_NO_ACCESS -> getApplication<Application>().getString(R.string.nfc_error_authentication_failed)
                TagError.ERROR_TAG_NOT_SUPPORTED -> getApplication<Application>().getString(R.string.nfc_error_tag_not_supported)
                TagError.ERROR_NO_DATA,
                TagError.ERROR_NOT_IMPLEMENTED -> getApplication<Application>().getString(R.string.nfc_error_unable_to_read)
                TagError.ERROR_LICENSE -> getApplication<Application>().getString(R.string.nfc_error_license, message.orEmpty())
                else -> getApplication<Application>().getString(R.string.nfc_error_read_failed)
            }
            nfcReader?.disable()
            _uiState.value = NfcUiState(scanState = NfcScanState.FAIL, message = errorMessage)
        }

        override fun tagReaderInformation(info: TagReaderInfo) {
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
     * Initialises the [NFCReader] using the given [activity].
     * Must be called from [MainActivity.onCreate] before any NFC operation.
     *
     * @param activity The hosting [androidx.fragment.app.FragmentActivity].
     */
    fun initReader(activity: androidx.fragment.app.FragmentActivity) {
        if (nfcReader == null) {
            nfcReader = NFCReader(activity, "license.txt", false)
            Log.d("DAON", "NFCReader initialised — SDK version: ${NFCReader.version()}")
        }
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
        pendingParams = params
        _uiState.value = NfcUiState(scanState = NfcScanState.IN_PROGRESS, message = getApplication<Application>().getString(R.string.nfc_status_hold_near_document))
        _dialogVisible.value = true
        reader?.enable()
    }

    /**
     * Dismisses the NFC scanning dialog and disables foreground dispatch.
     * Should be called when the user cancels the dialog.
     */
    fun cancelScan() {
        nfcReader?.disable()
        _dialogVisible.value = false
        _showResult.value = false
        _uiState.value = NfcUiState()
    }

    /**
     * Clears the [nfcAvailability] error state.
     * Call from the UI after the user dismisses the NFC unavailable / disabled dialog.
     */
    fun dismissNfcUnavailable() {
        _nfcAvailability.value = null
    }

    /**
     * Resets the scan state to [NfcScanState.IN_PROGRESS] and re-enables NFC foreground
     * dispatch using the previously stored [pendingParams]. Should be called when the user
     * taps **Try Again** after a failed scan.
     */
    fun retryScan() {
        if (pendingParams == null) return
        _uiState.value = NfcUiState(
            scanState = NfcScanState.IN_PROGRESS,
            message = getApplication<Application>().getString(R.string.nfc_status_hold_near_document),
        )
        nfcReader?.enable()
    }

    /**
     * Hides the scanning dialog and transitions to the result screen.
     * Should be called when the user taps "Done" after a successful scan.
     */
    fun showResults() {
        _dialogVisible.value = false
        _showResult.value = true
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
     * Disables NFC foreground dispatch.
     * Call from [MainActivity.onPause].
     */
    fun disableReader() {
        nfcReader?.disable()
    }

    /**
     * Re-enables NFC foreground dispatch when the dialog is visible.
     * Call from [MainActivity.onResume].
     */
    fun resumeReader() {
        if (_dialogVisible.value) {
            nfcReader?.enable()
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
        if (_uiState.value.scanState != NfcScanState.IN_PROGRESS) {
            return
        }
        nfcReader?.read(intent, params, tagReaderCallback)
    }
}
