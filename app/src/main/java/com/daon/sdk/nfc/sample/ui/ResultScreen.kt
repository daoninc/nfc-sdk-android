package com.daon.sdk.nfc.sample.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.daon.sdk.nfc.TagIssue
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import com.daon.sdk.nfc.reader.passport.PassportData
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Result screen shown after a successful NFC scan.
 *
 * Displays passport data as a scrollable list:
 * - The face image (DG2) is shown first if present.
 * - All non-empty data fields are shown in a labelled list inside an [ElevatedCard].
 * - Any scan issues are shown below a horizontal divider in a separate, error-tinted card.
 *
 * @param viewModel The [NfcViewModel] shared with
 *   [com.daon.sdk.nfc.sample.MainActivity].
 * @param onScanAgain Called when the user taps "Scan Again" to return to [ScanScreen].
 */
@Composable
fun ResultScreen(
    viewModel: NfcViewModel,
    onScanAgain: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val passportData = uiState.result as? PassportData
    val issues = uiState.result?.issues() ?: emptyList()
    val dataRows = buildDataRows(passportData)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = AppDimens.ScreenPaddingHorizontal, vertical = AppDimens.ScreenPaddingVertical),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item { ResultScreenHeader() }

            // ── Face image ────────────────────────────────────────────────────
            passportData?.photo?.let { photo ->
                item { FaceImageCard(photo) }
            }

            // ── Data rows ─────────────────────────────────────────────────────
            if (dataRows.isNotEmpty()) {
                item { DataCard(dataRows) }
            }

            // ── Issues section ────────────────────────────────────────────────
            if (issues.isNotEmpty()) {
                item { IssuesDivider() }
                item { IssuesCard(issues) }
            }

            // ── Scan Again button ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(AppDimens.SpacingXSmall))
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.ButtonHeight),
                    shape = RoundedCornerShape(AppDimens.CornerSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Scan Again",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

/**
 * Centred header for the result screen showing a badge icon, a bold title,
 * and a short subtitle confirming the successful read.
 */
@Composable
private fun ResultScreenHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(AppDimens.CornerLarge),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(AppDimens.LogoContainerSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(AppDimens.LogoIconSize),
                )
            }
        }

        Spacer(Modifier.height(AppDimens.SpacingLarge))

        Text(
            text = "Scan Results",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(AppDimens.SpacingXxSmall))

        Text(
            text = "Passport data successfully read",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Elevated card containing the passport holder's face photograph (DG2 group).
 *
 * @param photo The [Bitmap] decoded from the chip's DG2 data group.
 */
@Composable
private fun FaceImageCard(photo: Bitmap) {
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
                .padding(horizontal = AppDimens.CardContentPaddingHorizontal, vertical = AppDimens.CardContentPaddingHorizontal),
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
private fun DataCard(rows: List<Pair<String, String>>) {
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
 *
 * @param label Short field name shown as a caption above the value.
 * @param value The field value to display.
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

/**
 * Full-width divider row with an "Issues" label centred between two [HorizontalDivider]s.
 * Visually separates the main data card from the issues card when issues are present.
 */
@Composable
private fun IssuesDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = "Issues",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

/**
 * Elevated card rendered in the theme's `errorContainer` colour, containing one
 * [IssueRow] per [TagIssue] returned by the SDK.
 *
 * @param issues Non-empty list of [TagIssue]s reported during the scan.
 */
@Composable
private fun IssuesCard(issues: List<TagIssue>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.CornerMedium),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppDimens.CardElevation),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.CardContentPaddingHorizontal),
        ) {
            issues.forEachIndexed { index, issue ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f),
                    )
                }
                IssueRow(issue = issue)
            }
        }
    }
}

/**
 * Single row inside an [IssuesCard] showing a warning icon alongside the issue's message.
 *
 * @param issue The [TagIssue] to display.
 */
@Composable
private fun IssueRow(issue: TagIssue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.DataRowPaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(AppDimens.IssueIconSize)
                .padding(top = AppDimens.IssueIconTopPadding),
        )
        Text(
            text = issue.message ?: "Issue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

// ── Data row builder ──────────────────────────────────────────────────────────

/**
 * Builds the ordered list of label-value pairs to display from [data].
 * Only non-null, non-blank, and non-"NA" values are included.
 */
private fun buildDataRows(data: PassportData?): List<Pair<String, String>> {
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
