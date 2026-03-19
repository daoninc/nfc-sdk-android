package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Animated NFC indicator shown during [com.daon.sdk.nfc.sample.NfcScanState.IN_PROGRESS].
 *
 * Three concentric ripple rings emanate from a central [Surface] containing the NFC icon.
 * Each ring fades from primary-colour at 30 % alpha to transparent as it expands outward,
 * staggered at 600 ms intervals.
 */
@Composable
internal fun NfcPulsingIndicator() {
    val ringColor = MaterialTheme.colorScheme.primary
    val strokeWidthPx = with(LocalDensity.current) { AppDimens.NfcRingStrokeWidth.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "NfcRipple")

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
