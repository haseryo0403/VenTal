package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.DeleteRequestData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DeleteRequestRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun storeDeleteRequestData(deleteRequestData: DeleteRequestData) {
        val query = db
            .collection("deleteRequests")
            .document(deleteRequestData.entityType.toString())
            .collection(deleteRequestData.entityId)
            .document(deleteRequestData.requesterId)

        query.set(deleteRequestData).await()
    }
}