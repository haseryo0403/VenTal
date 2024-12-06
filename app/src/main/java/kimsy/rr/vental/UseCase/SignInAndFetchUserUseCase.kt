package kimsy.rr.vental.UseCase

import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignInAndFetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
//    private val defaultNotificationSettingsUseCase: SetDefaultNotificationSettingsUseCase
) {
//    suspend fun execute(idToken: String): Result<Unit> {
//        // Try Firebase authentication
//        return try {
//            userRepository.firebaseAuthWithGoogle(idToken).getOrThrow()
//            val user = userRepository.getCurrentUser().getOrThrow()
//            if (user == null) {
//                userRepository.saveUserToFirestore().getOrThrow()
//                Result.success(Unit)
//            } else {
//                Result.success(Unit)
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    //TODO auth and getUser is combo, save and settings are combo
    suspend fun execute(idToken: String): Result<User?> {
        return userRepository.firebaseAuthWithGoogle(idToken).fold(
            onSuccess = {
                // firebaseAuthWithGoogle が成功した場合に、getCurrentUser() を呼び出してその結果を返す
                userRepository.getCurrentUser()
            },
            onFailure = { exception ->
                // エラーが発生した場合、Result.failure() を使ってエラーを返す
                Result.failure(exception)
            }
        )
    }
//    suspend fun executes(idToken: String): Result<Unit> {
//        return userRepository.firebaseAuthWithGoogle(idToken)
//            .onSuccess {
//                userRepository.getCurrentUser()
//                    .onSuccess { user ->
//                        if (user == null) {
//                            userRepository.saveUserToFirestore()
//                                .onSuccess { newUserId->
//                                    defaultNotificationSettingsUseCase.execute(newUserId)
//                                }
//                        }
//                    }
//            }
//            .map { Unit }
//    }
}
