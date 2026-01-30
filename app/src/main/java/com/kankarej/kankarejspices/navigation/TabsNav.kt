package com.kankarej.kankarejspices.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.screens.tabs.TabOneScreen
import com.kankarej.kankarejspices.screens.tabs.TabTwoScreen
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@Composable
fun TabsNav(
    rootNav: NavController,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            // ADDED SHADOW HERE
            Surface(
                modifier = Modifier.shadow(elevation = 6.dp),
                color = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp // Reset tonal so our shadow takes precedence
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KankarejGreen,
                            selectedTextColor = KankarejGreen,
                            indicatorColor = KankarejGreen.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Orders") },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KankarejGreen,
                            selectedTextColor = KankarejGreen,
                            indicatorColor = KankarejGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> TabOneScreen(rootNav) 
                1 -> TabTwoScreen()
            }
        }
    }
}