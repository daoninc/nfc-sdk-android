package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Centred header for [ScanScreen]: the Daon logo, a bold title, and a short subtitle.
 */
@Composable
internal fun ScanScreenHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(AppDimens.CornerLarge),
            color = Color.Transparent,
            modifier = Modifier.size(AppDimens.LogoContainerSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.daon_label_logo),
                    contentDescription = "Daon logo",
                    modifier = Modifier.size(AppDimens.LogoIconSize),
                )
            }
        }

        Spacer(Modifier.height(AppDimens.SpacingLarge))

        Text(
            text = "Passport NFC Reader",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(AppDimens.SpacingXxSmall))

        Text(
            text = "Enter your document details to begin scanning",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
