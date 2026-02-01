package com.kankarej.kankarejspices.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kankarej.kankarejspices.R
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // CHANGE: Dynamic background color
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HEADER LOGO ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White) // Keep logo bg white to ensure visibility
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash_logo), 
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "Kankarej Spices",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KankarejGreen
            )
        )
        Text(
            text = "Authentic Taste of India",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // --- 2. LEGAL & LICENSE INFO ---
        Card(
            // CHANGE: Dynamic card container color
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Company Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = KankarejGreen
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (MaterialTheme.colorScheme.background == Color.White) Color(0xFFEEEEEE) else Color.DarkGray
                )
                
                InfoRow(Icons.Default.Business, "GSTIN", "24ABCDE1234F1Z5")
                Spacer(Modifier.height(12.dp))
                InfoRow(Icons.Default.VerifiedUser, "FSSAI License", "12345678901234")
                Spacer(Modifier.height(12.dp))
                InfoRow(Icons.Default.LocationOn, "Address", "123, Spice Market, Kankarej Road, Gujarat - 385555")
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 3. CONTACT PERSONS ---
        Text(
            text = "Key Contacts",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            // CHANGE: Dynamic text color
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        ContactPersonCard("Aniruddha Sonawane", "Owner / Founder", "+91 98765 43210")
        ContactPersonCard("Sales Department", "Wholesale Inquiries", "+91 91234 56789")
        ContactPersonCard("Customer Support", "Help & Queries", "+91 99887 76655")
        
        Spacer(Modifier.height(80.dp)) // Bottom spacing
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = KankarejGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            // CHANGE: Dynamic value text color
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContactPersonCard(name: String, role: String, phone: String) {
    val context = LocalContext.current
    Card(
        // CHANGE: Dynamic card container color
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                context.startActivity(intent)
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KankarejGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = KankarejGreen)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // CHANGE: Dynamic name text color
                Text(
                    text = name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = role, fontSize = 12.sp, color = Color.Gray)
            }
            
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Default.Call, "Call", tint = KankarejGreen)
            }
        }
    }
}