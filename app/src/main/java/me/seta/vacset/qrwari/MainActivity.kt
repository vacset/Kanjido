package me.seta.vacset.qrwari

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import me.seta.vacset.qrwari.presentation.state.EventBuilderViewModel
import me.seta.vacset.qrwari.presentation.navigation.Route
import me.seta.vacset.qrwari.presentation.entry.EntryScreen
import me.seta.vacset.qrwari.presentation.entry.PadKey
import me.seta.vacset.qrwari.presentation.participants.ParticipantsScreen
import me.seta.vacset.qrwari.presentation.qr.QuickQrScreen
import me.seta.vacset.qrwari.presentation.review.QrPagerScreen
import me.seta.vacset.qrwari.presentation.review.ReviewScreen
import me.seta.vacset.qrwari.presentation.summary.SummaryScreen
import me.seta.vacset.qrwari.presentation.settings.PromptPayViewModel
import me.seta.vacset.qrwari.presentation.settings.SettingsScreen

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
                        // State
                        vm = vm,
                        promptPayId = promptPayId,
                        // Top-row actions
                        onEditEventName = {
                            // Example: quick rename without dialog
                            // Replace with your own dialog if needed
                            vm.updateEventName(vm.eventName) // no-op; keep hook in place
                            // TODO: change the control to edit box with save button
                        },
                        onOpenHistory = {
                            // TODO: nav to your history list when implemented
                        },
                        onOpenSettings = { nav.navigate(Route.Settings.path) },

                        // Participants panel

                        // Keypad
                        onPadPress = { key ->
                            when (key) {
                                is PadKey.Digit -> vm.pressDigit(key.d)
                                PadKey.Dot -> vm.pressDot()
                                PadKey.Backspace -> vm.backspace()
                                PadKey.Clear -> vm.clearAmount()
                                PadKey.Enter -> vm.enterAmount()
                                PadKey.Negate -> vm.negateAmount()
                            }
                        },

                        // Left-pane action
                        onCaptureBill = {
                            // Hook your receipt capture flow here
                        },

                        // Right-pane actions
                        onQuickQr = {
                            if (vm.items.count() == 0) {
                                vm.enterAmount()
                            }
                            val amt = vm.totalAmount.toPlainString()
                            nav.navigate(Route.QuickQr.path(amt))
                        },
                        onOpenReview = { nav.navigate(Route.Review.path) }
                    )
                }
                composable(Route.Participants.path) {
                    ParticipantsScreen(vm = vm, onNext = { nav.navigate(Route.Review.path) })
                }
                composable(Route.Review.path) {
                    ReviewScreen(
                        vm = vm, onNext = { nav.navigate(Route.QrPager.path) },
                        promptPayId = promptPayId
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
