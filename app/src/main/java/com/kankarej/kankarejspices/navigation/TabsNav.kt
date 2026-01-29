package com.kankarej.kankarejspices.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kankarej.kankarejspices.screens.tabs.TabOneScreen
import com.kankarej.kankarejspices.screens.tabs.TabTwoScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsNav(
    rootNav: NavController,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var isDarkIcon by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (selectedTab == 0) "Kankarej Spices" else "Tab Two")
                },
                actions = {

                    // TAB 1 -> Modal button
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = { rootNav.navigate(Routes.MODAL) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info"
                            )
                        }
                    }

                    // TAB 2 -> Theme toggle button (same position)
                    if (selectedTab == 1) {
                        IconButton(
                            onClick = {
                                isDarkIcon = !isDarkIcon
                                onToggleTheme()
                            }
                        ) {
                            Icon(
                                imageVector = if (isDarkIcon)
                                    Icons.Default.DarkMode
                                else
                                    Icons.Default.LightMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Tab One") },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Tab Two") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> TabOneScreen()
                1 -> TabTwoScreen()
            }
        }
    }
}
