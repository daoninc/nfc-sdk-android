package com.daon.sdk.nfc.sample.ui.scan

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.daon.sdk.nfc.sample.NfcAvailability

/**
 * Dialog shown when NFC is absent or disabled on the device.
 *
 * - [NfcAvailability.DISABLED]: offers an "Open Settings" button that deep-links to
 *   the system NFC settings, plus a "Dismiss" button.
 * - [NfcAvailability.NOT_SUPPORTED]: shows an informational "OK" button only.
 *
 * @param availability Whether NFC is [NfcAvailability.NOT_SUPPORTED] or [NfcAvailability.DISABLED].
 * @param onDismiss Called when the dialog should be closed (clears the availability error state).
 */
@Composable
internal fun NfcUnavailableDialog(
    availability: NfcAvailability,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isDisabled = availability == NfcAvailability.DISABLED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isDisabled) "NFC disabled" else "NFC not supported")
        },
        text = {
            Text(
                if (isDisabled)
                    "NFC is turned off. Please enable NFC in Settings to scan your document."
                else
                    "This device does not have NFC hardware. NFC is required to read passport chips."
            )
        },
        confirmButton = {
            if (isDisabled) {
                Button(
                    onClick = {
                        onDismiss()
                        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                    },
                ) {
                    Text("Open Settings")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = if (isDisabled) {
            { TextButton(onClick = onDismiss) { Text("Dismiss") } }
        } else null,
    )
}
