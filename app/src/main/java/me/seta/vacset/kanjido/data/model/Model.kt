package me.seta.vacset.kanjido.data.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val label: String? = null, // optional
    val amount: BigDecimal,
    val taggedParticipantIds: Set<String> // one, many, or special "ALL"
)

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,        // e.g., "16 Aug evening"
    val createdAt: Instant = Instant.now(),
    val participants: List<Participant>,
    val items: List<Item>
)

data class PersonTotal(
    val participant: Participant,
    val amount: BigDecimal
)

data class SplitResult(
    val perPerson: List<PersonTotal>,
    val grandTotal: BigDecimal,
    val driftAppliedTo: String? // participant id
)
