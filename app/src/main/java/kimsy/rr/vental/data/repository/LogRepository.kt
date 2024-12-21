package kimsy.rr.vental.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.ErrorLog
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
){

    fun saveErrorLogToFirestore(errorLog: ErrorLog) {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val errorLogWithUid = errorLog.copy(uid = uid)
        db.collection("errorLog").add(errorLogWithUid)
    }
}