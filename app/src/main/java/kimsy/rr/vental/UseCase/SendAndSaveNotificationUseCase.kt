package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.NotificationRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import javax.inject.Inject

//class SendNotificationUseCase @Inject constructor(
//    private val notificationRepository: NotificationRepository,
//
//){
////TODO　先にFireStoreの通知ドキュメントにほぞんして、その後user情報も添えて通知すれば良いのでは？だからsendに必要なのはdeviceType, userImage, userName,title, body,typeかな
//    suspend operator fun invoke(
//    notificationData: NotificationData,
//    fromUser: User,
//    deviceToken: String): Result<Unit> {
//        val title = when(notificationData.type){
//            NotificationType.DEBATESTART -> "debate created"
//            NotificationType.DEBATEMESSAGE -> "new message"
//            NotificationType.DEBATECOMMENT -> "new comment"
//        }
//        // 通知を送信
//        return notificationRepository.sendNotification(notificationData, title, fromUser, deviceToken)
//    }
//}

class SendAndSaveNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase
) {
    suspend operator fun invoke(
        fromUserId: String,
        toUserId: String,
        body: String
        ): Result<Unit> {
        return try {
            val fromUser = getUserDetailsUseCase.execute(fromUserId).getOrThrow()
            val toUser = getUserDetailsUseCase.execute(toUserId).getOrThrow()
            val notificationData = NotificationData.createForDebateStart(fromUser.uid, body)
            // 通知をデータベースに保存
            notificationRepository.saveNotificationData(notificationData, toUser.uid)

            // 通知設定を取得
            val settings = notificationSettingsRepository.getNotificationSettings(toUser.uid).getOrThrow()

            // 通知タイプごとの処理
            val isNotificationEnabled = when (notificationData.type) {
                NotificationType.DEBATESTART -> settings.debateStartNotification
                NotificationType.DEBATEMESSAGE -> settings.messageNotification
                NotificationType.DEBATECOMMENT -> settings.commentNotification
            }

            if (isNotificationEnabled && settings.deviceToken.isNotEmpty()) {
                // 通知タイトルと本文を動的に設定
                val title = when (notificationData.type) {
                    NotificationType.DEBATESTART -> "New Debate Started!"
                    NotificationType.DEBATEMESSAGE -> "New Message in Debate!"
                    NotificationType.DEBATECOMMENT -> "New Comment on Debate!"
                }

                // 通知を送信
                notificationRepository.sendNotification(notificationData, title, settings.deviceToken)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
