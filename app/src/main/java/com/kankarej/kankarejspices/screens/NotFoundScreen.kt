package com.kankarej.kankarejspices.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.kankarej.kankarejspices.navigation.Routes

@Composable
fun NotFoundScreen(navController: NavController) {
    Button(
        onClick = {
            navController.navigate(Routes.TABS) {
                popUpTo(Routes.TABS) { inclusive = true }
                launchSingleTop = true
            }
        }
    ) {
        Text("Screen not found. Go Home")
    }
}
