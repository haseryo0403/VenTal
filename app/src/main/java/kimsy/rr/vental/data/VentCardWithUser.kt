package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue
import com.google.type.DateTime
import java.util.Date

data class VentCardWithUser(
    val swipeCardId: String = "",
    val posterId: String = "",
    val posterName: String = "",
    val posterImageURL: String = "",
    val swipeCardContent: String = "",
    val swipeCardImageURL: String = "",
    val likeCount: Int = 0,
    val tags: List<String> = emptyList(),
    val debateCount: Int = 0,
    val swipeCardReportFlag: Boolean = false,
    val swipeCardDeletionRequestFlag: Boolean = false,
    val swipeCardCreatedDateTime: Date? = null
)