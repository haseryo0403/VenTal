package kimsy.rr.vental.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import kimsy.rr.vental.R

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRegisterScreen(

){
    var userName by remember{ mutableStateOf("")}
    var selectedAgeGroup by remember { mutableStateOf("年齢層を選択") }
    var selfIntroduction by remember{ mutableStateOf("")}
    val gender = listOf("男性", "女性", "その他")
    var selectedIndex by remember { mutableStateOf(2) }
    var expanded by remember { mutableStateOf(false) }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),

    ) {
        val textFieldModifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier.size(160.dp)
                ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_account_circle_24),
                    contentDescription = "profilePicture",
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "プロフィール写真",
                    color = Color.Blue, // テキスト色をアイコンに合わせて調整
                    fontSize = 16.sp,
                )
            }
        }

        // 「必須」のラベルを塗り潰しの四角形で表示
        Box(
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
                .padding(4.dp) ,// テキスト周りのパディング
        ) {
            Text(text = "必須", color = Color.White)
        }

        Text("ユーザーネームを入力してください")

        OutlinedTextField(
            value = userName,
            onValueChange = {userName = it},
            label = {Text("ニックネームOK")},
            modifier = textFieldModifier,
            singleLine =true
        )

        Row {
            Text("あなたの性別を選択してください（非公開）")
            // 「必須」のラベルを塗り潰しの四角形で表示
            Box(
                modifier = Modifier
                    .background(Color.Red, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
                    .padding(4.dp) ,// テキスト周りのパディング
            ) {
                Text(text = "必須", color = Color.Gray)
            }
        }



        SingleChoiceSegmentedButtonRow(
            modifier = textFieldModifier
        ) {
            gender.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = gender.size),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    modifier = Modifier
                        .height(56.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(label)
                }
            }
        }

        // 「必須」のラベルを塗り潰しの四角形で表示
        Box(
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)) // 塗り潰しの色
                .padding(4.dp) ,// テキスト周りのパディング
        ) {
            Text(text = "必須", color = Color.White)
        }


        Text("年齢層を選択してください（非公開）",modifier = Modifier.padding(bottom = 8.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .border(BorderStroke(1.dp, Color.Gray), shape = MaterialTheme.shapes.small)
            .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedAgeGroup, modifier = Modifier.weight(1f))

                // 矢印アイコンを表示
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand age group selection"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.TopStart)
                    .offset(y = 8.dp)

            ) {
                val ageGroups = listOf("10代", "20代", "30代", "40代", "50代以上")
                ageGroups.forEach { ageGroup ->
                    DropdownMenuItem(
                        text = { Text(ageGroup) },
                        onClick = {
                            selectedAgeGroup = ageGroup
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    )
                }
            }
        }

        Text("自己紹介文を入力してください", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        OutlinedTextField(
            value = selfIntroduction,
            onValueChange = {selfIntroduction = it},
            modifier = textFieldModifier.height(120.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(width = 200.dp, height = 60.dp)
                ) {
                Text("登録")
            }
        }

    }
}


@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun ProfilePrev(){
    ProfileRegisterScreen()
}


