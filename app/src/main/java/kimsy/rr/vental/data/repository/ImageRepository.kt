package kimsy.rr.vental.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
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
    ): String {
        val originalFileName = getFileName(context, uri) ?: "image"
        val uniqueFileName = "${UUID.randomUUID()}_$originalFileName"

        storageRef.child("images/$uniqueFileName").putFile(uri).await()

        return storageRef.child("images/$uniqueFileName").downloadUrl.await().toString()
    }
//    @SuppressLint("SuspiciousIndentation", "Recycle")
//    suspend fun saveImageToStorages(
//        uri: Uri,
//        context: Context
//    ): Resource<String> {
//        return try {
//            withTimeout(10000L){
//                val originalFileName  = getFileName(context, uri) ?: "image"
//                val uniqueFileName = "${UUID.randomUUID()}_$originalFileName"
//
//                withTimeout(20000L){
//                    storageRef.child("images/$uniqueFileName").putFile(uri).await()
//
//                    val downloadUrl = storageRef.child("images/$uniqueFileName").downloadUrl.await().toString()
//
//                    Resource.success(downloadUrl)
//            }
//
//            }
//        } catch (e: IOException) {
//            Log.e("ImageRepository", "Failed to save image: ${e.message}", e)
//            Resource.failure(e.message)
//        } catch (e: Exception) {
//            Log.e("ImageRepository", "Failed to save image: ${e.message}", e)
//            Resource.failure(e.message)
//        }
//    }

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


