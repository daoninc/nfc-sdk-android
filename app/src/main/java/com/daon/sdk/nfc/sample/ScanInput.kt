package com.daon.sdk.nfc.sample

import com.daon.sdk.nfc.reader.passport.PassportParameters
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ScanInputError {
    DOCUMENT_NUMBER, DATE_OF_BIRTH, DATE_OF_EXPIRY
}

data class ScanInput(
    val documentNumber: String,
    val dateOfBirthMillis: Long?,
    val dateOfExpiryMillis: Long?,
    val activeAuthentication: Boolean,
) {
    fun validate(): ScanInputError? = when {
        documentNumber.isEmpty() || documentNumber.length > 9 -> ScanInputError.DOCUMENT_NUMBER
        dateOfBirthMillis == null -> ScanInputError.DATE_OF_BIRTH
        dateOfExpiryMillis == null -> ScanInputError.DATE_OF_EXPIRY
        else -> null
    }

    fun toParameters(): PassportParameters {
        val dob = requireNotNull(dateOfBirthMillis)
        val doe = requireNotNull(dateOfExpiryMillis)
        val formatter = SimpleDateFormat("yyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return PassportParameters(documentNumber, formatter.format(Date(dob)), formatter.format(Date(doe))).apply {
            enablePhoto(true)
            enableRawData(true)
            if (activeAuthentication) {
                setChallenge(ByteArray(8).also { SecureRandom().nextBytes(it) })
            }
        }
    }
}
