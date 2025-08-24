package me.seta.vacset.kanjido.domain.calc

import me.seta.vacset.kanjido.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class SplitEngineTest {

    @Test
    fun `even split with half-up rounding`() {
        val a = Participant(name = "A")
        val b = Participant(name = "B")
        val event = Event(
            name = "test",
            participants = listOf(a, b),
            items = listOf(
                Item(amount = BigDecimal("100.00"), taggedParticipantIds = setOf("ALL"))
            ),
            promptPayId = null
        )

        val result = splitEvent(event)
        assertEquals(BigDecimal("50.00"), result.perPerson.first { it.participant.name=="A" }.amount)
        assertEquals(BigDecimal("50.00"), result.perPerson.first { it.participant.name=="B" }.amount)
        assertEquals(BigDecimal("100.00"), result.grandTotal)
    }

    @Test
    fun `drift of 0_01 assigned to largest payer`() {
        val a = Participant(name = "A")
        val b = Participant(name = "B")
        // 3 items: 0_01 drift scenario after rounding
        val event = Event(
            name = "drift",
            participants = listOf(a, b),
            items = listOf(
                Item(amount = BigDecimal("33.33"), taggedParticipantIds = setOf("ALL")),
                Item(amount = BigDecimal("33.33"), taggedParticipantIds = setOf("ALL")),
                Item(amount = BigDecimal("33.34"), taggedParticipantIds = setOf("ALL")),
            ),
            promptPayId = null
        )

        val result = splitEvent(event)
        assertEquals(BigDecimal("100.00"), result.grandTotal)

        val aAmt = result.perPerson.first { it.participant.name=="A" }.amount
        val bAmt = result.perPerson.first { it.participant.name=="B" }.amount
        // totals must sum to grandTotal
        assertEquals(BigDecimal("100.00"), aAmt + bAmt)
        // one of them should carry the extra 0.01
        val amounts = setOf(aAmt, bAmt)
        assert(amounts.contains(BigDecimal("50.01")) && amounts.contains(BigDecimal("49.99")))
    }
}
