package com.daon.sdk.nfc.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.daon.sdk.nfc.sample.ui.result.ResultScreen
import com.daon.sdk.nfc.sample.ui.scan.ScanScreen
import com.daon.sdk.nfc.sample.ui.theme.NfcSdkAndroidTheme

/**
 * Single-activity host for the integration sample.
 *
 * Responsibilities:
 * - Initialises (and re-initialises after configuration changes) [NfcViewModel] with a
 *   reference to this activity so that [com.daon.sdk.nfc.NFCReader] always holds a live
 *   activity instance for NFC foreground dispatch registration.
 * - Forwards [onNewIntent] to the ViewModel when the NFC system delivers a tag.
 * - Forwards [onResume] / [onPause] lifecycle events to the ViewModel so the
 *   reader is enabled/disabled at the correct times.
 * - Drives top-level navigation between [ScanScreen] and [ResultScreen] by
 *   observing [NfcViewModel.showResult].
 */
class MainActivity : FragmentActivity() {

    private val viewModel: NfcViewModel by viewModels()
    private val readerLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            viewModel.resumeReader()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        viewModel.initReader(this)
        lifecycle.addObserver(readerLifecycleObserver)

        setContent {
            NfcSdkAndroidTheme {
                val showResult by viewModel.showResult.collectAsState()
                if (showResult) {
                    ResultScreen(
                        viewModel = viewModel,
                        onScanAgain = { viewModel.resetResult() },
                    )
                } else {
                    ScanScreen(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Disables NFC foreground dispatch when the activity moves to the background,
     * ensuring the reader is not active when the app is not in the foreground.
     */
    override fun onPause() {
        viewModel.disableReader()
        super.onPause()
    }

    override fun onStop() {
        viewModel.stopReader()
        super.onStop()
    }

    /**
     * Receives a new [Intent] from the NFC system when a tag is detected while this
     * activity is in the foreground.  The intent is forwarded to [NfcViewModel.onNewIntent]
     * which passes it to [com.daon.sdk.nfc.NFCReader.read].
     *
     * @param intent The NFC tag intent delivered by the Android system.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }
}
