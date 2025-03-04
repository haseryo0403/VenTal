
## アプリの紹介

### サービス概要

このアプリは、悪口や意見を投稿し、反論を受けて「いいね」の数で実質的な勝敗が決まる議論型SNSです。

他のアプリやサイトで蔓延る悪口やアンチコメントをこのアプリに限定したいと考えています。
悪口を言われている人が見える範囲から隔離することで、傷つく人を減らすことができます。
見たい人だけがインストールし、見たくない人は避けることで、無意識に傷つくことを避けられます。

#### ▼インストール方法

もしご興味あれば、まだ内部テスト中ですので、以下リポジトリと同様にコメントもしくはメールにてgmailをお伝えください。招待いたします。

ryo.hasegawa.work@gmail.com

### 開発背景

私はアンチコメントに苦しむ配信者やXで見たくもない悪口を見た時に胸が苦しくなります。
そのためどうしたら悪口を減らせるかとよく考えていました。
しかし結局、悪口を無くすことはできないという結論にいたりました。

ではせめて悪口を隔離して傷つく人を減らせないかと考え、開発することにしました。

### 意識したこと

できる限りこのアプリ内では悪口を言ってもらいたく、設計の段階で選択を迫られることがでた場合はよりアプリがカオスになるように選択しました。

例えば普通のアプリでは投稿などは削除することができません。
現実で言ってしまったことは取り消せませんよね。

### メイン機能の使い方

| 悪口を投稿する                                                                                                                                                              | 悪口に反論する                                                                                                                                                                       | 討論を閲覧、コメントする                                                                                                                                                            |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img width="150" alt="VenTal GIF 2025年2月26日.gif" src="https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/3913183/60d31b91-3a44-4aab-8c19-8d2481845844.gif"> | <img width="150" alt="VenTal GIF Feb 26 2025 (2) (1).gif" src="https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/3913183/cd7d5c58-f876-46c0-8bfc-852a22c7359e.gif"> | <img width="150" alt="VenTal Video Feb 27 2025.gif" src="https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/3913183/2e6b095a-a08f-4854-bf2d-90e848f93d62.gif"> |
| まず下の＋ボタンを押して、作成画面へ。ストレスが溜まっていることや普通に悪口を記入して送信ボタンを押す                                                                                                                  | 他人の悪口はボトムバーの左から2つ目の画面で閲覧可能。右にスライドでいいね。左にスライドで反論作成画面へ。記入して送信すればVSがスタート                                                                                                         | VSはタイムラインに流れます。いいねは2人のうちどちらしかいいねできません。VSにメッセージを送れるのは参加者の2人のみ。コメントは全員できます。                                                                                               |

![]()







## 要件定義

SNS開発　Vental 要件定義
※モデリングもあるので最重要ポイントだけに絞っています
#### ■アプリ概要
悪口や意見などを投稿して、他のユーザーから反論があれば全ユーザーが閲覧するタイムラインに表示され二人のユーザーでいいねの数を比較する。
実質的に優劣がつく

#### ■アプリを作る背景
YouTubeのコメント欄などでで喧嘩したり誹謗中傷している人を減らしたい
ストレスの吐口になるアプリにしたい
コメントで優劣がつけられずはっきりさせたい
他のSNSでは悪口を言いづらい

#### ■現状の問題点
クラウド・セキュリティの知識が薄い

#### ■目指すゴール
3年で月間利用者数平均500万人を作る
リリース後6ヶ月で　月間利用者数　20万人
リリース後12ヶ月で　月間利用者数　50万人
リリース後24ヶ月で　月間利用者数　100万人
リリース後36ヶ月で　月間利用者数　500万人

#### ■クリアすべき課題
システムのセキュリティを強化
Udemy・書籍 で学習する
セキュリティ面のテストを行いブラッシュアップ

マーケティング、集客
1年目：売上 1,000万円
2年目：売上 2,000万円
3年目：売上 5,000万円
4年目：売上 7,000万円
5年目：売上 1億円


#### ■システム開発のポイント
ユーザー目線
安心してアプリを利用できる
シンプルに使えてアプリがごちゃごちゃしていない
悪口を言いやすい環境
適度なストレスと解放感を感じることができる
ビジネス目線
犯罪を助長する投稿やコメントは通報してもらって適切な対処ができるようにする
ユーザーの年齢層と性別を統計でき、アプリのブラッシュアップに繋げられる


#### ■開発予算、スケジュール
予算：50,000円
スケジュール（最初違うアプリ想定していたので記載の名前違います）

![image.png](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/3913183/556cec97-fc2d-4928-930d-3b4cd558a93f.png)

## モデリング

designのmodelingをご参照ください。

内訳
・ユースケース図
・アクティビティ図
・ドメインモデル

## 基本設計

Functional_Designをご参照ください。

