package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.DebatingVentCard
import kimsy.rr.vental.data.LikedVentCard
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class VentCardRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    val limitNum = 10L

    suspend fun saveVentCardToFireStore(
        ventCard: VentCard
    ){
        val query = db
            .collection("users")
            .document(ventCard.posterId)
            .collection("swipeCards")

        val docRef = query
            .document()

        val ventCardWithId = ventCard.copy(swipeCardId = docRef.id)

        docRef.set(ventCardWithId).await()
    }

    suspend fun fetchUserVentCards(
        userId: String,
        lastVisible: DocumentSnapshot? = null
    ): Pair<List<VentCard>, DocumentSnapshot?> {

        val query = db
            .collection("users")
            .document(userId)
            .collection("swipeCards")
            .orderBy("swipeCardCreatedDateTime", Query.Direction.DESCENDING)

        val querySnapshot = if (lastVisible == null) {
            query.limit(limitNum).get().await()
        } else {
            query.startAfter(lastVisible).limit(limitNum).get().await()
        }

        if (querySnapshot.isEmpty) {
            // データがない場合、空リストとnullを返す
            return Pair(emptyList(), null)
        } else {
            val newLastVisible = querySnapshot.documents.lastOrNull()

            val ventCards = querySnapshot.documents.mapNotNull { document ->
                document.toObject(VentCard::class.java)?.copy(
                    swipeCardCreatedDateTime = document.getTimestamp("swipeCardCreatedDateTime")?.toDate()
                )?: return Pair(emptyList(), null)
            }
            return Pair(ventCards, newLastVisible)
        }
    }

    //TODO
suspend fun getVentCardItems(
    userId: String,
    likedVentCard: List<LikedVentCard>,
    debatingVentCard: List<DebatingVentCard>,
    lastVisible: DocumentSnapshot? = null
): Pair<List<VentCardItem>, DocumentSnapshot?> {
        val startTime = System.currentTimeMillis()

        val query = db
            .collectionGroup("swipeCards")
            .whereNotEqualTo("posterId", userId)
            .whereLessThan("debateCount", 3)
            .orderBy("swipeCardCreatedDateTime", Query.Direction.DESCENDING)

        val querySnapshot = if (lastVisible == null) {
            query.limit(limitNum).get().await()
        } else {
            query.startAfter(lastVisible).limit(limitNum).get().await()
        }

        if (querySnapshot.isEmpty) {
            // データがない場合、空リストとnullを返す
            return Pair(emptyList(), null)
        }

        val newLastVisible = querySnapshot.documents.lastOrNull()

        Log.d("TAG", "ventCards: $querySnapshot size: ${querySnapshot.size()}")

        val likedVentCardIds = likedVentCard.map { it.ventCardId }
        val debatingVentCardIds = debatingVentCard.map { it.swipeCardId }

        val ventCardItems = querySnapshot.documents.mapNotNull { document ->

            val ventCard = document.toObject(VentCard::class.java)?.copy(
                swipeCardId = document.reference.id
            )

            if (ventCard == null || likedVentCardIds.contains(ventCard.swipeCardId) || debatingVentCardIds.contains(ventCard.swipeCardId)) {
                return@mapNotNull null
            }

            val parentReference = document.reference.parent.parent

            if (parentReference != null) {
                val parentDocument = parentReference.get().await()
                val poster = parentDocument.toObject(User::class.java)!!
                VentCardItem(
                    ventCard = ventCard,
                    poster = poster
                )
            } else {
                null
            }

        }

        //いいねしたものや参加中のものを除いてnullになってしまったら次の項目を取得する
        if (ventCardItems.isEmpty() && newLastVisible != null) {
            return getVentCardItems(userId, likedVentCard, debatingVentCard, newLastVisible)
        }

        val endTime = System.currentTimeMillis()
        Log.d("Performance", "Firestore クエリ時間: ${endTime - startTime}ms")

        return Pair(ventCardItems, newLastVisible)
}

    suspend fun likeVentCard(userId: String, posterId: String, ventCardId: String){
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

        docRefOnSwipeCard
            .update("likeCount", FieldValue.increment(1)).await()
    }

    suspend fun disLikeVentCard(userId: String, ventCardId: String): Resource<Unit> {
        // TODO
        return Resource.success(Unit)
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
        val docRef = db
            .collection("users")
            .document(userId)
            .collection("likedSwipeCards")

        val likedVentCardsSnapshot = docRef
            .get()
            .await()

        val result = likedVentCardsSnapshot.documents.mapNotNull {document->
            document.toObject(LikedVentCard::class.java)
        }
        return result
    }

    suspend fun fetchDebatingVentCardIds(userId: String): List<DebatingVentCard> {
        val docRef = db
            .collection("users")
            .document(userId)
            .collection("debatingSwipeCards")

        val debatingVentCardsSnapshot = docRef
            .get()
            .await()

        val result = debatingVentCardsSnapshot.documents.mapNotNull { document ->
            document.toObject(DebatingVentCard::class.java)
        }
        return result
    }

    suspend fun fetchVentCard(posterId: String ,ventCardId: String): VentCard {
                val docRef = db
                    .collection("users")
                    .document(posterId)
                    .collection("swipeCards")
                    .document(ventCardId)

                val ventCardSnapshot = docRef.get().await()

                val ventCard = ventCardSnapshot.toObject(VentCard::class.java)
                    ?.copy(
                    swipeCardCreatedDateTime = ventCardSnapshot.getTimestamp("swipeCardCreatedDateTime")!!.toDate()
                )
                return ventCard?: throw NoSuchElementException("VentCard が見つかりません: posterId=$posterId, ventCardId=$ventCardId")
    }

    suspend fun updateReportFlag(
        swipeCardId: String,
        posterId: String
    ){
        val docRef = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(swipeCardId)

        docRef.update("swipeCardReportFlag", true).await()
    }

    suspend fun updateDeletionRequestFlag(
        swipeCardId: String,
        posterId: String
    ) {
        val docRef = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(swipeCardId)

        docRef.update("swipeCardDeletionRequestFlag", true).await()
    }

}