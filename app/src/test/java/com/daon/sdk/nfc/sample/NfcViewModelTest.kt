package com.daon.sdk.nfc.sample

import android.app.Application
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the [NfcViewModel] state machine.
 *
 * [android.app.Application] is mocked so the ViewModel can be instantiated on the JVM
 * without a real Android runtime.  NFC hardware operations ([com.daon.sdk.nfc.NFCReader])
 * are never exercised here; the tests focus purely on the [kotlinx.coroutines.flow.StateFlow]
 * values that the ViewModel emits in response to method calls.
 *
 * Note: [NfcViewModel.initReader] is intentionally **not** called in these tests because
 * it constructs an [com.daon.sdk.nfc.NFCReader] which requires real Android context.
 * The ViewModel is designed to handle a `null` reader gracefully (all `nfcReader?.xxx()`
 * calls are null-safe), so the state-machine tests remain valid without it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NfcViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: NfcViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val application = mockk<Application>(relaxed = true)
        viewModel = NfcViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial dialogVisible is false`() = runTest {
        assertFalse(viewModel.dialogVisible.value)
    }

    @Test
    fun `initial showResult is false`() = runTest {
        assertFalse(viewModel.showResult.value)
    }

    @Test
    fun `initial uiState is default NfcUiState`() = runTest {
        assertEquals(NfcUiState(), viewModel.uiState.value)
    }

    // ── startScan ─────────────────────────────────────────────────────────────

    @Test
    fun `startScan makes dialogVisible true`() = runTest {
        val params = buildPassportParameters()
        viewModel.startScan(params)
        assertTrue(viewModel.dialogVisible.value)
    }

    @Test
    fun `startScan sets scanState to IN_PROGRESS`() = runTest {
        viewModel.startScan(buildPassportParameters())
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
    }

    @Test
    fun `startScan sets a non-empty message`() = runTest {
        viewModel.startScan(buildPassportParameters())
        assertTrue(viewModel.uiState.value.message.isNotEmpty())
    }

    // ── cancelScan ────────────────────────────────────────────────────────────

    @Test
    fun `cancelScan after startScan hides the dialog`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.cancelScan()
        assertFalse(viewModel.dialogVisible.value)
    }

    @Test
    fun `cancelScan resets uiState to default`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.cancelScan()
        assertEquals(NfcUiState(), viewModel.uiState.value)
    }

    @Test
    fun `cancelScan keeps showResult false`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.cancelScan()
        assertFalse(viewModel.showResult.value)
    }

    // ── retryScan ─────────────────────────────────────────────────────────────

    @Test
    fun `retryScan without prior startScan does not change state`() = runTest {
        // pendingParams is null — retryScan should be a no-op.
        val stateBefore = viewModel.uiState.value
        viewModel.retryScan()
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun `retryScan after startScan resets to IN_PROGRESS`() = runTest {
        viewModel.startScan(buildPassportParameters())
        // Simulate a failure by directly observing the reset.
        viewModel.retryScan()
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
    }

    // ── showResults ───────────────────────────────────────────────────────────

    @Test
    fun `showResults sets showResult to true`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.showResults()
        assertTrue(viewModel.showResult.value)
    }

    @Test
    fun `showResults hides the dialog`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.showResults()
        assertFalse(viewModel.dialogVisible.value)
    }

    // ── resetResult ───────────────────────────────────────────────────────────

    @Test
    fun `resetResult sets showResult back to false`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.showResults()
        viewModel.resetResult()
        assertFalse(viewModel.showResult.value)
    }

    @Test
    fun `resetResult resets uiState to default`() = runTest {
        viewModel.startScan(buildPassportParameters())
        viewModel.showResults()
        viewModel.resetResult()
        assertEquals(NfcUiState(), viewModel.uiState.value)
    }

    // ── resumeReader ──────────────────────────────────────────────────────────

    @Test
    fun `resumeReader when dialog is not visible does not change state`() = runTest {
        assertFalse(viewModel.dialogVisible.value)
        // Should be a no-op; no exception thrown.
        viewModel.resumeReader()
        assertFalse(viewModel.dialogVisible.value)
    }

    @Test
    fun `resumeReader when dialog is visible does not throw`() = runTest {
        viewModel.startScan(buildPassportParameters())
        assertTrue(viewModel.dialogVisible.value)
        // NFCReader is null so the null-safe call is a no-op; state must remain stable.
        viewModel.resumeReader()
        assertTrue(viewModel.dialogVisible.value)
    }

    // ── disableReader ─────────────────────────────────────────────────────────

    @Test
    fun `disableReader does not alter any state flow`() = runTest {
        viewModel.startScan(buildPassportParameters())
        val stateBefore = viewModel.uiState.value
        viewModel.disableReader()
        // NFCReader is null; the null-safe call must not change state.
        assertEquals(stateBefore, viewModel.uiState.value)
        assertTrue(viewModel.dialogVisible.value)
    }

    // ── onNewIntent ───────────────────────────────────────────────────────────

    @Test
    fun `onNewIntent without prior startScan is a no-op`() = runTest {
        // pendingParams is null; the call must be silently ignored.
        val stateBefore = viewModel.uiState.value
        viewModel.onNewIntent(null)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a minimal [com.daon.sdk.nfc.reader.passport.PassportParameters] using
     * MockK so the ViewModel can store it as [pendingParams] without requiring a real SDK.
     */
    private fun buildPassportParameters() =
        mockk<com.daon.sdk.nfc.reader.passport.PassportParameters>(relaxed = true)
}
