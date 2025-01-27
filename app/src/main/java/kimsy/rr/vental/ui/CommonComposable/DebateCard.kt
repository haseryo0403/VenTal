package kimsy.rr.vental.ui.CommonComposable

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.viewModel.SharedDebateViewModel
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun DebateCard(
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
    onLikeStateSuccess: (debateItem: DebateItem) -> Unit,
    debateItem: DebateItem
) {
    var debate = debateItem.debate
    val ventCard = debateItem.ventCard
    val poster = debateItem.poster
    val debater = debateItem.debater

    val likeState by sharedDebateViewModel.likeState.collectAsState()

    when (likeState[debateItem]?.status) {
        Status.SUCCESS -> {
            //TODO もし必要なUIの処理があれば。
            onLikeStateSuccess(likeState[debateItem]?.data!!)
            sharedDebateViewModel.resetLikeState(debateItem)
        }
        Status.FAILURE -> {
            sharedDebateViewModel.showLikeFailedToast(LocalContext.current)
            sharedDebateViewModel.resetLikeState(debateItem)
        }
        else -> {}
    }



    Column(
        modifier = Modifier
            .clickable {
                sharedDebateViewModel.setCurrentDebateItem(debateItem)
                toDebateView()
            }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ){
            Image(
                painter = rememberAsyncImagePainter(poster.photoURL),
                contentDescription = "AccountIcon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        //TODO 遷移先ユーザーがログイン中のユーザーではないかチェックする
                        toAnotherUserPageView(poster)
                    },
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(5f)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = poster.name,
                        modifier = Modifier
                            .clickable {
                                toAnotherUserPageView(poster)
                            }
                    )
                }
                Text(text = ventCard.swipeCardContent)
                ventCard.tags.forEach { tag->
                    Text(text = tag, color = MaterialTheme.colorScheme.primary)
                }
                Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                    contentDescription = "Image",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                Divider()

            }
        }

        // ここ2つアイコンのライン
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(debater.photoURL),
                    contentDescription = "AccountIcon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            toAnotherUserPageView(debater)
                        },
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = debater.name,
                    modifier = Modifier
                        .clickable {
                            toAnotherUserPageView(debater)
                        }
                )
            }
            // ひだりいいね debater
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                IconButton(onClick = {
                    sharedDebateViewModel.handleLikeAction(debateItem, UserType.DEBATER)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                        contentDescription = "heart",
                        tint = if (debateItem.likedUserType == UserType.DEBATER) Color.Red else Color.Gray
                    )
                }
                Text(text = debate.debaterLikeCount.toString())
            }
            // みぎいいね poster
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                IconButton(onClick = {
                    sharedDebateViewModel.handleLikeAction(debateItem, UserType.POSTER)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                        contentDescription = "heart",
                        tint = if (debateItem.likedUserType == UserType.POSTER) Color.Red else Color.Gray

                    )
                }
                Text(text = debate.posterLikeCount.toString())
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(poster.photoURL),
                    contentDescription = "AccountIcon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            toAnotherUserPageView(poster)
                       },
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = poster.name,
                    modifier = Modifier
                        .clickable {
                            toAnotherUserPageView(poster)
                        }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Surface(
                modifier = Modifier
                    .padding(4.dp)
                    .widthIn(max = 250.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = debate.firstMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Text(
                text = debate.debateCreatedDatetime?.let {
                    formatTimeDifference(it)
                } ?: "日付不明",
            )

        }
    }
    Divider()
}
