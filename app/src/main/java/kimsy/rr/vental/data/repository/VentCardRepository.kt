package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kimsy.rr.vental.data.DebatingVentCard
import kimsy.rr.vental.data.LikedVentCard
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


class VentCardRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun saveVentCardToFireStore(
        ventCard: VentCard
    ): Resource<Unit>{
        return try {
            withTimeout(10000L){
                val query = db
                    .collection("users")
                    .document(ventCard.posterId)
                    .collection("swipeCards")

                val docRef = query
                    .document()

                val ventCardWithId = ventCard.copy(swipeCardId = docRef.id)

                docRef.set(ventCardWithId).await()

                Resource.success(Unit)
            }
        } catch (e : Exception) {
            Resource.failure(e.message)
        }
    }

    suspend fun fetchUserVentCards(
        userId: String,
        lastVisible: DocumentSnapshot? = null
    ): Pair<List<VentCard>, DocumentSnapshot?> {

        val query = db
            .collection("users")
            .document(userId)
            .collection("swipeCards")

        val querySnapshot = if (lastVisible == null) {
            query.limit(10).get().await()
        } else {
            query.startAfter(lastVisible).limit(10).get().await()
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
suspend fun getVentCardsWithUser(
    userId: String,
    likedVentCard: List<LikedVentCard>,
    debatingVentCard: List<DebatingVentCard>,
    lastVisible: DocumentSnapshot? = null
): Resource<Pair<List<VentCardWithUser>, DocumentSnapshot?>> {
    return try {
        val startTime = System.currentTimeMillis()

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
            // データがない場合、空リストとnullを返す
            return Resource.success(Pair(emptyList(), null))
        }

        val newLastVisible = querySnapshot.documents.lastOrNull()

        Log.d("TAG", "ventCards: $querySnapshot size: ${querySnapshot.size()}")

        val likedVentCardIds = likedVentCard.map { it.ventCardId }
        val debatingVentCardIds = debatingVentCard.map { it.swipeCardId }

        //892ms
//        val ventCardsWithUser = generateVentCardItem(querySnapshot, likedVentCardIds, debatingVentCardIds)

        //792ms
        val ventCardsWithUser = querySnapshot.documents.mapNotNull { document ->


            val ventCard = document.toObject(VentCardWithUser::class.java)?.copy(
                swipeCardId = document.reference.id
            )

            if (ventCard == null || likedVentCardIds.contains(ventCard.swipeCardId) || debatingVentCardIds.contains(ventCard.swipeCardId)) {
                return@mapNotNull null
            }

            val parentReference = document.reference.parent.parent

            if (parentReference != null) {
                try {
                    val parentDocument = parentReference.get().await()
                    val name = parentDocument.getString("name") ?: return@mapNotNull null
                    val photoURL = parentDocument.getString("photoURL") ?: return@mapNotNull null

                    // Firestoreから直接toObjectを使ってVentCardWithUserを作成
                    document.toObject(VentCardWithUser::class.java)?.copy(
                        swipeCardId = document.reference.id,
                        posterName = name,
                        posterImageURL = photoURL,
                        swipeCardCreatedDateTime = document.getTimestamp("swipeCardCreatedDateTime")!!.toDate()
                    )
                } catch (e: Exception) {
                    Log.w("Firestore", "Error getting parent document", e)
                    null
                }
            } else {
                null
            }


        }

        if (ventCardsWithUser.isEmpty() && newLastVisible != null) {
            return getVentCardsWithUser(userId, likedVentCard, debatingVentCard, newLastVisible)
        }

        val endTime = System.currentTimeMillis()
        Log.d("Performance", "Firestore クエリ時間: ${endTime - startTime}ms")

        Resource.success(Pair(ventCardsWithUser, newLastVisible))
    } catch (e: Exception) {
        Resource.failure("Error fetching vent cards: ${e.message}")
    }
}

    private suspend fun generateVentCardItem(
        querySnapshot: QuerySnapshot,
        likedVentCardIds: List<String>,
        debatingVentCardIds: List<String>
    ):  List<VentCardWithUser>{
        return coroutineScope {
            querySnapshot.documents.mapNotNull { document ->
                val ventCard = document.toObject(VentCardWithUser::class.java)?.copy(
                    swipeCardId = document.reference.id
                )

                if (ventCard == null || likedVentCardIds.contains(ventCard.swipeCardId) || debatingVentCardIds.contains(ventCard.swipeCardId)) {
                    return@mapNotNull null
                }

                val parentReference = document.reference.parent.parent

                if (parentReference != null) {
                    try {
                        val parentDocument = parentReference.get().await()
                        val name = parentDocument.getString("name") ?: return@mapNotNull null
                        val photoURL = parentDocument.getString("photoURL") ?: return@mapNotNull null

                        // Firestoreから直接toObjectを使ってVentCardWithUserを作成
                        document.toObject(VentCardWithUser::class.java)?.copy(
                            swipeCardId = document.reference.id,
                            posterName = name,
                            posterImageURL = photoURL,
                            swipeCardCreatedDateTime = document.getTimestamp("swipeCardCreatedDateTime")!!.toDate()
                        )
                    } catch (e: Exception) {
                        Log.w("Firestore", "Error getting parent document", e)
                        null
                    }
                } else {
                    null
                }
            }
        }
    }



    suspend fun likeVentCard(userId: String, posterId: String, ventCardId: String): Resource<Unit>{
        return try {
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

            docRefOnSwipeCard
                .update("likeCount", FieldValue.increment(1)).await()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }

    suspend fun disLikeVentCard(userId: String, ventCardId: String): Resource<Unit> {
        // TODO
        return Resource.success(Unit)
    }

    suspend fun checkIfLiked(userId: String, ventCardId: String): Result<Boolean> {
        return try {
            val docRef = db
                .collection("users")
                .document(userId)
                .collection("likedSwipeCards")
                .document(ventCardId)

            val docSnapshot = docRef.get().await()
            val isLiked = docSnapshot.exists()
            Result.success(isLiked)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLikedVentCardIds(userId: String): Resource<List<LikedVentCard>>{
        return try {
            withTimeout(10000L) {
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
                Resource.success(result)
            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching liked vent cards", e)
            Resource.failure("Error fetching liked vent cards: ${e.message}")
        }
    }
    suspend fun fetchDebatingVentCardIds(userId: String): Resource<List<DebatingVentCard>> {
        return try {
            withTimeout(10000L) {
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
                Resource.success(result)
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching debating vent cards", e)
            Resource.failure("Error fetching debating vent cards: ${e.message}")
        }
    }

//    suspend fun fetchVentCard(posterId: String ,ventCardId: String): VentCard? {
//        return try {
//            withTimeout(10000L) {
//                val docRef = db
//                    .collection("users")
//                    .document(posterId)
//                    .collection("swipeCards")
//                    .document(ventCardId)
//
//                val ventCardSnapshot = docRef.get().await()
//
//                ventCardSnapshot.toObject(VentCard::class.java)
//            }
//        } catch (e: Exception) {
//            Log.e("VCR", "error")
//            null
//        }
//    }

    suspend fun fetchVentCard(posterId: String ,ventCardId: String): Resource<VentCard> {
        return try {
            withTimeout(10000L) {
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
                if (ventCard != null) {
                    Resource.success(ventCard)
                } else {
                    Resource.failure("カードが見つかりません")
                }
            }
        } catch (e: Exception) {
            Log.e("VCR", "error")
            Resource.failure(e.message)
        }
    }

}