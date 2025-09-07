package me.seta.vacset.qrwari.presentation.navigation

sealed class Route(val path: String) {
    data object Entry : Route("entry")
    data object Participants : Route("participants")
    data object Review : Route("review")
    data object Summary : Route("summary")
    data object QuickQr : Route("quickqr/{amount}/{eventName}") { // Added eventName to path template
        fun path(amount: String, eventName: String) = "quickqr/$amount/$eventName" // Added eventName to function
    }

    data object QrPager : Route("qrpager")

    data object Settings : Route("settings")

}
