package kimsy.rr.vental.ui


import android.app.Activity.RESULT_OK
import android.graphics.drawable.Icon
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.AuthViewModel

@Composable
fun SignInScreen(authViewModel: AuthViewModel,onNavigateToMainView:()->Unit) {
    var showDialog by remember { mutableStateOf(false)}
    val errorMessage by authViewModel.errorMessage

    // 認証結果を監視
    val authResult by authViewModel.authResult.collectAsState()
//    val authResult by authViewModel.authResult.observeAsState()
    val isLoading = authViewModel.isLoading

    // Googleサインインの結果を受け取るランチャー
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.e("SU", "result_ok")
            val signInIntent = result.data
            // ViewModelで結果を処理
            authViewModel.handleSignInResult(signInIntent)
        } else {
            Log.e("SU", "result_NG")

            //TODO グーグル認証失敗処理
            authViewModel.updateLoading(false)
            showDialog = true
        }
    }

    // 認証成功で画面遷移
    LaunchedEffect(authResult) {
        if (authResult == true) {
            Log.d("TAG", "Navigate to timeline")
            onNavigateToMainView()  // 遷移先の処理を呼び出す
        }
    }

    if (errorMessage != null) {
        showDialog = true
    }

    if(showDialog){
        AlertDialog(onDismissRequest = {
            showDialog = false
            authViewModel.resetErrorMessage()  // ダイアログが閉じられたらエラーメッセージをリセット
        },
            confirmButton = { /*TODO*/ },
            title = { Text(text = "エラー")},
            text = { Text(text = errorMessage?: "不明なエラーが発生しました。")}
            )
    }

    // UIの定義
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(isLoading){
            CircularProgressIndicator()
        } else {
            Text(text = "VenTalへようこそ")
            Text(text = "Googleでサインイン")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.signInWithGoogle(launcher) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                )
            {
                Image(
                    painter = painterResource(id = R.drawable.google_icon), // GoogleアイコンのリソースID
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp) // アイコンサイズ

                )
                Text("Sign in with Google",modifier = Modifier.padding(horizontal = 8.dp))
            }
        }

    }
}


