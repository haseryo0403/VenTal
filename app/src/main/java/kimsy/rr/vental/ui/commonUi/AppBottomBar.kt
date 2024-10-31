package kimsy.rr.vental.ui.commonUi

import android.content.Context
import android.os.Build
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.R
import kimsy.rr.vental.data.ImageUtils
import kimsy.rr.vental.screensInBottom

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppBottomBarView(
    title: String,
    navController: NavController,
    currentRoute: String,
    imageUtils: ImageUtils,
    context: Context
){
    if (!title.contains("VCC") ){
        BottomNavigation(Modifier.wrapContentSize()) {
            screensInBottom.forEach {
                    item ->
                val isSelected = currentRoute == item.bottomRoute
                Log.d("Navigation", "Item: ${item.bottomTitle}, Current Route: $currentRoute")
                val tint = if(isSelected) Color.White else Color.Black

                Log.d("TAG","${item.bottomRoute}, ${item.bottomRoute}, ${item.icon}")
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

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {permission ->
                if (permission){
                    // I have access to images
                } else {
                    // ask for permission
                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                    if (rationalRequired){
                        Toast.makeText(
                            context,
                            "画像アクセス権が必要",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "画像アクセス権が必要。アンドロイドの設定で許可して",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        val permissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)

        androidx.compose.material3.BottomAppBar(
            actions = {
                IconButton(onClick = {
                    if (imageUtils.hasImagePermission(context)){
                        //permission ok
                    } else {
                        // request permission

                        // API レベルに応じて権限をリクエスト
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Android 13 以降
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            // Android 12 以下
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }

                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_image_24),
                        modifier = Modifier.size(40.dp),
                        contentDescription = "add Image"
                    )
                }
            },
            modifier = Modifier.height(48.dp)
        )
    }
}