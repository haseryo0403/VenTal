package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class LoadCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<User?> {
        return userRepository.getCurrentUser()
    }
}
