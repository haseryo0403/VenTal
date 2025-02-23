package kimsy.rr.vental

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kimsy.rr.vental.data.repository.UserRepository
import kimsy.rr.vental.ui.SignInScreen
import kimsy.rr.vental.ui.commonUi.MainView
import kimsy.rr.vental.ui.theme.VentalTheme
import kimsy.rr.vental.viewModel.AuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authViewModel: AuthViewModel

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("debateId")) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Log.d("MA", "activity is called with intent")
            finish()
        }

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        enableEdgeToEdge()

        askNotificationPermission()

        setContent {
            val navController = rememberNavController()
            var startDestination by remember { mutableStateOf<String?>(null) }

            val systemUiController = rememberSystemUiController()

            VentalTheme {
                val systemBarColor = MaterialTheme.colorScheme.background

                SideEffect {
                    systemUiController.setSystemBarsColor(systemBarColor)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
            Log.d("MA", "onNewIntent is called")
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d("data ", "Key: $key Value: $value")
            }
        }

        if (intent != null && intent.hasExtra("targetId")) {
            val targetId = intent.getStringExtra("targetId")
            Log.d("MA", "Received debateId: $targetId")
            //TODO IDから画面遷移
        }

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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
                },
                toAppGuideView = {
                    navController.navigate(Screen.AppGuideScreen.route)
                }
            )
        }
        composable(Screen.TimeLineScreen.route){
            MainView(
                authViewModel = authViewModel,
                )
        }
    }
}

