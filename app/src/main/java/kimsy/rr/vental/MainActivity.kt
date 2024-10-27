package kimsy.rr.vental

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import kimsy.rr.vental.ui.theme.VentalTheme
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.data.UserRepository
import kimsy.rr.vental.ui.MainView
import kimsy.rr.vental.ui.ProfileRegisterScreen
import kimsy.rr.vental.ui.SignInScreen
import kimsy.rr.vental.ui.VentCardsView

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GoogleSignInClientの作成
        val googleSignInClient = UserRepository.createGoogleSignInClient(this)

        // AuthRepositoryの初期化
        val authRepository = UserRepository(googleSignInClient)

        // AuthViewModelの初期化
        val authViewModel = AuthViewModel(authRepository)

        setContent {
            val navController = rememberNavController()
            VentalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationGraph(navController = navController, authViewModel = authViewModel)
//                    ProfileRegisterScreen()
//                    MainView()
                }
            }
        }
    }

}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
){

    NavHost(navController = navController, startDestination = Screen.SignupScreen.route){
        composable(Screen.SignupScreen.route){
            SignInScreen(authViewModel = authViewModel,onNavigateToMainView = { navController.navigate(Screen.TimeLineScreen.route) })
        }
        composable(Screen.TimeLineScreen.route){
            MainView()
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



