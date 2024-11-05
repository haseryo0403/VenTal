package kimsy.rr.vental.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

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

}