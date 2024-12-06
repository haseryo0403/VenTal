package kimsy.rr.vental.data

data class User(
    val uid: String = "",
    val name: String = "",
//    val email: String = "",
    val photoURL: String = ""
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
    }
}


