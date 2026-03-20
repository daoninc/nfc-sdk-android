package com.daon.sdk.nfc.sample.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.sample.R
import com.daon.sdk.nfc.sample.ui.theme.AppDimens
import com.daon.sdk.nfc.reader.passport.PassportData

/**
 * Result screen shown after a successful NFC scan.
 *
 * Renders [ResultScreenHeader], optionally [FaceImageCard], [DataCard],
 * and (if issues are present) [IssuesDivider] + [IssuesCard], all in a [LazyColumn].
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
            contentPadding = PaddingValues(
                horizontal = AppDimens.ScreenPaddingHorizontal,
                vertical = AppDimens.ScreenPaddingVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium),
        ) {
            item { ResultScreenHeader() }

            passportData?.photo?.let { photo ->
                item { FaceImageCard(photo) }
            }

            if (dataRows.isNotEmpty()) {
                item { DataCard(dataRows) }
            }

            if (issues.isNotEmpty()) {
                item { IssuesDivider() }
                item { IssuesCard(issues) }
            }

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
                        text = stringResource(R.string.button_scan_again),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
