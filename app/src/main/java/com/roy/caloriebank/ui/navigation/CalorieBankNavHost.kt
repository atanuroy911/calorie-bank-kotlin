package com.roy.caloriebank.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.ui.auth.IntroScreen
import com.roy.caloriebank.ui.auth.LoginScreen
import com.roy.caloriebank.ui.bank.BankScreen
import com.roy.caloriebank.ui.chat.ChatScreen
import com.roy.caloriebank.ui.home.HomeScreen
import com.roy.caloriebank.ui.manualentry.ManualBankWithdrawScreen
import com.roy.caloriebank.ui.manualentry.ManualExerciseScreen
import com.roy.caloriebank.ui.manualentry.ManualFoodScreen
import com.roy.caloriebank.ui.nutrition.NutritionDetailScreen
import com.roy.caloriebank.ui.onboarding.OnboardingScreen
import com.roy.caloriebank.ui.premium.PremiumScreen
import com.roy.caloriebank.ui.profile.EditProfileScreen
import com.roy.caloriebank.ui.profile.ProfileScreen
import com.roy.caloriebank.ui.settings.AiProviderSettingsScreen
import com.roy.caloriebank.ui.settings.SettingsScreen
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.transactions.TransactionsScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

object Routes {
    const val INTRO = "intro"
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"

    const val HOME = "home"
    const val CHAT = "chat"
    const val TRANSACTIONS = "transactions"
    const val BANK = "bank"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile/edit"
    const val PROFILE_SETTINGS = "profile/settings"
    const val PROFILE_AI_SETTINGS = "profile/ai-settings"
    const val PROFILE_PREMIUM = "profile/premium"
    const val NUTRITION = "nutrition"

    const val MANUAL_FOOD = "manual/food"
    const val MANUAL_EXERCISE = "manual/exercise"
    const val MANUAL_BANK_WITHDRAW = "manual/bank-withdraw"
}

private data class ShellTab(val route: String, val label: String, val icon: ImageVector)

private val shellTabs = listOf(
    ShellTab(Routes.HOME, "Home", Icons.Rounded.Home),
    ShellTab(Routes.CHAT, "AI", Icons.Rounded.AutoAwesome),
    ShellTab(Routes.TRANSACTIONS, "History", Icons.Rounded.ReceiptLong),
    ShellTab(Routes.BANK, "Bank", Icons.Rounded.AccountBalance),
    ShellTab(Routes.PROFILE, "Profile", Icons.Rounded.Person),
)

/** Small gate ViewModel that resolves the one-shot start destination from prefs (report section 8). */
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : androidx.lifecycle.ViewModel() {
    suspend fun resolveStartDestination(): String {
        val introSeen = preferencesRepository.introSeen.first()
        if (!introSeen) return Routes.INTRO
        val userId = preferencesRepository.userId.first()
        if (userId == null) return Routes.LOGIN
        val onboarded = preferencesRepository.onboardingCompleted.first()
        if (!onboarded) return Routes.ONBOARDING
        return Routes.HOME
    }
}

/**
 * Top-level nav host. Implements the real gating logic from report section 8:
 * intro -> login -> onboarding -> home shell, resolved once at launch (rather than a
 * reactive redirect-on-every-navigation like go_router). Explicit navigate() calls with
 * popUpTo(inclusive = true) are used at each transition point (finish intro, login/guest,
 * onboarding submit, sign out) to move between these flows.
 */
@Composable
fun CalorieBankNavHost(navController: NavHostController = rememberNavController()) {
    val gateViewModel: StartDestinationViewModel = hiltViewModel()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = gateViewModel.resolveStartDestination()
    }

    val resolved = startDestination
    if (resolved == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    NavHost(navController = navController, startDestination = resolved) {
        composable(Routes.INTRO) {
            IntroScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    // A freshly logged-in user (guest or google) is never onboarded yet.
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) { AppShell(navController) }

        composable(Routes.PROFILE_EDIT) {
            EditProfileScreen(onSaved = { navController.popBackStack() })
        }
        composable(Routes.PROFILE_SETTINGS) {
            SettingsScreen(
                onAiProviderSettings = { navController.navigate(Routes.PROFILE_AI_SETTINGS) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.PROFILE_AI_SETTINGS) { AiProviderSettingsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PROFILE_PREMIUM) { PremiumScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.NUTRITION) { NutritionDetailScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.MANUAL_FOOD) {
            ManualFoodScreen(onSaved = { navController.popBackStack() })
        }
        composable(Routes.MANUAL_EXERCISE) {
            ManualExerciseScreen(onSaved = { navController.popBackStack() })
        }
        composable(Routes.MANUAL_BANK_WITHDRAW) {
            ManualBankWithdrawScreen(onSaved = { navController.popBackStack() })
        }
    }
}

/**
 * Material3 bottom-nav shell hosting the 5 primary tabs in a nested NavHost, matching report
 * section 4's "App Shell" route table (Home/AI/History/Bank/Profile).
 */
@Composable
private fun AppShell(rootNavController: NavHostController) {
    val shellNavController = rememberNavController()

    // Any jump between shell tabs — whether from the bottom nav bar or an in-page shortcut like
    // the Home screen's profile avatar or "Log with CalBot" banner — must use this exact
    // popUpTo/launchSingleTop/restoreState pattern. A bare navigate(route) instead pushes a
    // duplicate back-stack entry for that tab; the bottom nav's own popUpTo(start) then only pops
    // that duplicate, leaving the tab looking selected but the Home tab unreachable/stuck.
    fun navigateToTab(route: String) {
        shellNavController.navigate(route) {
            popUpTo(shellNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by shellNavController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar(containerColor = SurfaceColor) {
                shellTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateToTab(tab.route) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryColor,
                            selectedTextColor = PrimaryColor,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor,
                            indicatorColor = SurfaceColor,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = shellNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenChat = { navigateToTab(Routes.CHAT) },
                    onOpenNutrition = { rootNavController.navigate(Routes.NUTRITION) },
                    onOpenProfile = { navigateToTab(Routes.PROFILE) },
                    onOpenTransactions = { navigateToTab(Routes.TRANSACTIONS) },
                    onOpenManualLog = { rootNavController.navigate(Routes.MANUAL_FOOD) },
                )
            }
            composable(Routes.CHAT) { ChatScreen() }
            composable(Routes.TRANSACTIONS) { TransactionsScreen() }
            composable(Routes.BANK) {
                BankScreen(onWithdraw = { rootNavController.navigate(Routes.MANUAL_BANK_WITHDRAW) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onEditProfile = { rootNavController.navigate(Routes.PROFILE_EDIT) },
                    onPremium = { rootNavController.navigate(Routes.PROFILE_PREMIUM) },
                    onAiSettings = { rootNavController.navigate(Routes.PROFILE_AI_SETTINGS) },
                    onSettings = { rootNavController.navigate(Routes.PROFILE_SETTINGS) },
                    onSignedOut = {
                        rootNavController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
