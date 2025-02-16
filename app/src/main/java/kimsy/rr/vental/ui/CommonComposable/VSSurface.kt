package kimsy.rr.vental.ui.CommonComposable

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.viewModel.DebateViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun VSSurface(
    debateItem: DebateItem,
    sharedDebateViewModel: SharedDebateViewModel,
    debateViewModel: DebateViewModel,
    toAnotherUserPageView: (user: User) -> Unit
) {
    val debate = debateItem.debate
    val debater = debateItem.debater
    val poster = debateItem.poster

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

}