package kimsy.rr.vental.ui.commonUi

import android.content.Context
import android.os.Build
import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.MainViewModel
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.data.ImageUtils
import kimsy.rr.vental.screensInBottom
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppBottomBarView(
    title: String,
    navController: NavController,
    currentRoute: String,
    context: Context,
    viewModel: VentCardCreationViewModel
){
    if (!title.contains("VCC") ){
        BottomNavigation(Modifier.wrapContentSize()) {
            screensInBottom.forEach {
                    item ->
                val isSelected = currentRoute == item.bottomRoute
                val tint = if(isSelected) Color.White else Color.Black

                BottomNavigationItem(
                    selected = currentRoute == item.bottomRoute,
                    onClick = { navController.navigate(item.bottomRoute) },
                    icon = { Icon(painter = painterResource(id = item.icon),
                        contentDescription = item.bottomTitle,
                        tint = tint) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.Black
                )

            }
        }
    } else {
        //TODO 削除予定
//        val pickMediaLauncher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.PickVisualMedia(),
//            onResult = {
//                uri->
//                if(uri != null){
//                    Log.d("PhotoPicker", "Selected URI: $uri")
//                    viewModel.updateSelectedImage(uri)
//
//                } else {
//                    Log.d("PhotoPicker", "No media selected")
//                }
//            })
//
//        val requestPermissionLauncher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.RequestPermission(),
//            onResult = {permission ->
//                if (!permission){
//                    // ask for permission
//                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
//                        context as MainActivity,
//                        Manifest.permission.READ_MEDIA_IMAGES
//                    )
//                    if (rationalRequired){
//                        Toast.makeText(
//                            context,
//                            "この機能を使用するには画像アクセス権が必要です",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "この機能を使用するには画像アクセス権が必要です。端末の設定で許可をしてください。",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//        )

        androidx.compose.material3.BottomAppBar(
            actions = {
                viewModel.handleImagePermissionAndSelection(context)
            },
            modifier = Modifier.height(48.dp)
        )
    }
}