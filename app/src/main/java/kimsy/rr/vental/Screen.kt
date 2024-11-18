package kimsy.rr.vental

import androidx.annotation.DrawableRes

sealed class Screen(val title: String, val route: String) {

    sealed class BottomScreen(
        val bottomTitle: String, val bottomRoute: String, @DrawableRes val icon: Int
    ): Screen(bottomTitle, bottomRoute){
        object TimeLine: BottomScreen("タイムライン", "timeline", R.drawable.baseline_home_24)
        object VentCards: BottomScreen("VentCards", "ventcards", R.drawable.baseline_view_array_24)
        object VentCardCreation: BottomScreen("VCC", "ventcardcreationscreen", R.drawable.baseline_add_24)
        object Follows: BottomScreen("フォロー中", "follows", R.drawable.baseline_people_24)
        object MyPage: BottomScreen("マイページ", "mypage", R.drawable.baseline_account_circle_24)
    }

    object SignupScreen:Screen("signup","signupscreen")
    object TimeLineScreen:Screen("timeline","timelinescreen")
    object Notifications: Screen("通知", "notifications")
    object DebateCreation: Screen("反論", "debate creation")

}

val screensInBottom = listOf(
    Screen.BottomScreen.TimeLine,
    Screen.BottomScreen.VentCards,
    Screen.BottomScreen.VentCardCreation,
    Screen.BottomScreen.Follows,
    Screen.BottomScreen.MyPage
)

val otherScreen = listOf(
    Screen.SignupScreen,
    Screen.TimeLineScreen,
    Screen.Notifications,
    Screen.DebateCreation
)