package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.daon.sdk.nfc.sample.NfcScanState
import com.daon.sdk.nfc.sample.NfcUiState
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Bottom-sheet dialog that reflects the current NFC scan state with animated transitions.
 *
 * Shows:
 * - [NfcScanState.IN_PROGRESS]: [NfcPulsingIndicator], a phase label, and (once data starts
 *   flowing) a [LinearProgressIndicator].
 * - [NfcScanState.SUCCESS]: [ScanStateIcon] with a check and a **View Results** button.
 * - [NfcScanState.FAIL]: [ScanStateIcon] with an X, a **Try Again** button, and **Cancel**.
 *
 * @param uiState Current [NfcUiState] to display.
 * @param onDismiss Called when the user cancels (FAIL / swipe-dismiss).
 * @param onDone Called when the user taps **View Results** after a successful scan.
 * @param onRetry Called when the user taps **Try Again** after a failed scan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanDialog(
    uiState: NfcUiState,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.ScreenPaddingHorizontal,
                    vertical = AppDimens.ScreenPaddingVertical,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
        ) {

            // ── Icon area (fixed size to prevent layout jumps on transitions) ──
            AnimatedContent(
                targetState = uiState.scanState,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                contentAlignment = Alignment.Center,
                label = "NfcIconArea",
                modifier = Modifier.size(AppDimens.NfcIndicatorBoxSize),
            ) { state ->
                when (state) {
                    NfcScanState.IN_PROGRESS -> NfcPulsingIndicator()
                    NfcScanState.SUCCESS     -> ScanStateIcon(success = true)
                    NfcScanState.FAIL        -> ScanStateIcon(success = false)
                }
            }

            // ── Title ─────────────────────────────────────────────────────────
            AnimatedContent(
                targetState = uiState.scanState,
                transitionSpec = {
                    fadeIn(tween(300, delayMillis = 100)) togetherWith fadeOut(tween(150))
                },
                label = "NfcTitle",
            ) { state ->
                Text(
                    text = when (state) {
                        NfcScanState.IN_PROGRESS -> "Scanning…"
                        NfcScanState.SUCCESS     -> "Scan complete"
                        NfcScanState.FAIL        -> "Scan failed"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Status / phase message ─────────────────────────────────────────
            if (uiState.message.isNotEmpty()) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = when (uiState.scanState) {
                        NfcScanState.FAIL -> MaterialTheme.colorScheme.error
                        else              -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            // ── Progress bar (appears once chip data starts flowing) ───────────
            AnimatedVisibility(visible = uiState.progress != null) {
                val progress = uiState.progress ?: 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingXSmall))

            // ── Action buttons ─────────────────────────────────────────────────
            AnimatedContent(
                targetState = uiState.scanState,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "NfcButtons",
            ) { state ->
                when (state) {
                    NfcScanState.IN_PROGRESS -> {
                        // Reserve space while scanning to prevent sheet resizing.
                        Spacer(modifier = Modifier.height(AppDimens.ButtonHeight))
                    }

                    NfcScanState.SUCCESS -> {
                        Button(
                            onClick = onDone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppDimens.ButtonHeight),
                        ) {
                            Text("View Results")
                        }
                    }

                    NfcScanState.FAIL -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(AppDimens.ButtonHeight),
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = onRetry,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(AppDimens.ButtonHeight),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingMedium))
        }
    }
}
