package com.daon.sdk.nfc.sample

import android.app.Application
import android.content.Intent
import android.nfc.NfcAdapter
import android.util.Log
import com.daon.sdk.nfc.NFCReader
import com.daon.sdk.nfc.TagData
import com.daon.sdk.nfc.TagError
import com.daon.sdk.nfc.TagReaderCallback
import com.daon.sdk.nfc.sample.NfcScanState
import com.daon.sdk.nfc.sample.NfcUiState
import com.daon.sdk.nfc.sample.NfcViewModel
import com.daon.sdk.nfc.sample.ScanInput
import com.daon.sdk.nfc.sample.ScanInputError
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
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
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        val application = mockk<Application>(relaxed = true)
        every { application.getString(any()) } returns "status"
        viewModel = NfcViewModel(application)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
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

    @Test
    fun `invalid form input does not open the scan dialog`() = runTest {
        assertEquals(
            ScanInputError.DOCUMENT_NUMBER,
            viewModel.startScan(ScanInput("", null, null, false)),
        )
        assertFalse(viewModel.dialogVisible.value)
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
    fun `retryScan while scan is in progress does not restart it`() = runTest {
        viewModel.startScan(buildPassportParameters())
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

    @Test
    fun `resuming an unfinished scan reattempts dispatch`() = runTest {
        val reader = readyReader()
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.resumeReader()
        verify(exactly = 2) { reader.enable() }

        viewModel.disableReader()
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
        verify(exactly = 1) { reader.disable() }

        viewModel.resumeReader()
        verify(exactly = 3) { reader.enable() }
    }

    @Test
    fun `failed dispatch registration is not cached as enabled`() = runTest {
        val reader = readyReader()
        every { reader.enable() } returns Unit
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())

        verify(exactly = 1) { reader.enable() }
        viewModel.resumeReader()
        verify(exactly = 2) { reader.enable() }
        viewModel.disableReader()
        verify(exactly = 1) { reader.disable() }
    }

    @Test
    fun `backgrounding a read fails the scan without rearming dispatch`() = runTest {
        val reader = readyReader()
        val callback = slot<TagReaderCallback>()
        every { reader.read(any<Intent>(), any(), capture(callback)) } returns Unit
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.onNewIntent(tagIntent())
        viewModel.disableReader()

        assertEquals(NfcScanState.FAIL, viewModel.uiState.value.scanState)
        viewModel.resumeReader()
        verify(exactly = 1) { reader.enable() }
        callback.captured.tagReaderFailed(TagError.ERROR_CANCELLED, "cancelled")
        assertEquals(NfcScanState.FAIL, viewModel.uiState.value.scanState)

        viewModel.retryScan()
        verify(exactly = 2) { reader.enable() }
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
        callback.captured.tagReaderFailed(TagError.ERROR_CANCELLED, "late")
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
        verify(exactly = 2) { reader.enable() }
    }

    @Test
    fun `cancelled read cannot overwrite a new scan`() = runTest {
        val reader = readyReader()
        val callback = slot<TagReaderCallback>()
        every { reader.read(any<Intent>(), any(), capture(callback)) } returns Unit
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.onNewIntent(tagIntent())
        val previousCallback = callback.captured
        viewModel.cancelScan()
        viewModel.startScan(buildPassportParameters())

        previousCallback.tagReaderFailed(TagError.ERROR_CANCELLED, "late")
        assertTrue(viewModel.dialogVisible.value)
        assertEquals(NfcScanState.IN_PROGRESS, viewModel.uiState.value.scanState)
        verify(exactly = 2) { reader.enable() }
    }

    @Test
    fun `a second tag intent does not start an overlapping read`() = runTest {
        val reader = readyReader()
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())

        viewModel.onNewIntent(tagIntent())
        viewModel.onNewIntent(tagIntent())

        verify(exactly = 1) { reader.read(any<Intent>(), any(), any()) }
    }

    @Test
    fun `tag delivered between pause and resume starts one read on resume`() = runTest {
        val reader = readyReader()
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.disableReader()

        viewModel.onNewIntent(tagIntent())
        verify(exactly = 0) { reader.read(any<Intent>(), any(), any()) }

        viewModel.resumeReader()
        verify(exactly = 1) { reader.read(any<Intent>(), any(), any()) }
        viewModel.onNewIntent(tagIntent())
        verify(exactly = 1) { reader.read(any<Intent>(), any(), any()) }
    }

    @Test
    fun `cancelled scan discards tag received during pause`() = runTest {
        val reader = readyReader()
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.disableReader()
        viewModel.onNewIntent(tagIntent())
        viewModel.cancelScan()

        viewModel.resumeReader()
        verify(exactly = 0) { reader.read(any<Intent>(), any(), any()) }
    }

    @Test
    fun `tag queued during pause is discarded on stop`() = runTest {
        val reader = readyReader()
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.disableReader()
        viewModel.onNewIntent(tagIntent())
        viewModel.stopReader()

        viewModel.resumeReader()
        verify(exactly = 0) { reader.read(any<Intent>(), any(), any()) }
    }

    @Test
    fun `completed scan stays disabled after activity resumes`() = runTest {
        val reader = readyReader()
        val callback = slot<TagReaderCallback>()
        every { reader.read(any<Intent>(), any(), capture(callback)) } returns Unit
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.onNewIntent(tagIntent())
        callback.captured.tagReaderDidFinish(mockk<TagData>())

        assertEquals(NfcScanState.SUCCESS, viewModel.uiState.value.scanState)
        verify(exactly = 1) { reader.disable() }
        viewModel.disableReader()
        viewModel.resumeReader()
        verify(exactly = 1) { reader.enable() }
    }

    @Test
    fun `failed scan stays disabled until retry`() = runTest {
        val reader = readyReader()
        val callback = slot<TagReaderCallback>()
        every { reader.read(any<Intent>(), any(), capture(callback)) } returns Unit
        viewModel.resumeReader()
        viewModel.startScan(buildPassportParameters())
        viewModel.onNewIntent(tagIntent())
        callback.captured.tagReaderFailed(TagError.ERROR_CONNECTION_LOST, "lost")

        assertEquals(NfcScanState.FAIL, viewModel.uiState.value.scanState)
        verify(exactly = 1) { reader.disable() }
        viewModel.disableReader()
        viewModel.resumeReader()
        verify(exactly = 1) { reader.enable() }
        viewModel.retryScan()
        verify(exactly = 2) { reader.enable() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun readyReader(): NFCReader = mockk<NFCReader>(relaxed = true).also {
        every { it.isSupported() } returns true
        every { it.isEnabled() } returns true
        viewModel.nfcReader = it
    }

    private fun tagIntent(): Intent = mockk {
        every { action } returns NfcAdapter.ACTION_TECH_DISCOVERED
    }

    /**
     * Builds a minimal [com.daon.sdk.nfc.reader.passport.PassportParameters] using
     * MockK so the ViewModel can store it as [pendingParams] without requiring a real SDK.
     */
    private fun buildPassportParameters() =
        mockk<com.daon.sdk.nfc.reader.passport.PassportParameters>(relaxed = true)
}
