package kimsy.rr.vental.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val storageRef: StorageReference
) {

    @SuppressLint("SuspiciousIndentation", "Recycle")
    suspend fun saveImageToStorage(
        uri: Uri,
        context: Context
    ): String {
        val originalFileName = getFileName(context, uri) ?: "image"
        val uniqueFileName = "${UUID.randomUUID()}_$originalFileName"

        storageRef.child("images/$uniqueFileName").putFile(uri).await()

        return storageRef.child("images/$uniqueFileName").downloadUrl.await().toString()
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }
}


