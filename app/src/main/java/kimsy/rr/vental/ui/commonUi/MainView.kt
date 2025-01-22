package kimsy.rr.vental.ui.commonUi

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kimsy.rr.vental.Screen
import kimsy.rr.vental.ViewModel.AnotherUserPageViewModel
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.DebateCreationViewModel
import kimsy.rr.vental.ViewModel.FollowPageViewModel
import kimsy.rr.vental.ViewModel.MyDebateViewModel
import kimsy.rr.vental.ViewModel.MyLikedDebateViewModel
import kimsy.rr.vental.ViewModel.MyPageViewModel
import kimsy.rr.vental.ViewModel.MyVentCardViewModel
import kimsy.rr.vental.ViewModel.NotificationsViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.ViewModel.TimeLineViewModel
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.otherScreen
import kimsy.rr.vental.screensInBottom
import kimsy.rr.vental.settingsScreen
import kimsy.rr.vental.ui.AnotherUserPageView
import kimsy.rr.vental.ui.DebateCreationView
import kimsy.rr.vental.ui.DebateView
import kimsy.rr.vental.ui.FollowListView
import kimsy.rr.vental.ui.FollowPageView
import kimsy.rr.vental.ui.MyPageView
import kimsy.rr.vental.ui.NotificationSettingsView
import kimsy.rr.vental.ui.NotificationsView
import kimsy.rr.vental.ui.ProfileEditView
import kimsy.rr.vental.ui.ReportDebateView
import kimsy.rr.vental.ui.SettingsView
import kimsy.rr.vental.ui.SwipeCardsView
import kimsy.rr.vental.ui.TimeLineView
import kimsy.rr.vental.ui.VentCardCreationView

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    authViewModel: AuthViewModel,
    ventCardCreationViewModel: VentCardCreationViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
){

    Log.e("View", "Main called")

    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val controller: NavController = rememberNavController()
    val nevBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = nevBackStackEntry?.destination?.route

    // 現在のルートに応じたタイトルを設定
    val title = remember { mutableStateOf("タイムライン") } // デフォルト値をセット

    //TODO delete comment when initializing problem is solved. this isn't cause
    // ルートとタイトルを一致させる
    LaunchedEffect(nevBackStackEntry) {
        val route = nevBackStackEntry?.destination?.route
        title.value = screensInBottom.find { it.bottomRoute == route }?.bottomTitle
            ?: otherScreen.find { it.route == route }?.title ?: settingsScreen.find{ it.route == route}?.title ?:""
        // スクロールの状態をリセット
        scrollBehavior.state.heightOffset = 0f
    }

    val isBottomBarVisible = remember { mutableStateOf(true) }
//    val nestedScrollConnection = remember {
//        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
//            private var lastDelta = 0f
//
//            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                lastDelta += available.y
//                // スクロールが上方向なら表示、下方向なら非表示
//                isBottomBarVisible.value = lastDelta >= 0
//                return Offset.Zero
//            }
//        }
//    }

    val bottomBarHeight = 48.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }


// connection to the nested scroll system and listen to the scroll
// happening inside child LazyColumn
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.value + delta
                bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(
            scrollBehavior.nestedScrollConnection
//            nestedScrollConnection
        ),
        bottomBar = {
            AppBottomBarView(
                title = title.value,
                navController = controller,
                currentRoute = currentRoute?:"null",
                context = context,
                viewModel = ventCardCreationViewModel,
                bottomBarHeight = bottomBarHeight,
                bottomBarOffsetHeightPx = bottomBarOffsetHeightPx
            )
        },
        topBar = {
                 //別ファイル
                 AppTopBarView(
                     title = title.value,
                     context = context,
                     navController = controller,
                     {controller.navigateUp()},
                     {controller.navigate(Screen.BottomScreen.VentCardCreation.route)},
                     scrollBehavior,
                     ventCardCreationViewModel = ventCardCreationViewModel,
                     notificationsViewModel = notificationsViewModel
                 )
        },
        scaffoldState = scaffoldState
    ) {
        Log.d("View", "MV calls Navigation")

        Navigation(
            navController = controller,
            authViewModel = authViewModel,
            ventCardCreationViewModel = ventCardCreationViewModel,
            sharedDebateViewModel = sharedDebateViewModel,
            context = context,
            pd = it)
    }

