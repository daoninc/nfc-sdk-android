package com.daon.sdk.nfc.sample

import com.daon.sdk.nfc.sample.ScanInput
import com.daon.sdk.nfc.sample.ScanInputError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScanInputTest {
    @Test
    fun validationReportsFirstInvalidField() {
        assertEquals(ScanInputError.DOCUMENT_NUMBER, ScanInput("", null, null, false).validate())
        assertEquals(ScanInputError.DOCUMENT_NUMBER, ScanInput("1234567890", 0L, 0L, false).validate())
        assertEquals(ScanInputError.DATE_OF_BIRTH, ScanInput("A", null, 0L, false).validate())
        assertEquals(ScanInputError.DATE_OF_EXPIRY, ScanInput("123456789", 0L, null, false).validate())
        assertNull(ScanInput("A", 0L, 0L, false).validate())
    }

    @Test
    fun buildsUtcPassportParametersAndOptionalChallenge() {
        val input = ScanInput("AB1234567", 490_233_600_000L, 947_030_400_000L, true)
        val params = input.toParameters()
        assertEquals("AB1234567", params.get("documentNumber"))
        assertEquals("850715", params.get("dateOfBirth"))
        assertEquals("000105", params.get("dateOfExpiry"))
        assertEquals("true", params.get("daon.sdk.param.photo"))
        assertEquals("true", params.get("daon.sdk.param.ef.data"))
        assertNotNull(params.get("daon.sdk.param.challenge"))

        assertNull(ScanInput("A", 0L, 0L, false).toParameters().get("daon.sdk.param.challenge"))
    }
}
