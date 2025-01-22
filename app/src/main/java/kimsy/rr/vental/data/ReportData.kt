package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReportData(
    val contentId: String = "",
    val reporterId: String = "",
    val reason: Int = 0,
    @ServerTimestamp
    val reportedDateTime: Date? = null
)
