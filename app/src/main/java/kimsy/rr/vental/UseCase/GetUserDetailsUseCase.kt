package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(uid: String): Result<User> {
        return try {
            Log.d("GUDUC", "called")
            userRepository.fetchUserInformation(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}