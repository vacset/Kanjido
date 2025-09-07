package me.seta.vacset.qrwari.domain.promptpay

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptPayUtilsTest {

    @Test
    fun phoneVariants_mapToPhone() {
        listOf(
            "0812345678",
            "+66812345678",
            "66 81 234 5678",
            "0066812345678"
        ).forEach { raw ->
            assertEquals(
                PromptPayBuilder.IdType.PHONE,
                detectPromptPayIdType(raw)
            )
        }
    }

    @Test
    fun nationalId_13digits_mapsToNationalId() {
        assertEquals(
            PromptPayBuilder.IdType.NATIONAL_ID,
            detectPromptPayIdType("8857947646731") // for testing, dummy valid value
        )
    }

    @Test
    fun unsupported_formats_returnNull() {
        listOf(
            "0141234567890",    // bank account style
            "999999",           // too short
            "ABCDEFG"           // non-digits
        ).forEach { raw ->
            assertEquals(null, detectPromptPayIdType(raw))
        }
    }
}
