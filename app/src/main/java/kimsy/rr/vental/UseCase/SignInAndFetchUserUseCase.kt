package kimsy.rr.vental.UseCase

import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInAndFetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
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

    suspend fun execute(idToken: String): Result<Unit> {
        return userRepository.firebaseAuthWithGoogle(idToken)
            .onSuccess {
                userRepository.getCurrentUser()
                    .onSuccess { user ->
                        if (user == null) {
                            userRepository.saveUserToFirestore()
                                .onFailure { return Result.failure(it) }
                        }
                    }
                    .onFailure { return Result.failure(it) }
            }
            .onFailure { return Result.failure(it) }
            .map { Unit }
    }
}


