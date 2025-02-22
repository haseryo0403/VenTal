package kimsy.rr.vental.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.FollowPageViewModel

@Composable
fun FollowListView(
    viewModel: FollowPageViewModel,
    toAnotherUserPageView: (user: User) -> Unit
) {
    val followingUserIdsState by viewModel.followingUserIdsState.collectAsState()
    val followingUserState by viewModel.followingUserState.collectAsState()
    val currentUser = viewModel.currentUser

    LaunchedEffect(Unit) {
        viewModel.observeFollowingUserIds()
    }

    if (followingUserIdsState.status == Status.FAILURE) {
        ErrorView(retry = {
            viewModel.observeFollowingUserIds()
        })
    } else {
        when(followingUserState.status) {
            Status.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Status.SUCCESS -> {
                val users = followingUserState.data
                if (!users.isNullOrEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ){
                        items(users) {user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        toAnotherUserPageView(user)
                                    }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(user.photoURL),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(text = user.name)
                                }
                                Row {
                                    when (followingUserIdsState.status) {
                                        Status.SUCCESS -> {
                                            val followingUserIds = followingUserIdsState.data
                                            if (currentUser != null) {
                                                if (followingUserIds != null) {
                                                    if (!followingUserIds.contains(user.uid)){
                                                        OutlinedButton(
                                                            onClick = {
                                                                viewModel.followUser(user.uid)
                                                            },
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text(text = "フォローする")
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                viewModel.unFollowUser(user.uid)
                                                            },
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text(text = "フォロー解除")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }


                    }
                } else {
                        Text(text = "フォローしてね")
                }
            }
            Status.FAILURE -> {
                ErrorView(retry = {
                    viewModel.observeFollowingUserIds()
                })
            }
            else -> {
                Text(text = "あれれ？")
            }
        }
    }
}