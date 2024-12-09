package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class VentCard(
    val posterId: String = "",
    val swipeCardContent: String = "",
    val swipeCardImageURL: String = "",
    val likeCount: Int = 0,
    val tags: List<String> = emptyList(),
    val swipeCardReportFlag: Boolean = false,
    val swipeCardDeletionRequestFlag: Boolean = false,
    val debateCount: Int = 0,
    val swipeCardCreatedDateTime: Any = FieldValue.serverTimestamp()
    ){
    companion object {
        fun createVentCard(
            posterId: String,
            swipeCardContent: String,
            swipeCardImageURL: String,
            tags: List<String>,
        ): VentCard {
            return VentCard(
                posterId = posterId,
                swipeCardContent = swipeCardContent,
                swipeCardImageURL = swipeCardImageURL,
                tags = tags
            )
        }
    }
}