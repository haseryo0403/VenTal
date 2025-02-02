package kimsy.rr.vental.ui.CommonComposable

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

fun Activity.showAsBottomSheet(content: @Composable (() -> Unit) -> Unit) {
    val viewGroup = this.findViewById(android.R.id.content) as ViewGroup
    addContentToView(viewGroup, content)
}

private fun addContentToView(
    viewGroup: ViewGroup,
    content: @Composable (() -> Unit) -> Unit
) {
    viewGroup.addView(
        ComposeView(viewGroup.context).apply {
            setContent {
                BottomSheetWrapper(viewGroup, this, content)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BottomSheetWrapper(
    parent: ViewGroup,
    composeView: ComposeView,
    content: @Composable (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var isSheetOpened by remember { mutableStateOf(false) }

    ModalBottomSheetLayout(
        sheetBackgroundColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = modalBottomSheetState,
        sheetContent = {
            content {
                coroutineScope.launch {

                    modalBottomSheetState.hide()
                }
            }
        }
    ) {}

    BackHandler {
        coroutineScope.launch {
            modalBottomSheetState.hide()
        }
    }

    LaunchedEffect(modalBottomSheetState.currentValue) {
        when (modalBottomSheetState.currentValue) {
            ModalBottomSheetValue.Hidden -> {
                when {
                    isSheetOpened -> parent.removeView(composeView)
                    else -> {
                        isSheetOpened = true
                        modalBottomSheetState.show()
                    }
                }
            }
            else -> {}
        }
    }
}
