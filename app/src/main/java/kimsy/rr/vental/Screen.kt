package kimsy.rr.vental

import androidx.annotation.DrawableRes

sealed class Screen(val title: String, val route: String) {

    sealed class BottomScreen(
        val bottomTitle: String, val bottomRoute: String, @DrawableRes val icon: Int, val label: String
    ): Screen(bottomTitle, bottomRoute){
        object TimeLine: BottomScreen("タイムライン", "timeline", R.drawable.baseline_home_24, "ホーム")
        object VentCards: BottomScreen("VentCards", "ventcards", R.drawable.baseline_view_array_24, "カード")
        object VentCardCreation: BottomScreen("VCC", "ventcardcreationscreen", R.drawable.baseline_add_24, "カード作成")
        object Follows: BottomScreen("フォロー中", "follows", R.drawable.baseline_people_24,"フォロー")
        object MyPage: BottomScreen("マイページ", "mypage", R.drawable.baseline_account_circle_24,"プロフィール")
    }

    sealed class SettingsMenuScreen(
        val settingTitle: String, val settingRoute: String
    ): Screen(settingTitle, settingRoute) {
        object NotificationSettingsScreen: Screen("通知設定", "notification settings")
    }

    object SignupScreen:Screen("signup","signupscreen")
    object TimeLineScreen:Screen("timeline","timelinescreen")
    object Notifications: Screen("通知", "notifications")
    object DebateCreation: Screen("反論", "debate creation")
    object DebateScreen: Screen("投稿", "debate")
    object SettingsScreen: Screen("設定", "settings")
    object ProfileEditScreen: Screen("プロフィール編集", "profile edit")
    object AnotherUserPageScreen: Screen("ユーザー", "another userPage")
    object FollowListScreen: Screen("フォロー一覧", "follow list")
    object ReportDebateScreen: Screen("討論を通報", "report debate")
    object RequestDebateDeletionScreen: Screen("討論の削除依頼", "request debate deletion")
    object ReportVentCardScreen: Screen("カードを通報", "report ventCard")
    object RequestVentCardDeletionScreen: Screen("カードの削除依頼", "request ventCard deletion")
    object AccountClosingScreen: Screen("退会する", "account closing screen")
}

val screensInBottom = listOf(
    Screen.BottomScreen.TimeLine,
    Screen.BottomScreen.VentCards,
    Screen.BottomScreen.VentCardCreation,
    Screen.BottomScreen.Follows,
    Screen.BottomScreen.MyPage
)

val settingsScreen = listOf(
    Screen.SettingsMenuScreen.NotificationSettingsScreen
)

val otherScreen = listOf(
    Screen.SignupScreen,
    Screen.TimeLineScreen,
    Screen.Notifications,
    Screen.DebateCreation,
    Screen.DebateScreen,
    Screen.SettingsScreen,
    Screen.ProfileEditScreen,
    Screen.AnotherUserPageScreen,
    Screen.FollowListScreen,
    Screen.ReportDebateScreen,
    Screen.RequestDebateDeletionScreen,
    Screen.ReportVentCardScreen,
    Screen.RequestVentCardDeletionScreen,
    Screen.AccountClosingScreen
)