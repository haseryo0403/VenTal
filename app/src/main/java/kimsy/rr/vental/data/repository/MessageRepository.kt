package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun fetchMessages (posterId: String, swipeCardId: String, debateId: String): Resource<List<Message>> {
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
                    document.toObject(Message::class.java)!!.copy(
                        sentDatetime = document.getTimestamp("sentDatetime")!!.toDate()
                    )
                }
                Resource.success(messages)
            }
        } catch (e: IOException) {
            Resource.failure(e.message)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }

    suspend fun observeMessages (posterId: String, swipeCardId: String, debateId: String): Flow<List<Message>> = callbackFlow {
        val docRef = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(swipeCardId)
            .collection("debates")
            .document(debateId)
            .collection("messages")
            .orderBy("sentDatetime", Query.Direction.ASCENDING)

        val subscription = docRef.addSnapshotListener{querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val messages = querySnapshot.documents.mapNotNull {document->
                    val message = document.toObject(Message::class.java)
                    val sentDatetime = document.getTimestamp("sentDatetime")?.toDate()

                    if (message != null && sentDatetime != null) {
                        message.copy(sentDatetime = sentDatetime)
                    } else {
                        null
                    }
                }
                trySend(messages)
            }
        }
        awaitClose{ subscription.remove() }
    }




}