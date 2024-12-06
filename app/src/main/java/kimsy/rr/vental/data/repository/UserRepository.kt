package kimsy.rr.vental.data.repository


import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kimsy.rr.vental.R
import kimsy.rr.vental.data.NotificationSettings
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

//    suspend fun firebaseAuthWithGoogle(idToken: String): Result<String> {
//        return try {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//            val authResult = auth.signInWithCredential(credential).await()
//            val user = authResult.user
//            if (user != null) {
//                val userId = user.uid
//                Result.success(userId)
//            } else {
//                Result.failure(Exception("User not found"))
//            }
////            val uid = authResult.user?.uid ?: return Result.failure(Exception("User not found"))
////            Result.success(uid)
//        } catch (e: Exception) {
//            Log.d("TAG", "firebase fail: ${e.message}")
//            Result.failure(e)
//        }
//    }
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
//                val userId = user.uid
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
//            val uid = authResult.user?.uid ?: return Result.failure(Exception("User not found"))
//            Result.success(uid)
        } catch (e: Exception) {
            Log.d("TAG", "firebase fail: ${e.message}")
            Result.failure(e)
        }
    }

//TODO
//    suspend fun getCurrentUsers(): User? {
//        val currentUser = auth.currentUser ?: return null
//        return try {
//            val uid = currentUser.uid
//            Log.d("TAG", "uid: $uid")
//            val result = db.collection("users")
//                .whereEqualTo("uid", uid)
//                .get()
//                .await()
//            Log.d("TAG","getUser")
//            Log.d("TAG",result.toString())
//
//            // 取得結果が空でない場合は最初の要素を返し、空の場合は null を返す
//            result.toObjects(User::class.java).firstOrNull()
//        } catch (e: FirebaseFirestoreException) {
//            Log.e("FirestoreError", "Firestore error: ${e.message}", e)
//            null
//        } catch (e: Exception) {
//            Log.e("GeneralError", "An error occurred: ${e.message}", e)
//            null
//        }
//    }

//    suspend fun getCurrentUser(): Result<User?> {
//        val currentUser = auth.currentUser ?: return Result.failure(Exception("No authenticated user found"))//ここnull返すかも
//        return try {
//            val uid = currentUser.uid
//            Log.d("TAG", "uid: $uid")
//            val result = db.collection("users")
//                .whereEqualTo("uid", uid)
//                .get()
//                .await()
//            Log.d("TAG","getUser")
//            Log.d("TAG",result.toString())
//
//            // 取得結果が空でない場合は最初の要素を返し、空の場合は null を返す
//            val user = result.toObjects(User::class.java).firstOrNull()
//            Result.success(user)
//        } catch (e: FirebaseFirestoreException) {
//            Log.e("FirestoreError", "Firestore error: ${e.message}", e)
//            Result.failure(e)
//        } catch (e: Exception) {
//            Log.e("GeneralError", "An error occurred: ${e.message}", e)
//            Result.failure(e)
//        }
//    }

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
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))
        return try {
            val userDoc = db.collection("users")
                .document(uid)
//                .whereEqualTo("uid", uid)
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




//    suspend fun saveUserToFirestore() {
//        val user = auth.currentUser
//        user?.let {
//            // 認証ユーザーの情報を取得
//            val newUser = User(
//                uid = it.uid,
//                name = it.displayName ?: "",
//                email = it.email ?: "",
//                photoURL = it.photoUrl?.toString() ?:""
//            )
//            db.collection("users").document(newUser.uid).set(newUser).await()
//        }
//    }

    suspend fun saveUserToFirestore(): Result<String> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No user to save"))
            // 認証ユーザーの情報を取得
//            val newUser = User(
//                uid = user.uid,
//                name = user.displayName ?: "",
////                email = user.email ?: "",
//                photoURL = user.photoUrl?.toString() ?:""
//            )
            val newUser = User.createUser(user.uid, user.displayName?: "", user.photoUrl.toString())
            db
                .collection("users")
                .document(newUser.uid)
                .set(newUser)
                .await()

            //TODO コード整理。ここはrepositoryわけたほうがいい。useCase使ってたら楽だったのに
//            val defaultNotificationSettings = NotificationSettings()
//            db.collection("notificationSettings")
//                .document(newUser.uid)
//                .set(defaultNotificationSettings)
//                .await()

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
    ): Result<User>{
        return try {
            withTimeout(10000L){
                val query = db
                    .collection("users")
                    .whereEqualTo("uid", uid)

                val querySnapshot = query.get().await()
                val user = querySnapshot.toObjects(User::class.java).firstOrNull()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User data not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
