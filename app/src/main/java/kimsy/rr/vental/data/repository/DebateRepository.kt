package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.IOError
import java.io.IOException
import javax.inject.Inject

class DebateRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {

    suspend fun fetchRelatedDebates(ventCardWithUser: VentCardWithUser): Result<List<Debate>> {
        return try {
            withTimeout(10000L) {
                val query = db
                    .collection("users")
                    .document(ventCardWithUser.posterId)
                    .collection("swipeCards")
                    .document(ventCardWithUser.swipeCardId)
                    .collection("debates")

                val querySnapshot = query.get().await()
                val debates = querySnapshot.documents.map { document->
                    document.toObject(Debate::class.java)!!
                }
                Result.success(debates)
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun createDebate(debate: Debate): Result<DebateWithUsers>{
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
                val createdDebate = createdDebateSnapshot.toObject(DebateWithUsers::class.java)
                    ?.copy(debateId = debateDocRef.id,
                        //TODO dateに変換？
                        )
                    ?: throw IllegalStateException("Failed to convert document to Debate")

                Result.success(createdDebate)
            }
        } catch (e: Exception) {
            Result.failure(e)
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

//    suspend fun fetchDebate(debaterId: String)Result<Debate> {
//        return try {
//            withTimeout(10000L) {
//                val docRef = db
//                    .collection("users")
//                    .document(ventCardWithUser.posterId)
//                    .collection("swipeCards")
//                    .document(ventCardWithUser.swipeCardId)
//                    .collection("debates")
//
//                val querySnapshot = query.get().await()
//                val debates = querySnapshot.documents.map { document->
//                    document.toObject(Debate::class.java)!!
//                }
//                Result.success(debates)
//            }
//        } catch (e: IOException) {
//            Result.failure(e)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }




}