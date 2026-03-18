package com.daon.sdk.nfc.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.reader.passport.PassportParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Main screen with document input fields and a "Start NFC Scan" button.
 *
 * - Document Number: free-text field, max 9 characters, auto-uppercased.
 * - Date of Birth / Date of Expiry: read-only fields;
 *   the chosen date is displayed in a human-readable format
 *   and stored internally as `yyMMdd` for the SDK.
 *
 * Validation errors are surfaced via an [AlertDialog].
 * The NFC scanning bottom-sheet is shown via [NfcScanDialog].
 *
 * @param viewModel The [NfcViewModel] shared with
 *   [com.daon.sdk.nfc.sample.MainActivity].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(viewModel: NfcViewModel) {
    val dialogVisible by viewModel.dialogVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var documentNumber by remember { mutableStateOf("") }
    var dateOfBirthMillis by remember { mutableStateOf<Long?>(null) }
    var dateOfExpiryMillis by remember { mutableStateOf<Long?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDobPicker by remember { mutableStateOf(false) }
    var showDoePicker by remember { mutableStateOf(false) }

    // Hoist picker state so each dialog reopens showing the previously chosen date.
    val dobPickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfBirthMillis)
    val doePickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfExpiryMillis)

    // Detect taps on the read-only date fields so the full field surface is tappable,
    // not just the trailing icon.
    val dobInteractionSource = remember { MutableInteractionSource() }
    val doeInteractionSource = remember { MutableInteractionSource() }
    val dobIsPressed by dobInteractionSource.collectIsPressedAsState()
    val doeIsPressed by doeInteractionSource.collectIsPressedAsState()
    LaunchedEffect(dobIsPressed) { if (dobIsPressed) showDobPicker = true }
    LaunchedEffect(doeIsPressed) { if (doeIsPressed) showDoePicker = true }

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
                    .padding(horizontal = AppDimens.ScreenPaddingHorizontal, vertical = AppDimens.SpacingHuge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // ── Header icon ──────────────────────────────────────────────
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

                Spacer(Modifier.height(AppDimens.SpacingXXLarge))

                // ── Form card ────────────────────────────────────────────────
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
                            .padding(horizontal = AppDimens.CardContentPaddingHorizontal, vertical = AppDimens.CardContentPaddingVertical),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
                    ) {
                        OutlinedTextField(
                            value = documentNumber,
                            onValueChange = { if (it.length <= 9) documentNumber = it.uppercase() },
                            label = { Text("Document Number") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CreditCard,
                                    contentDescription = null,
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters,
                            ),
                            shape = RoundedCornerShape(AppDimens.CornerSmall),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = dateOfBirthMillis?.let { formatDisplayDate(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date of Birth") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Select date of birth",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            interactionSource = dobInteractionSource,
                            shape = RoundedCornerShape(AppDimens.CornerSmall),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = dateOfExpiryMillis?.let { formatDisplayDate(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date of Expiry") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Select date of expiry",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            interactionSource = doeInteractionSource,
                            shape = RoundedCornerShape(AppDimens.CornerSmall),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

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

    // ── Date of Birth picker ──────────────────────────────────────────────────
    if (showDobPicker) {
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dobPickerState.selectedDateMillis?.let { dateOfBirthMillis = it }
                        showDobPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDobPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dobPickerState)
        }
    }

    // ── Date of Expiry picker ─────────────────────────────────────────────────
    if (showDoePicker) {
        DatePickerDialog(
            onDismissRequest = { showDoePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        doePickerState.selectedDateMillis?.let { dateOfExpiryMillis = it }
                        showDoePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDoePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = doePickerState)
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

/**
 * Shared [OutlinedTextFieldDefaults.colors] applied to all form fields.
 * Tints the focused indicator and label with the theme primary colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
)

/**
 * Validates the user's input.
 *
 * @param documentNumber Must be non-empty and at most 9 characters.
 * @param dateOfBirth Must be non-null (selected via date picker).
 * @param dateOfExpiry Must be non-null (selected via date picker).
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
    if (dateOfBirth == null) {
        return "Please select a date of birth."
    }
    if (dateOfExpiry == null) {
        return "Please select a date of expiry."
    }
    return null
}

/**
 * Formats epoch milliseconds as a human-readable date string (e.g. "Jul 15, 1985").
 * Uses UTC to match the timezone-neutral value stored by the Material3 date picker.
 */
private fun formatDisplayDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
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
