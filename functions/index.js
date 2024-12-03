/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
// const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// Firestore トリガー (ドキュメント作成時)
exports.sendNotificationOnCreate = onDocumentCreated(
    "users/{userId}/notifications/{notificationId}",
    async (event) => {
      try {
      // トリガーで取得したデータ
        const notificationData = event.data.data();
        const toUserId = event.params.userId;
        //        const {body, fromUserId} = notificationData;
        const {body, fromUserId, type} = notificationData;
        const notificationSettingsDoc = await admin.firestore()
            .doc("notificationSettings/"+toUserId).get();
        const fromUserDoc = await admin.firestore()
            .doc("users/"+fromUserId).get();
        const fromUser = fromUserDoc.data();

        const notificationSettings = notificationSettingsDoc.data();

        const deviceToken = notificationSettings.deviceToken;

        const action = {
          DEBATESTART: " さんが討論を始めました。",
          DEBATEMESSAGE: " さんが反論しました。",
          DEBATECOMMENT: " さんが討論にコメントしました。",
        }[type];

        const title = fromUser.name+action;

        //        const fromUserName = fromUser.name;

        if (!deviceToken) {
          console.log(`User ${toUserId} has no device token`);
          return;
        }

        // 通知メッセージを構築
        const message = {
          token: deviceToken,
          //          notification: {
          //            title: title,
          //            body: body,
          //          },
          data: {
            title: title,
            body: body,
            fromUserImageURL: fromUser.photoURL,
          },
        };

        // メッセージ送信
        const response = await getMessaging().send(message);
        console.log("Successfully sent notification:", response);
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    },
);

// HTTPリクエストでFirestoreのユーザーデータを取得する関数

// exports.getUser = functions.https.onRequest(async (request, response) => {
//  try {
//    // Firestore ドキュメントを取得
//    const doc = await admin.firestore()
//        .doc("users/bhNuS520Jld2M6lQBMX3hGBboCN2").get();
//
//    if (!doc.exists) {
//      response.status(404).send("User not found");
//      return;
//    }
//
//    // ドキュメントのデータをレスポンスとして返す
//    response.status(200).send(doc.data());
//  } catch (error) {
//    console.error("Error fetching user:", error);
//    response.status(500).send("Internal Server Error");
//  }
// });


// exports.getUser = functions.https.onRequest((request, response) => {
//    // Firestore ドキュメントを取得
//    const promise = admin.firestore()
// .doc("users/bhNuS520Jld2M6lQBMX3hGBboCN2").get()
//    .then(snapshot => {
//        const data = snapshot.data();
//        response.send(data);
//    })
//    .catch(error => {
//        console.log(error);
//        response.status(500).send(error);
//    });
// });
