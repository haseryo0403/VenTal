package kimsy.rr.vental.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateLikeData
import kimsy.rr.vental.data.LikeStatus
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject


class DebateRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {

    suspend fun fetchDebateByDebateId(debateId: String): Debate {
        val query = db
            .collectionGroup("debates")
            .whereEqualTo("debateId", debateId)

        val querySnapshot = query.get().await()
        val debate = querySnapshot.toObjects(Debate::class.java).firstOrNull()
        return debate ?: throw NoSuchElementException("Debate with ID $debateId not found")
    }

    suspend fun fetchRelatedDebates(ventCardWithUser: VentCardWithUser): Resource<List<Debate>> {
        return try {
            Log.d("DR", "fetchRD was called")
            withTimeout(10000L) {
                val query = db
                    .collection("users")
                    .document(ventCardWithUser.posterId)
                    .collection("swipeCards")
                    .document(ventCardWithUser.swipeCardId)
                    .collection("debates")

                val querySnapshot = query.get().await()
                val debates = querySnapshot.documents.mapNotNull { document->
                    document.toObject(Debate::class.java)
                }
                Resource.success(debates)
            }
        } catch (e: IOException) {
            Resource.failure(e.message)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }
    suspend fun getRelatedDebatesCount(posterId: String, swipeCardId: String): Int{
        return withTimeout(10000L) {
            val query = db
                .collection("users")
                .document(posterId)
                .collection("swipeCards")
                .document(swipeCardId)
                .collection("debates")

            val querySnapshot = query.count().get(AggregateSource.SERVER).await()

            val count = querySnapshot.count.toInt()

            count
        }
    }

    suspend fun getDebatesCountRelatedUser(userId: String): Int {
        val query = db.collectionGroup("debates").where(
            Filter.or(
                Filter.equalTo("debaterId", userId),
                Filter.equalTo("posterId", userId)
            )
        )

        val querySnapshot = query.count().get(AggregateSource.SERVER).await()
        return querySnapshot.count.toInt()
    }

    suspend fun fetch10DebatesRelatedUser(
        userId: String,
        lastVisible: DocumentSnapshot? = null
        ) : Pair<List<Debate>, DocumentSnapshot?> {
        val query = db.collectionGroup("debates").where(
            Filter.or(
                Filter.equalTo("debaterId", userId),
                Filter.equalTo("posterId", userId)
            )
        ).orderBy("debateCreatedDatetime", Query.Direction.DESCENDING)

        val querySnapshot = if (lastVisible == null) {
            query.limit(10).get().await()
        } else {
            query.startAfter(lastVisible).limit(10).get().await()
        }

        if (querySnapshot.isEmpty) {
            // データがない場合、空リストとnullを返す
            return Pair(emptyList(), null)
        }

        val newLastVisible = querySnapshot.documents.lastOrNull()

        val debates = querySnapshot.documents.mapNotNull { document->
            document.toObject(Debate::class.java)!!.copy(
                debateId = document.id,
                debateCreatedDatetime = document.getTimestamp("debateCreatedDatetime")?.toDate()
            )
        }

        return Pair(debates, newLastVisible)
    }

    //スクロールにて取得タイミングを管理。調べないと？
    suspend fun fetch10Debates(
        lastVisible: DocumentSnapshot? = null
    ): Resource<Pair<List<Debate>, DocumentSnapshot?>> {
        return try {
            Log.d("DR", "fetchD was called")
            val query = db
                .collectionGroup("debates")
                .orderBy("debateCreatedDatetime", Query.Direction.DESCENDING)

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

            val debates = querySnapshot.documents.mapNotNull { document->
                document.toObject(Debate::class.java)!!.copy(
                    debateId = document.id,
                    debateCreatedDatetime = document.getTimestamp("debateCreatedDatetime")?.toDate()
                )
            }
            Resource.success(Pair(debates, newLastVisible))
        } catch (e: Exception) {
            Log.e("DR" , "error : ${e.message}")
            Resource.failure(e.message)
        }
    }

//    suspend fun createDebate(debate: Debate): Resource<Debate>{
//        Log.d("DR", "createDebate called")
//        return try{
//            withTimeout(10000L) {
//                val docRefOnSwipeCard = db
//                    .collection("users")
//                    .document(debate.posterId)
//                    .collection("swipeCards")
//                    .document(debate.swipeCardId)
//
//                val debateDocRef = docRefOnSwipeCard
//                    .collection("debates")
//                    .add(debate)
//                    .await()
//
//                docRefOnSwipeCard
//                    .update("debateCount", FieldValue.increment(1))
//                    .await()
//
//                val createdDebateSnapshot = debateDocRef.get().await()
//
//                val createdDebate = createdDebateSnapshot.toObject(Debate::class.java)
//                    ?.copy(debateId = debateDocRef.id,
//                        //TODO dateに変換？
//                        )
//                    ?: throw IllegalStateException("Failed to convert document to Debate")
//
//                Resource.success(createdDebate)
//            }
//        } catch (e: Exception) {
//            Resource.failure(e.message)
//        }
//    }


    suspend fun createDebate(debate: Debate): Resource<Debate> {
        Log.d("DR", "createDebate called")
        return try {
            withTimeout(10000L) {
                val docRefOnSwipeCard = db
                    .collection("users")
                    .document(debate.posterId)
                    .collection("swipeCards")
                    .document(debate.swipeCardId)

                val debateDoc = docRefOnSwipeCard
                    .collection("debates")
                    .document() // 新しいドキュメントIDを生成

                // DebateオブジェクトにドキュメントIDを追加して保存
                val debateWithId = debate.copy(debateId = debateDoc.id)

                // ドキュメントを保存
                debateDoc.set(debateWithId).await()

                // swipeCardのdebateCountを更新
                docRefOnSwipeCard
                    .update("debateCount", FieldValue.increment(1))
                    .await()

                // 作成したドキュメントを取得
                val createdDebateSnapshot = debateDoc.get().await()

                // 作成したDebateオブジェクトを取得
                val createdDebate = createdDebateSnapshot.toObject(Debate::class.java)
                    ?: throw IllegalStateException("Failed to convert document to Debate")

                Resource.success(createdDebate)
            }
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }


    suspend fun addDebatingSwipeCard (debaterId: String, swipeCardId: String) {
        val docRef = db
            .collection("users")
            .document(debaterId)
            .collection("debatingSwipeCards")
            .document(swipeCardId)

        val data = mapOf("swipeCardId" to swipeCardId)

        docRef.set(data).await()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun setLikeDebateToUser(
        fromUserId: String,
        debateId: String,
        likeData: DebateLikeData,
        transaction: Transaction
    ){
            // データベースやAPIの操作
            val docRef = db
                .collection("users")
                .document(fromUserId)
                .collection("likedDebate")
                .document(debateId)

        //すでにいいねしてあるなら上書きされる
        transaction.set(docRef, likeData)

    }

    fun deleteLikeDebateFromUser(
        fromUserId: String,
        debateId: String,
        transaction: Transaction
    ){
        val docRef = db
            .collection("users")
            .document(fromUserId)
            .collection("likedDebate")
            .document(debateId)

        transaction.delete(docRef)
        }

    fun likeCountUp(
        posterId: String,
        ventCardId: String,
        debateId: String,
        userType: UserType,
        transaction: Transaction
    ){
        val docRef= db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(ventCardId)
            .collection("debates")
            .document(debateId)

        when (userType) {
            UserType.POSTER -> {
                transaction.update(docRef, "posterLikeCount", FieldValue.increment(1))
            }
            UserType.DEBATER -> {
                transaction.update(docRef, "debaterLikeCount", FieldValue.increment(1))
            }
        }
    }

    fun likeCountDown(
        posterId: String,
        ventCardId: String,
        debateId: String,
        userType: UserType,
        transaction: Transaction
    ){
        val docRef= db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(ventCardId)
            .collection("debates")
            .document(debateId)

        when (userType) {
            UserType.POSTER -> {
                transaction.update(docRef, "posterLikeCount", FieldValue.increment(-1))
            }
            UserType.DEBATER -> {
                transaction.update(docRef, "debaterLikeCount", FieldValue.increment(-1))
            }
        }
    }

    suspend fun fetchLikeState(
        fromUserId: String,
        debateId: String,
    ): Resource<LikeStatus> {
        return try {

            val docRef = db
                .collection("users")
                .document(fromUserId)
                .collection("likedDebate")
                .document(debateId)

            val docSnapshot = docRef.get().await()
            val isLiked = docSnapshot.exists()

            if (!isLiked) {
                Resource.success(LikeStatus.LIKE_NOT_EXIST)
            } else {
                val likeData = docSnapshot.toObject(DebateLikeData::class.java)!!
                when (likeData.userType) {
                    UserType.POSTER -> Resource.success(LikeStatus.LIKED_POSTER)
                    UserType.DEBATER -> Resource.success(LikeStatus.LIKED_DEBATER)
                }
            }

        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }

}