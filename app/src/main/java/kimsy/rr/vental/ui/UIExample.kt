

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
import androidx.compose.ui.layout.onGloballyPositioned
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
fun ProfileScreen2() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val scrollStates = remember { List(3) { LazyListState() } }
    val pagerState = rememberPagerState { 3 } // 3つのタブ
    val coroutineScope = rememberCoroutineScope()
    var profileHeight by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // ProfileSectionsの高さを取得するBox
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // ProfileSectionsの高さを取得
                    profileHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            // ProfileSections のスクロール状態
            val profileSectionScrollState = rememberScrollableState { delta ->
                // Offsetを更新してスクロール連携
                delta // 直接deltaを返す
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(profileHeight) // 高さをProfileSectionsの高さに設定
                    .background(Color.Gray)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = profileSectionScrollState
                    ) // スクロール可能にする
            ) {
                ProfileSections()
            }
        }

        // NestedScrollConnection の作成
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // スクロールイベントのハンドリング
                    return Offset(0f, available.y) // y軸のスクロールのみを反映
                }
            }
        }

        val offset = remember { mutableStateOf(0.dp) }

        // 下部のタブとコンテンツ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offset.value) // スクロールによる位置調整
                .nestedScroll(nestedScrollConnection) // nestedScroll を使用
        ) {
            // タブバー
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta ->
                            delta // deltaをそのまま返す
                        }
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

//@Composable
//fun ConnectionSample1() {
//    val density = LocalDensity.current
//    val scrollState = remember {
//        ConnectionSampleScrollStateImpl1(
//            maxOffset = 200.dp,
//            initialOffset = 200.dp,
//            density = density,
//        )
//    }
//    ProfileScreens(scrollState = scrollState)
//}

@Composable
fun ConnectionSample2() {
    val (nestedScrollConnection, offset) = RememberScrollState(
        maxOffset = 400.dp,
        initialOffset = 400.dp
    )

    // ProfileScreensなどで使用するためのoffsetを渡す
    ProfileScreen(nestedScrollConnection = nestedScrollConnection, offset = offset)
}

@Composable
fun ConnectionSample3() {
    var profileHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // ProfileScreenの高さを取得するためのBox

        // ProfileScreen に高さを渡す
        ProfileScreen2()
}




@Preview(showBackground = true)
@Composable
fun PreviewConnectionSample() {
//    ConnectionSample1()
//    ConnectionSample2()
    ConnectionSample3()
}
