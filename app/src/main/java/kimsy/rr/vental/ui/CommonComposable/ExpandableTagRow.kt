package kimsy.rr.vental.ui.CommonComposable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableTagRow(tags: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    // 1つ目のタグのみ表示
    if (tags.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tags.size > 1) {
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        if (expanded) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_chevron_right_24),
                                contentDescription = "close",
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.4f), shape = CircleShape)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_chevron_left_24),
                                contentDescription = "open",
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.4f), shape = CircleShape)
                            )
                        }
                    }
                }
            }

            FlowRow {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(4.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "#${tags.first().take(10)}${if (tags.first().length > 10) "..." else ""}", // 修正: ifの式を正しく記述
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }


                // 開いたときに残りのタグを表示
                if (expanded) {
                    tags.drop(1).forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .padding(4.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "#$tag",
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

            }
        }
    }
}