内訳
・機能設計（画面遷移図と画面詳細）
・ER図
・データフローダイアグラム

## 詳細設計

機能一覧

https://drive.google.com/file/d/1iEn-RsRa66e3Oa47MsSZ2zD8mZfdOOJG/view?usp=sharing

Technical_Designをご参照ください。

内訳
・ロバストネス図
・シーケンス図

## インフラ構成図
![構成図.drawio.jpg](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/3913183/18433680-edec-4c5c-a81f-2f2df244ba5f.jpeg)

### NoSQL設計（元々はRDB想定だったので上の方ではER図を紹介しています。）

```
users:
  # collection
  - user_id: "user123"
    user_name: "山田太郎"
    user_imageURL: "https://example.com/image123.jpg"
    self_introduction: "はじめまして！スポーツが好きです。"
    new_user_flag: "true"
    following:
      # collection
      - followingUserId: "user456"
    follower_count: 120
    user_created_datetime: "2024-01-10T10:00:00Z"
    age_group: "20-29"
    gender: "male"
    account_closing_flag: false
    liked_SwipeCards:
      # collection
      - swipeCard_id: "card789"
      - liked_date: "2024-01-10T10:00:00Z"
    skipped_SwipeCards:
      # collection
      - swipeCard_id: "card678"
    debating_swipeCards:
      # collection
      -swipeCard_id: "card789"
    liked_discussions:
      # collection
      - discussion_id: "discussion456"
        user_type: "poster"  # いいねしたユーザーがposterかdebaterか
    notifications:
      # collection
      - notification_id: "notification123"
        link_id: "discussion456"
        #TODO これだとユーザー情報が変更されたときに対応できない。たぶんidだけが正解
        action_user_name: "佐藤花子"  # お知らせの内容
        action_user_imageURL: "https://example.com/image789.jpg"  # お知らせの内容
        action_user_id: "kdkkjojd",
        timestamp: "2024-11-03T12:00:00Z"  # ISO 8601形式での日時
        type: "comment"  # お知らせの種類
        read_flag: false  # 既読フラグ

    swipeCards:
      # collection
      - swipeCard_id: "card123"
        poster_id: "user123"
        # TODO: 冗長。以下2行はCloudFunctionsを使用してユーザー情報更新時に更新する予定。なしなら削除
        poster_name: "山田太郎"
        poster_imageURL: "https://example.com/image123.jpg"
        swipeCard_content: "これは私の投稿です！"
        swipeCard_imageURL: "https://example.com/card123.jpg"
        likeCount: 45
        tags:
          # array
          - tag: "school"
          - tag: "game"
        swipeCard_report_flag: false
        swipeCard_deletion_request_flag: false
        swipeCard_created_datetime: "2024-11-01T10:00:00Z"

        debates:
          # collection
          - debate_id: "discussion456"
            swipeCard_id: "card123"
            swipeCard_imageURL: "https://example.com/card123.jpg"
            poster_likeCount: 10
            # TODO: 冗長。以下3行はCloudFunctionsを使用してユーザー情報更新時に更新する予定。なしなら削除
            poster_id: "user123"
            poster_name: "山田太郎"
            poster_imageURL: "https://example.com/image123.jpg"
            debater_id: "user789"
            # TODO: 冗長。以下2行はCloudFunctionsを使用してユーザー情報更新時に更新する予定。なしならIDのみ保持
            debater_name: "佐藤花子"
            debater_imageURL: "https://example.com/image789.jpg"
            debater_likeCount: 5
            first_message_content: "基礎みたいなもんだろ"
            first_message_imageURL: "https://example.com/card123.jpg"
            debate_report_flag: false
            debate_deletion_request_flag: false
            debate_created_datetime: "2024-11-02T15:00:00Z"

          # TODO: sender,debater側の視点では右寄左寄せは変えたいので、userTypeだとそれができないと思ったが、討論のほうで照合すればいいのか
            messages:
              # collection
              - message_id: "message123"
                userType: "debater"  # posterかdebaterか
                message_content: "それは面白いですね！"
                sent_datetime: "2024-11-02T16:00:00Z"

            comments:
              # collection
              - comment_id: "comment123"
                commenter_id: "user789"
                # TODO: 冗長。以下2行はCloudFunctionsを使用してユーザー情報更新時に更新する予定。なしならIDのみ保持
                commenter_name: "佐藤花子"
                commenter_imageURL: "https://example.com/image789.jpg"
                comment_content: "そうですね！"
                commented_datetime: "2024-11-02T16:05:00Z"

#withdrawals:
#  # collection
#  - withdrawalDate_userId: "2024-10-01_user123"
#    reason: "個人的な理由"
#    reLoginDate: "2024-10-20"

withdrawals:
  # collection
  - userId: "user123"
    withdrawalDate: "2024-10-01"
    reason: "個人的な理由"
    reLoginDate: "2024-10-20"

tags:
  # collection
  #IDはいらないかも
  #まだ実装できてない
  - tagId: "tag001"
    tagContent: "スポーツ"
    usedCount: 100
  - tagId: "tag002"
    tagContent: "音楽"
    usedCount: 5

# TODO ここにdeviceTOkenありじゃね
notificationSettings:
  # collection
  -userId: "user123"
  debateStartEnabled: true
  messageEnabled: true
  commentEnabled: true

reports:  # 通報の親コレクション
  debates:  # 議論に関する通報親コレクション
    debateId: # 各議論に関する通報コレクション
      - reporterId: "user789"
        reason: 1
        reportedDateTime: 2024-01-10T10:00:00Z

deleteRequests: #削除依頼の親コレ
  debates: #討論の削除依頼
    debateId: #討論ごとの削除依頼コレ
      - requesterId: "user789"
        contentId:
        userType: "poster"
        reason: 3
        requestedDateTime: 2024-01-10T10:00:00Z


notificationsLog: #将来入れるかも
```

