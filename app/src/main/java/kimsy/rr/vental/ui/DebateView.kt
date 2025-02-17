package kimsy.rr.vental.ui

import MessageView
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.primarySurface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Comment
import kimsy.rr.vental.data.CommentItem
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateShareModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CustomLinearProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.ui.CommonComposable.VSSurface
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import kimsy.rr.vental.ui.CommonComposable.showAsBottomSheet
import kimsy.rr.vental.viewModel.DebateViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel
import java.util.Date
import java.util.UUID


@OptIn(ExperimentalLayoutApi::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebateView(
    debateViewModel: DebateViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel,
    toReportDebateView: () -> Unit,
    toRequestDebateDeletionView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val currentUser = sharedDebateViewModel.currentUser

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isKeyboardVisible = WindowInsets.isImeVisible
    var messageText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val fetchCommentItemState by debateViewModel.fetchCommentItemState.collectAsState()
    val currentDebateItem by sharedDebateViewModel.currentDebateItem.collectAsState()

    val likeState by sharedDebateViewModel.likeState.collectAsState()
    val createMessageState by debateViewModel.createMessageState.collectAsState()
    val sendCommentState by debateViewModel.sendCommentState.collectAsState()
    val getCommentsCountState by debateViewModel.getCommentsCountState.collectAsState()
    var commentCount = 0

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("VS", "コメント")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    val listState = rememberLazyListState()

    LaunchedEffect(imageUri) {
        imageUri?.let {
            Log.d("DV", "scroll to bottom")
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    LaunchedEffect(Unit) {
        debateViewModel.observeFollowingUserIds()
        currentDebateItem?.let {
            debateViewModel.getCommentsCount(it.debate)
            debateViewModel.getComments(it.debate)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }
    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }


    when (likeState[currentDebateItem]?.status) {
        Status.SUCCESS -> {
            currentDebateItem?.let { sharedDebateViewModel.resetLikeState(it) }
        }
        Status.FAILURE -> {
            sharedDebateViewModel.showLikeFailedToast(LocalContext.current)
            currentDebateItem?.let { sharedDebateViewModel.resetLikeState(it) }
        }
        else -> {}
    }

    when (createMessageState.status) {
        Status.LOADING -> CustomLinearProgressIndicator()
        Status.SUCCESS -> {
            imageUri = null
            messageText = ""
            debateViewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(context, R.string.message_creation_fail, Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }

    when (sendCommentState.status) {
        Status.LOADING -> CustomLinearProgressIndicator()
        Status.SUCCESS -> {

            val newComment = CommentItem(
                comment = Comment(
                    commentId = UUID.randomUUID().toString(), // 仮のID
                    commenterId = currentUser.value.uid,
                    commentContent = commentText,
                    commentedDateTime = Date()
                ),
                user = currentUser.value // 送信者情報
            )

            // ローカルリストに即時追加
            debateViewModel.addCommentLocally(newComment)

            // 入力欄をクリア
            commentText = ""
            commentCount += 1

            debateViewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(context, R.string.send_comment_failure, Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }

    when(getCommentsCountState.status) {
        Status.SUCCESS -> {
            commentCount = getCommentsCountState.data?.toInt() ?: 0
        }
        else -> {
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item{
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clickable { /* Handle Click Action */ }
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        //TODO 色々な場所でcurrentDebateItemがNullかチェックしているが、どこかで一回に抑える。

                        if (currentDebateItem != null) {
                            DebateContent(
                                debateItem = currentDebateItem!!,
                                sharedDebateViewModel = sharedDebateViewModel,
                                debateViewModel = debateViewModel,
                                toReportDebateView, toRequestDebateDeletionView,
                                toAnotherUserPageView = toAnotherUserPageView
                            )
                        } else {
                            Toast.makeText(
                                context,
                                stringResource(id = R.string.fail_loading_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                        }

//TODO
//                            if (fetchCommentItemState.status == Status.SUCCESS) {
//                                Text(text = fetchCommentItemState.data?.size.toString())
//                            }


                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .width(200.dp)
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.background
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    modifier = Modifier.padding(8.dp),
                                    content = {
                                        Text(
                                            text = if (index == 1 && commentCount != 0) "$tab($commentCount)" else tab,
                                            color = if (selectedTabIndex == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) { index ->
                            when (index) {
                                0 -> {
                                    currentDebateItem?.let {
                                        MessageView(
                                            viewModel = debateViewModel,
                                            debate = it.debate,
                                            posterIcon = it.poster.photoURL,
                                            debaterIcon = it.debater.photoURL
                                        )
                                    }
                                }
                                1 -> {
                                    val debate = currentDebateItem?.debate
                                    if (debate != null) {
                                        CommentView(
                                            viewModel = debateViewModel,
                                            modifier = Modifier.fillMaxWidth(),
                                            debate = debate,
                                            toAnotherUserPageView = toAnotherUserPageView
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        when(selectedTabIndex) {
            0 -> {
                if (currentDebateItem?.debate?.posterId == currentUser.value.uid || currentDebateItem?.debate?.debaterId == currentUser.value.uid) {
                    debateTextField(
                        imageUri = imageUri,
                        onImageDelete = { imageUri = null },
                        isKeyboardVisible = isKeyboardVisible,
                        context = context,
                        onImageSelected = { imageUri = it },
                        text = messageText,
                        onTextChange = { messageText = it }
                    ) {
                        currentDebateItem?.let {
                            debateViewModel.createMessage(
                                debate = it.debate,
                                text = messageText,
                                imageUri = imageUri,
                                context = context
                            )
                        }
                    }
                }
            }
            1 -> {
                CommentTextField(
                    isKeyboardVisible = isKeyboardVisible,
                    text = commentText,
                    onTextChange = { commentText = it},
                    onSendClick = {
                        currentDebateItem?.let {
                            debateViewModel.sendComment(
                                debate = it.debate,
                                text = commentText,
                                context = context
                            )
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun DebateBottomSheet(
    modifier: Modifier,
    debate: Debate,
    currentUserId: String,
    toReportDebateView: () -> Unit,
    toRequestDebateDeletionView: () -> Unit,
    hideModal: () -> Unit
){
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                androidx.compose.material.MaterialTheme.colors.primarySurface
            )
    ){
        Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween){
            if (currentUserId == debate.posterId || currentUserId == debate.debaterId) {
                Row(
                    modifier = modifier
                        .padding(16.dp)
                        .clickable {
                            DebateShareModel.setDeleteRequestedDebateToModel(debate)
                            hideModal()
                            toRequestDebateDeletionView()
                        }
                ){
                    Icon(modifier = Modifier.padding(end = 8.dp),
                        painter =  painterResource(id = R.drawable.outline_delete_24),
                        contentDescription = "request debate deletion")
                    Text(text = stringResource(id = R.string.to_do_request_debate_deletion), fontSize = 20.sp, color = Color.White)
                }
            } else {
                Row(
                    modifier = modifier
                        .padding(16.dp)
                        .clickable {
                            DebateShareModel.setReportedDebateToModel(debate)
                            hideModal()
                            toReportDebateView()
                        }
                ){
                    Icon(modifier = Modifier.padding(end = 8.dp),
                        painter =  painterResource(id = R.drawable.outline_report_problem_24),
                        contentDescription = "report debate")
                    Text(text = stringResource(id = R.string.to_do_report_debate), fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}



@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun DebateContent(
    debateItem: DebateItem,
    sharedDebateViewModel: SharedDebateViewModel,
    debateViewModel: DebateViewModel,
    toReportDebateView: () -> Unit,
    toRequestDebateDeletionView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
    ) {
    val activity = LocalContext.current as Activity
    val followingUserIdsState by debateViewModel.followingUserIdsState.collectAsState()
    val currentUser = debateViewModel.currentUser
    val debate = debateItem.debate
    val ventCard = debateItem.ventCard
    val debater = debateItem.debater
    val poster = debateItem.poster
    val followState by debateViewModel.followState.collectAsState()

    when(followState.status) {
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.follow_fail), Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ){
        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountIcon(imageUrl = poster.photoURL)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = poster.name)
                        when (followingUserIdsState.status) {
                            Status.SUCCESS -> {
                                val followingUserIds = followingUserIdsState.data
                                if (followingUserIds != null && poster.uid != currentUser.value.uid) {
                                    if (!followingUserIds.contains(poster.uid)){
                                        OutlinedButton(
                                            onClick = {
                                                debateViewModel.followUser(poster.uid)
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.follow))
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                debateViewModel.unFollowUser(poster.uid)
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.unfollow))
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ventCard.swipeCardCreatedDateTime?.let {
                            formatTimeDifference(it)
                        } ?: "日付不明",
                    )
                    IconButton(onClick = {
                        activity.showAsBottomSheet { hideModal ->
                            DebateBottomSheet(
                                modifier = Modifier.fillMaxWidth(),
                                debate = debate,
                                currentUserId = currentUser.value.uid,
                                toReportDebateView = toReportDebateView,
                                toRequestDebateDeletionView = toRequestDebateDeletionView,
                                hideModal = hideModal
                            )
                        }

                    }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_more_vert_24),
                            contentDescription = "option")
                    }
                }
            }
            Image(
                painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                contentDescription = "ventCardImage",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
            Text(text = ventCard.swipeCardContent)
            ventCard.tags.forEach { tag->
                Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)

            }
        }
    }
    VSSurface(
        debateItem = debateItem,
        sharedDebateViewModel = sharedDebateViewModel,
        debateViewModel = debateViewModel,
        toAnotherUserPageView = toAnotherUserPageView
    )
}

@Composable
fun AccountIcon(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun showLoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxHeight(0.4f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun debateTextField(
    imageUri: Uri?,
    onImageDelete: () -> Unit,
    isKeyboardVisible: Boolean,
    context: Context,
    onImageSelected: (Uri?) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (imageUri != null) {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .heightIn(max = 60.dp)
                    .offset(x = (-24).dp, y = (-72).dp) // Columnの上に浮かせる
                    .zIndex(1f) // 前面に表示,
                    .background(Color.Transparent)
            ) {

                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .size(20.dp) // 背景のサイズを小さくする
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (6).dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    IconButton(
                        onClick = { onImageDelete() },
                        modifier = Modifier.fillMaxSize() // Boxのサイズに適合
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_clear_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp) // アイコンのサイズを個別に調整
                        )
                    }
                }
            }
        }

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
            placeHolder = { Text(text = stringResource(id = R.string.add_message)) },
            colors = TextFieldDefaults.colors(

                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = {
                Row {
                    ImagePermissionAndSelection(
                        context = context,
                        modifier = Modifier.padding(start = 8.dp),
                        onImageSelected = onImageSelected
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_image_24),
                            contentDescription = "add Image",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onSendClick) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}

