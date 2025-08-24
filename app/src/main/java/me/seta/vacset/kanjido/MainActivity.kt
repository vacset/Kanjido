package me.seta.vacset.kanjido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import me.seta.vacset.kanjido.presentation.settings.PromptPayViewModel
import me.seta.vacset.kanjido.presentation.settings.SettingsScreen

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: EventBuilderViewModel = viewModel() // shared across NavHost
            val settingsVm: PromptPayViewModel = viewModel()
            // observe saved PromptPay ID (null if not set)
            val promptPayId = settingsVm.promptPayIdFlow.collectAsState().value

            NavHost(navController = nav, startDestination = Route.Entry.path) {
                composable(Route.Entry.path) {
                    EntryScreen(
                        vm = vm,
                        onNext = { nav.navigate(Route.Participants.path) },
                        // NEW: pass a lambda that navigates to QuickQr with the amount
                        onQuickQr = { amount ->
                            nav.navigate(Route.QuickQr.path(amount))
                        },
                        promptPayId = promptPayId,
                        onOpenSettings = { nav.navigate(Route.Settings.path) }
                    )
                }
                composable(Route.Participants.path) {
                    ParticipantsScreen(vm = vm, onNext = { nav.navigate(Route.Review.path) })
                }
                composable(Route.Review.path) {
                    ReviewScreen(
                        vm = vm, onNext = { nav.navigate(Route.QrPager.path) },
                        promptPayId = promptPayId,
                        onOpenSettings = { nav.navigate(Route.Settings.path) }
                    )
                }
                composable(Route.QrPager.path) {
                    QrPagerScreen(
                        vm = vm,
                        promptPayId = promptPayId,
                        onBack = { nav.popBackStack() })
                }
                composable(Route.Summary.path) {
                    SummaryScreen()
                }
                // Quick QR destination
                composable(Route.QuickQr.path) { backStack ->
                    val amount = backStack.arguments?.getString("amount").orEmpty()
                    QuickQrScreen(
                        amountTHB = amount,
                        promptPayId = promptPayId,         // <— from Settings VM state
                        onBack = { nav.popBackStack() }
                    )
                }

                composable(Route.Settings.path) {
                    SettingsScreen(
                        vm = settingsVm,
                        onDone = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
