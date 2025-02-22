package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val commentId: String = "",
    val commenterId: String = "",
    val commentContent: String = "",
    @ServerTimestamp
    val commentedDateTime: Date? = null
)
