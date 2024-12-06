package kimsy.rr.vental.ui

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.MyPageViewModel

@Composable
fun MyPageView(
    viewModel: MyPageViewModel,
    authViewModel: AuthViewModel,
    onSignOutSuccess:() -> Unit
){
    val context = LocalContext.current

    // currentUserをobserveしてStateとして取得
    val user by authViewModel.currentUser.observeAsState()

    val signOutResult by authViewModel.signOutResult.observeAsState()

    // サインアウト結果に基づいて遷移処理を行う
    LaunchedEffect(signOutResult) {
        if (signOutResult == true) {
            // サインアウト成功時にメイン画面などに遷移
            onSignOutSuccess()
        } else if (signOutResult == false) {
            // サインアウト失敗時の処理（エラーメッセージなど）
            Log.e("SignOut", "Sign out failed")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ){
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Log.d("TAG", "Image URL: ${user?.photoURL}")
                        Image(
                            painter = rememberAsyncImagePainter(user?.photoURL),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        user?.name?.let { Text(text = it) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "7")
                            Text(text = "討論")
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "16")
                            Text(text = "フォロワー")
                        }
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "プロフィールを編集")
                    }
                    Button(onClick = {
//                        authViewModel.signOut(){
//                            val intent = Intent(context, MainActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                            context.startActivity(intent)
//                        }
                        authViewModel.signOut(context)
                    }) {
                        Text(text = "ログアウト")
                    }
                }
            }
            Divider()
        }
        item {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .weight(1f)
                            .size(48.dp)
                    )

                    Column(
                        modifier = Modifier.weight(5f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "User Name")
                            Text(text = "23時間")
                        }
                        Text(text = "古文ってあんまり勉強したら人生に役に立つって感じがしないんだよね")
                        Text(text = "#学校", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        //TODO color choose
                        Image(painter = painterResource(id = R.drawable.aston_martin),
                            contentDescription = "Image",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon")
                        Text(text = "userName")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                            contentDescription = "haert")
                        Text(text = "64")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                            contentDescription = "haert")
                        Text(text = "110")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon")
                        Text(text = "userName")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
    //                            Text(
    //                                text = "2023/04/03",
    //                                style = MaterialTheme.typography.bodySmall,
    //                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    //                            )
                        }
                    }
                }
            }
            Divider()
        }
        item {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .weight(1f)
                            .size(48.dp)
                    )

                    Column(
                        modifier = Modifier.weight(5f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "User Name")
                            Text(text = "23時間")
                        }
                        Text(text = "古文ってあんまり勉強したら人生に役に立つって感じがしないんだよね")
                        Image(painter = painterResource(id = R.drawable.aston_martin),
                            contentDescription = "Image",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon")
                        Text(text = "userName")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                            contentDescription = "haert")
                        Text(text = "64")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                            contentDescription = "haert")
                        Text(text = "110")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon")
                        Text(text = "userName")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
    //                            Text(
    //                                text = "2023/04/03",
    //                                style = MaterialTheme.typography.bodySmall,
    //                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    //                            )
                        }
                    }
                }
            }
            Divider()
        }
    }
}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun MyPagePrev(){
//    MyPageView()
}

