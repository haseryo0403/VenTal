package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class Debate(
    val swipeCardImageURL: String = "",
    val swipeCardId: String = "",
    val posterId: String = "",
    val posterLikeCount: Int = 0,
    val debaterId: String = "",
    val debaterLikeCount: Int = 0,
    val firstMessage: String = "",
    val firstMessageImageURL: String? = "",
    val debateReportFlag: Boolean = false,
    val debateDeletionRequestFlag: Boolean = false,
    val debateCreatedDatetime: Any = FieldValue.serverTimestamp(),
){
//    companion object {
//        fun createDebate(
//
//        )
//    }
}
