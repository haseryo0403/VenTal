package kimsy.rr.vental.ui.CommonComposable

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.viewModel.SharedDebateViewModel


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
            onLikeStateSuccess(likeState[debateItem]?.data!!)
            sharedDebateViewModel.resetLikeState(debateItem)
        }
        Status.FAILURE -> {
            sharedDebateViewModel.showLikeFailedToast(LocalContext.current)
            sharedDebateViewModel.resetLikeState(debateItem)
        }
        else -> {}
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { /* Handle Click Action */ }
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    sharedDebateViewModel.setCurrentDebateItem(debateItem)
                    toDebateView()
                }
        ) {
            //スワイプカードのコンテント以外の要素
            Box(modifier = Modifier.fillMaxWidth()) {

                Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp)
                ){
                    ExpandableTagRow(tags = ventCard.tags)
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(poster.photoURL),
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable {
                                toAnotherUserPageView(poster)
                            },
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        val textColor = if (ventCard.swipeCardImageURL.isEmpty()) {
                            MaterialTheme.colorScheme.onBackground// 画像がない場合のテキスト色（例: グレー）
                        } else {
                            Color.White // 画像がある場合のテキスト色（白）
                        }

                        val shadow = if (ventCard.swipeCardImageURL.isEmpty()) {
                            null // 画像がない場合はシャドウなし
                        } else {
                            Shadow(
                                color = Color.Black.copy(alpha = 0.6f), // シャドウの色を設定
                                offset = Offset(1f, 1f), // シャドウの位置を調整
                                blurRadius = 4f // シャドウのぼかし具合
                            )
                        }

                        Text(
                            text = poster.name,
                            modifier = Modifier
                                .clickable {
                                    toAnotherUserPageView(poster)
                                },
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                shadow = shadow // 影の有無を条件に応じて設定
                            )
                        )

                        val formattedDate = debate.debateCreatedDatetime?.let { formatTimeDifference(it) } ?: "日付不明"
                        Text(
                            text = formattedDate,
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                shadow = shadow // 影の有無を条件に応じて設定
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = ventCard.swipeCardContent)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // ここ2つアイコンのライン
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {

                        //投稿者画像
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(poster.photoURL),
                                contentDescription = "AccountIcon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .clickable {
                                        toAnotherUserPageView(poster)
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }

                        // 投稿者名前、いいね数
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${poster.name.take(5)}${if (poster.name.length > 5) "..." else ""}",
                                modifier = Modifier
                                    .clickable {
                                        toAnotherUserPageView(poster)
                                    }
                                    .padding(top = 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
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

                        }
                        Text(
                            text = "VS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        // 反論者名前、いいね数
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.weight(1f)

                        ) {
                                Text(
                                    text = "${debater.name.take(5)}${if (debater.name.length > 5) "..." else ""}",
                                    modifier = Modifier
                                        .clickable {
                                            toAnotherUserPageView(debater)
                                        }
                                        .padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
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
                        }

                        //反論者画像
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(debater.photoURL),
                                contentDescription = "AccountIcon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .clickable {
                                        toAnotherUserPageView(debater)
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                DebateFirstMessage(debate)
            }
        }
    }
}

@Composable
fun DebateFirstMessage(debate: Debate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {

        // 左側の縦棒
        Box(
            modifier = Modifier
                .width(4.dp) // 縦線の幅
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {

            if (debate.firstMessage.isNotEmpty()){
                Text(
                    text = debate.firstMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            debate.firstMessageImageURL?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "message Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (12).dp, y = (-12).dp),  // 位置調整
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Text(
                text = "反論メッセージ",
                color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp))
        }

    }
}