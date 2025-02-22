package kimsy.rr.vental.ui.CommonComposable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RememberScrollState(maxOffset: Dp, initialOffset: Dp): Pair<NestedScrollConnection, Dp> {
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { maxOffset.toPx() }
    val initialOffsetPx = with(density) { initialOffset.toPx() }
    var offsetPx by remember { mutableStateOf(initialOffsetPx) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if ((available.y >= 0f) || (offsetPx <= 0f)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if ((available.y <= 0f) || (offsetPx >= maxOffsetPx)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            private fun doScroll(delta: Float): Float {
                val oldOffset = offsetPx
                offsetPx = (offsetPx + delta).coerceIn(0f, maxOffsetPx)
                return offsetPx - oldOffset
            }
        }
    }

    // Dpに変換して返す
    val offsetDp = with(density) { offsetPx.toDp() }

    return nestedScrollConnection to offsetDp
}


@Composable
fun RememberScrollStates(): Pair<NestedScrollConnection, Dp> {
    val density = LocalDensity.current

    // `maxOffset` を動的に計算
    var maxOffsetPx by remember { mutableStateOf(0f) }
    var offsetPx by remember { mutableStateOf(0f) }

    // UI要素の高さを測定し、`maxOffset` を更新
    val measureModifier = Modifier.onSizeChanged { size ->
        maxOffsetPx = with(density) { size.height.toFloat() }
        offsetPx = maxOffsetPx // 初期値は最大値（閉じた状態）
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if ((available.y >= 0f) || (offsetPx <= 0f)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if ((available.y <= 0f) || (offsetPx >= maxOffsetPx)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            private fun doScroll(delta: Float): Float {
                val oldOffset = offsetPx
                offsetPx = (offsetPx + delta).coerceIn(0f, maxOffsetPx)
                return offsetPx - oldOffset
            }
        }
    }

    // `Dp` に変換して返す
    val offsetDp = with(density) { offsetPx.toDp() }

    return nestedScrollConnection to offsetDp
}

@Composable
fun rememberConnectionSampleScrollState(
    initialOffset: Dp = 200.dp,
    density: Density = LocalDensity.current
): ConnectionSampleScrollState {
    // 状態をrememberで保持
    val maxOffsetPx = with(density) { initialOffset.toPx() }
//    val offsetState = remember { mutableStateOf(maxOffsetPx) }
    val maxOffsetState = remember { mutableStateOf(maxOffsetPx) }
    var offsetPx by remember { mutableStateOf(maxOffsetPx) }


    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if ((available.y >= 0f) || (offsetPx <= 0f)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if ((available.y <= 0f) || (offsetPx >= maxOffsetPx)) return Offset.Zero
                val consumedY = doScroll(available.y)
                return Offset(0f, consumedY)
            }

            private fun doScroll(delta: Float): Float {
                val oldOffset = offsetPx
                offsetPx = (offsetPx + delta).coerceIn(0f, maxOffsetPx)
                return offsetPx - oldOffset
            }
        }
    }

    // スクロール処理の実体
    fun doScroll(delta: Float): Float {
        val oldOffset = offsetPx
        offsetPx = (offsetPx + delta).coerceIn(0f, maxOffsetState.value)
        return offsetPx - oldOffset
    }

    // 動的にサイズ変更された場合にオフセットを更新する
    fun updateOffsets(height: Int) {
        maxOffsetState.value = height.toFloat()
        offsetPx = height.toFloat() // 初期オフセットも動的に設定
    }

    return object : ConnectionSampleScrollState {
        override val offset: Dp
            get() = with(density) { offsetPx.toDp() }

        override val nestedScrollConnection: NestedScrollConnection
            get() = nestedScrollConnection
    }
}

