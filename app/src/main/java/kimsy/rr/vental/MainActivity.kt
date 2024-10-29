package kimsy.rr.vental

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kimsy.rr.vental.ui.theme.VentalTheme
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.MainViewModel
import kimsy.rr.vental.data.UserRepository
import kimsy.rr.vental.ui.MainView
import kimsy.rr.vental.ui.ProfileRegisterScreen
import kimsy.rr.vental.ui.SignInScreen
import kimsy.rr.vental.ui.VentCardsView
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Inject
    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            VentalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
){

    NavHost(navController = navController, startDestination = Screen.SignupScreen.route){
        composable(Screen.SignupScreen.route){
            SignInScreen(authViewModel = authViewModel,
                onNavigateToMainView = {
                    mainViewModel.loadCurrentUser()  // ユーザー情報をロード
                    navController.navigate(Screen.TimeLineScreen.route)
                })
        }
        composable(Screen.TimeLineScreen.route){
            MainView(mainViewModel = mainViewModel)
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



