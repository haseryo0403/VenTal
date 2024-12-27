package kimsy.rr.vental.data.repository


import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val googleSignInClient: GoogleSignInClient,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    // Googleサインインを開始するメソッド
    fun signInWithGoogle(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("ユーザーが見つかりませんでした。"))
            }
        } catch (e: Exception) {
            Log.d("TAG", "firebase fail: ${e.message}")
            Result.failure(e)
        }
    }

    // Firestore にデバイストークンを保存
    suspend fun saveDeviceToken(userId: String): Result<Unit> {
        return try {
            // 非同期でトークンを取得
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("UR", "token: $token, userId: $userId")

            // トークンを Firestore に保存
            db.collection("notificationSettings").document(userId)
                .update("deviceToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Failed to save device token", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User?> {
        Log.e("UR", "getcurrentUser called")
        val uid = auth.currentUser?.uid ?: return Result.success(null)
        return try {
            val userDoc = db.collection("users")
                .document(uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                Log.d("UR", "User document does not exist")
                Result.success(null)
            }

            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                Log.d("user", uid)
                Result.success(user)
            } else {
                Log.d("UR", "user data is not registered")
                Result.success(null)
//                Result.failure(Exception("User data not found"))
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserToFirestore(): Result<String> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No user to save"))

            val newUser = User.createUser(user.uid, user.displayName?: "", user.photoUrl.toString())
            db
                .collection("users")
                .document(newUser.uid)
                .set(newUser)
                .await()

            Result.success(newUser.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOutFromFirebaseAuth(): Result<Unit> {
        return try {
            withTimeout(10000L){
                auth.signOut()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("SignOut", "firebaseAuth Sign out failed", e)
            Result.failure(e)
        }
    }
    suspend fun signOutFromGoogle(): Result<Boolean> {
        return try {
            withTimeout(10000L) {
                googleSignInClient.signOut()
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("SignOut", "google Sign out failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserInformation(
        uid: String
    ): Resource<User> {
        return try {
            withTimeout(10000L){
                val query = db
                    .collection("users")
                    .whereEqualTo("uid", uid)

                val querySnapshot = query.get().await()
                val user = querySnapshot.toObjects(User::class.java).firstOrNull()
                if (user != null) {
                    Resource.success(user)
                } else {
                    Resource.failure("User data not found")
                }
            }
        } catch (e: Exception) {
            Resource.failure(e.message)
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
}
