package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import com.daon.sdk.nfc.reader.passport.PassportParameters
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Main scan screen.
 *
 * Orchestrates [ScanScreenHeader], [DocumentFormCard], the scan button,
 * a validation-error [AlertDialog], [NfcUnavailableDialog], and [NfcScanDialog].
 * All form-field state is hoisted here so the scan button can read it directly.
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

    var documentNumber by remember { mutableStateOf("") }
    var dateOfBirthMillis by remember { mutableStateOf<Long?>(null) }
    var dateOfExpiryMillis by remember { mutableStateOf<Long?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var enableActiveAuthentication by remember { mutableStateOf(false) }
    var encryptionAlgorithm by remember { mutableStateOf(EncryptionAlgorithm.DEFAULT) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        // BoxWithConstraints lets the inner Column use min-height = screen height so
        // Arrangement.Center works while verticalScroll handles small screens / keyboards.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val screenHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight)
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
                    encryptionAlgorithm = encryptionAlgorithm,
                    onEncryptionChange = { encryptionAlgorithm = it },
                )

                Spacer(Modifier.height(AppDimens.SpacingXLarge))

                // ── Primary action button ─────────────────────────────────────
                Button(
                    onClick = {
                        val dobShort = dateOfBirthMillis?.let { formatShortDate(it) }
                        val doeShort = dateOfExpiryMillis?.let { formatShortDate(it) }
                        val error = validate(documentNumber, dobShort, doeShort)
                        if (error != null) {
                            validationError = error
                        } else {
                            val params = PassportParameters(documentNumber, dobShort!!, doeShort!!)
                            params.enablePhoto(true)
                            params.enableRawData(true)
                            if (enableActiveAuthentication) {
                                params.setChallenge(generateNonce())
                            }
                            if (encryptionAlgorithm == EncryptionAlgorithm.SHA1) {
                                params.setSignatureAlgorithm("SHA1WithRSA/ISO9796-2")
                                params.setDigestAlgorithm("SHA1")
                            }
                            viewModel.startScan(params)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.ButtonHeight),
                    shape = RoundedCornerShape(AppDimens.CornerSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Start NFC Scan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }

    // ── Validation error dialog ───────────────────────────────────────────────
    validationError?.let { message ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Invalid input") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text("OK") }
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

// ── Private helpers ───────────────────────────────────────────────────────────

/**
 * Validates the user's input before building [PassportParameters].
 *
 * @return A human-readable error message, or `null` if all inputs are valid.
 */
private fun validate(
    documentNumber: String,
    dateOfBirth: String?,
    dateOfExpiry: String?,
): String? {
    if (documentNumber.isEmpty() || documentNumber.length > 9) {
        return "Document number must be between 1 and 9 characters."
    }
    if (dateOfBirth == null) return "Please select a date of birth."
    if (dateOfExpiry == null) return "Please select a date of expiry."
    return null
}

/**
 * Formats epoch milliseconds as a `yyMMdd` string expected by [PassportParameters].
 * Uses UTC to match the timezone-neutral value stored by the Material3 date picker.
 */
private fun formatShortDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyMMdd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

/**
 * Generates an 8-byte cryptographically random nonce for Active Authentication.
 * Mirrors the dev-app reference implementation.
 */
private fun generateNonce(): ByteArray {
    val nonce = ByteArray(8)
    SecureRandom.getInstance("SHA1PRNG").nextBytes(nonce)
    return nonce
}
