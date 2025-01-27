package kimsy.rr.vental.ui.commonUi

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kimsy.rr.vental.R
import kimsy.rr.vental.viewModel.VentCardCreationViewModel
import kimsy.rr.vental.screensInBottom
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppBottomBarView(
    title: String,
    navController: NavController,
    currentRoute: String,
    context: Context,
    viewModel: VentCardCreationViewModel,
    bottomBarHeight: Dp,
    bottomBarOffsetHeightPx: MutableState<Float>
){
    if (title.contains("VCC") ){
        BottomAppBar(
            actions = {
// 右寄               Spacer(modifier = Modifier.weight(1f))
                ImagePermissionAndSelection(
                    context = context,
                    modifier = Modifier.padding(start = 8.dp),
                    onImageSelected = {uri ->
                        // 選択された画像URIをここで処理
                        if (uri != null) {
                            // 画像URIが選択された場合の処理
                            viewModel.selectedImageUri = uri
                        }
                    }){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_image_24),
                            modifier = Modifier.size(40.dp),
                            contentDescription = "add Image"
                        )
                    }
            },
            modifier =
//            Modifier.height(48.dp),
            Modifier
                .fillMaxWidth()
                //TODO FIX
                .height(bottomBarHeight)
                .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.value.roundToInt()) },
//            containerColor = MaterialTheme.colorScheme.primary
        )
    } else if (title.contains("反論")){
//        var text by remember { mutableStateOf("") }
//        BottomAppBar(
//            //TODO Make it not RowScope
//            actions = {
//// 右寄               Spacer(modifier = Modifier.weight(1f))
//                ImagePermissionAndSelection(
//                    context = context,
//                    onImageSelected = {uri ->
//                        // 選択された画像URIをここで処理
//                        if (uri != null) {
//                            // 画像URIが選択された場合の処理
////                            viewModel.selectedImageUri = uri
//                        }
//                    })
//
//                MaxLengthOutlinedTextField(value = text, onValueChange = {text = it}, maxLength = 140)
//                TextButton(onClick = { /*TODO*/ }) {
//                    Text(text = "送信")
//                }
//            },
////            modifier = Modifier.height(48.dp)
//        )
    } else {

        BottomNavigation(
            Modifier
                .fillMaxWidth()
            //TODO FIX
                .height(bottomBarHeight)
                .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.value.roundToInt()) },
            backgroundColor = MaterialTheme.colorScheme.background,
            elevation = 0.dp
            ) {
            screensInBottom.forEach {
                    item ->
                val isSelected = currentRoute == item.bottomRoute
                val tint = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

                BottomNavigationItem(
                    modifier = Modifier.padding(0.dp),
                    selected = currentRoute == item.bottomRoute,
                    onClick = { navController.navigate(item.bottomRoute) },
                    icon = { Icon(painter = painterResource(id = item.icon),
                        contentDescription = item.bottomTitle,
                        tint = tint) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = Color.Gray,
//                    label = {
//                        Text(
//                            text = item.label,
//                            maxLines = 1,
//                            style = TextStyle(
//                                fontWeight = FontWeight.Normal,
//                                fontSize = 11.sp,
//                                letterSpacing = 0.4.sp
//                            )
//                        )
//                    },
                    )

            }
        }
    }
}