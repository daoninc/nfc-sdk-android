package com.daon.sdk.nfc.sample.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.daon.sdk.nfc.TagIssue
import com.daon.sdk.nfc.sample.ui.theme.AppDimens

/**
 * Full-width divider row with an "Issues" label centred between two [HorizontalDivider]s.
 * Visually separates the main data card from the issues card when issues are present.
 */
@Composable
internal fun IssuesDivider() {
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
internal fun IssuesCard(issues: List<TagIssue>) {
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
