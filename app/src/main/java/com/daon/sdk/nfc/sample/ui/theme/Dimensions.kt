package com.daon.sdk.nfc.sample.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralised dimension tokens for the nfc-sdk-android project.
 *
 * Organised into spacing, padding, component sizes, corner radii, elevation,
 * and typography scale. Reference these instead of hardcoded `.dp` / `.sp`
 * literals so that visual changes can be made in one place.
 */
internal object AppDimens {

    // ── Spacing (Spacer heights / Arrangement.spacedBy) ──────────────────────

    /** Tight gap between a data-row's label and its value. */
    val SpacingTiny = 3.dp

    /** Gap between a screen title and its subtitle. */
    val SpacingXxSmall = 6.dp

    /** Small gap; used as a button spacer in the scan dialog. */
    val SpacingXSmall = 8.dp

    /** Gap after a section header image inside a card. */
    val SpacingSmall = 12.dp

    /** Standard gap between list items and form fields. */
    val SpacingMedium = 16.dp

    /** Gap between the header icon and the title text. */
    val SpacingLarge = 20.dp

    /** Gap between the form card and the primary action button. */
    val SpacingXLarge = 24.dp

    /** Top / bottom inset for screen-level content columns. */
    val SpacingXXLarge = 32.dp

    /** Generous vertical inset for the centered scan-form screen. */
    val SpacingHuge = 48.dp

    // ── Padding ───────────────────────────────────────────────────────────────

    /** Horizontal padding for screen-level content (LazyColumn, bottom-sheet, form column). */
    val ScreenPaddingHorizontal = 24.dp

    /** Vertical padding for screen-level content (LazyColumn, bottom-sheet). */
    val ScreenPaddingVertical = SpacingXXLarge

    /** Horizontal padding inside card content areas. */
    val CardContentPaddingHorizontal = 20.dp

    /** Vertical padding inside card content areas. */
    val CardContentPaddingVertical = SpacingXLarge

    /** Vertical padding applied to each data / issue row. */
    val DataRowPaddingVertical = 14.dp

    /** Top offset applied to the issue-row warning icon to align it with body text. */
    val IssueIconTopPadding = 2.dp

    // ── Component sizes ───────────────────────────────────────────────────────

    /** Size of the header logo / icon container surface. */
    val LogoContainerSize = 72.dp

    /** Size of the image or icon drawn inside the logo container. */
    val LogoIconSize = 90.dp

    /** Fixed height for all primary action buttons. */
    val ButtonHeight = 52.dp

    // ── NFC scan dialog — animation sizes ─────────────────────────────────────

    /** Box that wraps the NFC icon + ripple rings (constrains ring radii). */
    val NfcIndicatorBoxSize = 160.dp

    /** Diameter of the solid primary circle behind the NFC icon. */
    val NfcIconCircleSize = 80.dp

    /** Size of the NFC icon drawn inside the icon circle. */
    val NfcIconSize = 40.dp

    /** Gap from the icon circle edge to the first ripple ring. */
    val NfcRingInnerGap = 6.dp

    /** Stroke width of each ripple ring. */
    val NfcRingStrokeWidth = 2.dp

    /** Margin from the outermost ring to the box edge. */
    val NfcRingOuterMargin = 4.dp

    /** Diameter of the circle used for the success / fail status icon. */
    val StatusIconContainerSize = 88.dp

    /** Size of the icon drawn inside the success / fail circle. */
    val StatusIconSize = 44.dp

    /** Fixed height of the face photo in the result screen. */
    val FaceImageHeight = 200.dp

    /** Size of the warning icon in each issue row. */
    val IssueIconSize = 18.dp

    // ── Corner radii ──────────────────────────────────────────────────────────

    /** Corner radius for text fields, buttons, and image clips. */
    val CornerSmall = 12.dp

    /** Corner radius for elevated cards. */
    val CornerMedium = 16.dp

    /** Corner radius for the header logo / icon container surface. */
    val CornerLarge = 20.dp

    // ── Elevation ─────────────────────────────────────────────────────────────

    /** Default elevation for all [androidx.compose.material3.ElevatedCard]s. */
    val CardElevation = 2.dp

}
