package kimsy.rr.vental

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kimsy.rr.vental.ui.theme.VentalTheme
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.data.UserRepository
import kimsy.rr.vental.ui.commonUi.MainView
import kimsy.rr.vental.ui.SignInScreen
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authViewModel: AuthViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            var startDestination by remember { mutableStateOf<String?>(null) }
            VentalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                     非同期処理でstartDestinationを決定
                    LaunchedEffect(Unit) {
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            Log.e("MA", "get user")
                            val user = userRepository.getCurrentUser()
                            startDestination = if (user != null) {
                                Screen.TimeLineScreen.route
                            } else {
                                Screen.SignupScreen.route
                            }
                        } else {
                            startDestination = Screen.SignupScreen.route
                        }
                    }

                    // startDestinationが決定していればNavHostを描画
                    startDestination?.let {
                        NavigationGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            startDestination = it // 動的に決定したstartDestinationを渡す
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String
){

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable(Screen.SignupScreen.route){
            SignInScreen(authViewModel = authViewModel,
                onNavigateToMainView = {
                    authViewModel.loadCurrentUser()  // ユーザー情報をロード
                    navController.navigate(Screen.TimeLineScreen.route)
                })
        }
        composable(Screen.TimeLineScreen.route){
            MainView(
                authViewModel = authViewModel,
                )
        }
    }
}

