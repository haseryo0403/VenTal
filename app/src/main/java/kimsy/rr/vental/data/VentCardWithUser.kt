package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class VentCardWithUser(
    val posterId: String = "",
    val posterName: String = "",
    val posterImageURL: String = "",
    val swipeCardContent: String = "",
    val swipeCardImageURL: String = "",
    val likeCount: Int = 0,
    val tags: List<String> = emptyList(),
    val swipeCardReportFlag: Boolean = false,
    val swipeCardDeletionRequestFlag: Boolean = false,
    val swipeCardCreatedDateTime: Any = FieldValue.serverTimestamp()
)