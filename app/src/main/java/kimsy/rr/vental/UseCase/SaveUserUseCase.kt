package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository
) {
    suspend fun execute(): Result<Unit> {
        return try {
            userRepository.saveUserToFirestore().fold(
                onSuccess = { newUserId ->
                    saveNotificationSettings(newUserId)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveNotificationSettings(newUserId: String): Result<Unit> {
        return notificationSettingsRepository.setNotificationSettings(newUserId).fold(
            onSuccess = {
                Result.success(Unit)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}