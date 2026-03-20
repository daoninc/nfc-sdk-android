package com.daon.sdk.nfc.sample.ui.scan

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Encryption algorithm options surfaced in [DocumentFormCard]. */
internal enum class EncryptionAlgorithm(@param:StringRes val labelRes: Int) {
    DEFAULT(R.string.encryption_algorithm_default),
    SHA1(R.string.encryption_algorithm_sha1),
}

/**
 * Elevated form card that owns all document input fields:
 * document number, date of birth, date of expiry,
 * active authentication toggle, and encryption algorithm selector.
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
    encryptionAlgorithm: EncryptionAlgorithm,
    onEncryptionChange: (EncryptionAlgorithm) -> Unit,
) {
    // Local UI state for pickers and dropdown.
    var showDobPicker by remember { mutableStateOf(false) }
    var showDoePicker by remember { mutableStateOf(false) }
    var encryptionMenuExpanded by remember { mutableStateOf(false) }

    val dobPickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfBirthMillis)
    val doePickerState = rememberDatePickerState(initialSelectedDateMillis = dateOfExpiryMillis)

    // Open the date picker when the read-only text field is tapped anywhere.
    val dobInteractionSource = remember { MutableInteractionSource() }
    val doeInteractionSource = remember { MutableInteractionSource() }
    val dobIsPressed by dobInteractionSource.collectIsPressedAsState()
    val doeIsPressed by doeInteractionSource.collectIsPressedAsState()
    LaunchedEffect(dobIsPressed) { if (dobIsPressed) showDobPicker = true }
    LaunchedEffect(doeIsPressed) { if (doeIsPressed) showDoePicker = true }

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
                value = dateOfBirthMillis?.let { formatDisplayDate(it) } ?: "",
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
                value = dateOfExpiryMillis?.let { formatDisplayDate(it) } ?: "",
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

            // ── Encryption algorithm selector (visible only when Active Authentication is on) ──
            AnimatedVisibility(visible = enableActiveAuthentication) {
                ExposedDropdownMenuBox(
                    expanded = encryptionMenuExpanded,
                    onExpandedChange = { encryptionMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = stringResource(encryptionAlgorithm.labelRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_encryption_algorithm)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = encryptionMenuExpanded)
                        },
                        shape = RoundedCornerShape(AppDimens.CornerSmall),
                        colors = outlinedFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = encryptionMenuExpanded,
                        onDismissRequest = { encryptionMenuExpanded = false },
                    ) {
                        EncryptionAlgorithm.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.labelRes)) },
                                onClick = {
                                    onEncryptionChange(option)
                                    encryptionMenuExpanded = false
                                },
                            )
                        }
                    }
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
                        dobPickerState.selectedDateMillis?.let { onDateOfBirthChange(it) }
                        showDobPicker = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDobPicker = false }) { Text(stringResource(R.string.cancel)) }
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
                        doePickerState.selectedDateMillis?.let { onDateOfExpiryChange(it) }
                        showDoePicker = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDoePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            DatePicker(state = doePickerState)
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
 * Formats epoch milliseconds as a human-readable date string (e.g. "Jul 15, 1985").
 * Uses UTC to match the timezone-neutral value stored by the Material3 date picker.
 */
private fun formatDisplayDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}
