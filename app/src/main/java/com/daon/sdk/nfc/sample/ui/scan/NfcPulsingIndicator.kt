package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Animated NFC indicator shown during [com.daon.sdk.nfc.sample.NfcScanState.IN_PROGRESS].
 *
 * Three concentric ripple rings emanate from a central [Surface] containing the NFC icon.
 * Each ring fades from primary-colour at 30 % alpha to transparent as it expands outward,
 * staggered at 600 ms intervals.
 *
 * All internal sizes are derived proportionally from [boxSize] so the indicator scales
 * correctly in both portrait ([AppDimens.NfcIndicatorBoxSize]) and landscape
 * ([AppDimens.NfcIndicatorBoxSizeCompact]) without the ring travel range collapsing.
 *
 * Animation is driven by [Animatable] + [LaunchedEffect] rather than
 * [androidx.compose.animation.core.rememberInfiniteTransition] so that it restarts
 * immediately when the composable re-enters composition after a configuration change
 * (e.g. screen rotation), avoiding the lifecycle-pause behaviour that would otherwise
 * keep the rings frozen.
 *
 * @param boxSize The square size of the entire indicator area. Defaults to
 *   [AppDimens.NfcIndicatorBoxSize]; pass [AppDimens.NfcIndicatorBoxSizeCompact] in
 *   landscape.
 */
@Composable
internal fun NfcPulsingIndicator(boxSize: Dp = AppDimens.NfcIndicatorBoxSize) {
    // Proportional sizes — portrait baseline: box=160dp, circle=80dp (50%), icon=40dp (25%).
    val iconCircleSize = boxSize * ICON_CIRCLE_FRACTION
    val iconSize = boxSize * ICON_FRACTION
    val ringColor = MaterialTheme.colorScheme.primary
    val strokeWidthPx = with(LocalDensity.current) { AppDimens.NfcRingStrokeWidth.toPx() }

    val ring1 = remember { Animatable(0f) }
    val ring2 = remember { Animatable(1f / 3f) }
    val ring3 = remember { Animatable(2f / 3f) }

    LaunchedEffect(Unit) {
        // Each ring completes the remainder of its first cycle from its initial offset, then
        // loops indefinitely. Using separate coroutines keeps them running in parallel.
        launch {
            while (isActive) {
                ring1.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = ((1f - ring1.value) * CYCLE_MS).roundToInt(),
                        easing = LinearEasing,
                    ),
                )
                ring1.snapTo(0f)
            }
        }
        launch {
            while (isActive) {
                ring2.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = ((1f - ring2.value) * CYCLE_MS).roundToInt(),
                        easing = LinearEasing,
                    ),
                )
                ring2.snapTo(0f)
            }
        }
        launch {
            while (isActive) {
                ring3.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = ((1f - ring3.value) * CYCLE_MS).roundToInt(),
                        easing = LinearEasing,
                    ),
                )
                ring3.snapTo(0f)
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(boxSize),
    ) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val boxRadius = size.minDimension / 2f
            // Derive iconCircleRadius from actual pixel size so ring bounds always match
            // the Surface drawn below, regardless of which boxSize was passed in.
            val iconCircleRadius = size.minDimension * ICON_CIRCLE_FRACTION / 2f
            val innerGap = AppDimens.NfcRingInnerGap.toPx()
            val outerMargin = AppDimens.NfcRingOuterMargin.toPx()
            val minRingRadius = iconCircleRadius + innerGap
            val maxRingRadius = boxRadius - outerMargin

            for (progress in listOf(ring1.value, ring2.value, ring3.value)) {
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
            modifier = Modifier.size(iconCircleSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Nfc,
                    contentDescription = stringResource(R.string.content_description_nfc_icon),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

private const val CYCLE_MS = 1800f
private const val ICON_CIRCLE_FRACTION = 0.5f   // icon circle diameter = 50 % of box
private const val ICON_FRACTION        = 0.25f  // icon size           = 25 % of box
