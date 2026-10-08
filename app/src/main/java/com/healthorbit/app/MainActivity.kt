package com.healthorbit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.healthorbit.app.di.AppContainer
import com.healthorbit.app.ui.DashboardViewModel
import com.healthorbit.app.ui.LoginViewModel
import com.healthorbit.app.ui.Screen
import com.healthorbit.app.ui.screens.DashboardScreen
import com.healthorbit.app.ui.screens.LoginScreen
import com.healthorbit.app.ui.screens.MetricDetailScreen
import com.healthorbit.app.ui.screens.SplashScreen
import com.healthorbit.app.ui.theme.HealthOrbitTheme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        val app = applicationContext as? HealthOrbitApplication
        app?.appContainer ?: AppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HealthOrbitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HealthOrbitAppNavHost(appContainer = appContainer)
                }
            }
        }
    }
}

@Composable
fun HealthOrbitAppNavHost(appContainer: AppContainer) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                onSessionValid = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSessionInvalid = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Authentication Screen
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.provideFactory(
                    authRepository = appContainer.authRepository
                )
            )

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    (navController.context.applicationContext as? HealthOrbitApplication)?.scheduleWearableSync()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Main Companion Dashboard
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.provideFactory(
                    wearableRepository = appContainer.wearableRepository,
                    authRepository = appContainer.authRepository,
                    sessionManager = appContainer.sessionManager
                )
            )

            DashboardScreen(
                viewModel = dashboardViewModel,
                onLogout = {
                    (navController.context.applicationContext as? HealthOrbitApplication)?.cancelWearableSync()
                    dashboardViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToMetric = { metricType ->
                    navController.navigate(Screen.MetricDetail.createRoute(metricType))
                }
            )
        }

        // 4. Metric Detail History Screen
        composable(
            route = "metric_detail/{metricType}",
            arguments = listOf(
                navArgument("metricType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricType = backStackEntry.arguments?.getString("metricType") ?: "steps"
            MetricDetailScreen(
                metricType = metricType,
                wearableRepository = appContainer.wearableRepository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}