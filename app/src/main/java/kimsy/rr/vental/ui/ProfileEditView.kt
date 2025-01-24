package kimsy.rr.vental.ui

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.ProfileEditViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CustomLinearProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditView(
    profileEditViewModel: ProfileEditViewModel = hiltViewModel(),
    toMyPageView: () -> Unit
){
    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()
    var userName by remember{ mutableStateOf(currentUser?.name?:"")}
    var selfIntroduction by remember { mutableStateOf(currentUser?.selfIntroduction?: "")}
    var profileImage by remember { mutableStateOf(currentUser?.photoURL?: "") }
    //TODO profileImageにuriたぶん入らないからnewImageなりなんなりを作ることになると思う
    var newProfileImageUri by remember { mutableStateOf<Uri?>(null) }
    val updateUserState by profileEditViewModel.updateUserState.collectAsState()
    val context = LocalContext.current

    var selectedAgeGroup by remember { mutableStateOf("年齢層を選択") }
//    var selfIntroduction by remember{ mutableStateOf("")}
    val gender = listOf("男性", "女性", "その他")
    var selectedIndex by remember { mutableStateOf(2) }
    var expanded by remember { mutableStateOf(false) }

    when (updateUserState.status) {
        Status.LOADING -> CustomLinearProgressIndicator()
        Status.SUCCESS -> {
            toMyPageView()
            profileEditViewModel.resetState()
            userName = ""
            selfIntroduction = ""
            newProfileImageUri = null
        }
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.update_user_fail), Toast.LENGTH_LONG).show()
            profileEditViewModel.resetState()
        }
        else -> {}
    }


    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),

    ) {
        val textFieldModifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            ImagePermissionAndSelection(
                context = context,
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape),
                onImageSelected = {newProfileImageUri = it}
            ) {
                Image(
                    painter = if(newProfileImageUri != null) rememberAsyncImagePainter(newProfileImageUri) else rememberAsyncImagePainter(profileImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Icon (
                    painter = painterResource(id = R.drawable.outline_camera_alt_24),
                    contentDescription = "camera",
                    modifier = Modifier
                        .size(72.dp)
                        .alpha(0.5F)
                )
            }
//            IconButton(onClick = { /*TODO*/ },
//                    modifier = Modifier
//                        .size(104.dp)
//                        .clip(CircleShape)
//                ) {
//                Image(
//                    painter = rememberAsyncImagePainter(profileImage),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(104.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//
////                Icon(
////                    painter = rememberAsyncImagePainter(profileImage),
////                    contentDescription = "profilePicture",
////                    modifier = Modifier.fillMaxSize()
////                )
//                Icon (
//                    painter = painterResource(id = R.drawable.outline_camera_alt_24),
//                    contentDescription = "camera",
//                    modifier = Modifier
//                        .size(72.dp)
//                        .alpha(0.5F)
//                )
//            }

        }

        // 「必須」のラベルを塗り潰しの四角形で表示
//        Box(
//            modifier = Modifier
//                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
//                .padding(4.dp) ,// テキスト周りのパディング
//        ) {
//            Text(text = "必須", color = Color.White)
//        }

        Text(stringResource(id = R.string.user_name))

        OutlinedTextField(
            value = userName,
            onValueChange = {userName = it},
            label = {Text(text = stringResource(id = R.string.please_write_userName))},
            modifier = textFieldModifier,
            singleLine =true
        )

//        Row {
//            Text("あなたの性別を選択してください（非公開）")
//            // 「必須」のラベルを塗り潰しの四角形で表示
//            Box(
//                modifier = Modifier
//                    .background(Color.Red, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
//                    .padding(4.dp) ,// テキスト周りのパディング
//            ) {
//                Text(text = "必須", color = Color.Gray)
//            }
//        }
//
//
//
//        SingleChoiceSegmentedButtonRow(
//            modifier = textFieldModifier
//        ) {
//            gender.forEachIndexed { index, label ->
//                SegmentedButton(
//                    shape = SegmentedButtonDefaults.itemShape(index = index, count = gender.size),
//                    onClick = { selectedIndex = index },
//                    selected = index == selectedIndex,
//                    modifier = Modifier
//                        .height(56.dp)
//                        .padding(top = 8.dp)
//                ) {
//                    Text(label)
//                }
//            }
//        }
//
//        // 「必須」のラベルを塗り潰しの四角形で表示
//        Box(
//            modifier = Modifier
//                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
//                .padding(4.dp) ,// テキスト周りのパディング
//        ) {
//            Text(text = "必須", color = Color.White)
//        }
//
//
//        Text("年齢層を選択してください（非公開）",modifier = Modifier.padding(bottom = 8.dp))
//
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .clickable { expanded = true }
//            .border(BorderStroke(1.dp, Color.Gray), shape = MaterialTheme.shapes.small)
//            .padding(16.dp)
//        ) {
//
//            Row(verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(selectedAgeGroup, modifier = Modifier.weight(1f))
//
//                // 矢印アイコンを表示
//                Icon(
//                    imageVector = Icons.Default.ArrowDropDown,
//                    contentDescription = "Expand age group selection"
//                )
//            }
//
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier
//                    .fillMaxWidth(0.8f)
//                    .align(Alignment.TopStart)
//                    .offset(y = 8.dp)
//
//            ) {
//                val ageGroups = listOf("10代", "20代", "30代", "40代", "50代以上")
//                ageGroups.forEach { ageGroup ->
//                    DropdownMenuItem(
//                        text = { Text(ageGroup) },
//                        onClick = {
//                            selectedAgeGroup = ageGroup
//                            expanded = false
//                        },
//                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
//                    )
//                }
//            }
//        }

        Text(text = stringResource(id = R.string.self_introduce), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        OutlinedTextField(
            value = selfIntroduction,
            onValueChange = {selfIntroduction = it},
            label = {Text(text = stringResource(id = R.string.please_write_self_introduce))},
            modifier = textFieldModifier.height(120.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = { profileEditViewModel.updateUser(userName, newProfileImageUri, selfIntroduction, context) },
                modifier = Modifier.size(width = 200.dp, height = 60.dp)
                ) {
                Text(stringResource(id = R.string.change))
            }
        }

    }
}


//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun ProfilePrev(){
//    ProfileEditView()
//}


