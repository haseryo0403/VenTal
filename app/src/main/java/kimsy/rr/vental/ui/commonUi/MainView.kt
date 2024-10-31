package kimsy.rr.vental.ui.commonUi

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kimsy.rr.vental.Screen
import kimsy.rr.vental.ViewModel.MainViewModel
import kimsy.rr.vental.otherScreen
import kimsy.rr.vental.screensInBottom
import kotlinx.coroutines.CoroutineScope
import kimsy.rr.vental.R
import kimsy.rr.vental.data.ImageUtils
import kimsy.rr.vental.ui.FollowsView
import kimsy.rr.vental.ui.MyPageView
import kimsy.rr.vental.ui.MySwipeCardDemo
import kimsy.rr.vental.ui.NotificationsView
import kimsy.rr.vental.ui.TimeLineView
import kimsy.rr.vental.ui.VentCardCreationView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(mainViewModel: MainViewModel){

    val context = LocalContext.current
    val imageUtils = ImageUtils(context)

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope: CoroutineScope = rememberCoroutineScope()
    val controller: NavController = rememberNavController()
    val nevBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = nevBackStackEntry?.destination?.route
    val currentScreen = remember {
        mainViewModel.currentScreen.value
    }

    // 現在のルートに応じたタイトルを設定
    val title = remember { mutableStateOf("タイムライン") } // デフォルト値をセット

    // ルートとタイトルを一致させる
    LaunchedEffect(nevBackStackEntry) {
        val route = nevBackStackEntry?.destination?.route
        title.value = screensInBottom.find { it.bottomRoute == route }?.bottomTitle
            ?: otherScreen.find { it.route == route }?.title ?:"タイムライン"

        // スクロールの状態をリセット
        scrollBehavior.state.heightOffset = 0f
    }

//TODO delete
//    val bottomBar: @Composable () -> Unit = {
//
//        if (title.value != "VCC"){
//            BottomNavigation(Modifier.wrapContentSize()) {
//                screensInBottom.forEach {
//                        item ->
//                    val isSelected = currentRoute == item.bottomRoute
//                    Log.d("Navigation", "Item: ${item.bottomTitle}, Current Route: $currentRoute")
//                    val tint = if(isSelected)Color.White else Color.Black
//
//                    Log.d("TAG","${item.bottomRoute}, ${item.bottomRoute}, ${item.icon}")
//                    BottomNavigationItem(
//                        selected = currentRoute == item.bottomRoute,
//                        onClick = { controller.navigate(item.bottomRoute) },
//                        icon = { Icon(painter = painterResource(id = item.icon),
//                            contentDescription = item.bottomTitle,
//                            tint = tint) },
//                        selectedContentColor = Color.White,
//                        unselectedContentColor = Color.Black
//                    )
//
//                }
//            }
//        } else {
//            BottomAppBar(
//                actions = {
//                    IconButton(onClick = { /* do something */ }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.baseline_image_24),
//                            modifier = Modifier.size(40.dp),
//                            contentDescription = "add Image")
//                    }
//                },
//                modifier = Modifier.height(48.dp)
//            )
//        }
//    }


    Scaffold(
        bottomBar = {
                    AppBottomBarView(
                        title = title.value,
                        navController = controller,
                        currentRoute = currentRoute?:"null",
                        imageUtils = imageUtils,
                        context = context
                    )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
                 //別ファイル
                 AppTopBarView(
                     title = title.value,
                     {controller.navigateUp()},
                     scrollBehavior)

        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            if(
                title.value.contains("タイムライン") || title.value.contains("VentCards")
            ) {
                FloatingActionButton(
                    onClick = {
                              controller.navigate(Screen.VentCardCreation.route)
                    },
                    modifier = Modifier.padding(all = 8.dp),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) {
        Navigation(navController = controller, viewModel = mainViewModel, pd = it)
    }
}

@Composable
fun BottomAppBar(actions: () -> Unit, floatingActionButton: () -> Unit) {

}

@Composable
fun Navigation(navController: NavController, viewModel: MainViewModel, pd:PaddingValues){

    NavHost(navController = navController as NavHostController,
        startDestination = Screen.BottomScreen.TimeLine.bottomRoute,
        modifier = Modifier.padding(pd)){

        composable(Screen.BottomScreen.VentCards.bottomRoute) {
            MySwipeCardDemo()
        }
        composable(Screen.BottomScreen.TimeLine.bottomRoute) {
            TimeLineView()
        }
        composable(Screen.BottomScreen.Follows.bottomRoute) {
            FollowsView()
        }
        composable(Screen.BottomScreen.Notifications.bottomRoute) {
            NotificationsView()
        }
        composable(Screen.BottomScreen.MyPage.bottomRoute) {
            MyPageView(viewModel)
        }
        composable(Screen.VentCardCreation.route) {
            VentCardCreationView()
        }

    }
}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun MainViewPre(){
//    MainView()
}