package kimsy.rr.vental.ViewModel

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.R
import kimsy.rr.vental.data.ImageRepository
import kimsy.rr.vental.data.ImageUtils
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class VentCardCreationViewModel @Inject constructor(
    private val mainViewModel: MainViewModel,
    private val imageUtils: ImageUtils,
    private val imageRepository: ImageRepository,
    private val ventCardRepository: VentCardRepository
): ViewModel(){
    init {
        Log.d("VentCardCreationViewModel", "ViewModel instance created: $this")
    }

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var content by mutableStateOf<String>("")
    var tags =  mutableStateListOf<String>()
    var isSent by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun handleImagePermissionAndSelection(
        context: Context
    ) {
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {permission ->
                if (!permission){
                    // ask for permission
                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                    if (rationalRequired){
                        Toast.makeText(
                            context,
                            "この機能を使用するには画像アクセス権が必要です",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "この機能を使用するには画像アクセス権が必要です。端末の設定で許可をしてください。",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri: Uri? -> uri?.let {
                if(uri != null){
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    selectedImageUri = uri
                } else {
                    Log.d("PhotoPicker", "No media selected")
                } } }
        )
        
        IconButton(
            onClick = {
                if (imageUtils.hasImagePermission(context)) {
                    // パーミッションがある場合、画像選択を実行
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    // パーミッションリクエストを実行
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_image_24),
                modifier = Modifier.size(40.dp),
                contentDescription = "add Image"
            )
        }
    }

    fun startSavingVentCard(
        context: Context,
        onComplete: () -> Unit,
        onError: () -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            val result = saveVentCard(context)
            result
                .onSuccess { onComplete() }
                .onFailure{
                    Log.d("VCCVM", "onFailure executed")
                    onError()
                }
        }
    }

    suspend fun saveVentCard(context: Context):Result<Unit> {
        return if (selectedImageUri == null){
            Log.d("VCCVM", "saveVentCardWithoutImage executed")

            saveVentCardWithoutImage()
        } else {
            Log.d("VCCVM", " saveVentCardWithImage executed")

            saveVentCardWithImage(context)
        }
    }
    private suspend fun saveVentCardWithoutImage(): Result<Unit> {
        val ventCard = VentCard(
            userId = mainViewModel.currentUser.value?.uid.toString(),
            swipeCardContent = content,
            swipeCardImageURL = "",
            tags = tags
        )
        return ventCardRepository.saveVentCardToFireStore(ventCard)
            .onSuccess {
                Log.d("VCCVM", " saveVentCardToFireStore success")
                // 成功時の後処理
                isSent = true
                content = ""
                selectedImageUri = null
                tags.clear()
            }
    }
    private suspend fun saveVentCardWithImage(context: Context): Result<Unit> {
        // 画像を保存してからVentCardを保存する
        return imageRepository.saveImageToStorage(selectedImageUri!!, context)
            .mapCatching { downloadUrl ->
                Log.d("VCCVM", " saveImageToStorage success")
                // 画像保存が成功したので、次にVentCardを保存する
                val ventCard = VentCard(
                    userId = mainViewModel.currentUser.value?.uid.toString(),
                    swipeCardContent = content,
                    swipeCardImageURL = downloadUrl,
                    tags = tags
                )
                // VentCardの保存処理
                ventCardRepository.saveVentCardToFireStore(ventCard).getOrThrow() // エラーがあれば例外を投げる
            }.onSuccess {
                Log.d("VCCVM", " saveVentCardToFireStore success")

                // 全て成功した場合、リセットなどの処理を行う
                isSent = true
                content = ""
                selectedImageUri = null
                tags.clear()
            }
    }



//    suspend fun saveVentCard(context: Context): Result<Unit>{
//        Log.d("VCCVM", "call repository")
//
//        if(selectedImageUri == null){
//                // 成功時の処理
//                // ダウンロードURLをUIに渡したり、DBに保存するなど
//                Log.d("saveImage","Success")
//                val ventCard = VentCard(
//                    userId = mainViewModel.currentUser.value?.uid.toString(),
//                    swipeCardContent = content,
//                    swipeCardImageURL = "",
//                    tags = tags
//                )
//                val saveVentCardResult = ventCardRepository.saveVentCardToFireStore(ventCard)
//                saveVentCardResult.onSuccess {
//                    isSent = true
//                    content = ""
//                    selectedImageUri = null
//                    tags.clear()
//                }.onFailure { e ->
//                    //TODO スワイプカード保存失敗時の処理
//                    //toastか画面を戻るか
//                    //toastで戻るボタンとかでもありかも
//
//                }
//        } else {
//            val saveImageResult = imageRepository.saveImageToStorage(selectedImageUri!!,context)
//            saveImageResult.onSuccess {downloadUrl ->
//                // 成功時の処理
//                // ダウンロードURLをUIに渡したり、DBに保存するなど
//                Log.d("saveImage","Success")
//                val ventCard = VentCard(
//                    userId = mainViewModel.currentUser.value?.uid.toString(),
//                    swipeCardContent = content,
//                    swipeCardImageURL = downloadUrl,
//                    tags = tags
//                )
//                val saveVentCardResult = ventCardRepository.saveVentCardToFireStore(ventCard)
//                saveVentCardResult.onSuccess {
//                    isSent = true
//                    content = ""
//                    selectedImageUri = null
//                    tags.clear()
//                }.onFailure { e ->
//                    //TODO スワイプカード保存失敗時の処理
//
//                }
//            }.onFailure { e ->
//                //TODO 画像保存失敗時の処理
//
//            }
//        }
//    }
}
