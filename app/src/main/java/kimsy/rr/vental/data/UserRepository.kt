package kimsy.rr.vental.data


import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import kimsy.rr.vental.R
import kotlinx.coroutines.tasks.await

class UserRepository(private val googleSignInClient: GoogleSignInClient) {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Googleサインインを開始するメソッド
    fun signInWithGoogle(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("User not found"))
            Log.d("TAG", uid)
            Result.success(uid)
        } catch (e: Exception) {
            Log.d("TAG", "firebase fail: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUser(): List<User> {
        return try {
            val uid = auth.currentUser!!.uid
            val result = db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            Log.d("TAG","getUser success")
            result.toObjects(User::class.java)
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreError", "Firestore error: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("GeneralError", "An error occurred: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun saveUserToFirestore() {
        val user = auth.currentUser
        user?.let {
            // 認証ユーザーの情報を取得
            val newUser = User(
                uid = it.uid,
                name = it.displayName ?: "",
                email = it.email ?: ""
            )
            db.collection("users").document(newUser.uid).set(newUser).await()
        }
    }



    companion object {
        fun createGoogleSignInClient(activity: ComponentActivity): GoogleSignInClient {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(activity, gso)
        }
    }

}
