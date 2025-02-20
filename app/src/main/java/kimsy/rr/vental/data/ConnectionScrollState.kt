package kimsy.rr.vental.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp


interface ConnectionScrollStateInterface {
    val nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    val offset: Dp
}

@Stable
class ConnectionScrollState(
    maxOffset: Dp,
    initialOffset: Dp,
    private val density: androidx.compose.ui.unit.Density,
) : ConnectionScrollStateInterface {
    private val maxOffsetPx = with(density) { maxOffset.toPx() }
    private val initialOffsetPx = with(density) { initialOffset.toPx() }
    private var _offsetPx by mutableStateOf(initialOffsetPx)
    override val offset: Dp
        get() = with(density) { _offsetPx.toDp() }

    override val nestedScrollConnection = object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
            if ((available.y >= 0f) or (_offsetPx <= 0f)) return Offset.Zero
            val consumedY = doScroll(available.y)
            return Offset(0f, consumedY)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
        ): Offset {
            if ((available.y <= 0f) or (_offsetPx >= maxOffsetPx)) return Offset.Zero
            val consumedY = doScroll(available.y)
            return Offset(0f, consumedY)
        }
    }

    private fun doScroll(delta: Float): Float {
        val oldOffset = _offsetPx
        _offsetPx = (_offsetPx + delta).coerceIn(0f, maxOffsetPx)
        return _offsetPx - oldOffset
    }
}
