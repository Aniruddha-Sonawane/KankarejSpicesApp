package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.kankarej.kankarejspices.R
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Banner
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.ui.theme.SkeletonHomeScreen
import com.kankarej.kankarejspices.ui.theme.shimmerEffect
import com.kankarej.kankarejspices.util.getOptimizedUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

val LightGreenBg = Color(0xFFB9E4C9)

// First screenful shown immediately.
private const val INITIAL_PAGE = 20

// How many products get appended per "load more" trigger while scrolling.
// Smaller pages = less composition/layout work per burst = smoother scroll,
// while still feeling continuous like a real e-commerce feed.
private const val PAGE_SIZE = 16

// Start loading the next batch this many items before the end of what's
// currently rendered in the grid (banner/category/header rows included).
private const val LOAD_MORE_BUFFER = 6

// Grid thumbnails are shown at roughly half-screen width, so we don't need
// full 600px images here - this cuts network + decode cost noticeably,
// which is most of what was causing scroll jank.
private const val GRID_THUMB_WIDTH = 360

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabOneScreen(rootNav: NavController) {
    val repo = remember { ProductRepository() }
    val context = LocalContext.current
    
    // FETCH DATA
    val categories by repo.getCategoriesFlow().collectAsState(initial = emptyList())
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())
    val banners by repo.getBannersFlow().collectAsState(initial = emptyList())
    
    var displayedCount by remember { mutableIntStateOf(INITIAL_PAGE) }
    
    // Randomize products once when data is loaded
    val randomProducts = remember(allProducts) { allProducts.shuffled() }
    
    val displayedProducts by remember(randomProducts, displayedCount) {
        derivedStateOf { randomProducts.take(displayedCount) }
    }

    // Reset paging if the underlying product set changes (e.g. Firebase pushes an update).
    LaunchedEffect(randomProducts) {
        displayedCount = minOf(INITIAL_PAGE, randomProducts.size).coerceAtLeast(0)
        if (randomProducts.isNotEmpty()) {
            // Preload the first screenful of images up front.
            randomProducts.take(INITIAL_PAGE).forEach { product ->
                val request = ImageRequest.Builder(context)
                    .data(getOptimizedUrl(product.imageUrl, width = GRID_THUMB_WIDTH))
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    val gridState = rememberLazyGridState()

    // Real infinite-scroll pattern: continuously watch scroll position rather
    // than gating on a Boolean flag. A Boolean-keyed LaunchedEffect can only
    // fire once per true/false transition - if it stays "true" for several
    // frames (which it easily can once you're partway down a long list), it
    // never re-fires and loading gets permanently stuck. Streaming the raw
    // scroll position instead means every new scroll position is evaluated,
    // so batches keep loading one after another as you keep scrolling, the
    // same way Amazon/Flipkart-style feeds behave.
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex to info.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItemsCount) ->
                if (totalItemsCount > 0 &&
                    lastVisibleIndex >= totalItemsCount - LOAD_MORE_BUFFER &&
                    displayedCount < randomProducts.size
                ) {
                    val nextEnd = minOf(displayedCount + PAGE_SIZE, randomProducts.size)
                    val nextBatch = randomProducts.subList(displayedCount, nextEnd)

                    // Prefetch the next batch's images before they're actually
                    // revealed, so by the time the grid grows, most images are
                    // already in Coil's cache and paint instantly - this is
                    // what makes it feel "instant" instead of popping in.
                    nextBatch.forEach { product ->
                        val request = ImageRequest.Builder(context)
                            .data(getOptimizedUrl(product.imageUrl, width = GRID_THUMB_WIDTH))
                            .build()
                        context.imageLoader.enqueue(request)
                    }

                    displayedCount = nextEnd
                }
            }
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KankarejGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_header_logo2),
                                contentDescription = "Icon",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Image(
                            painter = painterResource(id = R.drawable.app_header_logo),
                            contentDescription = "Kankarej Logo",
                            modifier = Modifier
                                .height(50.dp)
                                .wrapContentWidth(Alignment.Start),
                            contentScale = ContentScale.Fit
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { rootNav.navigate(Routes.SEARCH) }) {
                        Icon(
                            Icons.Default.Search, 
                            "Search", 
                            tint = if (isDarkTheme) Color.White else KankarejGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        
        val backgroundModifier = if (isDarkTheme) {
            Modifier.background(MaterialTheme.colorScheme.surface)
        } else {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightGreenBg, MaterialTheme.colorScheme.background)
                )
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .then(backgroundModifier)
        ) {
            if (allProducts.isEmpty() && categories.isEmpty() && banners.isEmpty()) {
                SkeletonHomeScreen()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    
                    item(key = "banner", span = { GridItemSpan(2) }) {
                        FullWidthBannerPager(banners)
                    }

                    if (categories.isNotEmpty()) {
                        item(key = "categories", span = { GridItemSpan(2) }) {
                            CategorySection(categories) { catName ->
                                rootNav.navigate(Routes.CATEGORY_LIST.replace("{categoryName}", catName))
                            }
                        }
                    }
                    
                    item(key = "featured_header", span = { GridItemSpan(2) }) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(if (isDarkTheme) Color.Gray else Color.Black)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Featured Products",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else KankarejGreen
                                ),
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        }
                    }

                    items(
                        items = displayedProducts,
                        key = { product -> product.name }
                    ) { product ->
                        ProductGridItem(product) {
                            rootNav.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                        }
                    }
                    
                    if (displayedCount < randomProducts.size) {
                         item(key = "loading_more", span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = KankarejGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullWidthBannerPager(banners: List<Banner>) {
    if (banners.isEmpty()) return

    val startIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { Int.MAX_VALUE })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            try { pagerState.animateScrollToPage(pagerState.currentPage + 1) } catch (_: Exception) { }
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFEEEEEE))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val banner = banners[page % banners.size]
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getOptimizedUrl(banner.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = banner.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().shimmerEffect()) }
            )
        }
    }
}

@Composable
fun CategorySection(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(if (isDarkTheme) Color.Gray else Color.Black)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold, 
                color = if (isDarkTheme) Color.White else KankarejGreen
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = categories, key = { it.name }) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(category.name) }
                ) {
                    SubcomposeAsyncImage(
                        model = getOptimizedUrl(category.imageUrl),
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(2.dp, KankarejGreen, CircleShape),
                        loading = { Box(Modifier.fillMaxSize().shimmerEffect()) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ProductGridItem(product: Product, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getOptimizedUrl(product.imageUrl, width = GRID_THUMB_WIDTH))
                        // Crossfade looks nice for hero/detail images, but running it on
                        // every grid item during a fast scroll is what was causing the
                        // jittery feel - disable it here specifically.
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)).shimmerEffect()) },
                    error = { Box(Modifier.fillMaxSize().background(Color.LightGray)) }
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price}",
                        style = MaterialTheme.typography.bodyLarge.copy(color = KankarejGreen, fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.weight(1f))
                    if (product.quantity.isNotBlank()) {
                        Surface(
                            color = KankarejGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = product.quantity,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                fontSize = 13.sp,
                                color = KankarejGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}