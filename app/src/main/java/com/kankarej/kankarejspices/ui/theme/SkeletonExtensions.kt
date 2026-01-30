package com.kankarej.kankarejspices.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFF0F0F0),
                Color(0xFFE0E0E0),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
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
            .background(Color.White)
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
            .background(Color.White)
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
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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