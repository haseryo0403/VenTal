package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import javax.inject.Inject

//abstract class BaseUseCase @Inject constructor(
//    private val networkUtils: NetworkUtils
//) {
//    protected suspend fun <T> executeWithNetworkCheck(action: suspend () -> Resource<T>): Resource<T> {
//        return if (!networkUtils.isOnline()) {
//            Resource.failure("インターネットの接続を確認してください")
//        } else {
//            action()
//        }
//    }
//}