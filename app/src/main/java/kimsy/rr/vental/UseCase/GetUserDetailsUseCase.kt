package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(uid: String): Resource<User> {
//        return try {
//            Log.d("GUDUC", "called")
          return  userRepository.fetchUserInformation(uid)
//        } catch (e: Exception) {
//            Resource.failure(e.message)
//        }
    }
}