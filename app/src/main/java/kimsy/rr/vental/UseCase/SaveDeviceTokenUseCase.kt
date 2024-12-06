package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SaveDeviceTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(userId: String){
        userRepository.saveDeviceToken(userId)
            .onSuccess {
                Log.d("SDTUC","saved device token" )
            }
            .onFailure {
                Log.e("SDTUC", "save device token failed")
                //TODO リカバリー必要なら。いまのところ通知できないからスルー
            }
    }
}