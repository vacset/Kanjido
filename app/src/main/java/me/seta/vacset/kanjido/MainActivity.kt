package me.seta.vacset.kanjido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import me.seta.vacset.kanjido.presentation.navigation.Route
import me.seta.vacset.kanjido.presentation.entry.EntryScreen
import me.seta.vacset.kanjido.presentation.participants.ParticipantsScreen
import me.seta.vacset.kanjido.presentation.qr.QuickQrScreen
import me.seta.vacset.kanjido.presentation.review.QrPagerScreen
import me.seta.vacset.kanjido.presentation.review.ReviewScreen
import me.seta.vacset.kanjido.presentation.summary.SummaryScreen

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: EventBuilderViewModel = viewModel() // shared across NavHost

            NavHost(navController = nav, startDestination = Route.Entry.path) {
                composable(Route.Entry.path) {
                    EntryScreen(
                        vm = vm,
                        onNext = { nav.navigate(Route.Participants.path) },
                        // NEW: pass a lambda that navigates to QuickQr with the amount
                        onQuickQr = { amount ->
                            nav.navigate(Route.QuickQr.path(amount))
                        }
                    )
                }
                composable(Route.Participants.path) {
                    ParticipantsScreen(vm = vm, onNext = { nav.navigate(Route.Review.path) })
                }
                composable(Route.Review.path) {
                    ReviewScreen(vm = vm, onNext = { nav.navigate(Route.QrPager.path) })
                }
                composable(Route.QrPager.path) {
                    QrPagerScreen(vm = vm, onClose = { nav.popBackStack() })
                }
                composable(Route.Summary.path) {
                    SummaryScreen()
                }
                // NEW: Quick QR destination
                composable(Route.QuickQr.path) { backStack ->
                    val amount = backStack.arguments?.getString("amount").orEmpty()
                    QuickQrScreen(
                        amountTHB = amount,
                        onClose = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
