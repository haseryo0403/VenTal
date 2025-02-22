package kimsy.rr.vental.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ErrorLog(
    val uid: String = "",
    val message: String? = "",
    val stackTrace: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val deviceInfo: String? = null, // デバイス情報（任意）
    val errorType: String? = null, // エラータイプ（任意）
    val contextInfo: String? = null, // コンテキスト情報（任意）
    val errorId: String? = null // エラーID（任意）
)
