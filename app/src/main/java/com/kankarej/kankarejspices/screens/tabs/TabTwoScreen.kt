package com.kankarej.kankarejspices.screens.tabs

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.kankarej.kankarejspices.R
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.util.getOptimizedUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define Purple Color
val GamePurple = Color(0xFF6200EE)

// --- Game Models ---
data class MemoryCard(
    val id: Int,
    val product: Product,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

enum class GameStatus { FETCHING_DATA, PRELOADING_IMAGES, COUNTDOWN, PLAYING, WON, LOST }

@Composable
fun TabTwoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { ProductRepository() }
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())
    
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    
    var totalAccumulatedScore by remember { mutableIntStateOf(prefs.getInt("total_score", 0)) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }

    var cards by remember { mutableStateOf<List<MemoryCard>>(emptyList()) }
    var gameStatus by remember { mutableStateOf(GameStatus.FETCHING_DATA) }
    var sessionScore by remember { mutableIntStateOf(0) }
    var streakCount by remember { mutableIntStateOf(1) }
    var timeLeft by remember { mutableIntStateOf(60) }
    var countdownValue by remember { mutableIntStateOf(5) }
    
    var selectedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    fun saveScores() {
        prefs.edit().apply {
            putInt("total_score", totalAccumulatedScore)
            putInt("high_score", highScore)
            apply()
        }
    }

    fun prepareGame() {
        if (allProducts.isEmpty()) return
        sessionScore = 0
        streakCount = 1
        timeLeft = 60
        countdownValue = 5
        selectedIndices = emptyList()
        isProcessing = false
        gameStatus = GameStatus.PRELOADING_IMAGES

        val selectedProducts = allProducts.shuffled().take(6)
        val pairs = (selectedProducts + selectedProducts).shuffled()
        
        scope.launch {
            val distinctUrls = selectedProducts.map { getOptimizedUrl(it.imageUrl, width = 200) }
            distinctUrls.forEach { url ->
                val request = ImageRequest.Builder(context).data(url).build()
                context.imageLoader.execute(request)
            }
            cards = pairs.mapIndexed { index, product -> MemoryCard(id = index, product = product) }
            gameStatus = GameStatus.COUNTDOWN
        }
    }

    LaunchedEffect(allProducts) {
        if (allProducts.isNotEmpty() && gameStatus == GameStatus.FETCHING_DATA) {
            prepareGame()
        }
    }

    LaunchedEffect(gameStatus) {
        if (gameStatus == GameStatus.COUNTDOWN) {
            while (countdownValue > 0) {
                delay(1000)
                countdownValue -= 1
            }
            gameStatus = GameStatus.PLAYING
        }
    }

    LaunchedEffect(gameStatus, timeLeft) {
        if (gameStatus == GameStatus.PLAYING) {
            if (timeLeft > 0) {
                delay(1000)
                timeLeft -= 1
            } else {
                gameStatus = GameStatus.LOST
                if (sessionScore > highScore) {
                    highScore = sessionScore
                    saveScores()
                }
            }
        }
    }

    fun onCardClick(index: Int) {
        if (gameStatus != GameStatus.PLAYING || isProcessing) return
        if (cards[index].isFlipped || cards[index].isMatched) return

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

            if (card1.product.name == card2.product.name) {
                val points = 100 * streakCount
                sessionScore += points
                totalAccumulatedScore += points
                streakCount += 1
                saveScores()
                newCards[idx1] = newCards[idx1].copy(isMatched = true)
                newCards[idx2] = newCards[idx2].copy(isMatched = true)
                cards = newCards
                selectedIndices = emptyList()
                isProcessing = false
                if (cards.all { it.isMatched }) {
                    gameStatus = GameStatus.WON
                    if (sessionScore > highScore) {
                        highScore = sessionScore
                        saveScores()
                    }
                }
            } else {
                streakCount = 1
            }
        }
    }

    LaunchedEffect(selectedIndices) {
        if (selectedIndices.size == 2) {
            val idx1 = selectedIndices[0]
            val idx2 = selectedIndices[1]
            if (!cards[idx1].isMatched && !cards[idx2].isMatched) {
                delay(800)
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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- MAIN GAME UI ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- TOP ROW: High Score | Timer | Total Score ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: High Score
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "High Score", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = "$highScore", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                    }

                    // CENTER: Timer
                    Card(colors = CardDefaults.cardColors(containerColor = GamePurple.copy(alpha = 0.1f))) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, null, tint = GamePurple, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "${timeLeft}s", color = GamePurple, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }

                    // RIGHT: Total Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = GamePurple)
                        Text(text = "$totalAccumulatedScore", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = GamePurple)
                    }
                }
                
                // CHANGE: Increased spacer to push content down
                Spacer(modifier = Modifier.height(24.dp)) 

                // --- SECOND ROW: Session Score & Streak ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session: $sessionScore",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KankarejGreen
                    )
                    
                    if (streakCount > 1) {
                        Text(
                            text = "${streakCount}x Streak! 🔥",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // CHANGE: Increased spacer between stats and grid
                Spacer(modifier = Modifier.height(16.dp)) 

                // --- GAME GRID ---
                if (gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.WON || gameStatus == GameStatus.LOST) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(cards) { index, card ->
                            GameCardItem(
                                card = card,
                                onClick = { onCardClick(index) }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }

            // --- OVERLAYS ---
            AnimatedVisibility(
                visible = gameStatus == GameStatus.FETCHING_DATA || 
                          gameStatus == GameStatus.PRELOADING_IMAGES || 
                          gameStatus == GameStatus.COUNTDOWN,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (gameStatus == GameStatus.FETCHING_DATA || gameStatus == GameStatus.PRELOADING_IMAGES) {
                            LinearProgressIndicator(
                                modifier = Modifier.width(200.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = KankarejGreen,
                                trackColor = KankarejGreen.copy(alpha = 0.3f)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.masala_memory),
                                contentDescription = "Game Logo",
                                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
                                contentScale = ContentScale.FillWidth
                            )
                            Text("Begins in...", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(16.dp))
                            Text("$countdownValue", fontSize = 80.sp, fontWeight = FontWeight.ExtraBold, color = KankarejGreen)
                        }
                    }
                }
            }

            if (gameStatus == GameStatus.WON || gameStatus == GameStatus.LOST) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (gameStatus == GameStatus.WON) Icons.Default.EmojiEvents else Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (gameStatus == GameStatus.WON) Color(0xFFFFD700) else Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (gameStatus == GameStatus.WON) "Victory!" else "Time's Up!",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("High Score", color = Color.Gray, fontSize = 18.sp)
                                Text("$highScore", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Session Score", color = KankarejGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("$sessionScore", color = KankarejGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = { prepareGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = KankarejGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Play Again", fontSize = 18.sp)
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
            .aspectRatio(0.8f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() }
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp, 
                // CHANGE: Use GamePurple if matched, otherwise semi-transparent Green
                color = if (card.isMatched) GamePurple else KankarejGreen.copy(alpha = 0.5f), 
                shape = RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (rotation <= 90f) {
            Box(
                modifier = Modifier.fillMaxSize().background(KankarejGreen),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_header_logo2),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
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
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}