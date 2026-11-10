package com.daon.sdk.nfc.sample.ui.scan

import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.chrono.Chronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Elevated form card that owns all document input fields:
 * document number, date of birth, date of expiry,
 * and the active authentication toggle.
 *
 * Date picker dialogs are managed internally; selected values are reported
 * via [onDateOfBirthChange] / [onDateOfExpiryChange] callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentFormCard(
    documentNumber: String,
    onDocumentNumberChange: (String) -> Unit,
    dateOfBirthMillis: Long?,
    onDateOfBirthChange: (Long) -> Unit,
    dateOfExpiryMillis: Long?,
    onDateOfExpiryChange: (Long) -> Unit,
    enableActiveAuthentication: Boolean,
    onActiveAuthChange: (Boolean) -> Unit,
) {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    val dateInputPattern = remember(locale) { dateInputPattern(locale) }

    // Local UI state for pickers.
    var showDobPicker by remember { mutableStateOf(false) }
    var showDoePicker by remember { mutableStateOf(false) }
    var dobInputError by remember { mutableStateOf(false) }
    var doeInputError by remember { mutableStateOf(false) }

    val dobPickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfBirthMillis)
    val doePickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfExpiryMillis)
    val focusManager = LocalFocusManager.current

    LaunchedEffect(dobPickerState.selectedDateMillis) {
        if (dobPickerState.selectedDateMillis != null) dobInputError = false
    }
    LaunchedEffect(doePickerState.selectedDateMillis) {
        if (doePickerState.selectedDateMillis != null) doeInputError = false
    }

    val dismissDobPicker = {
        showDobPicker = false
        dobInputError = false
        focusManager.clearFocus(force = true)
    }
    val dismissDoePicker = {
        showDoePicker = false
        doeInputError = false
        focusManager.clearFocus(force = true)
    }

    // Open the date picker when the read-only text field is tapped anywhere.
    val dobInteractionSource = remember { MutableInteractionSource() }
    val doeInteractionSource = remember { MutableInteractionSource() }
    val dobIsPressed by dobInteractionSource.collectIsPressedAsState()
    val doeIsPressed by doeInteractionSource.collectIsPressedAsState()
    LaunchedEffect(dobIsPressed) {
        if (dobIsPressed) {
            dobInputError = false
            showDobPicker = true
        }
    }
    LaunchedEffect(doeIsPressed) {
        if (doeIsPressed) {
            doeInputError = false
            showDoePicker = true
        }
    }

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
                    vertical = AppDimens.CardContentPaddingVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
        ) {
            // ── Document Number ───────────────────────────────────────────────
            OutlinedTextField(
                value = documentNumber,
                onValueChange = { if (it.length <= 9) onDocumentNumberChange(it.uppercase()) },
                label = { Text(stringResource(R.string.label_document_number)) },
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

            // ── Date of Birth ─────────────────────────────────────────────────
            OutlinedTextField(
                value = dateOfBirthMillis?.let { formatDisplayDate(it, locale) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_date_of_birth)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(R.string.content_description_select_date_of_birth),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                interactionSource = dobInteractionSource,
                shape = RoundedCornerShape(AppDimens.CornerSmall),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Date of Expiry ────────────────────────────────────────────────
            OutlinedTextField(
                value = dateOfExpiryMillis?.let { formatDisplayDate(it, locale) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_date_of_expiry)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(R.string.content_description_select_date_of_expiry),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                interactionSource = doeInteractionSource,
                shape = RoundedCornerShape(AppDimens.CornerSmall),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Active Authentication toggle ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_active_authentication),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.label_active_authentication_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(AppDimens.SpacingMedium))
                Switch(
                    checked = enableActiveAuthentication,
                    onCheckedChange = onActiveAuthChange,
                )
            }
        }
    }

    // ── Date of Birth picker ──────────────────────────────────────────────────
    if (showDobPicker) {
        DatePickerDialog(
            onDismissRequest = dismissDobPicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = dobPickerState.selectedDateMillis
                        if (selectedDateMillis == null) {
                            dobInputError = true
                        } else {
                            onDateOfBirthChange(selectedDateMillis)
                            dismissDobPicker()
                        }
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = dismissDobPicker,
                ) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            Column {
                DatePicker(state = dobPickerState)
                if (dobInputError) {
                    Text(
                        text = stringResource(R.string.error_date_picker_invalid, dateInputPattern),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = AppDimens.CardContentPaddingHorizontal),
                    )
                }
            }
        }
    }

    // ── Date of Expiry picker ─────────────────────────────────────────────────
    if (showDoePicker) {
        DatePickerDialog(
            onDismissRequest = dismissDoePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = doePickerState.selectedDateMillis
                        if (selectedDateMillis == null) {
                            doeInputError = true
                        } else {
                            onDateOfExpiryChange(selectedDateMillis)
                            dismissDoePicker()
                        }
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = dismissDoePicker,
                ) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            Column {
                DatePicker(state = doePickerState)
                if (doeInputError) {
                    Text(
                        text = stringResource(R.string.error_date_picker_invalid, dateInputPattern),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = AppDimens.CardContentPaddingHorizontal),
                    )
                }
            }
        }
    }
}

/**
 * Shared [OutlinedTextFieldDefaults.colors] applied to all form fields in [DocumentFormCard].
 * Tints the focused indicator and label with the theme primary colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
)

/**
 * Formats epoch milliseconds as a human-readable date string using the UI locale.
 * Uses UTC to match the timezone-neutral value stored by the Material3 date picker.
 */
internal fun formatDisplayDate(millis: Long, locale: Locale): String {
    val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

/**
 * Matches Material3's numeric input pattern, including its pre-API 26 locale lookup.
 */
internal fun dateInputPattern(locale: Locale): String {
    val shortPattern = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            FormatStyle.SHORT,
            null,
            Chronology.ofLocale(locale),
            locale,
        )
    } else {
        (DateFormat.getDateInstance(DateFormat.SHORT, locale) as SimpleDateFormat).toPattern()
    }
    return shortPattern
        .replace(Regex("[^dMy/\\-.]"), "")
        .replace(Regex("d{1,2}"), "dd")
        .replace(Regex("M{1,2}"), "MM")
        .replace(Regex("y{1,4}"), "yyyy")
        .replace("My", "M/y")
        .removeSuffix(".")
        .uppercase(Locale.ROOT)
}
