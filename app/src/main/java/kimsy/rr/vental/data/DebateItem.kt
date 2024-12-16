package kimsy.rr.vental.data

import android.util.Log

data class DebateItem(
    val debate: Debate,
    val ventCard: VentCard,
    val poster: User,
    val debater: User,
    val likedUserType: UserType?
)object DebateItemSharedModel {
    //一気に登録用
    private var currentDebateItem: DebateItem? = null
    //個別用
    private var currentDebate: Debate? = null
    private var currentVentCard: VentCard? = null
    private var currentPoster: User? = null
    private var currentDebater: User? = null

    fun setDebateItem(debateItem: DebateItem) {
        Log.d("TLI", "debateItem set")
        currentDebateItem = debateItem
    }

    fun getDebateItem(): DebateItem? {
        Log.d("TLI", "debateItem get")
        return currentDebateItem
    }

    fun clearDebateItem() {
        currentDebateItem = null
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
