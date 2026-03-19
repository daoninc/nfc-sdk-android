package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Spring-animated status icon for scan success and failure states.
 *
 * A [Surface] with [CircleShape] scales from 0 to 1 with a medium-bouncy spring on first
 * composition, giving a satisfying "pop" effect.
 *
 * @param success `true` for the check icon in `primaryContainer`;
 *   `false` for the X icon in `errorContainer`.
 */
@Composable
internal fun ScanStateIcon(success: Boolean) {
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
