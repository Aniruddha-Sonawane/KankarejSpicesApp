package com.kankarej.kankarejspices.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

// --- SHIMMER EFFECT ---
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), 
        label = "ShimmerOffset"
    )

    // Check if we are in dark mode based on the current theme's background luminance
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Define colors based on theme
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF2B2B2B), // Dark Gray Base
            Color(0xFF3D3D3D), // Slightly lighter Highlight
            Color(0xFF2B2B2B)
        )
    } else {
        listOf(
            Color(0xFFE0E0E0), // Light Gray Base
            Color(0xFFF0F0F0), // Lighter Highlight
            Color(0xFFE0E0E0)
        )
    }

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(startOffsetX, 0f),
            // CHANGE: Y coordinate set to 0f instead of size.height.toFloat()
            // This forces the gradient to be purely horizontal (Left -> Right)
            end = Offset(startOffsetX + size.width.toFloat(), 0f) 
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

// --- COMPONENTS ---

@Composable
fun SkeletonBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surface) // Dynamic background
            .shimmerEffect()
    )
}

@Composable
fun SkeletonCategoryItem() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
fun SkeletonProductItem() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface) // Dynamic background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .shimmerEffect()
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Box(Modifier.fillMaxWidth(0.7f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Box(Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
    }
}

// --- FULL SCREEN SKELETON ---
@Composable
fun SkeletonHomeScreen() {
    // Dynamic container background
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 1. Banner Skeleton
        SkeletonBanner()
        
        Spacer(Modifier.height(16.dp))

        // 2. Categories Skeleton (Horizontal Row)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { SkeletonCategoryItem() }
        }

        Spacer(Modifier.height(16.dp))

        // 3. Section Title Skeleton
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(150.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(Modifier.height(16.dp))

        // 4. Products Grid Skeleton
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(6) {
                SkeletonProductItem()
            }
        }
    }
}