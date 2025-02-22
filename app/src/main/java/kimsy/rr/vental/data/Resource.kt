package kimsy.rr.vental.data

data class Resource<T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T): Resource<T> = Resource(Status.SUCCESS, data)
        fun <T> failure(message: String? = "不明なエラー", data: T? = null): Resource<T> =
            Resource(Status.FAILURE, data, message)
        fun <T> loading(data: T? = null): Resource<T> = Resource(Status.LOADING, data)
        fun <T> idle(): Resource<T> = Resource(Status.IDLE) // 初期状態用
    }
}

enum class Status {
    SUCCESS,
    FAILURE,
    LOADING,
    IDLE // 初期状態
}
