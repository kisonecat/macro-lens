package com.macrolens.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.macrolens.ui.camera.CameraScreen
import com.macrolens.ui.dailylog.DailyLogScreen
import com.macrolens.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object Routes {
    const val CAMERA = "camera"
    const val DAILY_LOG = "daily_log"
    const val SETTINGS = "settings"
}

@Composable
fun PhoodApp(
    startRoute: String? = null,
    incomingRoutes: Flow<String> = emptyFlow()
) {
    val navController = rememberNavController()
    val resolvedStart = when (startRoute) {
        Routes.DAILY_LOG -> Routes.DAILY_LOG
        else -> Routes.CAMERA
    }

    LaunchedEffect(incomingRoutes) {
        incomingRoutes.collect { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStart
    ) {
        composable(Routes.CAMERA) {
            CameraScreen(
                onFoodCaptured = {
                    navController.navigate(Routes.DAILY_LOG) {
                        popUpTo(Routes.CAMERA) { inclusive = false }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToDailyLog = {
                    navController.navigate(Routes.DAILY_LOG) {
                        popUpTo(Routes.CAMERA) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.DAILY_LOG) {
            DailyLogScreen(
                onAddFoodClick = {
                    navController.navigate(Routes.CAMERA)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
