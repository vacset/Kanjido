package me.seta.vacset.kanjido.domain.promptpay

/**
 * Heuristic to guess which PromptPay ID type a user entered.
 * Not support bank account (bank code+account number) due to false positive with phone number in some case
 * Rules:
 * - 0066..., +66..., 66..., or local 0xxxxxxxxx → PHONE
 * - exactly 13 digits → NATIONAL_ID
 * - otherwise → null
 */
fun detectPromptPayIdType(raw: String): PromptPayBuilder.IdType? {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.startsWith("0066") && digits.length >= 13 ||
                digits.startsWith("66") && digits.length == 11 ||
                digits.startsWith("0") && digits.length == 10 ->
            PromptPayBuilder.IdType.PHONE
        digits.length == 13 && isValidThaiNationalId(digits) ->
            PromptPayBuilder.IdType.NATIONAL_ID
        else ->
            null
    }
}

fun isValidThaiNationalId(d13: String): Boolean {
    if (d13.length != 13 || !d13.all { it.isDigit() }) return false
    val weights = (13 downTo 2).toList()
    val sum = d13.take(12).mapIndexed { i, c -> (c - '0') * weights[i] }.sum()
    val check = (11 - (sum % 11)) % 10
    return check == (d13.last() - '0')
}