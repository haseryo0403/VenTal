package kimsy.rr.vental

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.initialize
import com.google.firebase.ktx.initialize
import dagger.hilt.android.AndroidEntryPoint
import kimsy.rr.vental.ui.theme.VentalTheme
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.MainViewModel
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.ViewModel.VentCardsViewModel
import kimsy.rr.vental.data.UserRepository
import kimsy.rr.vental.ui.commonUi.MainView
import kimsy.rr.vental.ui.ProfileRegisterScreen
import kimsy.rr.vental.ui.SignInScreen
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var ventCardCreationViewModel: VentCardCreationViewModel

    @Inject
    lateinit var ventCardsViewModel: VentCardsViewModel


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
                    // 非同期処理でstartDestinationを決定
                    LaunchedEffect(Unit) {
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
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
                            mainViewModel = mainViewModel,
                            ventCardCreationViewModel = ventCardCreationViewModel,
                            startDestination = it // 動的に決定したstartDestinationを渡す
                        )
                    }
                }
            }
        }
//
//    override fun onStart () {
//        super.onStart()
//    }

    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    ventCardCreationViewModel: VentCardCreationViewModel,
    startDestination: String
){

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable(Screen.SignupScreen.route){
            SignInScreen(authViewModel = authViewModel,
                onNavigateToMainView = {
                    mainViewModel.loadCurrentUser()  // ユーザー情報をロード
                    navController.navigate(Screen.TimeLineScreen.route)
                })
        }
        composable(Screen.TimeLineScreen.route){
            MainView(
                mainViewModel = mainViewModel,
                ventCardCreationViewModel = ventCardCreationViewModel
                )
        }
    }
}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun ProfilePrev(){
    ProfileRegisterScreen()
}



