package com.daon.sdk.nfc.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Unit tests for the private utility functions in `ScanScreen.kt` and `ResultScreen.kt`.
 *
 * Because these functions are `private`, their logic is mirrored here so they can be
 * exercised on the JVM without any Android dependencies.  Any change to the originals
 * must be reflected here to keep the tests accurate.
 */
class UtilityFunctionTest {

    // ── Mirrored implementations ──────────────────────────────────────────────

    /** Mirror of the private `validate` in ScanScreen.kt. */
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

    /** Mirror of the private `formatDisplayDate` in ScanScreen.kt. */
    private fun formatDisplayDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }

    /** Mirror of the private `formatShortDate` in ScanScreen.kt. */
    private fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyMMdd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }

    /** Mirror of the private `String?.meaningful()` extension in ResultScreen.kt. */
    private fun String?.meaningful(): String? =
        takeIf { !it.isNullOrBlank() && it != "NA" }

    // ── validate() ────────────────────────────────────────────────────────────

    @Test
    fun `validate returns null for valid inputs`() {
        val result = validate("AB1234567", "850715", "271231")
        assertNull(result)
    }

    @Test
    fun `validate returns error when document number is empty`() {
        val result = validate("", "850715", "271231")
        assertNotNull(result)
    }

    @Test
    fun `validate returns error when document number exceeds 9 characters`() {
        val result = validate("AB123456789", "850715", "271231")
        assertNotNull(result)
    }

    @Test
    fun `validate accepts a single-character document number`() {
        val result = validate("A", "850715", "271231")
        assertNull(result)
    }

    @Test
    fun `validate accepts exactly 9-character document number`() {
        val result = validate("123456789", "850715", "271231")
        assertNull(result)
    }

    @Test
    fun `validate returns error when date of birth is null`() {
        val result = validate("AB1234567", null, "271231")
        assertNotNull(result)
        assertEquals("Please select a date of birth.", result)
    }

    @Test
    fun `validate returns error when date of expiry is null`() {
        val result = validate("AB1234567", "850715", null)
        assertNotNull(result)
        assertEquals("Please select a date of expiry.", result)
    }

    @Test
    fun `validate checks document number before dates`() {
        // Empty doc number should fail before checking null dates.
        val result = validate("", null, null)
        assertNotNull(result)
        assertEquals("Document number must be between 1 and 9 characters.", result)
    }

    // ── formatDisplayDate() ───────────────────────────────────────────────────

    @Test
    fun `formatDisplayDate formats a known date correctly`() {
        // 1985-07-15 00:00:00 UTC = 490233600000 ms
        val millis = 490_233_600_000L
        val result = formatDisplayDate(millis)
        assertEquals("Jul 15, 1985", result)
    }

    @Test
    fun `formatDisplayDate formats single-digit day without leading zero`() {
        // 2000-01-05 00:00:00 UTC
        val millis = 947_030_400_000L
        val result = formatDisplayDate(millis)
        assertEquals("Jan 5, 2000", result)
    }

    @Test
    fun `formatDisplayDate uses UTC not local timezone`() {
        // Any midnight UTC value should not shift to the previous day.
        val millis = 0L // 1970-01-01 00:00:00 UTC
        val result = formatDisplayDate(millis)
        assertEquals("Jan 1, 1970", result)
    }

    // ── formatShortDate() ─────────────────────────────────────────────────────

    @Test
    fun `formatShortDate produces yyMMdd format`() {
        // 1985-07-15 UTC
        val millis = 490_233_600_000L
        val result = formatShortDate(millis)
        assertEquals("850715", result)
    }

    @Test
    fun `formatShortDate uses UTC to avoid timezone drift`() {
        val millis = 0L // 1970-01-01
        val result = formatShortDate(millis)
        assertEquals("700101", result)
    }

    @Test
    fun `formatShortDate pads month and day with leading zeros`() {
        // 2000-01-05 UTC
        val millis = 947_030_400_000L
        val result = formatShortDate(millis)
        assertEquals("000105", result)
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
