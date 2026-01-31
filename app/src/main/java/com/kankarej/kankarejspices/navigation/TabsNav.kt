package com.kankarej.kankarejspices.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings // Import Settings Icon
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.screens.ContactScreen
import com.kankarej.kankarejspices.screens.ModalScreen // Using ModalScreen as Settings
import com.kankarej.kankarejspices.screens.tabs.TabOneScreen
import com.kankarej.kankarejspices.screens.tabs.TabTwoScreen
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@Composable
fun TabsNav(
    rootNav: NavController,
    darkTheme: Boolean, // Received from RootNav
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.shadow(elevation = 8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    // Tab 1: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, null) },
                        colors = navColors()
                    )
                    
                    // Tab 2: Orders
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Orders") },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        colors = navColors()
                    )

                    // Tab 3: Contact
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Contact") },
                        icon = { Icon(Icons.Default.SupportAgent, null) },
                        colors = navColors()
                    )

                    // Tab 4: Settings (Right Side)
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Default.Settings, null) },
                        colors = navColors()
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
                2 -> ContactScreen()
                3 -> ModalScreen( // Render Settings here
                    navController = rootNav,
                    isDarkTheme = darkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = KankarejGreen,
    selectedTextColor = KankarejGreen,
    indicatorColor = KankarejGreen.copy(alpha = 0.1f)
)