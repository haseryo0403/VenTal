package kimsy.rr.vental.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import formatDate
import kimsy.rr.vental.R
import kimsy.rr.vental.data.CommentItem
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.DebateViewModel


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CommentView(
    viewModel: DebateViewModel,
    modifier: Modifier,
    debate: Debate,
    toAnotherUserPageView: (user: User) -> Unit,
){
    val fetchCommentItemState by viewModel.fetchCommentItemState.collectAsState()
    val commentItems by viewModel.commentItems.collectAsState()

    Box(
        Modifier
            .fillMaxWidth()
    ){
        Column(modifier = modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ){
            when{
                commentItems.isNotEmpty() -> {
                    CommentItemRows(
                        commentItems = commentItems,
                        viewModel = viewModel,
                        debate = debate,
                        toAnotherUserPageView = toAnotherUserPageView
                    )
                }
                else -> {
                    when(fetchCommentItemState.status){
                        Status.LOADING -> {
                            CustomCircularProgressIndicator()
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
    }
}

@Composable
fun CommentItemRows(
    commentItems: List<CommentItem>,
    viewModel: DebateViewModel,
    debate: Debate,
    toAnotherUserPageView: (user: User) -> Unit
) {
    val hasFinishedLoadingAllCommentItem = viewModel.hasFinishedLoadingAllCommentItems

    commentItems.forEachIndexed {index, commentItem ->
        val comment = commentItem.comment
        val commenter = commentItem.user
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column {
                IconButton(onClick = { toAnotherUserPageView(commenter) }) {
                    AccountIcon(imageUrl = commenter.photoURL)
                }
            }
            Column {
                Row {
                    Text(text = commenter.name)
                    Text(text = comment.commentedDateTime?.let { formatDate(it) }.toString(), color = Color.Gray)
                }
                Text(text = comment.commentContent)
            }
        }
        if ((index + 1) % 7 == 0) {
            if (!hasFinishedLoadingAllCommentItem) {
                LoadCommentItems(viewModel = viewModel, debate = debate)
            }
        }
    }
}

@Composable
fun LoadCommentItems(viewModel: DebateViewModel, debate: Debate) {
    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        viewModel.getComments(debate)
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
    MaxLengthOutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        maxLength = 140,
        modifier = Modifier
            .offset(y = (-8).dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .heightIn(max = if (isKeyboardVisible) 200.dp else 56.dp),
        placeHolder = {Text(text = stringResource(id = R.string.add_comment))},
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
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