## 選定技術の採用理由

#### FirebaseかAmplifyか
・Firebaseは無料枠が大きいので、予算5万ということを加味しました。
・udemyでFirebaseを組み合わせている講座が多く、学習コストが安いと考えました。
・Amplifyの方がスケーラビリティや大規模になった際のコストは良いと思いましたが、このアプリの目標ユーザー数の推移が緩やかであるので推移に合わせて移行することもありかと考え、初期構築コストを優先しました。

#### なぜRDBではなくNoSQL？
実はこのアプリの前にJava、spring bootでTODOリストを作成した際にRDBを使った上に、DBAシルバーとSQLシルバーの資格も取っていたのであえてNoSQLを選びました。
設計段階ではRDB以外考慮していなかったのですが。

#### Kotlin
このアプリ作成の直前までJavaを使っていたので、Javaに似ているというか進化系のようなKotlinを使ってみたかったからです。

## ポートフォリオ作成までに学習したこと

### Udemy

・【超初心者向け】【システム開発の流れから学ぶ】エンジニアとして活躍するための知識・スキルを明確化！【現場を知る】
・【入門】システム要件定義と基本設計（実践ワークで理解する上流工程の進め方）
・【超実践】ビジネス要件分析・基本設計・詳細設計をやり抜く実践ワーク講座
・はじめての Kotlin【Java 知らなくてOK！丁寧な解説で Android に必要な Kotlin の基本を学習】
・Udemy The Complete Android 14 & Kotlin Development Masterclass

## アプリ作成の進め方

#### 1.アイデア出し
なるべく誰かのためになるようなアプリ、経験できる機能を網羅できることを意識しました。

#### 2.要件定義
以降の設計で判断を迫られた際に基準となるように使用するユーザーの層やアプリの方向性を決めました。
実際に若い層を想定していたので丁寧すぎず、シンプルに若い層が直感的に使えるような設計を心がけていました。

#### 3.モデリング
・ユースケース図
・アクティビティ図
・ドメインモデル

ここでかなりアプリのイメージが沸きました。
ユースケース図の段階で何ができて、何をできないアプリにするかを考えられたので基本設計をスムーズに進められました。

#### 4.基本設計
・機能設計（画面遷移図と画面詳細）
・ER図
・データフローダイアグラム

実装では特に機能設計が重宝しました。
画面遷移図を丁寧に作成したので詳細設計で楽できたと思います。


#### 5.詳細設計
・機能一覧
・ロバストネス図
・シーケンス図

ここまで必要かと思われる方もいるかと思いますが、実際作ってみると要件漏れなどが多く見つかり全体の質がかなり上がりました。

#### 6.コード実装
ただ一言。
「楽しい」
エラーが出ても楽しい。
自分が設計してきたものが形になって手で触れることがとても幸せでした。

#### 7.テスト
まだ終わってません...

## おわりに

ポートフォリオを作成される方には、ここまで綿密に設計することはあまりおすすめしません。途中で心が折れてしまうかもしれません…（笑）。
私は「1からすべて自分でやってみたい」という思いが強かったので、しっかりと設計に取り組みましたが、正直なところ、経験が足りずに設計の半分以上が無駄になってしまった部分もありました。ただ、それも覚悟の上で進めていたので、大きな問題ではありませんでした。
設計の段階では、理解しきれていないまま進めることも多かったのですが、その過程で要件の抜け漏れに気づくなど、多くの学びがありました。また、次の設計工程に進むことで、前の段階で行ったことを後から理解できるようになり、何度も修正を繰り返していました。
そうした試行錯誤の積み重ねがあったからこそ、今回のアプリではある程度、責務を意識して作ることができたのではないかと思っています。

時間は思った以上にかかってしまいましたが、実際に形になり、手で触れられるようになったときの感動は何にも代えがたいものがあり、とても良い経験になりました。
