package kimsy.rr.vental.ui.CommonComposable

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

@SuppressLint("SimpleDateFormat")

fun formatTimeDifference(postedDate: Date): String {
    val now = Date()
    val diffInMillis = now.time - postedDate.time

    // 時間差を計算
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis).toInt()
    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis).toInt()
//    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

    val calendarNow = Calendar.getInstance()
    calendarNow.time = now

    val calendarCreated = Calendar.getInstance()
    calendarCreated.time = postedDate

    val diffInDays = calendarNow.get(Calendar.DAY_OF_YEAR) - calendarCreated.get(Calendar.DAY_OF_YEAR)
    return when {
        // 10分未満：1分単位
        diffInMinutes < 10 -> "${diffInMinutes}分前"

        // 10分以上かつ60分未満：5分単位（切り捨て）
        diffInMinutes < 60 -> "${(diffInMinutes / 5) * 5}分前"

        // 7時間未満：1時間単位
        diffInHours < 7 -> "${diffInHours}時間前"

        // 同日の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) == calendarCreated.get(Calendar.DAY_OF_YEAR) -> "今日"

        // 昨日の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) - calendarCreated.get(Calendar.DAY_OF_YEAR) == 1 -> "昨日"

        // 3日以内：日単位
        diffInDays <= 3 -> "${diffInDays}日前"

        // 同年の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) -> {
            val dateFormat = SimpleDateFormat("M月d日")
            dateFormat.format(postedDate)
        }

        // 異なる年の日付
        else -> {
            val dateFormat = SimpleDateFormat("yyyy年M月d日")
            dateFormat.format(postedDate)
        }
    }
}

