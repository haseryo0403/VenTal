package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kimsy.rr.vental.data.Message
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun sendMessage (posterId: String, swipeCardId: String, debateId: String, message: Message): Result<Unit>{
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(posterId)
                    .collection("swipeCards")
                    .document(swipeCardId)
                    .collection("debates")
                    .document(debateId)
                    .collection("messages")

                docRef.add(message)
                    .await()

                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMessages (posterId: String, swipeCardId: String, debateId: String):Result<List<Message>> {
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(posterId)
                    .collection("swipeCards")
                    .document(swipeCardId)
                    .collection("debates")
                    .document(debateId)
                    .collection("messages")

                val querySnapshot = docRef.get().await()
                val messages = querySnapshot.documents.map {document->
                    document.toObject(Message::class.java)!!
                }
                Result.success(messages)
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}