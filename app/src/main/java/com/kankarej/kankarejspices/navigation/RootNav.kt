package com.kankarej.kankarejspices.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kankarej.kankarejspices.screens.ModalScreen
import com.kankarej.kankarejspices.screens.NotFoundScreen

object Routes {
    const val TABS = "tabs"
    const val MODAL = "modal"
    const val NOT_FOUND = "not_found"
}

@Composable
fun RootNav(
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.TABS
    ) {
        composable(Routes.TABS) {
            TabsNav(
                rootNav = navController,
                onToggleTheme = onToggleTheme
            )
        }
        composable(Routes.MODAL) {
            ModalScreen()
        }
        composable(Routes.NOT_FOUND) {
            NotFoundScreen(navController)
        }
    }
}
