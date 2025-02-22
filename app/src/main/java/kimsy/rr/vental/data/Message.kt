package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val userType: UserType = UserType.DEBATER,
    val text: String = "",
    val imageURL: String? = "",
    @ServerTimestamp
    val sentDatetime: Date? = null,
)

enum class UserType(val type: String) {
    POSTER("poster"),
    DEBATER("debater")
}