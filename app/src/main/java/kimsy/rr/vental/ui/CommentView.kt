package kimsy.rr.vental.ui

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import formatDate
import kimsy.rr.vental.R
import kimsy.rr.vental.data.CommentItem
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.CustomLinearProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.MaxLengthTextField
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.DebateViewModel
import java.util.Date


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommentView(
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
    val commentSize = (fetchCommentItemState.data?.size?: 0).toString()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.getComments(debate)
    }


    when(sendCommentState.status) {
        Status.LOADING -> {
            CustomLinearProgressIndicator()
        }
        Status.SUCCESS -> {
            text = ""
            viewModel.getComments(debate)
            viewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.send_comment_failure), Toast.LENGTH_LONG).show()
        }
        else -> {}
    }

    Box(
        Modifier
            .fillMaxWidth()
    ){
        Column(modifier = modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.Top

        ){
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = stringResource(id = R.string.comment) + " " + commentSize + stringResource(
//                        id = R.string.unit_of_comment
//                    ),
//                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                )
//                IconButton(onClick = { hideModal() }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.baseline_clear_24),
//                        contentDescription = "close bottom sheet",
//                    )
//                }
//
//            }

            when(fetchCommentItemState.status) {
                Status.LOADING -> {
                    CustomCircularProgressIndicator()
                }
                Status.SUCCESS -> {
                    fetchCommentItemState.data?.let { CommentItemRows(it) }?: Text(text = "nullllll")
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


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CommentTextField(
    isKeyboardVisible: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    MaxLengthTextField(
        value = text,
        onValueChange = onTextChange,
        maxLength = 140,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = (-20).dp)
            .clip(RoundedCornerShape(16.dp))
            .heightIn(max = if (isKeyboardVisible) 200.dp else 56.dp),
        placeHolder = {Text(text = stringResource(id = R.string.add_comment))},
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 背景を透明に設定
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // フォーカス時の背景を透明に設定
        ),
        trailingIcon = {
            IconButton(onClick = onSendClick) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}