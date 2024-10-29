package kimsy.rr.vental

import androidx.annotation.DrawableRes

sealed class Screen(val title: String, val route: String) {

    sealed class BottomScreen(
        val bottomTitle: String, val bottomRoute: String, @DrawableRes val icon: Int
    ): Screen(bottomTitle, bottomRoute){
        object TimeLine: BottomScreen("タイムライン", "timeline", R.drawable.baseline_home_24)
        object VentCards: BottomScreen("VentCards", "ventcards", R.drawable.baseline_view_array_24)
        object Follows: BottomScreen("フォロー中", "follows", R.drawable.baseline_people_24)
        object Notifications: BottomScreen("通知", "notifications", R.drawable.baseline_notifications_24)
        object MyPage: BottomScreen("マイページ", "mypage", R.drawable.baseline_account_circle_24)
    }

    object SignupScreen:Screen("signup","signupscreen")
    object TimeLineScreen:Screen("timeline","timelinescreen")
    object VentCardCreation:Screen("", "ventcardcreationscreen")

}

val screensInBottom = listOf(
    Screen.BottomScreen.TimeLine,
    Screen.BottomScreen.VentCards,
    Screen.BottomScreen.Follows,
    Screen.BottomScreen.Notifications,
    Screen.BottomScreen.MyPage
)

val otherScreen = listOf(
    Screen.SignupScreen,
    Screen.TimeLineScreen,
    Screen.VentCardCreation
)