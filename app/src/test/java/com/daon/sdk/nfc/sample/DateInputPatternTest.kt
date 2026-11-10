package com.daon.sdk.nfc.sample

import com.daon.sdk.nfc.sample.ui.scan.dateInputPattern
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 28])
class DateInputPatternTest {
    @Test
    fun usesLocaleOrderAndSeparatorsWithFullLengthFields() {
        assertEquals("MM/DD/YYYY", dateInputPattern(Locale.US))
        assertEquals("DD/MM/YYYY", dateInputPattern(Locale.UK))
        assertEquals("DD.MM.YYYY", dateInputPattern(Locale.GERMANY))
        assertEquals("YYYY/MM/DD", dateInputPattern(Locale.JAPAN))
    }
}
