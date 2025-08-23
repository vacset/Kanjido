package me.seta.vacset.kanjido.presentation.navigation

sealed class Route(val path: String) {
    data object Entry: Route("entry")
    data object Participants: Route("participants")
    data object Review: Route("review")
    data object Summary: Route("summary")
}
