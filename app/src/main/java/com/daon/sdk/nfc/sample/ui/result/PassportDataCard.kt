package com.daon.sdk.nfc.sample.ui.result

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import com.daon.sdk.nfc.reader.passport.PassportData
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Elevated card showing the passport holder's face photograph (DG2 group).
 *
 * @param photo The [Bitmap] decoded from the chip's DG2 data group.
 */
@Composable
internal fun FaceImageCard(photo: Bitmap) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.CornerMedium),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppDimens.CardElevation),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.CardContentPaddingHorizontal,
                    vertical = AppDimens.CardContentPaddingHorizontal,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "FACE IMAGE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppDimens.SpacingSmall))

            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = "Face photo",
                modifier = Modifier
                    .height(AppDimens.FaceImageHeight)
                    .clip(RoundedCornerShape(AppDimens.CornerSmall)),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/**
 * Elevated card that renders an ordered list of label-value data rows separated
 * by [HorizontalDivider]s.
 *
 * @param rows Ordered list of (label, value) pairs to display.
 */
@Composable
internal fun DataCard(rows: List<Pair<String, String>>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.CornerMedium),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppDimens.CardElevation),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.CardContentPaddingHorizontal),
        ) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                DataRow(label = label, value = value)
            }
        }
    }
}

/**
 * Single label-value row within a [DataCard].
 * The label is displayed in uppercased caption style; the value in body style below it.
 */
@Composable
private fun DataRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.DataRowPaddingVertical),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(AppDimens.SpacingTiny))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Data row builder ──────────────────────────────────────────────────────────

/**
 * Builds the ordered list of label-value pairs to display from [data].
 * Only non-null, non-blank, and non-"NA" values are included.
 */
internal fun buildDataRows(data: PassportData?): List<Pair<String, String>> {
    if (data == null) return emptyList()

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    return buildList {
        data.firstName.meaningful()?.let { add("First Name" to it) }
        data.lastName.meaningful()?.let { add("Last Name" to it) }
        data.documentNumber.meaningful()?.let { add("Document Number" to it) }
        data.documentCode.meaningful()?.let { add("Document Code" to it) }
        data.documentType.meaningful()?.let { add("Document Type" to it) }
        data.personalNumber.meaningful()?.let { add("Personal Number" to it) }
        data.nationality.meaningful()?.let { add("Nationality" to it) }
        data.state.meaningful()?.let { add("Issuing State" to it) }
        data.gender.meaningful()?.let { add("Gender" to it) }
        data.dateOfBirth?.let { add("Date of Birth" to dateFormat.format(it)) }
        data.dateOfExpiry?.let { add("Date of Expiry" to dateFormat.format(it)) }
        data.dateOfIssue?.let { add("Date of Issue" to dateFormat.format(it)) }
        data.placeOfBirth.meaningful()?.let { add("Place of Birth" to it) }
        data.address.meaningful()?.let { add("Address" to it) }
        data.documentNumbers.meaningful()?.let { add("Other Document Numbers" to it) }
        data.issuingAuthority.meaningful()?.let { add("Issuing Authority" to it) }
        // Only include certificate status when a certificate was actually present.
        if (data.certificate != null) {
            add("Certificate Verified" to if (data.isCertificateVerified) "Yes" else "No")
        }
        add("Document Expired" to if (data.isExpired) "Yes" else "No")
        data.mrz.meaningful()?.let { add("MRZ" to it) }
    }
}

/** Returns this string if it is non-null, non-blank, and not the SDK placeholder "NA". */
private fun String?.meaningful(): String? =
    takeIf { !it.isNullOrBlank() && it != "NA" }
