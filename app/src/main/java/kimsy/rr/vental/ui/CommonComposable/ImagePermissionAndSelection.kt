package kimsy.rr.vental.ui.CommonComposable

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.data.hasImagePermission


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ImagePermissionAndSelection(
    context: Context,
    modifier: Modifier,
    onImageSelected: (Uri?) -> Unit, // 画像選択時のコールバック
    iconContent: @Composable () -> Unit // アイコン部分をカスタマイズ可能に
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (!permissionGranted) {
            val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                context as MainActivity,
                Manifest.permission.READ_MEDIA_IMAGES
            )
            val message = if (rationaleRequired) {
                "この機能を使用するには画像アクセス権が必要です"
            } else {
                "この機能を使用するには画像アクセス権が必要です。端末の設定で許可をしてください。"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri) // 画像が選択されたときにコールバックを呼び出す
        } else {
        }
    }

    IconButton(
        onClick = {
            if (hasImagePermission(context)) {
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
        },
        modifier = modifier
    ) {
        iconContent()
    }
}
