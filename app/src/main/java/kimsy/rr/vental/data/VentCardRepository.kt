package kimsy.rr.vental.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
import okhttp3.internal.wait
import javax.inject.Inject
import java.util.Date


class VentCardRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val storageRef: StorageReference
) {

    suspend fun saveVentCardToFireStore(
        ventCard: VentCard
    ): Result<Unit>{
        return try {
            withTimeout(10000L){
                db
                    .collection("users")
                    .document(ventCard.userId)
                    .collection("swipeCards")
                    .add(ventCard)
                    .await()
                Result.success(Unit)
            }
        } catch (e : Exception) {
            Result.failure(e)
        }
    }

//    suspend fun getVentCardsWithUser(): Result<List<VentCardWithUser>> = try {
//        val querySnapshot = db.collectionGroup("swipeCards").get().await()
//        Log.d("TAG", "ventCards: $querySnapshot")
//
//        val ventCardsWithUser = querySnapshot.documents.mapNotNull { document ->
//            val parentReference = document.reference.parent.parent
//            val ventCard = document.toObject(VentCard::class.java)
//
//            if (parentReference != null && ventCard != null) {
//                // 親ドキュメントを取得し、必要なフィールドを抽出
//                val parentDocumentSnapshot = parentReference.get().await()
//                val name = parentDocumentSnapshot.getString("name")
//                val photoURL = parentDocumentSnapshot.getString("photoURL")
//
//                // VentCardとユーザー情報を合成
//                ventCard.toVentCardWithUser(name, photoURL)
//            } else {
//                null // nullを返してmapNotNullで除外
//            }
//        }
//
//        Result.success(ventCardsWithUser)
//    } catch (e: Exception) {
//        Log.w("Firestore", "Error fetching vent cards with user data", e)
//        Result.failure(e)
//    }




//    suspend fun getVentCardsWithUser(): Result<List<VentCardWithUser>> = try {
//            val querySnapshot = db.collectionGroup("swipeCards").get().await()
//            Log.d("TAG","ventCards: $querySnapshot")
//            //TODO　mapNotNullは一旦ヌルになる可能性を考慮して、後々mapに変更
//            val ventCardsWithUser = querySnapshot.documents.map { document ->
//                val parentReference = document.reference.parent.parent
//                val ventCard = document.toObject(VentCard::class.java)
//                if (parentReference != null && ventCard != null){
//                    parentReference
//                        ?.get()
//                        ?.addOnSuccessListener { parentDocument ->
//                            val name = parentDocument.getString("name")
//                            val photoURL = parentDocument.getString("photoURL")
//                            ventCard.toVentCardWithUser(name, photoURL)
//                        } ?.addOnFailureListener { exception ->
//                        Log.w("Firestore", "Error getting parent document", exception)
//                    }
//                }
//            }
//            Result.success(ventCardsWithUser)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }

    suspend fun getVentCardsWithUser(userId: String): Result<List<VentCardWithUser>> = try {
        val querySnapshot = db.collectionGroup("swipeCards").whereNotEqualTo("userId", userId).get().await()
        Log.d("TAG", "ventCards: $querySnapshot")

        val ventCardsWithUser = querySnapshot.documents.mapNotNull { document ->
            val parentReference = document.reference.parent.parent
//            val ventCard = document.toObject(VentCard::class.java)

            if (parentReference != null) {
                try {
                    val parentDocument = parentReference.get().await()
                    val name = parentDocument.getString("name")?: throw IllegalArgumentException("Poster Name is missing")
                    val photoURL = parentDocument.getString("photoURL")?: throw IllegalArgumentException("Poster Image is missing")

                    // Firestoreから直接toObjectを使ってVentCardWithUserを作成
                    val ventCardWithUser = document.toObject(VentCardWithUser::class.java)?.copy(
                        posterId = document.getString("userId") ?: "",
                        posterName = name,
                        posterImageURL = photoURL,
                        swipeCardCreatedDateTime = document.getTimestamp("swipeCardCreatedDateTime")!!.toDate()
                    )

                    ventCardWithUser

//                    ventCard.toVentCardWithUser(name, photoURL)
                } catch (e: Exception) {
                    Log.w("Firestore", "Error getting parent document", e)
                    null
                }
            } else {
                null
            }
        }
        Result.success(ventCardsWithUser)
    } catch (e: Exception) {
        Result.failure(e)
    }


//    suspend fun

    // VentCardとユーザー情報からVentCardWithUserを生成するファクトリメソッド
//    fun VentCard.toVentCardWithUser(posterName: String, posterImageURL: String): VentCardWithUser {
//        return VentCardWithUser(
//            posterId = this.userId,
//            posterName = posterName,
//            posterImageURL = posterImageURL,
//            swipeCardContent = this.swipeCardContent,
//            swipeCardImageURL = this.swipeCardImageURL,
//            likeCount = this.likeCount,
//            tags = this.tags,
//            swipeCardReportFlag = this.swipeCardReportFlag,
//            swipeCardDeletionRequestFlag = this.swipeCardDeletionRequestFlag,
//            swipeCardCreatedDateTime = this.swipeCardCreatedDateTime.toDate()
//        )
//    }


}