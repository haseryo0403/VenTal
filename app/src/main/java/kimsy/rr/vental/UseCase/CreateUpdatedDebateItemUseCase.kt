package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.UserType
import javax.inject.Inject

class CreateUpdatedDebateItemUseCase@Inject constructor() {

    fun execute(
        debateItem: DebateItem,
        userType: UserType
    ): DebateItem {
        val newPosterLikeCount = when (debateItem.likedUserType) {
            UserType.POSTER -> debateItem.debate.posterLikeCount - 1
            //つまり　いいね済み　-> いいねしたいユーザータイプがposter　else　debater
            UserType.DEBATER -> if (userType == UserType.POSTER) debateItem.debate.posterLikeCount + 1 else debateItem.debate.posterLikeCount
            null -> if (userType == UserType.POSTER) debateItem.debate.posterLikeCount + 1 else debateItem.debate.posterLikeCount
        }

        val newDebaterLikeCount = when (debateItem.likedUserType) {
//            UserType.DEBATER -> {
//                if (userType == UserType.DEBATER || userType == UserType.POSTER){
//                    debateItem.debate.debaterLikeCount - 1
//                } else {
//                    debateItem.debate.debaterLikeCount
//                }
//            }
//            UserType.DEBATER -> if (userType == UserType.DEBATER) debateItem.debate.debaterLikeCount - 1 else debateItem.debate.debaterLikeCount - 1
            UserType.DEBATER -> debateItem.debate.debaterLikeCount - 1
            //つまり　いいね済み　-> いいねしたいユーザータイプがdebater　else　poster
            UserType.POSTER -> if (userType == UserType.DEBATER) debateItem.debate.debaterLikeCount + 1 else debateItem.debate.debaterLikeCount
            null -> if (userType == UserType.DEBATER) debateItem.debate.debaterLikeCount + 1 else debateItem.debate.debaterLikeCount
        }

        val newLikeUserType = when (debateItem.likedUserType) {
            userType -> null // 取り消し
            else -> userType // 新規または変更
        }

        return debateItem.copy(
            debate = debateItem.debate.copy(
                posterLikeCount = newPosterLikeCount,
                debaterLikeCount = newDebaterLikeCount
            ),
            likedUserType = newLikeUserType
        )
    }
}
