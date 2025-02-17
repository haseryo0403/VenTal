package kimsy.rr.vental.ui


import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.viewModel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignInScreen(authViewModel: AuthViewModel,onNavigateToMainView:()->Unit) {
    var showDialog by remember { mutableStateOf(false)}
    val termsDialogOpen = remember { mutableStateOf(false)}
    val privacyDialogOpen = remember { mutableStateOf(false)}

    val authState by authViewModel.authState.collectAsState()
//    val isLoading = authViewModel.isLoading
    val (checkedState, onCheckedStateChange) = remember { mutableStateOf(false) }
    val (uncheckedState, onUncheckedStateChange) = remember { mutableStateOf(false) }

    // Googleサインインの結果を受け取るランチャー
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val signInIntent = result.data
            // ViewModelで結果を処理
            authViewModel.handleSignInResult(signInIntent)
        } else {
//            authViewModel.updateLoading(false)
            showDialog = true
        }
    }

    // 認証成功で画面遷移
    LaunchedEffect(authState) {
        if (authState.data == true) {
            Log.d("TAG", "Navigate to timeline")
            onNavigateToMainView()  // 遷移先の処理を呼び出す
        }
    }

    when(authState.status) {
        Status.SUCCESS -> {
            if (authState.data == true) {
                onNavigateToMainView()
            }
            authViewModel.resetState()
        }
        Status.FAILURE -> {
            showDialog = true
            authViewModel.resetState()
        }
        else -> {}
    }

    if(showDialog){
        AlertDialog(onDismissRequest = {
            showDialog = false
            authViewModel.resetState()
        },
            confirmButton = { /*TODO*/ },
            title = { Text(text = "エラー")},
            text = { Text(text = "エラーが発生しました。時間をおいて再度お試しください。")}
            )
    }
    termsDialog(dialogOpen = termsDialogOpen)
    privacyDialog(dialogOpen = privacyDialogOpen)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(24.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(authState.status) {
                Status.LOADING -> {
                    CustomCircularProgressIndicator()
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.welcome_to_vental),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.vental_subheading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checkedState,
                                onValueChange = { onCheckedStateChange(!checkedState) },
                                role = Role.Checkbox
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedState,
                            onCheckedChange = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        //TODO テキストボタンを組み合わせる
//                        Text(
//                            text = stringResource(id = R.string.check_to_agree_terms),
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
                        TermsAndPrivacyText(
                            onTermsClick = { termsDialogOpen.value = true },
                            onPrivacyClick = { privacyDialogOpen.value = true }
                            )
                    }
                    if (uncheckedState){
                        Text(text = stringResource(id = R.string.close_account_agreement_required),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleMedium)
                    }

//                    Text(text = "Googleでサインイン")
//                    Spacer(modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    ElevatedButton(onClick = {
                        if (checkedState) {
                            authViewModel.signInWithGoogle(launcher)
                        } else {
                            onUncheckedStateChange(true)
                        }
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),

                    ){
                        Image(
                            painter = painterResource(id = R.drawable.google_icon), // GoogleアイコンのリソースID
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(28.dp) // アイコンサイズ

                        )
                        Text(stringResource(id = R.string.sign_in_with_google),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.agree_to_terms), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
//        if(isLoading){
//            CircularProgressIndicator()
//        } else {

//        }

        }
    }





}


@Composable
fun TermsAndPrivacyText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(stringResource(id = R.string.I) + " ")

        pushStringAnnotation(tag = "terms", annotation = "利用規約")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary, textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.terms_of_service))
        }
        pop()

        append(" " + stringResource(id = R.string.and) + " ")

        pushStringAnnotation(tag = "privacy", annotation = "プライバシーポリシー")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary, textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.privacy_policy))
        }
        pop()

        append(" " + stringResource(id = R.string.agree_to))
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        "terms" -> onTermsClick()
                        "privacy" -> onPrivacyClick()
                    }
                }
        }
    )
}

@Composable
fun termsDialog(
    dialogOpen: MutableState<Boolean>,

    ) {
    if (dialogOpen.value) {
        AlertDialog(
            onDismissRequest = { dialogOpen.value = false },
            confirmButton = { /*TODO*/ },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.terms_of_service))
                    IconButton(onClick = { dialogOpen.value = false }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_clear_24), contentDescription = "clear")
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    item {
                        Text(text = stringResource(id = R.string.terms_of_service_content))
                    }
                }
            }
        )
    }
}

@Composable
fun privacyDialog(
    dialogOpen: MutableState<Boolean>,

    ) {
    if (dialogOpen.value) {
        AlertDialog(
            onDismissRequest = { dialogOpen.value = false },
            confirmButton = { /*TODO*/ },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.privacy_policy))
                    IconButton(onClick = { dialogOpen.value = false }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_clear_24), contentDescription = "clear")
                    }
                }
                    },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    item { 
                        Text(text = stringResource(id = R.string.privacy_policy_content))
                    }
                }
            }
        )
    }
}

