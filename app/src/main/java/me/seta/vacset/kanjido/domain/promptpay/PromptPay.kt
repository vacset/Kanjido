package me.seta.vacset.kanjido.domain.promptpay

import java.util.Locale

data class PromptPayPayload(
    val content: String, // EMV-compatible text payload
    val reference: String? = null
)

/**
 * Minimal builder: phone/citizen ID + amount + optional reference.
 * NOTE: Validate with receiving banks during QA; adjust EMV tags as needed.
 */
object PromptPayBuilder {
    fun build(id: String, amountTHB: String, reference: String?): PromptPayPayload {
        // Sanitize: digits only for phone/citizen ID
        val normalizedId = id.filter { it.isDigit() }

        // Placeholder: return a conventional format that many reader apps accept.
        // Replace with full EMV tag build + CRC in a later commit.
        val content = buildString {
            append("PROMPTPAY:")
            append(normalizedId)
            if (amountTHB.isNotBlank()) {
                append("|AMT="); append(amountTHB)
            }
            if (!reference.isNullOrBlank()) {
                append("|REF="); append(reference.uppercase(Locale.getDefault()))
            }
        }
        return PromptPayPayload(content = content, reference = reference)
    }
}
