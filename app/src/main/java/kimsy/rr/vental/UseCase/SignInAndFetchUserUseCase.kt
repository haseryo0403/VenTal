package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SignInAndFetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
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
}
