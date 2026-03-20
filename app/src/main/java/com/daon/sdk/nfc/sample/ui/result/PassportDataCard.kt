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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.daon.sdk.nfc.sample.R
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
                text = stringResource(R.string.label_face_image),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppDimens.SpacingSmall))

            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = stringResource(R.string.content_description_face_photo),
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
@Composable
internal fun buildDataRows(data: PassportData?): List<Pair<String, String>> {
    if (data == null) return emptyList()

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    val labelFirstName = stringResource(R.string.data_label_first_name)
    val labelLastName = stringResource(R.string.data_label_last_name)
    val labelDocumentNumber = stringResource(R.string.label_document_number)
    val labelDocumentCode = stringResource(R.string.data_label_document_code)
    val labelDocumentType = stringResource(R.string.data_label_document_type)
    val labelPersonalNumber = stringResource(R.string.data_label_personal_number)
    val labelNationality = stringResource(R.string.data_label_nationality)
    val labelIssuingState = stringResource(R.string.data_label_issuing_state)
    val labelGender = stringResource(R.string.data_label_gender)
    val labelDateOfBirth = stringResource(R.string.label_date_of_birth)
    val labelDateOfExpiry = stringResource(R.string.label_date_of_expiry)
    val labelDateOfIssue = stringResource(R.string.data_label_date_of_issue)
    val labelPlaceOfBirth = stringResource(R.string.data_label_place_of_birth)
    val labelAddress = stringResource(R.string.data_label_address)
    val labelOtherDocumentNumbers = stringResource(R.string.data_label_other_document_numbers)
    val labelIssuingAuthority = stringResource(R.string.data_label_issuing_authority)
    val labelCertificateVerified = stringResource(R.string.data_label_certificate_verified)
    val labelDocumentExpired = stringResource(R.string.data_label_document_expired)
    val labelMrz = stringResource(R.string.data_label_mrz)
    val valueYes = stringResource(R.string.value_yes)
    val valueNo = stringResource(R.string.value_no)

    return buildList {
        data.firstName.meaningful()?.let { add(labelFirstName to it) }
        data.lastName.meaningful()?.let { add(labelLastName to it) }
        data.documentNumber.meaningful()?.let { add(labelDocumentNumber to it) }
        data.documentCode.meaningful()?.let { add(labelDocumentCode to it) }
        data.documentType.meaningful()?.let { add(labelDocumentType to it) }
        data.personalNumber.meaningful()?.let { add(labelPersonalNumber to it) }
        data.nationality.meaningful()?.let { add(labelNationality to it) }
        data.state.meaningful()?.let { add(labelIssuingState to it) }
        data.gender.meaningful()?.let { add(labelGender to it) }
        data.dateOfBirth?.let { add(labelDateOfBirth to dateFormat.format(it)) }
        data.dateOfExpiry?.let { add(labelDateOfExpiry to dateFormat.format(it)) }
        data.dateOfIssue?.let { add(labelDateOfIssue to dateFormat.format(it)) }
        data.placeOfBirth.meaningful()?.let { add(labelPlaceOfBirth to it) }
        data.address.meaningful()?.let { add(labelAddress to it) }
        data.documentNumbers.meaningful()?.let { add(labelOtherDocumentNumbers to it) }
        data.issuingAuthority.meaningful()?.let { add(labelIssuingAuthority to it) }
        // Only include certificate status when a certificate was actually present.
        if (data.certificate != null) {
            add(labelCertificateVerified to if (data.isCertificateVerified) valueYes else valueNo)
        }
        add(labelDocumentExpired to if (data.isExpired) valueYes else valueNo)
        data.mrz.meaningful()?.let { add(labelMrz to it) }
    }
}

/** Returns this string if it is non-null, non-blank, and not the SDK placeholder "NA". */
private fun String?.meaningful(): String? =
    takeIf { !it.isNullOrBlank() && it != "NA" }
