package me.seta.vacset.qrwari.domain.promptpay

import java.math.BigDecimal
import java.util.Locale

/**
 * Build Thai QR PromptPay (Tag 29) EMV payload.
 *
 * Tag 29 subtemplate:
 *  - 00: AID "A000000677010111"
 *  - 01: Mobile (e.g., 0066XXXXXXXXX)  [only one of 01/02/03/04 is required]
 *  - 02: National ID / Tax ID (13 digits)
 *  - 03: e-Wallet ID (up to 15)
 *  - 04: Bank Account (bank code + account number, var up to 43)
 *
 * EMV top-level fields we set:
 *  - 00 = "01" (payload format indicator)
 *  - 01 = "11" static (no fixed amount) OR "12" dynamic (fixed amount)
 *  - 29 = PromptPay merchant account information (the subtemplate above)
 *  - 53 = "764" (currency THB)
 *  - 54 = amount (optional; when present we set PoI=12)
 *  - 58 = "TH" (country)
 *  - 63 = CRC-16/CCITT (X25) uppercase hex
 *
 *  Sample Usage
 *  // Example 1: Person-to-person using a Thai mobile number (dynamic with fixed amount)
 * val phonePromptPay = PromptPayBuilder.build(
 *     PromptPayBuilder.Input(
 *         idType = PromptPayBuilder.IdType.PHONE,
 *         idValueRaw = "0832434399",                 // raw local phone number
 *         amountTHB = BigDecimal("123.45")           // fixed amount → dynamic QR (PoI=12)
 *     )
 * )
 *
 * // Example 2: Person-to-person using National ID (static, no fixed amount)
 * val nationalIdPromptPay = PromptPayBuilder.build(
 *     PromptPayBuilder.Input(
 *         idType = PromptPayBuilder.IdType.NATIONAL_ID,
 *         idValueRaw = "1234567890123",              // 13-digit citizen ID
 *         amountTHB = null                           // no amount → static QR (PoI=11)
 *     )
 * )
 *
 * // Example 3: Transfer using Bank Account number (dynamic with fixed amount)
 * val bankAccountPromptPay = PromptPayBuilder.build(
 *     PromptPayBuilder.Input(
 *         idType = PromptPayBuilder.IdType.BANK_ACCOUNT,
 *         idValueRaw = "0141234567890",              // BankCode+AccountNo format
 *         amountTHB = BigDecimal("50.00")            // dynamic QR
 *     )
 * )
 *
 */
object PromptPayBuilder {

    enum class IdType { PHONE, NATIONAL_ID, TAX_ID, EWALLET_ID, BANK_ACCOUNT }

    data class Input(
        val idType: IdType,
        val idValueRaw: String,
        val amountTHB: BigDecimal? = null,   // when set → dynamic QR (PoI=12)
        val countryCode: String = "TH",
        val currencyCodeNumeric: String = "764",
        val poiStaticIfNoAmount: Boolean = true // if no amount: use 11 (static)
    )

    data class Result(
        val content: String,   // EMV payload string to encode into QR
        val isDynamic: Boolean // true if PoI=12
    )

    fun build(input: Input): Result {
        val idField = when (input.idType) {
            IdType.PHONE -> subTag("01", normalizePhoneTo0066(input.idValueRaw))
            IdType.NATIONAL_ID, IdType.TAX_ID -> subTag("02", digitsOnly(input.idValueRaw))
            IdType.EWALLET_ID -> subTag("03", digitsOnly(input.idValueRaw))
            IdType.BANK_ACCOUNT -> subTag("04", input.idValueRaw.trim())
        }

        // Tag 29 subtemplate: AID + one ID field
        val tag29Value = buildString {
            append(subTag("00", "A000000677010111")) // AID for merchant-presented PromptPay
            append(idField)
        }
        val tag29 = tag("29", tag29Value)

        // Top-level fields
        val top = mutableListOf(
            tag("00", "01"), // Payload Format Indicator
            // Point of Initiation: 12 when amount is fixed, else 11 (if configured)
            tag(
                "01",
                if (input.amountTHB != null) "12" else if (input.poiStaticIfNoAmount) "11" else "12"
            ),
            tag29,
            tag("53", input.currencyCodeNumeric),
            tag("58", input.countryCode.uppercase(Locale.ROOT))
        )

        // Amount (54) – optional but when present implies dynamic PoI
        input.amountTHB?.let {
            // EMV expects a decimal string; keep scale to 2
            val amt = it.setScale(2).toPlainString()
            top.add(tag("54", amt))
        }

        // Assemble all but CRC
        val withoutCrc = top.joinToString(separator = "")

        // Append CRC placeholder "63" + length "04"
        val toCrc = withoutCrc + "63" + "04"
        val crc = Crc16.ccitt(toCrc.toByteArray(Charsets.US_ASCII)).uppercase(Locale.ROOT)

        val full = withoutCrc + "63" + "04" + crc
        val isDyn = full.contains("010212") || input.amountTHB != null
        return Result(content = full, isDynamic = isDyn)
    }

    // ---------- helpers (EMV TLV utilities) ----------

    private fun tag(id2: String, value: String): String {
        require(id2.length == 2) { "Tag id must be 2 chars" }
        val len = value.length
        require(len in 1..99) { "Value length out of range for tag $id2" }
        return id2 + len.toString().padStart(2, '0') + value
    }

    private fun subTag(id2: String, value: String) = tag(id2, value)

    private fun digitsOnly(s: String) = s.filter { it.isDigit() }

    /**
     * Normalize Thai mobile to 0066XXXXXXXXX (remove leading 0 and non-digits).
     * Examples:
     *  "0812345678" -> "0066812345678"
     *  "+66812345678" -> "0066812345678"
     *  "66 81 234 5678" -> "0066812345678"
     */
    private fun normalizePhoneTo0066(raw: String): String {
        val d = digitsOnly(raw)
        return when {
            // already normalized international form
            d.startsWith("0066") -> d
            // "66xxxxxxxxx" -> add "00" prefix
            d.startsWith("66") -> "00$d"
            // local "0xxxxxxxxx" -> replace leading 0 with "0066"
            d.startsWith("0") && d.length >= 10 -> "0066" + d.drop(1)
            // best-effort: if it's just the 9-10 digit local without leading 0
            else -> "0066$d"
        }
    }

}
