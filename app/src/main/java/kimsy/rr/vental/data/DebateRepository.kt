package kimsy.rr.vental.data

import android.util.Log
import androidx.compose.runtime.Composable
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRelatedDebatesCount(debate: Debate):Result<Int>{
        Log.d("DR", "getRelatedDC called")
        return try {
            withTimeout(10000L) {
                val query = db
                    .collection("users")
                    .document(debate.posterId)
                    .collection("swipeCards")
                    .document(debate.swipeCardId)
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

    suspend fun createDebate(debate: Debate): Result<Unit>{
        Log.d("DR", "createDebate called")
        return try{
            withTimeout(10000L) {
                db
                    .collection("users")
                    .document(debate.posterId)
                    .collection("swipeCards")
                    .document(debate.swipeCardId)
                    .collection("debates")
                    .add(debate)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




}