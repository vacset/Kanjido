package me.seta.vacset.qrwari.domain.calc

import me.seta.vacset.qrwari.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            )
        )

        val result = splitEvent(event)
        assertEquals(BigDecimal("50.00"), result.perPerson.first { it.participant.name=="A" }.amount)
        assertEquals(BigDecimal("50.00"), result.perPerson.first { it.participant.name=="B" }.amount)
        assertEquals(BigDecimal("100.00"), result.grandTotal)
    }

    @Test
    fun `drift of 0_01 assigned to one participant when splitting 100 among 3`() {
        val a = Participant(name = "A")
        val b = Participant(name = "B")
        val c = Participant(name = "C")

        val event = Event(
            name = "drift-3p",
            participants = listOf(a, b, c),
            items = listOf(
                Item(amount = BigDecimal("100.00"), taggedParticipantIds = setOf("ALL"))
            )
        )

        val result = splitEvent(event)

        // Totals should sum to grand total
        val sum = result.perPerson.map { it.amount }.reduce(BigDecimal::add)
        assertEquals(BigDecimal("100.00"), result.grandTotal)
        assertEquals(result.grandTotal, sum)

        // Expect exactly one person to have 33.34, others 33.33
        val amounts = result.perPerson.map { it.amount }
        val count334 = amounts.count { it == BigDecimal("33.34") }
        val count333 = amounts.count { it == BigDecimal("33.33") }
        assertTrue(count334 == 1 && count333 == 2)
    }
}
