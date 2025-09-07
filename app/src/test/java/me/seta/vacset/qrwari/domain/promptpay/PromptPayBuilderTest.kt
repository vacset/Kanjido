package me.seta.vacset.qrwari.domain.promptpay

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PromptPayBuilderTest {

    // --- Helpers -------------------------------------------------------------

    /** Extract TLV value for a 2-char tag (e.g., "29", "54", "63"). */
    private fun tlvValue(payload: String, tag: String): String? {
        var i = 0
        while (i + 4 <= payload.length) {
            val t = payload.substring(i, i + 2)
            val len = payload.substring(i + 2, i + 4).toInt()
            val start = i + 4
            val end = start + len
            if (end > payload.length) return null
            if (t == tag) return payload.substring(start, end)
            i = end
        }
        return null
    }

    /** Extract Tag-29 subtemplate value for a 2-char subTag (e.g., "00","01","02","03","04"). */
    private fun tag29SubValue(payload: String, subTag: String): String? {
        val tag29 = tlvValue(payload, "29") ?: return null
        var i = 0
        while (i + 4 <= tag29.length) {
            val t = tag29.substring(i, i + 2)
            val len = tag29.substring(i + 2, i + 4).toInt()
            val start = i + 4
            val end = start + len
            if (end > tag29.length) return null
            if (t == subTag) return tag29.substring(start, end)
            i = end
        }
        return null
    }

    /** Validate CRC by recomputing over everything up to (and including) "63" + "04". */
    private fun isCrcValid(payload: String): Boolean {
        val idx = payload.indexOf("63")
        if (idx < 0 || idx + 4 >= payload.length) return false
        val given = payload.takeLast(4)
        val toCrc = payload.substring(0, idx + 4) // includes "63" + "04"
        val calc = Crc16.ccitt(toCrc.toByteArray(Charsets.US_ASCII))
        return given.equals(calc, ignoreCase = true)
    }

    // --- Tests ---------------------------------------------------------------

    @Test
    fun buildPhoneNumberDynamicQr_shouldUsePoI12_andContainTag29WithAIDandPhoneOnly() {
        val qr = PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = PromptPayBuilder.IdType.PHONE,
                idValueRaw = "081-234-5678",
                amountTHB = BigDecimal("123.45") // dynamic
            )
        )

        // PoI = 12 (dynamic)
        val poi = tlvValue(qr.content, "01")
        assertEquals("12", poi)

        // Currency THB and country TH
        assertEquals("764", tlvValue(qr.content, "53"))
        assertEquals("TH", tlvValue(qr.content, "58"))

        // Amount present and exact
        assertEquals("123.45", tlvValue(qr.content, "54"))

        // Tag 29: has AID and phone subtag only
        assertEquals("A000000677010111", tag29SubValue(qr.content, "00"))
        assertNotNull(tag29SubValue(qr.content, "01")) // phone
        assertNull(tag29SubValue(qr.content, "02"))
        assertNull(tag29SubValue(qr.content, "03"))
        assertNull(tag29SubValue(qr.content, "04"))

        // CRC integrity
        assertTrue(isCrcValid(qr.content))
    }

    @Test
    fun buildNationalIdStaticQr_shouldUsePoI11_andContainOnlyNationalIdSubTag() {
        val qr = PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = PromptPayBuilder.IdType.NATIONAL_ID,
                idValueRaw = "1234567890123",
                amountTHB = null // static
            )
        )

        // PoI = 11 (static)
        assertEquals("11", tlvValue(qr.content, "01"))

        // No amount tag (54)
        assertNull(tlvValue(qr.content, "54"))

        // Tag 29: AID and national-id subtag only
        assertEquals("A000000677010111", tag29SubValue(qr.content, "00"))
        assertNotNull(tag29SubValue(qr.content, "02"))
        assertNull(tag29SubValue(qr.content, "01"))
        assertNull(tag29SubValue(qr.content, "03"))
        assertNull(tag29SubValue(qr.content, "04"))

        assertTrue(isCrcValid(qr.content))
    }

    @Test
    fun buildBankAccountDynamicQr_shouldContainOnlyBankAccountSubTag() {
        val qr = PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = PromptPayBuilder.IdType.BANK_ACCOUNT,
                idValueRaw = "0141234567890",       // example format: BankCode+AccountNumber
                amountTHB = BigDecimal("50.00")
            )
        )

        // Dynamic (12) because amount present
        assertEquals("12", tlvValue(qr.content, "01"))

        // Tag 29: AID and bank-account subtag only
        assertEquals("A000000677010111", tag29SubValue(qr.content, "00"))
        assertNotNull(tag29SubValue(qr.content, "04"))
        assertNull(tag29SubValue(qr.content, "01"))
        assertNull(tag29SubValue(qr.content, "02"))
        assertNull(tag29SubValue(qr.content, "03"))

        assertTrue(isCrcValid(qr.content))
    }

    @Test
    fun normalizePhone_shouldYieldSameSubTagForVariousInputs() {
        val rawForms = listOf(
            "0812345678",
            "+66812345678",
            "66 81 234 5678",
            "0066812345678"
        )

        // Build once with the first form to compare
        val baseline = PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = PromptPayBuilder.IdType.PHONE,
                idValueRaw = rawForms.first(),
                amountTHB = BigDecimal("1.00")
            )
        )
        val baselinePhone = tag29SubValue(baseline.content, "01")!! // should exist

        rawForms.drop(1).forEach { f ->
            val qr = PromptPayBuilder.build(
                PromptPayBuilder.Input(
                    idType = PromptPayBuilder.IdType.PHONE,
                    idValueRaw = f,
                    amountTHB = BigDecimal("1.00")
                )
            )
            val phone = tag29SubValue(qr.content, "01")
            assertEquals(
                "Different normalization for input: $f",
                baselinePhone,
                phone
            )
        }
    }

    @Test
    fun crc_shouldMatchComputedCcittOverPayloadIncluding6304() {
        val qr = PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = PromptPayBuilder.IdType.PHONE,
                idValueRaw = "0812345678",
                amountTHB = BigDecimal("10.00")
            )
        )

        assertTrue("CRC mismatch or malformed payload", isCrcValid(qr.content))
        // Sanity: payload ends with 63 04 <4-hex>
        assertTrue(qr.content.matches(Regex(".*6304[0-9A-Fa-f]{4}$")))
    }
}
