package kimsy.rr.vental.ui.commonUi

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.Screen
import kimsy.rr.vental.ViewModel.MainViewModel
import kimsy.rr.vental.otherScreen
import kimsy.rr.vental.screensInBottom
import kotlinx.coroutines.CoroutineScope
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.MyPageViewModel
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.ViewModel.VentCardsViewModel
import kimsy.rr.vental.data.ImageUtils
import kimsy.rr.vental.ui.FollowsView
import kimsy.rr.vental.ui.MyPageView
import kimsy.rr.vental.ui.MySwipeCardDemo
import kimsy.rr.vental.ui.NotificationsView
import kimsy.rr.vental.ui.SignInScreen
import kimsy.rr.vental.ui.TimeLineView
import kimsy.rr.vental.ui.VentCardCreationView
import javax.inject.Inject



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    ventCardCreationViewModel: VentCardCreationViewModel,
    ventCardsViewModel: VentCardsViewModel
){

    Log.e("View", "Main called")

    val context = LocalContext.current
    val imageUtils = ImageUtils(context)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val controller: NavController = rememberNavController()
    val nevBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = nevBackStackEntry?.destination?.route
    val currentScreen = remember {
        mainViewModel.currentScreen.value
    }
    val auth = FirebaseAuth.getInstance()


    // 現在のルートに応じたタイトルを設定
    val title = remember { mutableStateOf("タイムライン") } // デフォルト値をセット

    //TODO delete comment when initializing problem is solved. this isn't cause
    // ルートとタイトルを一致させる
    LaunchedEffect(nevBackStackEntry) {
        val route = nevBackStackEntry?.destination?.route
        title.value = screensInBottom.find { it.bottomRoute == route }?.bottomTitle
            ?: otherScreen.find { it.route == route }?.title ?:"タイムライン"

        // スクロールの状態をリセット
        scrollBehavior.state.heightOffset = 0f
    }

//    LaunchedEffect(Unit) {
//        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            val user = firebaseAuth.currentUser
//            if (user == null) {
//                // サインアウトされた場合の処理（例えば、サインイン画面へ遷移）
//                Log.e("AuthStateListener", "User signed out")
//                // サインイン画面に遷移
//                val intent = Intent(context, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                context.startActivity(intent)            } else {
//                Log.d("AuthStateListener", "User signed in: ${user.uid}")
//            }
//        }
//        auth.addAuthStateListener(authStateListener)
//    }

    Scaffold(
        bottomBar = {
                    AppBottomBarView(
                        title = title.value,
                        navController = controller,
                        currentRoute = currentRoute?:"null",
                        context = context,
                        viewModel = ventCardCreationViewModel
                    )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
                 //別ファイル
                 AppTopBarView(
                     title = title.value,
                     context = context,
                     navController = controller,
                     {controller.navigateUp()},
                     {controller.navigate(Screen.BottomScreen.VentCardCreation.route)},
                     scrollBehavior,
                     viewModel = ventCardCreationViewModel)

        },
        scaffoldState = scaffoldState,
//        floatingActionButton = {
//            if(
//                title.value.contains("タイムライン") || title.value.contains("VentCards")
//            ) {
//                FloatingActionButton(
//                    onClick = {
//                              controller.navigate(Screen.VentCardCreation.route)
//                    },
//                    modifier = Modifier.padding(all = 8.dp),
//                ) {
//                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
//                }
//            }
//        }
    ) {
        Log.d("View", "MV calls Navigation")

        Navigation(
            navController = controller,
            mainViewModel = mainViewModel,
            authViewModel = authViewModel,
            ventCardCreationViewModel = ventCardCreationViewModel,
            ventCardsViewModel = ventCardsViewModel,
            pd = it)
    }

    //TODO delete comment when initializing problem is solved. this isn't cause
    LaunchedEffect(ventCardCreationViewModel.isSent){
        if (ventCardCreationViewModel.isSent){
            Toast.makeText(context,"送信完了",Toast.LENGTH_SHORT).show()
            ventCardCreationViewModel.isSent = false
        }
    }
}

@Composable
fun Navigation(
    navController: NavController,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    ventCardCreationViewModel: VentCardCreationViewModel,
    ventCardsViewModel: VentCardsViewModel,
    pd:PaddingValues){

    Log.d("Navigation", "Navigation called")

    val context = LocalContext.current
    val imageUtils = ImageUtils(context)

    NavHost(navController = navController as NavHostController,
        startDestination = Screen.BottomScreen.TimeLine.bottomRoute,
        modifier = Modifier.padding(pd)){

        composable(Screen.BottomScreen.VentCards.bottomRoute) {
            Log.d("Navigation", "to MSCD")
            MySwipeCardDemo(ventCardsViewModel)
        }
        composable(Screen.BottomScreen.TimeLine.bottomRoute) {
            TimeLineView()
        }
        composable(Screen.BottomScreen.Follows.bottomRoute) {
            Log.d("Navigation", "to Follows")
            FollowsView()
        }
        composable(Screen.Notifications.route) {
            Log.d("Navigation", "to Noti")
            NotificationsView()
        }
        composable(Screen.BottomScreen.MyPage.bottomRoute) {
            val viewModel = MyPageViewModel(mainViewModel)
            MyPageView(viewModel, authViewModel, mainViewModel ,onSignOutSuccess = {
                navController.navigate(Screen.SignupScreen.route)
            })
        }
        composable(Screen.BottomScreen.VentCardCreation.route) {
            Log.d("Navigation", "to VCCVM")
            VentCardCreationView(ventCardCreationViewModel, context)
        }
        composable(Screen.SignupScreen.route) {
            SignInScreen(authViewModel = authViewModel){
                navController.navigate(Screen.BottomScreen.TimeLine.bottomRoute)
            }
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