package kimsy.rr.vental.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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
    suspend fun getRelatedDebatesCount(posterId: String, swipeCardId: String):Result<Int>{
        Log.d("DR", "getRelatedDC called")
        return try {
            withTimeout(10000L) {
                val query = db
                    .collection("users")
                    .document(posterId)
                    .collection("swipeCards")
                    .document(swipeCardId)
                    .collection("debates")

                val querySnapshot = query.count().get(AggregateSource.SERVER).await()

                val count = querySnapshot.count.toInt()

                Result.success(count)
            }
        } catch (e: Exception){
            Log.e("DRgRDC", "error: ${e.message}")
            Result.failure(e)
        }
    }

    //TODO グループコレクションで討論取得、時間で並び替え、10けんくらいを都度取得。
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
                    debateCreatedDatetime = document.getTimestamp("debateCreatedDatetime")!!.toDate()
                )
            }
            Resource.success(Pair(debates, newLastVisible))
        } catch (e: Exception) {
            Log.e("DR" , "error : ${e.message}")
            Resource.failure(e.message)
        }
    }

    suspend fun createDebate(debate: Debate): Resource<Debate>{
        Log.d("DR", "createDebate called")
        return try{
            withTimeout(10000L) {
                val docRefOnSwipeCard = db
                    .collection("users")
                    .document(debate.posterId)
                    .collection("swipeCards")
                    .document(debate.swipeCardId)

                val debateDocRef = docRefOnSwipeCard
                    .collection("debates")
                    .add(debate)
                    .await()

                docRefOnSwipeCard
                    .update("debateCount", FieldValue.increment(1))
                    .await()

                val createdDebateSnapshot = debateDocRef.get().await()

                val createdDebate = createdDebateSnapshot.toObject(Debate::class.java)
                    ?.copy(debateId = debateDocRef.id,
                        //TODO dateに変換？
                        )
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

            //TODO　すでにある場合更新されるかチェック。されないならoption追加
        transaction.set(docRef, likeData)
//            docRefOnUser.set(likeData)

    }
//
//    suspend fun addLikeDebateToUser(
//        fromUserId: String,
//        debateId: String,
//        likeData: DebateLikeData
//    ): Resource<Unit>{
//        return try {
//            val docRefOnUser = db
//                .collection("users")
//                .document(fromUserId)
//                .collection("likedDebate")
//                .document(debateId)
//
//            //TODO　すでにある場合更新されるかチェック。されないならoption追加
//            docRefOnUser.set(likeData).await()
//
//            Resource.success(Unit)
//        } catch (e: Exception) {
//            Resource.failure(e.message)
//        }
//    }

    suspend fun deleteLikeDebateFromUser(
        fromUserId: String,
        debateId: String
    ):Resource<Unit> {
        return try {
            val docRefOnUser = db
                .collection("users")
                .document(fromUserId)
                .collection("likedDebate")
                .document(debateId)

            //TODO　すでにある場合更新されるかチェック。されないならoption追加
            docRefOnUser.delete()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
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
                docRef.update("posterLikeCount", FieldValue.increment(1))
            }
            UserType.DEBATER -> {
                docRef.update("debaterLikeCount", FieldValue.increment(1))
            }
        }
    }


//    suspend fun likeCountUp(
//        posterId: String,
//        ventCardId: String,
//        debateId: String,
//        userType: UserType
//    ): Resource<Unit> {
//        return try {
//            val docRef= db
//                .collection("users")
//                .document(posterId)
//                .collection("swipeCards")
//                .document(ventCardId)
//                .collection("debates")
//                .document(debateId)
//
//            when (userType) {
//                UserType.POSTER -> {
//                    docRef.update("posterLikeCount", FieldValue.increment(1)).await()
//                }
//                UserType.DEBATER -> {
//                    docRef.update("debaterLikeCount", FieldValue.increment(1)).await()
//                }
//            }
//
//            Resource.success(Unit)
//        } catch (e: Exception) {
//            Resource.failure(e.message)
//        }
//    }

    suspend fun likeCountDown(
        posterId: String,
        ventCardId: String,
        debateId: String,
        userType: UserType
    ): Resource<Unit> {
        return try {
            val docRef= db
                .collection("users")
                .document(posterId)
                .collection("swipeCards")
                .document(ventCardId)
                .collection("debates")
                .document(debateId)

            when (userType) {
                UserType.POSTER -> {
                    docRef.update("posterLikeCount", FieldValue.increment(-1)).await()
                }
                UserType.DEBATER -> {
                    docRef.update("debaterLikeCount", FieldValue.increment(-1)).await()
                }
            }

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }

    // likestate -> not exist -> like
    // exist -> usertype same -> dislike count -1
    // exist -> usertype not same -> update usertypelike count + 1 , opponentUserTypecount -1
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

    suspend fun likeDebater(fromUserId: String, posterId: String, ventCardId: String, debateId: String): Resource<Unit>{
        return try {
            Log.d("VCR", "likeVentCard called")

            val likeData = mapOf(
                "debateId" to debateId,
                "likedDate" to FieldValue.serverTimestamp(),
                "userType" to UserType.DEBATER
            )
            val docRefOnUser = db
                .collection("users")
                .document(fromUserId)
                .collection("likedDebate")
                .document(debateId)

            val docRefOnDebate= db
                .collection("users")
                .document(posterId)
                .collection("swipeCards")
                .document(ventCardId)
                .collection("debates")
                .document(debateId)

            docRefOnUser
                .set(likeData)
                .await()

            //TODO debaterとposterでいいね数のバランスをとる、一人1回までってこと。片方プラス、片方マイナス
            docRefOnDebate
                .update("debaterLikeCount", FieldValue.increment(1)).await()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }
    suspend fun likePoster(fromUserId: String, posterId: String, ventCardId: String, debateId: String): Resource<Unit>{
        return try {
            Log.d("VCR", "likeVentCard called")

            val likeData = mapOf(
                "debateId" to debateId,
                "likedDate" to FieldValue.serverTimestamp(),
                "userType" to UserType.POSTER
            )
            val docRefOnUser = db
                .collection("users")
                .document(fromUserId)
                .collection("likedDebate")
                .document(debateId)

            val docRefOnDebate= db
                .collection("users")
                .document(posterId)
                .collection("swipeCards")
                .document(ventCardId)
                .collection("debates")
                .document(debateId)

            docRefOnUser
                .set(likeData)
                .await()

            //TODO debaterとposterでいいね数のバランスをとる、一人1回までってこと。片方プラス、片方マイナス
            docRefOnDebate
                .update("posterLikeCount", FieldValue.increment(1)).await()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }
}