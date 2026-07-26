package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
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

val LightGreenBg = Color(0xFFB9E4C9)

// First screenful shown immediately.
private const val INITIAL_PAGE = 20

// How many products get appended per "load more" trigger.
private const val PAGE_SIZE = 16

// Grid thumbnails are shown at roughly half-screen width, so we don't need
// full 600px images here - this cuts network + decode cost noticeably.
private const val GRID_THUMB_WIDTH = 360

// How far (in dp) the user must pull past the bottom edge before it
// triggers loading the next batch. Crossing this while still dragging
// (no release needed) fires the load immediately.
private const val PULL_THRESHOLD_DP = 56f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabOneScreen(rootNav: NavController) {
    val repo = remember { ProductRepository() }
    val context = LocalContext.current

    // FETCH DATA
    val categories by repo.getCategoriesFlow().collectAsState(initial = emptyList())
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())
    val banners by repo.getBannersFlow().collectAsState(initial = emptyList())

    // Stable shuffled list. We deliberately do NOT key this off every new
    // 'allProducts' reference - Firebase's ValueEventListener re-fires
    // onDataChange on things like reconnects even when the underlying data
    // hasn't actually changed, which would otherwise reshuffle the list and
    // silently reset scroll/paging progress back to the start. We only
    // rebuild when the actual set of product names changes.
    var randomProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var displayedCount by remember { mutableIntStateOf(INITIAL_PAGE) }

    LaunchedEffect(allProducts) {
        if (allProducts.isEmpty()) return@LaunchedEffect

        val currentNames = randomProducts.map { it.name }.toSet()
        val newNames = allProducts.map { it.name }.toSet()

        if (randomProducts.isEmpty() || currentNames != newNames) {
            randomProducts = allProducts.shuffled()
            displayedCount = minOf(INITIAL_PAGE, randomProducts.size)

            // Preload the first screenful of images up front.
            randomProducts.take(INITIAL_PAGE).forEach { product ->
                val request = ImageRequest.Builder(context)
                    .data(getOptimizedUrl(product.imageUrl, width = GRID_THUMB_WIDTH))
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    val displayedProducts by remember(randomProducts, displayedCount) {
        derivedStateOf { randomProducts.take(displayedCount) }
    }

    val gridState = rememberLazyGridState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { PULL_THRESHOLD_DP.dp.toPx() }

    // --- Pull-up-past-the-bottom gesture ---
    // Loads as soon as the drag crosses the threshold - no release needed.
    var isLoadingMore by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    // Raw drag offset, written synchronously on every scroll delta - no
    // coroutine launch per frame, which is what was causing the stutter.
    var pullRaw by remember { mutableFloatStateOf(0f) }
    val hasMore = displayedCount < randomProducts.size

    // While dragging: snap 1:1 to the finger, zero animation overhead.
    // Once the drag ends (or a load fires and resets it): animate back to 0.
    val pullOffset by animateFloatAsState(
        targetValue = if (isDragging) pullRaw else 0f,
        animationSpec = if (isDragging) snap() else tween(220),
        label = "PullOffset"
    )

    fun loadNextBatch() {
        isLoadingMore = true
        val nextEnd = minOf(displayedCount + PAGE_SIZE, randomProducts.size)
        val nextBatch = randomProducts.subList(displayedCount, nextEnd)
        nextBatch.forEach { product ->
            val request = ImageRequest.Builder(context)
                .data(getOptimizedUrl(product.imageUrl, width = GRID_THUMB_WIDTH))
                .build()
            context.imageLoader.enqueue(request)
        }
        displayedCount = nextEnd
        isLoadingMore = false
        // Snap the pull gesture closed immediately - the batch already loaded,
        // no need to keep holding the footer open or wait for a release.
        isDragging = false
        pullRaw = 0f
    }

    val nestedScrollConnection = remember(hasMore, displayedCount, randomProducts) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // available.y < 0 means the user is still dragging upward
                // (trying to scroll further down) after the grid itself can
                // no longer consume that scroll - i.e. genuine overscroll
                // past the bottom edge, not just "reached the last item".
                if (hasMore && !isLoadingMore && !gridState.canScrollForward && available.y < 0) {
                    isDragging = true
                    pullRaw = (pullRaw + available.y).coerceIn(-thresholdPx * 1.6f, 0f)

                    // Fire the load the moment the pull crosses the threshold -
                    // no release/lift-finger required.
                    if (pullRaw <= -thresholdPx) {
                        loadNextBatch()
                    }
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // If the user lifted their finger before crossing the
                // threshold, just snap the footer closed.
                isDragging = false
                pullRaw = 0f
                return Velocity.Zero
            }
        }
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        // Disable inset consumption here: the outer TabsNav Scaffold already
        // leaves room for its bottom NavigationBar, and that NavigationBar
        // applies its own navigation-bar insets internally. If this inner
        // Scaffold also consumed the systemBars bottom inset by default, the
        // same inset gets applied twice - once by NavigationBar, once here -
        // producing a blank gap directly above the bottom nav bar. The
        // TopAppBar handles its own status-bar inset internally regardless,
        // so disabling it here doesn't affect the top.
        contentWindowInsets = WindowInsets(0),
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(backgroundModifier)
                .nestedScroll(nestedScrollConnection)
        ) {
            if (allProducts.isEmpty() && categories.isEmpty() && banners.isEmpty()) {
                SkeletonHomeScreen()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    // Small resting padding only - the pull footer supplies
                    // its own space during drag/load, so we don't need a
                    // permanent large bottom gap anymore.
                    contentPadding = PaddingValues(bottom = 8.dp),
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

                    // Footer only occupies space while the user is actively
                    // pulling past the bottom edge, or briefly while a batch
                    // loads - it disappears entirely once there's nothing
                    // left to load, and never reserves permanent blank space.
                    if (hasMore) {
                        item(key = "pull_footer", span = { GridItemSpan(2) }) {
                            PullUpFooter(
                                pullOffsetPx = pullOffset,
                                isLoading = isLoadingMore
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PullUpFooter(pullOffsetPx: Float, isLoading: Boolean) {
    val density = LocalDensity.current
    val dragHeightDp = with(density) { (-pullOffsetPx).coerceAtLeast(0f).toDp() }
    val height = if (isLoading) 56.dp else dragHeightDp.coerceAtMost(80.dp)

    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        if (height > 8.dp) {
            if (isLoading) {
                CircularProgressIndicator(color = KankarejGreen, modifier = Modifier.size(28.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = KankarejGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Pull up to load more",
                        style = MaterialTheme.typography.bodySmall,
                        color = KankarejGreen
                    )
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