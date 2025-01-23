package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class VentCard(
    val swipeCardId: String = "",
    val posterId: String = "",
    val swipeCardContent: String = "",
    val swipeCardImageURL: String = "",
    val likeCount: Int = 0,
    val tags: List<String> = emptyList(),
    val swipeCardReportFlag: Boolean = false,
    val swipeCardDeletionRequestFlag: Boolean = false,
    val debateCount: Int = 0,

    @ServerTimestamp
    val swipeCardCreatedDateTime: Date? = null
//    val swipeCardCreatedDateTime: Any = FieldValue.serverTimestamp()
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
} object VentCardShareModel{
    private var reportedVentCard: VentCard? = null
    private var deleteRequestedVentCard: VentCard? = null

    fun setReportedVentCardToModel(ventCard: VentCard) {
        reportedVentCard = ventCard
    }

    fun getReportedVentCardFromModel():VentCard? {
        return reportedVentCard
    }

    fun resetReportedVentCardOnModel() {
        reportedVentCard = null
    }

    fun setDeleteRequestedVentCardToModel(ventCard: VentCard) {
        deleteRequestedVentCard = ventCard
    }

    fun getDeleteRequestedVentCardFromModel():VentCard? {
        return deleteRequestedVentCard
    }

    fun resetDeleteRequestedVentCardOnModel() {
        deleteRequestedVentCard = null
    }
}