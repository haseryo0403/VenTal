package kimsy.rr.vental.ui.commonUi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
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
import kimsy.rr.vental.screensInBottom
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppBottomBarView(
    title: String,
    navController: NavController,
    currentRoute: String,
    bottomBarHeight: Dp,
    bottomBarOffsetHeightPx: MutableState<Float>
){
    if (title.contains("反論")){

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