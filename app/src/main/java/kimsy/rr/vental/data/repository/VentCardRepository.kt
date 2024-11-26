package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kimsy.rr.vental.data.DebatingVentCard
import kimsy.rr.vental.data.LikedVentCard
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
import okhttp3.internal.wait
import javax.inject.Inject
import java.util.Date


class VentCardRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun saveVentCardToFireStore(
        ventCard: VentCard
    ): Result<Unit>{
        return try {
            withTimeout(10000L){
                db
                    .collection("users")
                    .document(ventCard.posterId)
                    .collection("swipeCards")
                    .add(ventCard)
                    .await()
                Result.success(Unit)
            }
        } catch (e : Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVentCardsWithUser(
        userId: String,
        likedVentCard: List<LikedVentCard>,
        debatingVentCard: List<DebatingVentCard>,
        lastVisible: DocumentSnapshot? = null
    ): Result<Pair<List<VentCardWithUser>, DocumentSnapshot?>> = try {
        val query = db
            .collectionGroup("swipeCards")
            .whereNotEqualTo("posterId", userId)
            .whereLessThan("debateCount", 3)
            .orderBy("swipeCardCreatedDateTime", Query.Direction.DESCENDING)

        val querySnapshot = if (lastVisible == null) {
            query.limit(10).get().await()
        } else {
            query.startAfter(lastVisible).limit(10).get().await()
        }

        if (querySnapshot.isEmpty) {
            //TODO　ここでDBから取得したものがnullならreturnするとかでいいんじゃないだろうか
//            Result.success(Pair(null, null))// たぶんもっと良い方法ある
        }

        val newLastVisible = querySnapshot.documents.lastOrNull()

        Log.d("TAG", "ventCards: $querySnapshot size: ${querySnapshot.size()}")

        val likedVentCarIds = likedVentCard.map { it.ventCardId }
        val debatingVentCardIds = debatingVentCard.map { it.swipeCardId }

        val ventCardsWithUser = querySnapshot.documents.mapNotNull { document ->
            val ventCard = document.toObject(VentCardWithUser::class.java)?.copy(
                swipeCardId = document.reference.id
            )

            if (ventCard == null || likedVentCarIds.contains(ventCard.swipeCardId) || debatingVentCardIds.contains(ventCard.swipeCardId)){
                return@mapNotNull null
            }
            val parentReference = document.reference.parent.parent

            if (parentReference != null) {
                try {
                    val parentDocument = parentReference.get().await()
                    val name = parentDocument.getString("name")?: throw IllegalArgumentException("Poster Name is missing")
                    val photoURL = parentDocument.getString("photoURL")?: throw IllegalArgumentException("Poster Image is missing")

                    //TODO　ここでうえでインスタンス化したやつについかすれば
                    // Firestoreから直接toObjectを使ってVentCardWithUserを作成
                    val ventCardWithUser = document.toObject(VentCardWithUser::class.java)?.copy(
                        swipeCardId = document.reference.id,
                        posterName = name,
                        posterImageURL = photoURL,
                        swipeCardCreatedDateTime = document.getTimestamp("swipeCardCreatedDateTime")!!.toDate()
                    )

                    ventCardWithUser

                } catch (e: Exception) {
                    Log.w("Firestore", "Error getting parent document", e)
                    null
                }
            } else {
                null
            }
        }
        Result.success(Pair(ventCardsWithUser, newLastVisible))
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun likeVentCard(userId: String, posterId: String, ventCardId: String){

        Log.d("VCR", "likeVentCard called")

        val likeData = mapOf(
            "ventCardId" to ventCardId,
            "likedDate" to FieldValue.serverTimestamp()
        )
        val docRefOnUser = db
            .collection("users")
            .document(userId)
            .collection("likedSwipeCards")
            .document(ventCardId)

        val docRefOnSwipeCard = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(ventCardId)

        docRefOnUser
            .set(likeData)
            .await()

        // add to like list
        docRefOnSwipeCard
            .update("likeCount", FieldValue.increment(1)).await()
        //increment sc like
    }

    suspend fun disLikeVentCard(userId: String, ventCardId: String) {
        // TODO
    }

    suspend fun checkIfLiked(userId: String, ventCardId: String): Boolean {
        val docRef = db
            .collection("users")
            .document(userId)
            .collection("likedSwipeCards")
            .document(ventCardId)

        val docSnapshot = docRef.get().await()
        return docSnapshot.exists()
    }

    suspend fun fetchLikedVentCardIds(userId: String): List<LikedVentCard>{
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(userId)
                    .collection("likedSwipeCards")

                val likedVentCardsSnapshot = docRef
                    .get()
                    .await()

                likedVentCardsSnapshot.documents.mapNotNull {document->
                    document.toObject(LikedVentCard::class.java)
                }
            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching liked vent cards", e)
            emptyList()  // エラー時は空のリストを返す
        }
    }
    suspend fun fetchDebatingVentCardIds(userId: String): List<DebatingVentCard>{
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(userId)
                    .collection("debatingSwipeCards")

                val debatingVentCardsSnapshot = docRef
                    .get()
                    .await()

                debatingVentCardsSnapshot.documents.mapNotNull {document->
                    document.toObject(DebatingVentCard::class.java)
                }
            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching debating vent cards", e)
            emptyList()  // エラー時は空のリストを返す
        }
    }

    suspend fun fetchVentCard(posterId: String ,ventCardId: String): VentCard? {
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(posterId)
                    .collection("swipeCards")
                    .document(ventCardId)

                val ventCardSnapshot = docRef.get().await()

                ventCardSnapshot.toObject(VentCard::class.java)
            }
        } catch (e: Exception) {
            Log.e("VCR", "error")
            null
        }
    }

}