//    LaunchedEffect(ventCardCreationViewModel.isSent){
//        if (ventCardCreationViewModel.isSent){
//            Toast.makeText(context,"送信完了",Toast.LENGTH_SHORT).show()
//            ventCardCreationViewModel.isSent = false
//        }
//    }
    val saveState = ventCardCreationViewModel.saveState.collectAsState()

    LaunchedEffect(saveState.value.status){
        Log.d("ssss", "ssss")
        if (saveState.value.status == Status.SUCCESS){
            Toast.makeText(context,"送信完了",Toast.LENGTH_SHORT).show()
            //TODO リセット？
        }
    }

}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation(
    navController: NavController,
    authViewModel: AuthViewModel,
    ventCardCreationViewModel: VentCardCreationViewModel,
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel,
    debateCreationViewModel: DebateCreationViewModel = hiltViewModel(),
    timeLineViewModel: TimeLineViewModel = hiltViewModel(),
    myPageViewModel: MyPageViewModel = hiltViewModel(),
    myDebateViewModel: MyDebateViewModel = hiltViewModel(),
    myVentCardViewModel: MyVentCardViewModel = hiltViewModel(),
    myLikedDebateViewModel: MyLikedDebateViewModel = hiltViewModel(),
    anotherUserPageViewModel: AnotherUserPageViewModel = hiltViewModel(),
    followPageViewModel: FollowPageViewModel = hiltViewModel(),
    context: Context,
    pd:PaddingValues){

    NavHost(navController = navController as NavHostController,
        startDestination = Screen.BottomScreen.TimeLine.bottomRoute,
        modifier = Modifier.padding(pd)){


        composable(Screen.BottomScreen.VentCards.bottomRoute) {
            Log.d("Navigation", "to MSCD")
//            MySwipeCardDemo(authViewModel = authViewModel)
            SwipeCardsView(
                context = context,
                authViewModel = authViewModel,
                debateCreationViewModel = debateCreationViewModel,
                toDebateCreationView = {navController.navigate(Screen.DebateCreation.route)}
            )
        }
        composable(Screen.BottomScreen.TimeLine.bottomRoute) {
            TimeLineView(
                toDebateView = {
                    Log.d("MV", "to DebateScreen")
                    navController.navigate(Screen.DebateScreen.route)
                },
                toAnotherUserPageView = { user ->
                    navigateToUserPage(user, navController)
                },
                timeLineViewModel = timeLineViewModel,
                sharedDebateViewModel = sharedDebateViewModel
            )
        }
        composable(Screen.BottomScreen.Follows.bottomRoute) {
            Log.d("Navigation", "to Follows")
            FollowPageView(
                viewModel = followPageViewModel,
                sharedDebateViewModel = sharedDebateViewModel,
                toDebateView = {
                    navController.navigate(Screen.DebateScreen.route)
                },
                toAnotherUserPageView =  { user ->
                    navigateToUserPage(user, navController)
                },
                toFollowListView = {
                    navController.navigate(Screen.FollowListScreen.route)
                }
            )
        }
        composable(Screen.Notifications.route) {
            Log.d("Navigation", "to Noti")
            NotificationsView(
                sharedDebateViewModel = sharedDebateViewModel,
                notificationsViewModel = notificationsViewModel,
                toDebateView = {
                    navController.navigate(Screen.DebateScreen.route)
                },
                toAnotherUserPageView =  { user ->
                    navigateToUserPage(user, navController)
                }
            )
        }
        composable(Screen.BottomScreen.MyPage.bottomRoute) {
//            val viewModel = MyPageViewModel()
            MyPageView(
                sharedDebateViewModel = sharedDebateViewModel,
                viewModel = myPageViewModel,
                myDebateViewModel = myDebateViewModel,
                myVentCardViewModel = myVentCardViewModel,
                myLikedDebateViewModel = myLikedDebateViewModel,
                toDebateView = {
                    navController.navigate(Screen.DebateScreen.route)
                },
                toProfileEditView = {
                    navController.navigate(Screen.ProfileEditScreen.route)
                },
                toAnotherUserPageView = { user ->
                    navigateToUserPage(user, navController)
                }
            )
        }
        composable(Screen.BottomScreen.VentCardCreation.route) {
            Log.d("Navigation", "to VCCVM")
            VentCardCreationView(ventCardCreationViewModel, context)
        }
        composable(Screen.DebateCreation.route) {
            DebateCreationView(
                context = context,
                authViewModel = authViewModel,
                debateCreationViewModel = debateCreationViewModel,
                sharedDebateViewModel = sharedDebateViewModel,
                toDebateView = {
                    Log.d("MV", "to DebateScreen")
                    navController.navigate(Screen.DebateScreen.route)
                }
            )
        }
        composable(Screen.DebateScreen.route) {
            DebateView(
                sharedDebateViewModel = sharedDebateViewModel,
                toReportDebateView = {
                    navController.navigate(Screen.ReportDebateScreen.route)
                }
            )
        }
        composable(Screen.SettingsScreen.route) {
            SettingsView(
                authViewModel = authViewModel,
                toNotificationSettingsView = {
                    navController.navigate(Screen.SettingsMenuScreen.NotificationSettingsScreen.route)
                }
            )
        }
        composable(Screen.ProfileEditScreen.route) {
            ProfileEditView(
                toMyPageView = {
                    navController.navigate(Screen.BottomScreen.MyPage.route)
                }
            )
        }
        composable(Screen.AnotherUserPageScreen.route) {
            AnotherUserPageView(
                sharedDebateViewModel = sharedDebateViewModel,
                toDebateView = {
                    navController.navigate(Screen.DebateScreen.route)
                },
                toAnotherUserPageView =  { user ->
                    navigateToUserPage(user, navController)
                }
            )
        }

        composable(Screen.SettingsMenuScreen.NotificationSettingsScreen.route) {
            NotificationSettingsView()
        }

        composable(Screen.FollowListScreen.route) {
            FollowListView(
                viewModel = followPageViewModel,
                toAnotherUserPageView =   { user ->
                navigateToUserPage(user, navController)
                }
            )
        }
        
        composable(Screen.ReportDebateScreen.route) {
            ReportDebateView(
                toDebateView = {
                    navController.navigate(Screen.DebateScreen.route)
                }
            )
        }

    }
}

fun navigateToUserPage(
    user: User,
    navController: NavController
) {
    val currentUserId = User.CurrentUserShareModel.getCurrentUserFromModel()?.uid
    if (user.uid == currentUserId) {
        // 自分のアカウントならマイページへ遷移
        navController.navigate(Screen.BottomScreen.MyPage.route)
    } else {
        // 他人ユーザーページへ遷移
        User.AnotherUserShareModel.setAnotherUser(user)
        navController.navigate(Screen.AnotherUserPageScreen.route)
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