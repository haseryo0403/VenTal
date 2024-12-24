/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// [START sd_logging_import]
// const {Logging} = require("@google-cloud/logging");
// [END sd_logging_import]
// const functions = require('firebase-functions');
// const {onRequest} = require("firebase-functions/v2/https");
// const functions = require("firebase-functions");

const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const admin = require("firebase-admin");

admin.initializeApp();

// Firestore トリガー (ドキュメント作成時)
exports.sendNotificationOnCreate = onDocumentCreated(
    "users/{userId}/notifications/{notificationId}",
    async (event) => {
      try {
      // トリガーで取得したデータ
        const notificationData = event.data.data();
        const toUserId = event.params.userId;
        const {body, fromUserId, type, targetItemId} = notificationData;
        const notificationSettingsDoc = await admin.firestore()
            .doc("notificationSettings/"+toUserId).get();
        const fromUserDoc = await admin.firestore()
            .doc("users/"+fromUserId).get();
        const fromUser = fromUserDoc.data();

        const notificationSettings = notificationSettingsDoc.data();

        const {
          deviceToken,
          debateStartNotification,
          messageNotification,
          commentNotification,
        } = notificationSettings;

        //        const deviceToken = notificationSettings.deviceToken;

        const action = {
          DEBATESTART: " さんが討論を始めました。",
          DEBATEMESSAGE: " さんが反論しました。",
          DEBATECOMMENT: " さんが討論にコメントしました。",
        }[type];

        // TODO 通知のセッティングを確認して通知を送るか条件分岐
        const isEnableNotification = {
          DEBATESTART: debateStartNotification,
          DEBATEMESSAGE: messageNotification,
          DEBATECOMMENT: commentNotification,
        }[type];

        const title = fromUser.name+action;

        if (!deviceToken) {
          console.log(`User ${toUserId} has no device token`);
          return;
        }

        if (!isEnableNotification) {
          console.log(`User ${toUserId} turn off notification`);
          return;
        }

        // 通知メッセージを構築
        const message = {
          token: deviceToken,
          data: {
            title: title,
            body: body,
            fromUserImageURL: fromUser.photoURL,
            targetItemId: targetItemId,
          },
        };

        console.log("data", message.data);
        // メッセージ送信
        const response = await getMessaging().send(message);
        console.log("Successfully sent notification:", response);
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    },
);
