package com.kankarej.kankarejspices.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingsSectionTitle("Appearance")
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DarkMode, null, tint = KankarejGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(if (isDarkTheme) "On" else "Off", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = KankarejGreen)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionTitle("General")
            SettingsCard {
                SettingsItem(Icons.Default.Notifications, "Notifications", "Manage app notifications") {}
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                SettingsItem(Icons.Default.Language, "Language", "English (Default)") {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionTitle("About")
            SettingsCard {
                SettingsItem(Icons.Default.Info, "Version", "1.0.0 (Build 101)", showArrow = false) {}
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Made with ❤️ for Kankarej Spices", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = KankarejGreen,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (showArrow) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}