package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FollowRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    suspend fun observeFollowingUserIds(
        currentUserId: String
    ): Flow<List<String>> = callbackFlow {

        val docRef = db
            .collection("users")
            .document(currentUserId)
            .collection("following")

        val subscription = docRef.addSnapshotListener{ querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                val followingIds = querySnapshot.documents.map { document ->
                    document.id
                }
                trySend(followingIds)
            }
        }
        awaitClose{ subscription.remove() }
    }

    fun addUserIdToFollowingUserId(
        fromUserId:String,
        //TODO use data class??
        toUserId: String,
        transaction: Transaction
    ) {
        val query = db
            .collection("users")
            .document(fromUserId)
            .collection("following")
            .document(toUserId)

        val data = mapOf("followingUserId" to toUserId)

        transaction.set(query, data)
    }

    fun deleteUserIdFromFollowingUserId(
        fromUserId:String,
        toUserId: String,
        transaction: Transaction
    ) {
        val docRef = db
            .collection("users")
            .document(fromUserId)
            .collection("following")
            .document(toUserId)

        transaction.delete(docRef)
    }

    fun followerCountUp(
        userId: String,
        transaction: Transaction
    ) {
        val docRef = db
            .collection("users")
            .document(userId)

        transaction.update(docRef, "followerCount", FieldValue.increment(1))
    }

    fun followerCountDown(
        userId: String,
        transaction: Transaction
    ) {
        val docRef = db
            .collection("users")
            .document(userId)

        transaction.update(docRef, "followerCount", FieldValue.increment(-1))
    }

}