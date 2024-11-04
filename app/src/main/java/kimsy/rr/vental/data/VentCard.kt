package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class VentCard(
    val userId: String = "",
    val swipeCardContent: String = "",
    val swipeCardImageURL: String = "",
    val tags: List<String> = emptyList(),
    val swipeCardCreatedDateTime: Any = FieldValue.serverTimestamp()
    )