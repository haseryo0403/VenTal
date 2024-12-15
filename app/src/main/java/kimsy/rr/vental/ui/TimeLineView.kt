package kimsy.rr.vental.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.TimeLineViewModel
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard

@Composable
fun TimeLineView(
    timeLineViewModel: TimeLineViewModel = hiltViewModel()
){
    val timeLineItems = timeLineViewModel.timelineItems

    val hasFinishedLoadingAllItems = timeLineViewModel.hasFinishedLoadingAllItems

    LaunchedEffect(Unit) {
        timeLineViewModel.getTimeLineItems()
    }

    //とりあえずこれ。 TODO resource分岐追加
    when {
        timeLineItems.isNotEmpty() -> {
            LazyColumn(){
                items(timeLineItems) {item->
                    timeLineItem(
                        debate = item.debate,
                        ventCard = item.ventCard,
                        poster = item.poster,
                        debater = item.debater
                    )
                }
                if (!hasFinishedLoadingAllItems) {
                    item { LoadingIndicator(timeLineViewModel) }
                }
            }
        }
        else -> {
            Text(text = "items are null")
        }
    }

}
@Composable
fun LoadingIndicator(timeLineViewModel: TimeLineViewModel) {
    CircularProgressIndicator()

    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        timeLineViewModel.getTimeLineItems()
    }
}

@Composable
fun timeLineItem(debate: Debate, ventCard: VentCard, poster: User, debater: User) {
        Column(
            modifier = Modifier
                .clickable {
                    //TODO go to detail
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            //TODO go to user
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
                                    //TODO go to user
                                }
                        )
                        Text(text = ventCard.swipeCardCreatedDateTime.toString())
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

                }
            }
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
                                //TODO go to user
                            },
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = debater.name,
                        modifier = Modifier
                            .clickable {
                                //TODO go to user
                            }
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                        contentDescription = "haert")
                    Text(text = debate.debaterLikeCount.toString())
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                        contentDescription = "haert")
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
                                //TODO go to user
                            },
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = poster.name,
                        modifier = Modifier
                            .clickable {
                                //TODO go to user
                            }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Start
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
            }
        }
        Divider()
}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun TimeLinePrev(){
    TimeLineView()
}
