package com.daon.sdk.nfc.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [NfcUiState] and [NfcScanState].
 *
 * These tests verify that the data class behaves correctly with its default values,
 * copy semantics, and equality contract, all of which are relied upon by the ViewModel
 * when emitting state updates.
 */
class NfcUiStateTest {

    // ── NfcScanState ──────────────────────────────────────────────────────────

    @Test
    fun `NfcScanState has exactly three values`() {
        val values = NfcScanState.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `NfcScanState entries are IN_PROGRESS, SUCCESS, FAIL`() {
        assertEquals(NfcScanState.IN_PROGRESS, NfcScanState.entries[0])
        assertEquals(NfcScanState.SUCCESS, NfcScanState.entries[1])
        assertEquals(NfcScanState.FAIL, NfcScanState.entries[2])
    }

    // ── NfcUiState defaults ───────────────────────────────────────────────────

    @Test
    fun `default NfcUiState has IN_PROGRESS scan state`() {
        val state = NfcUiState()
        assertEquals(NfcScanState.IN_PROGRESS, state.scanState)
    }

    @Test
    fun `default NfcUiState has empty message`() {
        val state = NfcUiState()
        assertEquals("", state.message)
    }

    @Test
    fun `default NfcUiState has null progress`() {
        val state = NfcUiState()
        assertNull(state.progress)
    }

    @Test
    fun `default NfcUiState has null result`() {
        val state = NfcUiState()
        assertNull(state.result)
    }

    // ── NfcUiState copy semantics ─────────────────────────────────────────────

    @Test
    fun `copy only changes the specified field`() {
        val original = NfcUiState(
            scanState = NfcScanState.IN_PROGRESS,
            message = "Reading…",
            progress = 0.5f,
        )
        val updated = original.copy(scanState = NfcScanState.SUCCESS)

        assertEquals(NfcScanState.SUCCESS, updated.scanState)
        // Unchanged fields remain intact.
        assertEquals("Reading…", updated.message)
        assertEquals(0.5f, updated.progress)
    }

    @Test
    fun `two NfcUiState instances with same values are equal`() {
        val a = NfcUiState(scanState = NfcScanState.FAIL, message = "Error")
        val b = NfcUiState(scanState = NfcScanState.FAIL, message = "Error")
        assertEquals(a, b)
    }

    @Test
    fun `NfcUiState instances with different scan states are not equal`() {
        val a = NfcUiState(scanState = NfcScanState.IN_PROGRESS)
        val b = NfcUiState(scanState = NfcScanState.SUCCESS)
        assert(a != b)
    }

    // ── Progress field boundary values ────────────────────────────────────────

    @Test
    fun `progress 0f is a valid value (different from null)`() {
        val state = NfcUiState(progress = 0f)
        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress 1f represents a fully-complete read`() {
        val state = NfcUiState(progress = 1f)
        assertEquals(1f, state.progress)
    }

    @Test
    fun `progress mid-value is stored precisely`() {
        val state = NfcUiState(progress = 0.42f)
        assertEquals(0.42f, state.progress ?: 0f, 0.0001f)
    }
}
