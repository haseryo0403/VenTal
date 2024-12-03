package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SaveDeviceTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return userRepository.saveDeviceToken(userId)
    }
}