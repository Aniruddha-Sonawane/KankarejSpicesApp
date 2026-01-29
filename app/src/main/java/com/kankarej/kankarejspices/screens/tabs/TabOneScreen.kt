package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabOneScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp) // space for bottom nav
    ) {
        SearchSection()
        BannerSection()
        CategorySection()
        ProductSection()
    }
}

@Composable
private fun SearchSection() {
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        // Search Bar
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chips
        val chips = listOf("Favorites", "History", "Following")
        val icons = listOf(
            Icons.Default.Favorite,
            Icons.Default.History,
            Icons.Default.Person
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(chips.size) { index ->
                AssistChip(
                    onClick = {},
                    label = { Text(chips[index]) },
                    leadingIcon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BannerSection() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEBEBEB))
            .padding(20.dp)
    ) {
        Text(
            text = "Banner Title",
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Pagination dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 0) Color.DarkGray else Color.LightGray
                        )
                )
            }
        }
    }
}

@Composable
private fun CategorySection() {
    SectionHeader(title = "Categories")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(6) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Title", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ProductSection() {
    SectionHeader(title = "Products")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(6) {
            Column(
                modifier = Modifier.width(160.dp)
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDDDDDD))
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Brand", fontSize = 12.sp, color = Color.Gray)
                Text("Product name", fontSize = 14.sp)
                Text("₹199", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 22.sp)
    }
}
