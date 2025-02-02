package kimsy.rr.vental.data.repository


import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kimsy.rr.vental.data.CloseAccountData
import kimsy.rr.vental.data.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val googleSignInClient: GoogleSignInClient,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun signInWithGoogle(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String){
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user ?: throw IllegalArgumentException("signInWithCredentialが失敗しました。")
    }

    suspend fun saveDeviceToken(userId: String){
            val token = FirebaseMessaging.getInstance().token.await()

            db.collection("notificationSettings").document(userId)
                .update("deviceToken", token)
                .await()
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val userDoc = db.collection("users")
            .document(uid)
            .get()
            .await()

        if (!userDoc.exists()) {
            return null
        }

        return userDoc.toObject(User::class.java)
    }

    suspend fun saveUserToFirestore(): String{
            val user = auth.currentUser
                ?: throw IllegalArgumentException("保存するユーザーがありません。")

            val newUser = User.createUser(user.uid, user.displayName?: "", user.photoUrl.toString())
            db
                .collection("users")
                .document(newUser.uid)
                .set(newUser)
                .await()

            return newUser.uid
    }

    fun signOutFromFirebaseAuth() {
        auth.signOut()
    }

    fun signOutFromGoogle(){
        googleSignInClient.signOut()
    }

    suspend fun fetchUserInformation(
        uid: String
    ): User {
        val query = db
            .collection("users")
            .whereEqualTo("uid", uid)

        val querySnapshot = query.get().await()
        val user = querySnapshot.toObjects(User::class.java).firstOrNull()
        if (user != null) {
            return user
        } else {
            throw IllegalArgumentException("ユーザーが見つかりませんでした。")
        }
    }

    suspend fun updateUser(
        user: User
    ) {

        val query = db
            .collection("users")
            .document(user.uid)

        val updates = mapOf(
            "name" to user.name,
            "photoURL" to user.photoURL,
            "selfIntroduction" to user.selfIntroduction
        )

        query.update(updates).await()
    }

    //TODO ここでfollowingが存在すればの処理をする。もしくはlistがnullでもUseCaseはsuccessで返

    suspend fun fetchFollowingUserIds(
        userId: String
    ): List<String> {
        val docRef = db
            .collection("users")
            .document(userId)
            .collection("following")

        val querySnapshot = docRef.get().await()
        val followingUserIds = querySnapshot.documents.map { document ->
            document.id
        }

        return followingUserIds
    }

    suspend fun updateAccountClosingFlagToTrue(
        currentUserId: String
    ) {
        val docRef = db
            .collection("users")
            .document(currentUserId)

        docRef.update("accountClosingFlag", true).await()
    }

    suspend fun saveCloseAccountData(closeAccountData: CloseAccountData) {
        val query = db
            .collection("withdrawals")
            .document(closeAccountData.userId)

        query.set(closeAccountData).await()
    }

    suspend fun updateAccountClosingFlagToFalse(
        currentUserId: String
    ) {
        val docRef = db
            .collection("users")
            .document(currentUserId)

        docRef.update("accountClosingFlag", false).await()
    }

    suspend fun updateReLoginDate(
        currentUserId: String
    ) {
        val docRef = db
            .collection("withdrawals")
            .document(currentUserId)

        docRef.update("reLoginDate", FieldValue.serverTimestamp()).await()
    }

}
