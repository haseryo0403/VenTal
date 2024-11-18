package kimsy.rr.vental.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.data.ImageRepository
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentCardCreationViewModel @Inject constructor(
    private val authViewModel: AuthViewModel,
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
            posterId = authViewModel.currentUser.value?.uid.toString(),
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
                    posterId = authViewModel.currentUser.value?.uid.toString(),
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
}
