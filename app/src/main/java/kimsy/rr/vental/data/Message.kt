package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class Message(
    val userType: UserType = UserType.DEBATER,
    val text: String = "",
    val imageURL: String? = "",
    val sentDatetime: Any = FieldValue.serverTimestamp(),
)

enum class UserType(val type: String) {
    POSTER("poster"),
    DEBATER("debater")
}