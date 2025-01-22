package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Debate(
    val debateId: String = "",
    val swipeCardImageURL: String = "",
    val swipeCardId: String = "",
    val posterId: String = "",
    val posterLikeCount: Int = 0,
    val debaterId: String = "",
    val debaterLikeCount: Int = 0,
    val firstMessage: String = "",
    val firstMessageImageURL: String? = "",
    val totalLikeCount: Int = 0,
    val debateReportFlag: Boolean = false,
    val debateDeletionRequestFlag: Boolean   = false,
    @ServerTimestamp
    val debateCreatedDatetime: Date? = null
){
//    companion object {
//        fun createDebate(
//
//        )
//    }
} object DebateShareModel {
    private var reportedDebate: Debate? = null

    fun setReportedDebateToModel(debate: Debate) {
        reportedDebate = debate
    }

    fun getReportedDebateFromModel():Debate? {
        return reportedDebate
    }

    fun resetReportedDebateOnModel() {
        reportedDebate = null
    }

}

enum class LikeStatus {
    LIKE_NOT_EXIST,
    LIKED_DEBATER,
    LIKED_POSTER
}

