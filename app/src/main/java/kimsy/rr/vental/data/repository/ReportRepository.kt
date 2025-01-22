package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.ReportData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRepository @Inject constructor(
    private val db: FirebaseFirestore,
){
    suspend fun storeReportDebateData(reportData: ReportData) {
        val query = db
            .collection("reports")
            .document("debates")
            .collection(reportData.contentId)
            .document(reportData.reporterId)

        query.set(reportData).await()
    }
}