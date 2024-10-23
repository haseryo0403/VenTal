package kimsy.rr.vental

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.data.UserRepository
import kimsy.rr.vental.ui.ProfileRegistScreen
import kimsy.rr.vental.ui.SignInScreen
import kimsy.rr.vental.ui.TimeLineScreen
import kimsy.rr.vental.ui.theme.VenTalTheme

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
            VenTalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    NavigationGraph(navController = navController, authViewModel = authViewModel)
                    ProfileRegistScreen()
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
            SignInScreen(authViewModel = authViewModel,onNavigateToTimeLine = { navController.navigate(Screen.TimeLineScreen.route) })
        }
        composable(Screen.TimeLineScreen.route){
            TimeLineScreen()
        }
    }
}



