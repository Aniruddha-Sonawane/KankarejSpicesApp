package com.kankarej.kankarejspices.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.screens.tabs.ProductGridItem
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.ui.theme.SkeletonProductItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { ProductRepository() }
    
    // States
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Data for Suggestions
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())
    val allCategories by repo.getCategoriesFlow().collectAsState(initial = emptyList())

    // Recent Searches (Stored in SharedPrefs)
    val prefs = remember { context.getSharedPreferences("search_history", Context.MODE_PRIVATE) }
    var recentSearches by remember { 
        mutableStateOf(
            prefs.getStringSet("history", emptySet())?.toList() ?: emptyList()
        ) 
    }

    // Random Suggestions (Computed once when data loads)
    val suggestions by remember(allProducts, allCategories) {
        derivedStateOf {
            val prodNames = allProducts.map { it.name }
            val catNames = allCategories.map { it.name }
            (prodNames + catNames).shuffled().take(10) // Pick 10 random tags
        }
    }

    // Function to add search to history
    fun addToHistory(searchText: String) {
        if (searchText.isBlank()) return
        val currentSet = prefs.getStringSet("history", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(searchText)
        prefs.edit().putStringSet("history", currentSet).apply()
        recentSearches = currentSet.toList().reversed() // Show newest first
    }

    // Function to remove search from history
    fun removeFromHistory(searchText: String) {
        val currentSet = prefs.getStringSet("history", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.remove(searchText)
        prefs.edit().putStringSet("history", currentSet).apply()
        recentSearches = currentSet.toList().reversed()
    }

    // Search Logic
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(300) // Debounce
        searchResults = repo.searchProducts(query)
        isSearching = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search spices...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = KankarejGreen,
                            focusedTextColor = Color.Black
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    // CHANGE: Switched to navigateUp() for reliable "Up" navigation
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, "Clear", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // CASE 1: User is typing -> Show Results Grid
            if (query.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isSearching) {
                        items(6) { SkeletonProductItem() }
                    } else if (searchResults.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No products found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(searchResults) { product ->
                            ProductGridItem(product) {
                                addToHistory(query) // Save to history when clicked
                                navController.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                            }
                        }
                    }
                }
            } 
            // CASE 2: Search is empty -> Show History & Recommendations
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // --- RECENT SEARCHES ---
                    if (recentSearches.isNotEmpty()) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentSearches.take(6).forEach { historyItem ->
                                InputChip(
                                    selected = false,
                                    onClick = { query = historyItem },
                                    label = { Text(historyItem) },
                                    leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp)) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { removeFromHistory(historyItem) }
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = Color(0xFFF5F5F5),
                                        labelColor = Color.Black
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- SUGGESTIONS (Randomly Placed Chips) ---
                    if (suggestions.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = KankarejGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Try Searching",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = KankarejGreen
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            suggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                        .border(1.dp, KankarejGreen.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .clickable { query = suggestion }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        color = KankarejGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}