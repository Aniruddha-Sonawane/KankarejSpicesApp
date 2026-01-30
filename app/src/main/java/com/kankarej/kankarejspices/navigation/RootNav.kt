package com.kankarej.kankarejspices.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kankarej.kankarejspices.screens.CategoryProductScreen
import com.kankarej.kankarejspices.screens.ModalScreen
import com.kankarej.kankarejspices.screens.NotFoundScreen
import com.kankarej.kankarejspices.screens.ProductDetailScreen
import com.kankarej.kankarejspices.screens.SearchScreen

object Routes {
    const val TABS = "tabs"
    const val MODAL = "modal"
    const val PRODUCT_DETAIL = "product_detail/{productName}"
    const val CATEGORY_LIST = "category_list/{categoryName}"
    const val SEARCH = "search"
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
            TabsNav(rootNav = navController, onToggleTheme = onToggleTheme)
        }
        
        composable(Routes.MODAL) {
            ModalScreen()
        }
        
        composable(Routes.SEARCH) {
            SearchScreen(navController)
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            ProductDetailScreen(navController, productName)
        }

        composable(
            route = Routes.CATEGORY_LIST,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val catName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryProductScreen(navController, catName)
        }
    }
}