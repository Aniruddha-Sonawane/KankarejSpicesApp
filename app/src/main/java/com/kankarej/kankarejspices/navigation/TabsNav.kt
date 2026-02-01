package com.kankarej.kankarejspices.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports // CHANGE: Import Game Icon
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.screens.ContactScreen
import com.kankarej.kankarejspices.screens.ModalScreen
import com.kankarej.kankarejspices.screens.tabs.TabOneScreen
import com.kankarej.kankarejspices.screens.tabs.TabTwoScreen
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@Composable
fun TabsNav(
    rootNav: NavController,
    darkTheme: Boolean,
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
                    
                    // Tab 2: Games (Formerly Orders)
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        // CHANGE: Updated Label and Icon
                        label = { Text("Games") }, 
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Games") }, 
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

                    // Tab 4: Settings
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
                1 -> TabTwoScreen() // This now renders your Memory Match Game
                2 -> ContactScreen()
                3 -> ModalScreen(
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