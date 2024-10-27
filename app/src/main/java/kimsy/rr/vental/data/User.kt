package kimsy.rr.vental.data

import android.net.Uri

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photeURL: Uri? = null,
    )