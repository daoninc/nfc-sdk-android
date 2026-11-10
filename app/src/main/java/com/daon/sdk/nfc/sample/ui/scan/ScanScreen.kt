package com.daon.sdk.nfc.sample.ui.scan

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.sample.ScanInput
import com.daon.sdk.nfc.sample.ScanInputError
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Main scan screen.
 *
 * Orchestrates [ScanScreenHeader], [DocumentFormCard], the scan button,
 * a validation-error [AlertDialog], [NfcUnavailableDialog], and [NfcScanDialog].
 * All form-field state is hoisted here so the scan button can read it directly.
 *
 * In landscape the layout splits into two panes: the header on the left and the
 * scrollable form + button on the right. In portrait a single centred column is used.
 *
 * @param viewModel The [NfcViewModel] shared with
 *   [com.daon.sdk.nfc.sample.MainActivity].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(viewModel: NfcViewModel) {
    val dialogVisible by viewModel.dialogVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val nfcAvailability by viewModel.nfcAvailability.collectAsState()

    var documentNumber by rememberSaveable { mutableStateOf("") }
    var dateOfBirthMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var dateOfExpiryMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var enableActiveAuthentication by rememberSaveable { mutableStateOf(false) }

    val errDocNum = stringResource(R.string.error_document_number_length)
    val errDob = stringResource(R.string.error_date_of_birth_required)
    val errDoe = stringResource(R.string.error_date_of_expiry_required)

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Shared scan button click handler.
    val onScanClick = {
        validationError = when (viewModel.startScan(
            ScanInput(documentNumber, dateOfBirthMillis, dateOfExpiryMillis, enableActiveAuthentication)
        )) {
            ScanInputError.DOCUMENT_NUMBER -> errDocNum
            ScanInputError.DATE_OF_BIRTH -> errDob
            ScanInputError.DATE_OF_EXPIRY -> errDoe
            null -> null
        }
        Unit
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (isLandscape) {
            // ── Landscape: header left | form + button right ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimens.ScreenPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingXXLarge),
            ) {
                // Left pane — compact header centred vertically.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    ScanScreenHeader(compact = true)
                }

                // Right pane — form card + button, scrollable.
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = AppDimens.SpacingLarge),
                    verticalArrangement = Arrangement.Center,
                ) {
                    DocumentFormCard(
                        documentNumber = documentNumber,
                        onDocumentNumberChange = { documentNumber = it },
                        dateOfBirthMillis = dateOfBirthMillis,
                        onDateOfBirthChange = { dateOfBirthMillis = it },
                        dateOfExpiryMillis = dateOfExpiryMillis,
                        onDateOfExpiryChange = { dateOfExpiryMillis = it },
                        enableActiveAuthentication = enableActiveAuthentication,
                        onActiveAuthChange = { enableActiveAuthentication = it },
                    )

                    Spacer(Modifier.height(AppDimens.SpacingXLarge))

                    Button(
                        onClick = onScanClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimens.ButtonHeight),
                        shape = RoundedCornerShape(AppDimens.CornerSmall),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.start_nfc_scan),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        } else {
            // ── Portrait: centred single column ───────────────────────────────
            // onSizeChanged captures the available height once the Box is laid out so
            // the inner Column can use heightIn(min = availableHeight). This keeps
            // Arrangement.Center working while verticalScroll handles overflow.
            var availableHeightPx by remember { mutableIntStateOf(0) }
            val availableHeightDp = with(LocalDensity.current) { availableHeightPx.toDp() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onSizeChanged { availableHeightPx = it.height },
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = availableHeightDp)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = AppDimens.ScreenPaddingHorizontal,
                            vertical = AppDimens.SpacingHuge,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    ScanScreenHeader()

                    Spacer(Modifier.height(AppDimens.SpacingXXLarge))

                    DocumentFormCard(
                        documentNumber = documentNumber,
                        onDocumentNumberChange = { documentNumber = it },
                        dateOfBirthMillis = dateOfBirthMillis,
                        onDateOfBirthChange = { dateOfBirthMillis = it },
                        dateOfExpiryMillis = dateOfExpiryMillis,
                        onDateOfExpiryChange = { dateOfExpiryMillis = it },
                        enableActiveAuthentication = enableActiveAuthentication,
                        onActiveAuthChange = { enableActiveAuthentication = it },
                    )

                    Spacer(Modifier.height(AppDimens.SpacingXLarge))

                    Button(
                        onClick = onScanClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimens.ButtonHeight),
                        shape = RoundedCornerShape(AppDimens.CornerSmall),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.start_nfc_scan),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    // ── Validation error dialog ───────────────────────────────────────────────
    validationError?.let { message ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text(stringResource(R.string.error_invalid_input_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text(stringResource(R.string.ok)) }
            },
        )
    }

    // ── NFC unavailable dialog ────────────────────────────────────────────────
    nfcAvailability?.let { availability ->
        NfcUnavailableDialog(
            availability = availability,
            onDismiss = { viewModel.dismissNfcUnavailable() },
        )
    }

    // ── NFC scanning bottom-sheet ─────────────────────────────────────────────
    if (dialogVisible) {
        NfcScanDialog(
            uiState = uiState,
            onDismiss = { viewModel.cancelScan() },
            onDone = { viewModel.showResults() },
            onRetry = { viewModel.retryScan() },
        )
    }
}
