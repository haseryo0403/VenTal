package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val userRepository: UserRepository
){
    suspend fun execute ():Result<Unit>  {
        return userRepository.signOutFromFirebaseAuth()
            .onSuccess {
                userRepository.signOutFromGoogle()
            }
    }
}