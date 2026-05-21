package com.mindeye.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindeye.app.feature.mindeye.ui.DestinationAskScreen
import com.mindeye.app.feature.mindeye.ui.PreTripScanScreen
import com.mindeye.app.feature.mindeye.ui.QuickAskScreen
import com.mindeye.app.feature.mindeye.ui.TravelNavigationScreen
import com.mindeye.app.feature.mindeye.ui.TravelPlanScreen
import com.mindeye.app.feature.settings.viewmodel.SettingsViewModel
import com.mindeye.app.feature.settings.ui.SettingsScreen
import com.mindeye.app.feature.mindeye.ui.OcrScreen
import com.mindeye.app.feature.home.ui.HomeScreen
import com.mindeye.app.feature.psychology.ui.SupportScreen
import com.mindeye.app.feature.community.ui.CommunityScreen
import com.mindeye.app.feature.volunteer.ui.VolunteerScreen
import com.mindeye.app.feature.volunteer.viewmodel.VolunteerViewModel
import com.mindeye.app.feature.sos.ui.EmergencyScreen
import com.mindeye.app.feature.home.viewmodel.HomeViewModel
import com.mindeye.app.feature.psychology.viewmodel.SupportViewModel
import com.mindeye.app.feature.community.viewmodel.CommunityViewModel
import com.mindeye.app.feature.sos.viewmodel.EmergencyViewModel
import com.mindeye.app.feature.mindeye.viewmodel.OcrViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MindEyeDestination : Screen("mindeye_destination")
    data object MindEyePreTripScan : Screen("mindeye_pretrip_scan") {
        fun createRoute(destination: String): String = "mindeye_pretrip_scan/${URLEncoder.encode(destination, "UTF-8")}"
    }
    data object MindEyeTravelPlan : Screen("mindeye_travel_plan") {
        fun createRoute(destination: String): String = "mindeye_travel_plan/${URLEncoder.encode(destination, "UTF-8")}"
    }
    data object MindEyeNavigation : Screen("mindeye_navigation") {
        fun createRoute(destination: String): String = "mindeye_navigation/${URLEncoder.encode(destination, "UTF-8")}"
    }
    data object QuickAsk : Screen("quick_ask")
    data object Psychology : Screen("psychology")
    data object Community : Screen("community")
    data object Volunteer : Screen("volunteer")
    data object Emergency : Screen("emergency")
    data object Settings : Screen("settings")
    data object Ocr : Screen("ocr")
    data object OcrResult : Screen("ocr_result") {
        fun createRoute(text: String): String = "ocr_result/${URLEncoder.encode(text, "UTF-8")}"
    }
}

private fun enterTransition() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun exitTransition() = slideOutHorizontally(
    targetOffsetX = { -it / 3 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

private fun popEnterTransition() = slideInHorizontally(
    initialOffsetX = { -it / 3 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun popExitTransition() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

@Composable
fun MindEyeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToMindEyeTravel = { navController.navigate(Screen.MindEyeDestination.route) },
                onNavigateToQuickAsk = { navController.navigate(Screen.Emergency.route) },
                onNavigateToEmergency = { navController.navigate(Screen.Emergency.route) },
                onNavigateToCommunity = { navController.navigate(Screen.Community.route) },
                onNavigateToPsychology = { navController.navigate(Screen.Psychology.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.MindEyeDestination.route) {
            DestinationAskScreen(
                onDestinationSelected = { destination ->
                    navController.navigate(Screen.MindEyePreTripScan.createRoute(destination))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.MindEyePreTripScan.route}/{destination}",
            arguments = listOf(navArgument("destination") { type = NavType.StringType })
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: "目的地"
            PreTripScanScreen(
                destination = destination,
                onScanFinished = { navController.navigate(Screen.MindEyeTravelPlan.createRoute(destination)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.MindEyeTravelPlan.route}/{destination}",
            arguments = listOf(navArgument("destination") { type = NavType.StringType })
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: "目的地"
            TravelPlanScreen(
                destination = destination,
                onStartNavigation = { navController.navigate(Screen.MindEyeNavigation.createRoute(destination)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.MindEyeNavigation.route}/{destination}",
            arguments = listOf(navArgument("destination") { type = NavType.StringType })
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: "目的地"
            TravelNavigationScreen(
                destination = destination,
                onQuickAsk = { navController.navigate(Screen.QuickAsk.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuickAsk.route) {
            QuickAskScreen(
                onStartOcr = { navController.navigate(Screen.Ocr.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Psychology.route) {
            val viewModel: SupportViewModel = hiltViewModel()
            SupportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Community.route) {
            val viewModel: CommunityViewModel = hiltViewModel()
            CommunityScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVolunteer = { navController.navigate(Screen.Volunteer.route) }
            )
        }

        composable(Screen.Volunteer.route) {
            val viewModel: VolunteerViewModel = hiltViewModel()
            VolunteerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Emergency.route) {
            val viewModel: EmergencyViewModel = hiltViewModel()
            EmergencyScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVolunteer = { navController.navigate(Screen.Volunteer.route) }
            )
        }

        composable(Screen.Ocr.route) {
            val viewModel: OcrViewModel = hiltViewModel()
            OcrScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onTextRecognized = { text ->
                    navController.navigate(Screen.OcrResult.createRoute(text))
                }
            )
        }

        composable(
            route = "${Screen.OcrResult.route}/{text}",
            arguments = listOf(navArgument("text") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedText = backStackEntry.arguments?.getString("text") ?: ""
            val text = URLDecoder.decode(encodedText, "UTF-8")
            com.mindeye.app.feature.mindeye.ui.OcrResultScreen(
                recognizedText = text,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
