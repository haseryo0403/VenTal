package kimsy.rr.vental.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(mainViewModel: MainViewModel){

    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope: CoroutineScope = rememberCoroutineScope()
//    val viewModel: MainViewModel = viewModel()

    val controller: NavController = rememberNavController()
    val nevBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = nevBackStackEntry?.destination?.route
    val currentScreen = remember {
        mainViewModel.currentScreen.value
    }

//    val title = remember{
//        mutableStateOf(currentScreen.title)
//    }


    // 現在のルートに応じたタイトルを設定
    val title = remember { mutableStateOf("タイムライン") } // デフォルト値をセット

//    // currentBackStackEntryが変わるたびにtitleを更新
//    LaunchedEffect(nevBackStackEntry) {
//        title.value = screensInBottom.find {
//            it.bottomRoute == nevBackStackEntry?.destination?.route
//        }?.bottomTitle ?: "タイムライン"
//    }

    // ルートとタイトルを一致させる
    LaunchedEffect(nevBackStackEntry) {
        val route = nevBackStackEntry?.destination?.route
        title.value = screensInBottom.find { it.bottomRoute == route }?.bottomTitle
            ?: otherScreen.find { it.route == route }?.title ?:"タイムライン"
    }


    val context = LocalContext.current

    val bottomBar: @Composable () -> Unit = {
//        if(currentScreen is Screen.BottomScreen.VentCards){
            BottomNavigation(Modifier.wrapContentSize()) {
                screensInBottom.forEach {
                    item ->
                    val isSelected = currentRoute == item.bottomRoute
                    Log.d("Navigation", "Item: ${item.bottomTitle}, Current Route: $currentRoute")
                    val tint = if(isSelected)Color.White else Color.Black

                    Log.d("TAG","${item.bottomRoute}, ${item.bottomRoute}, ${item.icon}")
                    BottomNavigationItem(
                        selected = currentRoute == item.bottomRoute,
                        onClick = { controller.navigate(item.bottomRoute)
//                                  title.value = item.bottomTitle
                                  },
                        icon = { Icon(painter = painterResource(id = item.icon),
                            contentDescription = item.bottomTitle,
                            tint = tint) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Black
                        )

                }
            }
//        }
    }

    Scaffold(
        bottomBar = bottomBar,
        topBar = {
                 //TODO delete 直接指定
//            TopAppBar(title = { Text(title.value)},
//                elevation = 3.dp,
//                navigationIcon = {
//                    if(!title.value.contains("タイムライン")){
//                        IconButton(onClick = {
//                            Toast.makeText(context, "Button Clicked", Toast.LENGTH_LONG).show()
//                            controller.navigateUp()
//
//                        }) {
//                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                        }
//                    } else {
//                        null
//                    }
//                }
//            )

                 //別ファイル
                 AppBarView(
                     title = title.value,
                     {controller.navigateUp()})

        },scaffoldState = scaffoldState,
        floatingActionButton = {
            if(
                title.value.contains("タイムライン")
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
//        Text(text = "aaa", modifier = Modifier.padding(it))
    }
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