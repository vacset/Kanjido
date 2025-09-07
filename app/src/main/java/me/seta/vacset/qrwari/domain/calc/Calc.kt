package me.seta.vacset.qrwari.domain.calc

import me.seta.vacset.qrwari.data.model.*
import java.math.BigDecimal
import java.math.RoundingMode

private val TWO = BigDecimal("2")
private val ZERO = BigDecimal.ZERO

fun splitEvent(event: Event): SplitResult {
    require(event.participants.isNotEmpty()) { "At least one participant required." }

    // Helper maps
    val idToParticipant = event.participants.associateBy { it.id }
    val allIds = event.participants.map { it.id }.toSet()

    // Initialize running totals
    val totals = event.participants.associate { it.id to ZERO }.toMutableMap()

    // Split each item across its tagged participants (or ALL)
    event.items.forEach { item ->
        val targets = if (item.taggedParticipantIds.isEmpty() || item.taggedParticipantIds == setOf("ALL"))
            allIds else item.taggedParticipantIds
        val share = item.amount.divide(BigDecimal(targets.size), 10, RoundingMode.HALF_UP)
        targets.forEach { pid ->
            totals[pid] = totals.getValue(pid) + share
        }
    }

    // Round half up to 2 decimals person-wise
    var sumRounded = ZERO
    val rounded = totals.mapValues { (_, amt) ->
        amt.setScale(2, RoundingMode.HALF_UP).also { sumRounded += it }
    }.toMutableMap()

    val grand = event.items.fold(ZERO) { acc, it -> acc + it.amount }.setScale(2, RoundingMode.HALF_UP)
    val drift = grand - sumRounded

    var driftAppliedTo: String? = null
    // If tiny drift exists (|drift| <= 0.02), assign to largest total to preserve grand total
    if (drift.abs() <= BigDecimal("0.02") && drift.compareTo(ZERO) != 0) {
        val target = rounded.maxBy { it.value }.key
        rounded[target] = (rounded[target] ?: ZERO) + drift
        sumRounded += drift
        driftAppliedTo = target
    }

    val perPerson = rounded.map { (pid, amt) ->
        PersonTotal(participant = idToParticipant.getValue(pid), amount = amt)
    }.sortedBy { it.participant.name }

    return SplitResult(
        perPerson = perPerson,
        grandTotal = grand,
        driftAppliedTo = driftAppliedTo
    )
}
