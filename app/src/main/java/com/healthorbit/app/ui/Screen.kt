package com.healthorbit.app.ui

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object MetricDetail : Screen("metric_detail/{metricType}") {
        fun createRoute(metricType: String) = "metric_detail/$metricType"
    }
}