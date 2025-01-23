package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DeleteRequestData(
    val entityId: String = "", // エンティティID
    val entityType: EntityType = EntityType.DEBATE, // エンティティの種類 (debate, message, ventcard など)
    val requesterId: String = "",
    val reasonNumber: Int = 0,
    @ServerTimestamp
    val requestDateTime: Date? = null
)

enum class EntityType(val type: String) {
    DEBATE("debates"),
    MESSAGE("messages"),
    VENTCARD("ventcards")
}