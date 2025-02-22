package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.ReportData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRepository @Inject constructor(
    private val db: FirebaseFirestore,
){
    suspend fun storeReportData(reportData: ReportData) {
        val query = db
            .collection("reports")
            .document(reportData.entityType.toString())
            .collection(reportData.entityId)
            .document(reportData.reporterId)

        query.set(reportData).await()
    }

}

