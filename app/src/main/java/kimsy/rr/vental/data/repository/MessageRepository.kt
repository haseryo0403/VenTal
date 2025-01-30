package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun sendMessage (posterId: String, swipeCardId: String, debateId: String, message: Message){
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