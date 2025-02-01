package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CloseAccountData(
    val userId: String = "",
    val reasonNumber: Int = 0,
    @ServerTimestamp
    val withdrawalDate: Date? = null,
    val reLoginDate: Date? = null
)