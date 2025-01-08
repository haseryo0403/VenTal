package kimsy.rr.vental.ui.CommonComposable

fun <T> SplitList(array: List<T>, n: Int): List<List<T>> {
    return array.foldIndexed(mutableListOf<List<T>>()) { index, acc, element ->
        if (index % n == 0) {
            acc.add(listOf(element)) // 新しいグループを作成
        } else {
            val lastGroup = acc.last().toMutableList()
            lastGroup.add(element)
            acc[acc.size - 1] = lastGroup // 最後のグループを更新
        }
        acc
    }
}