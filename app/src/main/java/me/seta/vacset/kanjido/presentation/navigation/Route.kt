package me.seta.vacset.kanjido.presentation.navigation

sealed class Route(val path: String) {
    data object Entry : Route("entry")
    data object Participants : Route("participants")
    data object Review : Route("review")
    data object Summary : Route("summary")
    data object QuickQr : Route("quickqr/{amount}") {
        fun path(amount: String) = "quickqr/$amount"
    }
    data object QrPager: Route("qrpager")
}
