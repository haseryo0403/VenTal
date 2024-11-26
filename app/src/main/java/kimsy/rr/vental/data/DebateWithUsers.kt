package kimsy.rr.vental.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import java.util.Date

data class DebateWithUsers(
    val debateId: String = "",
    val swipeCardImageURL: String = "",
    val swipeCardId: String = "",
    val posterId: String = "",
    val posterName: String = "",
    val posterImageURL: String = "",
    val posterLikeCount: Int = 0,
    val debaterId: String = "",
    val debaterName: String = "",
    val debaterImageURL: String = "",
    val debaterLikeCount: Int = 0,
    val firstMessage: String = "",
    val firstMessageImageURL: String? = "",
    val debateReportFlag: Boolean = false,
    val debateDeletionRequestFlag: Boolean = false,
    val debateCreatedDatetime: Date? = null

    )

object DebateSharedModel {
    private var currentDebate: DebateWithUsers? = null

    fun setDebate(debateWithUsers: DebateWithUsers) {
        Log.d("DWU", "debate set")
        currentDebate = debateWithUsers
    }

    fun getDebate(): DebateWithUsers? {
        Log.d("DWU", "debate get")
        return currentDebate
    }

    fun clearDebate() {
        currentDebate = null
    }
}