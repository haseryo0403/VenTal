package kimsy.rr.vental.ViewModel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.R
import kimsy.rr.vental.data.ImageRepository
import kimsy.rr.vental.data.ImageUtils
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class VentCardCreationViewModel @Inject constructor(
    private val mainViewModel: MainViewModel,
    private val imageUtils: ImageUtils,
    private val imageRepository: ImageRepository
): ViewModel(){
    init {
        Log.d("VentCardCreationViewModel", "ViewModel instance created: $this")
    }

    var selectedImageUri by mutableStateOf<Uri?>(null)

    var painter : AsyncImagePainter? = null

//    fun updateSelectedImage(uri: Uri){
//        selectedImageUri = uri.toFile()
//        imgBitmap = BitmapFactory.decodeFile(selectedImageUri!!.absolutePath)
//    }
fun updateSelectedImage(uri: Uri){
        selectedImageUri = uri

    }

//TODO 削除予定

//    // 権限確認処理
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    fun handleImagePermissionAndSelection(
//        context: Context,
//        requestPermissionLauncher: ActivityResultLauncher<String>,
//        pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
//    ) {
//        if (imageUtils.hasImagePermission(context)) {
//            // パーミッションがある場合、画像選択を実行
//            pickImage(pickMediaLauncher)
//        } else {
//            // パーミッションリクエストを実行
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
//            } else {
//                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//            }
//        }
//    }
//    private fun pickImage(pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>) {
//        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//    }

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
//                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    suspend fun saveImage(
        context: Context
    ){
        Log.d("VCCVM", "call repository")
        val result = selectedImageUri?.let {
            imageRepository.saveImageToStorage(it,context)

        }
            if (result != null) {
                result.onSuccess {downloadUrl ->
                    // 成功時の処理
                    // ダウンロードURLをUIに渡したり、DBに保存するなど
                    Log.d("saveImage","Success")
                    imageRepository.saveSwipeCardImageToFireStore(downloadUrl)

                }.onFailure { e ->
                    // 失敗時の処理
            }
        }
    }
}
