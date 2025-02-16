package kimsy.rr.vental.ui.commonUi

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import formatDate
import kimsy.rr.vental.R
import kimsy.rr.vental.data.CommentItem
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.AccountIcon
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.CustomLinearProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.viewModel.DebateViewModel
import kotlinx.coroutines.launch
import java.util.Date

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebateCommentBottomSheet(
    viewModel: DebateViewModel,
    modifier: Modifier,
    debate: Debate,
    toAnotherUserPageView: () -> Unit,
    hideModal: () -> Unit,
){
    val isKeyboardVisible = WindowInsets.isImeVisible
    var isForcus by remember { mutableStateOf(false) }  // 状態を保持するように変更
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sendCommentState by viewModel.sendCommentState.collectAsState()
    val fetchCommentItemState by viewModel.fetchCommentItemState.collectAsState()
//    val commentSize = (fetchCommentItemState.data?.size?: 0).toString()
    val focusManager = LocalFocusManager.current


    when(sendCommentState.status) {
        Status.LOADING -> {
            CustomLinearProgressIndicator()
        }
        Status.SUCCESS -> {
            text = ""
            viewModel.getComments(debate)
        }
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.send_comment_failure), Toast.LENGTH_LONG).show()
        }
        else -> {}
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                MaterialTheme.colors.background
            )
            .imePadding()
    ){
        Column(modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Top

        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text(
//                    text = stringResource(id = R.string.comment) + " " + commentSize + stringResource(
//                        id = R.string.unit_of_comment
//                    ),
//                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                )
                IconButton(onClick = { hideModal() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_clear_24),
                        contentDescription = "close bottom sheet",
                    )
                }

            }

            CommentTextField(
                isKeyboardVisible = isKeyboardVisible,
                text = text,
                onTextChange = { text = it},
                onKeyboardVisibleChanged = {state ->
                    isForcus = state
                },
                onSendClick = {
                    viewModel.sendComment(
                        debate = debate,
                        text = text,
                        context = context
                    )
                }
            )

            when(fetchCommentItemState.status) {
                Status.LOADING -> {
                    CustomCircularProgressIndicator()
                }
                Status.SUCCESS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
//                            fetchCommentItemState.data?.let { CommentItemRows(it) }
                        }

                    }

                }
                Status.FAILURE -> {
                    ErrorView(retry = {
                        viewModel.getComments(debate)
                    })
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CommentItemRows(commentItems: List<CommentItem>) {
    var previousDate: Date? by remember { mutableStateOf(null) }
    commentItems.forEach {commentItem ->
        val comment = commentItem.comment
        val commenter = commentItem.user
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column {
                AccountIcon(imageUrl = commenter.photoURL)
            }
            Column {
                Row {
                    Text(text = commenter.name)
                    Text(text = comment.commentedDateTime?.let { formatDate(it) }.toString(), color = Color.Gray)
                }
                Text(text = comment.commentContent)
            }


        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CommentTextField(
    isKeyboardVisible: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onKeyboardVisibleChanged: (state: Boolean) -> Unit
) {

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = keyboardController) {
        // キーボードが閉じられたことを検知してフォーカスを消す
        if (keyboardController == null) {
            focusManager.clearFocus()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.background) // 親レイアウトを画面全体に拡張
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        onKeyboardVisibleChanged(true)
                    }
                } else {
                    coroutineScope.launch {
                        onKeyboardVisibleChanged(false)
                    }
                }
            },
//        contentAlignment = Alignment.BottomCenter
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                    .heightIn(max = if (isKeyboardVisible) 140.dp else 56.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MaxLengthOutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    maxLength = 140,
                    modifier = Modifier
                        .weight(1f)
                        .background(color = MaterialTheme.colors.onSurface)
                )
                IconButton(onClick = onSendClick) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}