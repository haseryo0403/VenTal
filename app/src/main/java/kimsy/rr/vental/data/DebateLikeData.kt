package kimsy.rr.vental.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class DebateLikeData(
    @get:Exclude val debateId: String = "",
    val userType: UserType = UserType.POSTER,
    @ServerTimestamp
    val likedDate: Date? = null
)
