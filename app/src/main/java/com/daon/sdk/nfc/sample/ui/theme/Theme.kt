package com.daon.sdk.nfc.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Daon brand-aligned navy blue palette ─────────────────────────────────────

/**
 * Material3 light colour scheme using Daon brand navy-blue tones.
 * Applied when the system UI mode is light (default).
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF003B6F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E4FF),
    onPrimaryContainer = Color(0xFF001C3D),
    secondary = Color(0xFF005F8E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E7FF),
    onSecondaryContainer = Color(0xFF001E2E),
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF191C1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1F),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41474E),
    outline = Color(0xFF71787F),
    outlineVariant = Color(0xFFC1C7CE),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

/**
 * Material3 dark colour scheme using Daon brand navy-blue tones.
 * Applied when the system UI mode is dark.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF00315A),
    primaryContainer = Color(0xFF004880),
    onPrimaryContainer = Color(0xFFD4E4FF),
    secondary = Color(0xFF8ECFF5),
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF004C68),
    onSecondaryContainer = Color(0xFFC7E7FF),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF1C2026),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF41474E),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9198),
    outlineVariant = Color(0xFF41474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

/**
 * Root theme for the nfc-sdk-android project.
 *
 * Automatically switches between [LightColorScheme] and [DarkColorScheme] based on the
 * current system UI mode. Pass an explicit [darkTheme] value to override the default
 * (useful for previews).
 *
 * @param darkTheme Whether to apply the dark colour scheme. Defaults to the system setting.
 * @param content The Composable content to theme.
 */
@Composable
fun NfcSdkAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
