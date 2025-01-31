package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SaveDeviceTokenUseCase @Inject constructor(
    private val userRepository: UserRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(userId: String): Resource<Unit>{
        return executeWithLoggingAndNetworkCheck {
            userRepository.saveDeviceToken(userId)
            Resource.success(Unit)
        }
//        userRepository.saveDeviceToken(userId)
//            .onSuccess {
//                Log.d("SDTUC","saved device token" )
//            }
//            .onFailure {
//                Log.e("SDTUC", "save device token failed")
//                //TODO リカバリー必要なら。いまのところ通知できないからスルー
//            }
    }
}