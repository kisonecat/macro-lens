package com.phood.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phood.ui.camera.CameraScreen
import com.phood.ui.dailylog.DailyLogScreen
import com.phood.ui.settings.SettingsScreen

object Routes {
    const val CAMERA = "camera"
    const val DAILY_LOG = "daily_log"
    const val SETTINGS = "settings"
}

@Composable
fun PhoodApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CAMERA
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
