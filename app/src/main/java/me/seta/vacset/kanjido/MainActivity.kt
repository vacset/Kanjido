package me.seta.vacset.kanjido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.seta.vacset.kanjido.presentation.navigation.Route
import me.seta.vacset.kanjido.presentation.entry.EntryScreen
import me.seta.vacset.kanjido.presentation.participant.ParticipantsScreen
import me.seta.vacset.kanjido.presentation.review.ReviewScreen
import me.seta.vacset.kanjido.presentation.summary.SummaryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Route.Entry.path) {
                composable(Route.Entry.path) { EntryScreen(onNext = { nav.navigate(Route.Participants.path) }) }
                composable(Route.Participants.path) { ParticipantsScreen(onNext = { nav.navigate(Route.Review.path) }) }
                composable(Route.Review.path) { ReviewScreen(onNext = { nav.navigate(Route.Summary.path) }) }
                composable(Route.Summary.path) { SummaryScreen() }
            }
        }
    }
}
