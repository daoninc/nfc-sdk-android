package com.daon.sdk.nfc.sample.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.daon.sdk.nfc.sample.NfcScanState
import com.daon.sdk.nfc.sample.NfcUiState
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Bottom-sheet dialog that reflects the current NFC scan state with animated transitions.
 *
 * Shows:
 * - [NfcScanState.IN_PROGRESS]: animated ripple rings around the NFC icon, a phase label,
 *   and (once data starts flowing) a [LinearProgressIndicator].
 * - [NfcScanState.SUCCESS]: a spring-scaled check icon in a `primaryContainer` circle
 *   and a **View Results** button.
 * - [NfcScanState.FAIL]: a spring-scaled X icon in an `errorContainer` circle, a
 *   **Try Again** primary button, and a **Cancel** outlined button.
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
                    NfcScanState.SUCCESS -> ScanStateIcon(success = true)
                    NfcScanState.FAIL -> ScanStateIcon(success = false)
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
                        NfcScanState.SUCCESS -> "Scan complete"
                        NfcScanState.FAIL -> "Scan failed"
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
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                        // No buttons while scanning; reserve space to avoid sheet resizing.
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

// ── Private composables ───────────────────────────────────────────────────────

/**
 * Animated NFC indicator shown during [NfcScanState.IN_PROGRESS].
 *
 * Three concentric ripple rings emanate from a central [Surface] containing the NFC icon.
 * Each ring fades from primary-colour at 30% alpha to transparent as it expands outward,
 * staggered at 600 ms intervals.
 */
@Composable
private fun NfcPulsingIndicator() {
    val ringColor = MaterialTheme.colorScheme.primary
    val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        AppDimens.NfcRingStrokeWidth.toPx()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NfcRipple")

    // Three rings, each staggered 600 ms apart.
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0, StartOffsetType.FastForward),
        ),
        label = "Ring1",
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(600, StartOffsetType.FastForward),
        ),
        label = "Ring2",
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1200, StartOffsetType.FastForward),
        ),
        label = "Ring3",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(AppDimens.NfcIndicatorBoxSize),
    ) {
        Canvas(modifier = Modifier.size(AppDimens.NfcIndicatorBoxSize)) {
            val boxRadius = size.minDimension / 2f
            val iconCircleRadius = AppDimens.NfcIconCircleSize.toPx() / 2f
            val innerGap = AppDimens.NfcRingInnerGap.toPx()
            val outerMargin = AppDimens.NfcRingOuterMargin.toPx()
            val minRingRadius = iconCircleRadius + innerGap
            val maxRingRadius = boxRadius - outerMargin

            for (progress in listOf(ring1, ring2, ring3)) {
                val radius = minRingRadius + (maxRingRadius - minRingRadius) * progress
                val alpha = (1f - progress).coerceIn(0f, 0.3f) / 0.3f * 0.3f
                drawCircle(
                    color = ringColor,
                    radius = radius,
                    alpha = alpha,
                    style = Stroke(width = strokeWidthPx),
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AppDimens.NfcIconCircleSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Nfc,
                    contentDescription = "NFC",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(AppDimens.NfcIconSize),
                )
            }
        }
    }
}

/**
 * Spring-animated status icon for [NfcScanState.SUCCESS] and [NfcScanState.FAIL].
 *
 * A [Surface] with [CircleShape] scales from 0 to 1 with a medium-bouncy spring on first
 * composition, giving a satisfying "pop" effect.
 *
 * @param success `true` for the check icon in `primaryContainer`; `false` for the X icon
 *   in `errorContainer`.
 */
@Composable
private fun ScanStateIcon(success: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "StatusIconScale",
    )

    val containerColor = if (success) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (success) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(AppDimens.NfcIndicatorBoxSize),
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier
                .size(AppDimens.StatusIconContainerSize)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (success) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = if (success) "Success" else "Failed",
                    tint = contentColor,
                    modifier = Modifier.size(AppDimens.StatusIconSize),
                )
            }
        }
    }
}
