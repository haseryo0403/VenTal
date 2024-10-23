package kimsy.rr.vental

sealed class Screen(val route: String) {
    object SignupScreen:Screen("signupscreen")
    object TimeLineScreen:Screen("timelinescreen")
}