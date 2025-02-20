

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.ui.CommonComposable.RememberScrollState
import kotlinx.coroutines.launch

@Composable
fun TabContent(selectedTabIndex: Int, scrollState: LazyListState) {
    val contentLists = listOf(
        List(50) { "Tab 1 - Item $it" },
        List(50) { "Tab 2 - Item $it" },
        List(50) { "Tab 3 - Item $it" }
    )

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize() // ここで高さを適切に指定
    ) {
        items(contentLists[selectedTabIndex]) { item ->
            Text(text = item, modifier = Modifier.padding(16.dp))
        }
    }
}


@Composable
fun ProfileScreens(
    scrollState: ConnectionSampleScrollState
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val scrollStates = remember { List(3) { LazyListState() } }
    val pagerState = rememberPagerState { 3 } // 3つのタブ
    val coroutineScope = rememberCoroutineScope()

    // ProfileSectionsのスクロール状態管理
    val profileSectionScrollState = rememberScrollableState { delta ->
        // ProfileSectionsのスクロール変化を反映させる
        scrollState.nestedScrollConnection.onPreScroll(Offset(0f, delta), NestedScrollSource.Drag).y
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollState.nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(scrollState.offset)
                .background(Color.Gray)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = profileSectionScrollState
                )

        ){
            ProfileSections()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = scrollState.offset)
        ) {
            // タブバー
            TabRow(selectedTabIndex = selectedTabIndex) {
                listOf("Tab 1", "Tab 2", "Tab 3").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                TabContent(
                    selectedTabIndex = page,
                    scrollState = scrollStates[page]
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    nestedScrollConnection: NestedScrollConnection,
    offset: Dp
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val scrollStates = remember { List(3) { LazyListState() } }
    val pagerState = rememberPagerState { 3 } // 3つのタブ
    val coroutineScope = rememberCoroutineScope()


    // ProfileSections のスクロール状態
    val profileSectionScrollState = rememberScrollableState { delta ->
        // Offset を更新してスクロール連携
        nestedScrollConnection.onPreScroll(Offset(0f, delta), NestedScrollSource.Drag).y
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(offset)
                .background(Color.Gray)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = profileSectionScrollState
                ) // スクロール可能にする
        ){
            ProfileSections()
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offset)
        ) {
            // タブバー
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = profileSectionScrollState
                    ) // スクロール可能にする
            ) {
                listOf("Tab 1", "Tab 2", "Tab 3").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                TabContent(
                    selectedTabIndex = page,
                    scrollState = scrollStates[page]
                )
            }
        }
    }
}


@Composable
fun ProfileSections() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "User Name", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "This is the user's profile description.")
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TabContents(selectedTabIndex: Int) {
    val contentLists = listOf(
        List(50) { "Tab 1 - Item $it" },
        List(50) { "Tab 2 - Item $it" },
        List(50) { "Tab 3 - Item $it" }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        contentLists[selectedTabIndex].forEach { item ->
            Text(text = item, modifier = Modifier.padding(16.dp))
        }
    }
}

interface ConnectionSampleScrollState {
    val nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    val offset: Dp
}

@Stable
class ConnectionSampleScrollStateImpl1(
    maxOffset: Dp,
    initialOffset: Dp,
    private val density: androidx.compose.ui.unit.Density,
) : ConnectionSampleScrollState {
    private val maxOffsetPx = with(density) { maxOffset.toPx() }
    private val initialOffsetPx = with(density) { initialOffset.toPx() }
    private var _offsetPx by mutableStateOf(initialOffsetPx)
    override val offset: Dp
        get() = with(density) { _offsetPx.toDp() }

    override val nestedScrollConnection = object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
            if ((available.y >= 0f) or (_offsetPx <= 0f)) return Offset.Zero
            val consumedY = doScroll(available.y)
            return Offset(0f, consumedY)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
        ): Offset {
            if ((available.y <= 0f) or (_offsetPx >= maxOffsetPx)) return Offset.Zero
            val consumedY = doScroll(available.y)
            return Offset(0f, consumedY)
        }
    }

    private fun doScroll(delta: Float): Float {
        val oldOffset = _offsetPx
        _offsetPx = (_offsetPx + delta).coerceIn(0f, maxOffsetPx)
        return _offsetPx - oldOffset
    }
}

@Composable
fun ConnectionSample1() {
    val density = LocalDensity.current
    val scrollState = remember {
        ConnectionSampleScrollStateImpl1(
            maxOffset = 200.dp,
            initialOffset = 200.dp,
            density = density,
        )
    }
    ProfileScreens(scrollState = scrollState)
}

@Composable
fun ConnectionSample2() {
    val (nestedScrollConnection, offset) = RememberScrollState(
        maxOffset = 400.dp,
        initialOffset = 400.dp
    )

    // ProfileScreensなどで使用するためのoffsetを渡す
    ProfileScreen(nestedScrollConnection = nestedScrollConnection, offset = offset)
}


@Preview(showBackground = true)
@Composable
fun PreviewConnectionSample() {
//    ConnectionSample1()
    ConnectionSample2()
}
