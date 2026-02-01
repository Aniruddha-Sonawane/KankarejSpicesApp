package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kankarej.kankarejspices.R
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.util.getOptimizedUrl
import kotlinx.coroutines.delay

// --- Game Models & State ---
data class MemoryCard(
    val id: Int,
    val product: Product,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

enum class GameStatus { LOADING, PLAYING, WON, LOST }

@Composable
fun TabTwoScreen() {
    val repo = remember { ProductRepository() }
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())

    // Game State
    var cards by remember { mutableStateOf<List<MemoryCard>>(emptyList()) }
    var gameStatus by remember { mutableStateOf(GameStatus.LOADING) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(60) }
    
    // Logic State
    var selectedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) } // Block input during flip back

    // --- Game Logic Functions ---
    fun startNewGame() {
        if (allProducts.isEmpty()) return
        
        // Select 6 random products for 6 pairs (12 cards total)
        val selectedProducts = allProducts.shuffled().take(6)
        // Duplicate to make pairs
        val pairs = (selectedProducts + selectedProducts).shuffled()
        
        cards = pairs.mapIndexed { index, product ->
            MemoryCard(id = index, product = product)
        }
        score = 0
        timeLeft = 60
        selectedIndices = emptyList()
        isProcessing = false
        gameStatus = GameStatus.PLAYING
    }

    // Initialize game once products load
    LaunchedEffect(allProducts) {
        if (allProducts.isNotEmpty() && gameStatus == GameStatus.LOADING) {
            startNewGame()
        }
    }

    // Timer Logic
    LaunchedEffect(gameStatus, timeLeft) {
        if (gameStatus == GameStatus.PLAYING) {
            if (timeLeft > 0) {
                delay(1000L)
                timeLeft -= 1
            } else {
                gameStatus = GameStatus.LOST
            }
        }
    }

    // Card Click Handler
    fun onCardClick(index: Int) {
        if (gameStatus != GameStatus.PLAYING || isProcessing) return
        if (cards[index].isFlipped || cards[index].isMatched) return

        // Flip the card
        val newCards = cards.toMutableList()
        newCards[index] = newCards[index].copy(isFlipped = true)
        cards = newCards
        
        val currentSelection = selectedIndices + index
        selectedIndices = currentSelection

        if (currentSelection.size == 2) {
            isProcessing = true
            val idx1 = currentSelection[0]
            val idx2 = currentSelection[1]
            val card1 = cards[idx1]
            val card2 = cards[idx2]

            // Check Match
            if (card1.product.name == card2.product.name) {
                // Match Found
                score += 100
                newCards[idx1] = newCards[idx1].copy(isMatched = true)
                newCards[idx2] = newCards[idx2].copy(isMatched = true)
                cards = newCards
                selectedIndices = emptyList()
                isProcessing = false
                
                // Check Win Condition
                if (cards.all { it.isMatched }) {
                    gameStatus = GameStatus.WON
                }
            } else {
                // No Match - Flip back after delay (Logic inside LaunchedEffect usually better, but simplified here)
                // We cannot launch a coroutine directly inside this callback easily without a scope, 
                // so we rely on a SideEffect or simply use a separate LaunchedEffect triggered by state.
            }
        }
    }

    // Handle Mismatch Delay using LaunchedEffect
    LaunchedEffect(selectedIndices) {
        if (selectedIndices.size == 2) {
            val idx1 = selectedIndices[0]
            val idx2 = selectedIndices[1]
            // If they are not matched yet, it means it was a mismatch
            if (!cards[idx1].isMatched && !cards[idx2].isMatched) {
                delay(1000) // Show cards for 1 second
                // Flip back
                val newCards = cards.toMutableList()
                newCards[idx1] = newCards[idx1].copy(isFlipped = false)
                newCards[idx2] = newCards[idx2].copy(isFlipped = false)
                cards = newCards
                selectedIndices = emptyList()
                isProcessing = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer
                Card(colors = CardDefaults.cardColors(containerColor = KankarejGreen.copy(alpha = 0.1f))) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, null, tint = KankarejGreen)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${timeLeft}s",
                            color = KankarejGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Score
                Text(
                    text = "Score: $score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // --- Game Area ---
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (gameStatus == GameStatus.LOADING) {
                    CircularProgressIndicator(color = KankarejGreen)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(cards) { index, card ->
                            GameCardItem(
                                card = card,
                                onClick = { onCardClick(index) }
                            )
                        }
                    }
                }

                // --- Game Over Overlay ---
                if (gameStatus == GameStatus.WON || gameStatus == GameStatus.LOST) {
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (gameStatus == GameStatus.WON) "🎉 You Won!" else "⌛ Time's Up!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gameStatus == GameStatus.WON) KankarejGreen else Color.Red
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Final Score: $score",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { startNewGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = KankarejGreen)
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Play Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardItem(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipAnimation"
    )

    Box(
        modifier = Modifier
            .aspectRatio(0.8f) // Card Shape
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() }
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp, 
                color = if (card.isMatched) Color.Green else KankarejGreen.copy(alpha = 0.5f), 
                shape = RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (rotation <= 90f) {
            // BACK OF CARD (Visible when 0-90 deg)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KankarejGreen),
                contentAlignment = Alignment.Center
            ) {
                // Use the secondary logo for the back design
                Image(
                    painter = painterResource(id = R.drawable.app_header_logo2),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            // FRONT OF CARD (Visible when 90-180 deg)
            // We rotate content 180 again so it doesn't look mirrored
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getOptimizedUrl(card.product.imageUrl, width = 200))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}