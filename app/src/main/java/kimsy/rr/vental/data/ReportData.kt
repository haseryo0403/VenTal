package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReportData(
    val entityId: String = "",
    val entityType: EntityType = EntityType.DEBATE, // エンティティの種類 (debate, message, ventcard など)
    val reporterId: String = "",
    val reasonNumber: Int = 0,
    @ServerTimestamp
    val reportedDateTime: Date? = null
)
