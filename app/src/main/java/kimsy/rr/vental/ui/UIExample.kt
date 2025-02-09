
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R


@Composable
fun FireScreen() {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 投稿者画像
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("https://www.example.com/poster_image.jpg"), // ハードコーディングした画像URL
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                // 投稿者名前、いいね数
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp) // 間隔を詰める
                ) {
                    Text(
                        text = "Poster Name", // ハードコーディングした名前
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp) // アイコンとテキストの間隔を縮める
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "heart",
                            tint = Color.Red // ハードコーディングした色
                        )
                        Text(text = "123") // ハードコーディングしたいいね数
                    }
                }

                Text(
                    text = "VS",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp) // VSの間隔を調整
                )

                // 反論者名前、いいね数
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp) // 間隔を詰める
                ) {
                    Text(
                        text = "Debater Name", // ハードコーディングした名前
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp) // アイコンとテキストの間隔を縮める
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "heart",
                            tint = Color.Gray // ハードコーディングした色
                        )
                        Text(text = "45") // ハードコーディングしたいいね数
                    }
                }

                // 反論者画像
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("https://www.example.com/debater_image.jpg"), // ハードコーディングした画像URL
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

@Composable
fun MessageCard(message: Message, selectedVotes: MutableState<MutableMap<Int, String>>) {
    val painter = rememberAsyncImagePainter(model = message.content)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF3A1F1F))
            .clip(RoundedCornerShape(12.dp))
    ) {
        when (message.type) {
            "text" -> {
                Text(
                    text = message.content,
                    style = TextStyle(color = Color.White, fontSize = 14.sp)
                )
            }
            "image" -> {
                Image(
                    painter = painter,
                    contentDescription = "投稿の画像",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            "both" -> {
                Column {
                    Text(
                        text = message.content,
                        style = TextStyle(color = Color.White, fontSize = 14.sp)
                    )
                    Image(
                        painter = painter,
                        contentDescription = "投稿の補足画像",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 投票機能
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VoteButton(
                index = 0,
                voteType = "poster",
                selectedVotes = selectedVotes
            )
            VoteButton(
                index = 0,
                voteType = "opponent",
                selectedVotes = selectedVotes
            )
        }
    }
}

@Composable
fun VoteButton(index: Int, voteType: String, selectedVotes: MutableState<MutableMap<Int, String>>) {
    val isSelected = selectedVotes.value[index] == voteType
    val color = if (isSelected) Color(0xFFFF4242) else Color.Gray

    Row(
        modifier = Modifier
            .clickable {
                selectedVotes.value[index] = voteType
            }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = color
        )
        Text(
            text = "45", // 投票数
            color = color,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

//data class Message(val type: String, val content: String)





@Composable
fun SwipeCardScreen() {
    // データや状態の準備
    val tags = listOf("#タグ1", "#タグ2")
    val messages = listOf(
        Message(type = "text", content = "スワイプカードの内容がここに表示されます。"),
        Message(type = "image", content = "https://example.com/image.jpg")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F7)) // 背景色
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        items(5) { index ->
            Card(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .clickable { /* クリックアクション */ },
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = "https://example.com/image.jpg"),
//                            rememberImagePainter(),
                            contentDescription = "投稿画像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // タグ表示
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            tags.forEach {
                                TagItem(tag = it)
                            }
                        }
                        // 投稿者情報
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text("投稿者名", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("2時間前", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // カードの内容
                    Text(
                        text = "スワイプカードの内容がここに表示されます。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // メッセージ
                    MessageCard(message = messages[index % 2])
                }
            }
        }
    }
}

@Composable
fun TagItem(tag: String) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(Color(0xFF4A1D7F), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MessageCard(message: Message) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF8F0EF)
    ) {
        when (message.type) {
            "text" -> {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            "image" -> {
                Image(
                    painter = rememberAsyncImagePainter(model = message.content),
//                    rememberImagePainter(),
                    contentDescription = "反論の画像",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {}
        }
    }
}

// メッセージデータのクラス
data class Message(val type: String, val content: String)


//@Composable
//fun TryScreen() {
//    val loading = remember { mutableStateOf(true) }
//    val activeTab = remember { mutableStateOf(0) }
//    val selectedVotes = remember { mutableStateListOf<String?>() }
//
//    // Dummy tab names
//    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
//
//    // Dummy messages
//    val messages = listOf(
//        Message(type = "text", content = "Message 1"),
//        Message(type = "image", content = "image_url_1"),
//        Message(type = "both", content = MessageContent("Text with Image", "image_url_2"))
//    )
//
//    LaunchedEffect(Unit) {
//        loading.value = false
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFFF8F7))
//    ) {
//        // Header
//        TopAppBar(
//            title = {
//                Text("VENTAL", color = Color(0xFF972529), fontWeight = FontWeight.Bold)
//            },
//            navigationIcon = null,
//            actions = {
//                tabs.forEachIndexed { index, tab ->
//                    Button(
//                        onClick = { activeTab.value = index },
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = if (activeTab.value == index) Color(0xFFFFF8F7) else Color.Transparent
//                        ),
//                        contentPadding = PaddingValues(16.dp)
//                    ) {
//                        Text(tab, color = if (activeTab.value == index) Color(0xFF972529) else Color(0xFF584140))
//                    }
//                }
//            },
//            backgroundColor = Color.White,
//            elevation = 4.dp
//        )
//
//        // Main content
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            if (loading.value) {
//                // Loading UI
//            } else {
//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(3),
//                    contentPadding = PaddingValues(8.dp),
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(5) { index ->
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                                .shadow(4.dp)
//                                .clickable {},
//                            shape = RoundedCornerShape(16.dp)
//                        ) {
//                            Column {
//                                // Tag section
//                                Row(
//                                    modifier = Modifier
//                                        .padding(8.dp)
//                                        .fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    Text("#タグ1", color = Color.White, modifier = Modifier
//                                        .background(Color(0xFF4A1D7F))
//                                        .padding(4.dp))
//                                    Text("#タグ2", color = Color.White, modifier = Modifier
//                                        .background(Color(0xFF4A1D7F))
//                                        .padding(4.dp))
//                                }
//
//                                // Image Section
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .aspectRatio(4f / 3f)
//                                ) {
//                                    Image(painter = painterResource(id = R.drawable.aston_martin), contentDescription = "投稿画像", modifier = Modifier.fillMaxSize())
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .align(Alignment.BottomStart)
//                                            .background(Color.Black.copy(alpha = 0.5f))
//                                            .padding(8.dp)
//                                    ) {
//                                        // Post information
//                                        Row(verticalAlignment = Alignment.CenterVertically) {
//                                            Image(painter = painterResource(id = R.drawable.baseline_account_circle_24), contentDescription = "投稿者のプロフィール画像", modifier = Modifier
//                                                .size(40.dp)
//                                                .clip(CircleShape))
//                                            Spacer(modifier = Modifier.width(8.dp))
//                                            Column {
//                                                Text("投稿者名", color = Color.White)
//                                                Text("2時間前", color = Color.White.copy(alpha = 0.8f))
//                                            }
//                                        }
//                                    }
//                                }
//
//                                // Content description
//                                Text(
//                                    text = "スワイプカードの内容がここに表示されます。",
//                                    color = Color(0xFF251918),
//                                    modifier = Modifier.padding(16.dp)
//                                )
//
//                                // Votes section
//                                Row(modifier = Modifier.fillMaxWidth()) {
//                                    Button(
//                                        onClick = { /* Handle vote */ },
//                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4A1D7F)),
//                                        modifier = Modifier.weight(1f)
//                                    ) {
//                                        Text("VS", color = Color.White)
//                                    }
//                                    Button(
//                                        onClick = { /* Handle vote */ },
//                                        modifier = Modifier.weight(1f)
//                                    ) {
//                                        Text("反論者", color = Color(0xFF251918))
//                                    }
//                                }
//
//                                // Message section
//                                Box(modifier = Modifier.padding(8.dp)) {
//                                    val message = messages[index % 3]
//                                    when (message.type) {
//                                        "text" -> Text(text = message.content.toString(), color = Color(0xFF584140))
//                                        "image" -> Image(painter = painterResource(id = R.drawable.aston_martin), contentDescription = "反論の画像")
//                                        "both" -> {
//                                            val content = message.content as MessageContent
//                                            Column {
//                                                Text(text = content.text, color = Color(0xFF584140))
//                                                Image(painter = painterResource(id = R.drawable.aston_martin), contentDescription = "反論の補足画像")
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Bottom navigation
//        BottomNavigation(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            backgroundColor = Color.White,
//            elevation = 8.dp
//        ) {
//            BottomNavigationItem(
//                selected = false,
//                onClick = {},
//                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
//            )
//            BottomNavigationItem(
//                selected = false,
//                onClick = {},
//                icon = { Icon(Icons.Filled.Email, contentDescription = "Explore") }
//            )
//            BottomNavigationItem(
//                selected = false,
//                onClick = {},
//                icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") }
//            )
//            BottomNavigationItem(
//                selected = false,
//                onClick = {},
//                icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
//            )
//        }
//    }
//}

//data class Message(val type: String, val content: Any)
//data class MessageContent(val text: String, val image: String)


//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun TimeLinePrev(){
//    TimeLineView()
//}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)@Composable
fun DefaultPreview() {
//    TryScreen()
//    SwipeCardScreen()
    FireScreen()
}


//はい、テキストとハートボタンの色を明るく調整します。
//```main:javascript
//function MainComponent() {
//    const [loading, setLoading] = React.useState(true);
//    const [selectedVotes, setSelectedVotes] = React.useState({});
//    const [messages] = React.useState([
//        { type: "text", content: "テキストメッセージの例です。" },
//        { type: "image", content: "/example-image.jpg" },
//        {
//            type: "both",
//            content: {
//            text: "画像付きメッセージの例です。",
//            image: "/example-image.jpg",
//        },
//        },
//    ]);
//
//    useEffect(() => {
//        setLoading(false);
//    }, []);
//
//    const handleVote = (index, type) => {
//        setSelectedVotes((prev) => ({
//            ...prev,
//            [index]: type,
//        }));
//    };
//    const renderMessage = (message) => {
//        switch (message.type) {
//            case "text":
//            return <p className="text-gray-200 text-sm">{message.content}</p>;
//            case "image":
//            return (
//            <img
//            src={message.content}
//            alt="投稿の画像"
//            className="w-full h-48 object-cover rounded-lg"
//            />
//            );
//            case "both":
//            return (
//            <div className="space-y-2">
//            <p className="text-gray-200 text-sm">{message.content.text}</p>
//            <img
//            src={message.content.image}
//            alt="投稿の補足画像"
//            className="w-full h-48 object-cover rounded-lg"
//            />
//            </div>
//            );
//            default:
//            return null;
//        }
//    };
//
//    return (
//    <div className="min-h-screen bg-[#2a0f0f] text-white">
//    <header className="fixed top-0 left-0 right-0 z-50 bg-[#2a0f0f] border-b-2 border-[#ff6b6b]">
//    <div className="max-w-5xl mx-auto">
//    <div className="flex items-center px-4 h-16">
//    <h1 className="text-3xl font-extrabold mr-8 text-[#ff4242]">
//    FLAME<span className="text-[#ff6b6b]">WAR</span>
//    </h1>
//    </div>
//    </div>
//    </header>
//    <main className="max-w-5xl mx-auto px-4 pt-24 pb-24">
//    {loading ? (
//        <div className="flex justify-center items-center h-64">
//        <div className="text-4xl">🔥</div>
//        </div>
//        ) : (
//        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
//        {[...Array.from({ length: 5 })].map((_, index) => (
//            <div
//            key={index}
//            className="relative bg-[#3a1f1f] rounded-2xl shadow-lg hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer hover:-translate-y-1 group"
//            >
//            <div className="absolute top-4 right-4 z-10 flex gap-2">
//            <span className="bg-[#972529] text-white px-3 py-1 rounded-full text-xs">
//            #タグ1
//            </span>
//            <span className="bg-[#972529] text-white px-3 py-1 rounded-full text-xs">
//            #タグ2
//            </span>
//            </div>
//            <div className="aspect-[4/3] relative">
//            <img
//            src="/example-image.jpg"
//            alt="投稿画像"
//            className="w-full h-full object-cover"
//            />
//            <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/50 to-transparent p-4">
//            <div className="flex items-center gap-3">
//            <div className="w-10 h-10 rounded-full bg-white overflow-hidden border-2 border-[#972529]">
//            <img
//            src="/default-avatar.png"
//            alt="投稿者のプロフィール画像"
//            className="w-full h-full object-cover"
//            />
//            </div>
//            <div>
//            <div className="font-medium text-white text-sm">
//            投稿者名
//            </div>
//            <div className="text-xs text-white/80">2時間前</div>
//            </div>
//            </div>
//            </div>
//            </div>
//            <div className="p-6">
//            <div className="flex flex-col gap-4">
//            {renderMessage(messages[index % 3])}
//            <div className="bg-[#4a2f2f] p-4 rounded-xl transform transition-all duration-300 hover:scale-[1.02]">
//            <div className="flex items-center justify-between">
//            <div className="flex items-center gap-3">
//            <button
//            onClick={() => handleVote(index, "poster")}
//            className={`flex items-center gap-1 ${
//                selectedVotes[index] === "poster"
//                ? "text-[#ff4242]"
//                : "text-gray-300 hover:text-[#ff4242]"
//            }`}
//            disabled={
//                selectedVotes[index] &&
//                        selectedVotes[index] !== "poster"
//            }
//            >
//            <i
//            className={`${selectedVotes[index] === "poster" ? "fas" : "far"} fa-heart`}
//            ></i>
//            <span className="text-sm">45</span>
//            </button>
//            </div>
//            <div className="relative px-6 py-2">
//            <div className="text-[#ff4242] font-bold text-lg">
//            VS
//            </div>
//            </div>
//            <div className="flex items-center gap-3">
//            <button
//            onClick={() => handleVote(index, "opponent")}
//            className={`flex items-center gap-1 ${
//                selectedVotes[index] === "opponent"
//                ? "text-[#ff4242]"
//                : "text-gray-300 hover:text-[#ff4242]"
//            }`}
//            disabled={
//                selectedVotes[index] &&
//                        selectedVotes[index] !== "opponent"
//            }
//            >
//            <i
//            className={`${selectedVotes[index] === "opponent" ? "fas" : "far"} fa-heart`}
//            ></i>
//            <span className="text-sm">38</span>
//            </button>
//            </div>
//            </div>
//            </div>
//            </div>
//            </div>
//            </div>
//            ))}
//        </div>
//        )}
//    </main>
//    <nav className="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-[#3a1f1f] rounded-full shadow-lg px-2">
//    <div className="flex items-center h-14">
//    <button className="px-6 text-[#ff4242]">
//    <i className="fas fa-home text-xl"></i>
//    </button>
//    <button className="px-6 text-gray-400 hover:text-[#ff4242] transition-colors">
//    <i className="fas fa-compass text-xl"></i>
//    </button>
//    <button className="px-6 text-gray-400 hover:text-[#ff4242] transition-colors">
//    <i className="fas fa-bell text-xl"></i>
//    </button>
//    <button className="px-6 text-gray-400 hover:text-[#ff4242] transition-colors">
//    <i className="fas fa-user text-xl"></i>
//    </button>
//    </div>
//    </nav>
//    </div>
//    );
//}
//```