package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserRepository
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(uid: String): Result<User> {
        return try {
            userRepository.fetchUserInformation(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}