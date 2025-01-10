package kimsy.rr.vental.data

data class User(
    val uid: String = "",
    val name: String = "",
//    val email: String = "",
    val photoURL: String = "",
    val selfIntroduction: String? = "",
    val followerCount: Int = 0
    ){
    companion object {
        fun createUser(
            userId: String,
            name: String,
            photoURL: String
        ): User {
            return User(
                uid = userId,
                name = name,
                photoURL = photoURL
            )
        }
    } object CurrentUserShareModel {
        private var currentUser: User? = null

        fun setCurrentUserToModel(user: User) {
            currentUser = user
        }

        fun getCurrentUserFromModel():User? {
            return currentUser
        }

        fun resetCurrentUserOnModel() {
            currentUser = null
        }

    } object AnotherUserShareModel {
        private var anotherUser: User? = null

        fun setAnotherUser(user: User) {
            anotherUser = user
        }

        fun getAnotherUser(): User? {
            return anotherUser
        }
    }
}


