package com.daon.sdk.nfc.sample

import com.daon.sdk.nfc.sample.ui.scan.formatDisplayDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

/**
 * Unit tests for display helpers in `DocumentFormCard.kt` and `PassportDataCard.kt`.
 *
 * Input validation and passport date conversion are tested against the actual
 * implementation in [ScanInputTest].
 */
class UtilityFunctionTest {

    // ── Mirrored implementations ──────────────────────────────────────────────

    /** Mirror of the private `String?.meaningful()` extension in PassportDataCard.kt. */
    private fun String?.meaningful(): String? =
        takeIf { !it.isNullOrBlank() && it != "NA" }

    // ── formatDisplayDate() ───────────────────────────────────────────────────

    @Test
    fun `formatDisplayDate formats a known date correctly`() {
        // 1985-07-15 00:00:00 UTC = 490233600000 ms
        val millis = 490_233_600_000L
        val result = formatDisplayDate(millis, Locale.US)
        assertEquals("Jul 15, 1985", result)
    }

    @Test
    fun `formatDisplayDate formats single-digit day without leading zero`() {
        // 2000-01-05 00:00:00 UTC
        val millis = 947_030_400_000L
        val result = formatDisplayDate(millis, Locale.US)
        assertEquals("Jan 5, 2000", result)
    }

    @Test
    fun `formatDisplayDate uses UTC not local timezone`() {
        // Any midnight UTC value should not shift to the previous day.
        val millis = 0L // 1970-01-01 00:00:00 UTC
        val originalTimeZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            assertEquals("Jan 1, 1970", formatDisplayDate(millis, Locale.US))
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun `formatDisplayDate follows locale date order and month names`() {
        val millis = 490_233_600_000L
        assertEquals("15 Jul 1985", formatDisplayDate(millis, Locale.UK))
        assertEquals("15.07.1985", formatDisplayDate(millis, Locale.GERMANY))
        assertEquals("15 juil. 1985", formatDisplayDate(millis, Locale.FRANCE))
    }

    // ── String?.meaningful() ─────────────────────────────────────────────────

    @Test
    fun `meaningful returns the string when it has content`() {
        assertEquals("John", "John".meaningful())
    }

    @Test
    fun `meaningful returns null for null input`() {
        assertNull(null.meaningful())
    }

    @Test
    fun `meaningful returns null for blank string`() {
        assertNull("   ".meaningful())
    }

    @Test
    fun `meaningful returns null for empty string`() {
        assertNull("".meaningful())
    }

    @Test
    fun `meaningful returns null for SDK placeholder NA`() {
        assertNull("NA".meaningful())
    }

    @Test
    fun `meaningful returns non-NA strings that contain NA as substring`() {
        // "CANADA" is a real value and should pass through.
        assertEquals("CANADA", "CANADA".meaningful())
    }

    @Test
    fun `meaningful is case-sensitive - lowercase na is returned`() {
        // Only the exact string "NA" is filtered; "na" is a valid value.
        assertEquals("na", "na".meaningful())
    }
}
