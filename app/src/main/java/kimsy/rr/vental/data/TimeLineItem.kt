package kimsy.rr.vental.data

import android.util.Log

data class TimeLineItem(
//    val debateId: String = "",
    val debate: Debate,
    val ventCard: VentCard,
    val poster: User,
    val debater: User
)object TimeLineItemSharedModel {
    //一気に登録用
    private var currentTimeLineItem: TimeLineItem? = null
    //個別用
    private var currentDebate: Debate? = null
    private var currentVentCard: VentCard? = null
    private var currentPoster: User? = null
    private var currentDebater: User? = null

    fun setTimeLineItem(timeLineItem: TimeLineItem) {
        Log.d("TLI", "timeLineItem set")
        currentTimeLineItem = timeLineItem
    }

    fun getTimeLineItem(): TimeLineItem? {
        Log.d("TLI", "timeLineItem get")
        return currentTimeLineItem
    }

    fun clearTimeLineItem() {
        currentTimeLineItem = null
    }

    fun setDebate(debate: Debate) {
        Log.d("TLI", "debate set")
        currentDebate = debate
    }

    fun getDebate(): Debate? {
        Log.d("TLI", "debate get")
        return currentDebate
    }

    fun clearDebate() {
        currentDebate = null
    }

    fun setVentCard(ventCard: VentCard) {
        Log.d("TLI", "ventCard set")
        currentVentCard = ventCard
    }

    fun getVentCard(): VentCard? {
        Log.d("TLI", "ventCard set")
        return currentVentCard
    }

    fun clearVentCard() {
        currentVentCard = null
    }

    fun setPoster(poster: User) {
        Log.d("TLI", "Poster set")
        currentPoster = poster
    }

    fun getPoster(): User? {
        Log.d("TLI", "Poster set")
        return currentPoster
    }

    fun clearPoster() {
        currentPoster = null
    }

    fun setDebater(debater: User) {
        Log.d("TLI", "Debater set")
        currentDebater = debater
    }

    fun getDebater(): User? {
        Log.d("TLI", "Debater set")
        return currentDebater
    }

    fun clearDebater() {
        currentDebater = null
    }



}
