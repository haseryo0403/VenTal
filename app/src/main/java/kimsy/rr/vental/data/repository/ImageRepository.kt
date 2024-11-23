package kimsy.rr.vental.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kimsy.rr.vental.MainActivity
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val storageRef: StorageReference
) {

    @SuppressLint("SuspiciousIndentation", "Recycle")
    suspend fun saveImageToStorage(
        uri: Uri,
        context: Context
    ): Result<String> {
        return try {

            val originalFileName  = getFileName(context, uri) ?: "image"
            val uniqueFileName = "${UUID.randomUUID()}_$originalFileName"

            Log.d("TAG", "${storageRef.child("images/$uniqueFileName")}")
            Log.d("TAG","uri is: $uri")

            withTimeout(10000L){
                storageRef.child("images/$uniqueFileName").putFile(uri).await()

                val downloadUrl = storageRef.child("images/$uniqueFileName").downloadUrl.await().toString()
                Log.d("IRepo", "URL get success: $downloadUrl")

                Result.success(downloadUrl)
            }
        } catch (e: TimeoutException) {
            Log.e("ImageRepository", "Failed to save image: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Failed to save image: ${e.message}", e)
            Result.failure(e)
        }
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

    suspend fun saveSwipeCardImageToFireStore(
        imageUrl: String
    ){
        db.collection("swipeCardImages").document().set(imageUrl).await()
    }
}


