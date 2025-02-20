package kimsy.rr.vental.ui.CommonComposable


import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

interface ConnectionSampleScrollState {
    val nestedScrollConnection: NestedScrollConnection
    val offset: Dp
}

@Stable
class ConnectionSample(
    maxOffset: Dp,
    initialOffset: Dp,
    private val density: Density,
) : ConnectionSampleScrollState {
    private val maxOffsetPx = with(density) { maxOffset.toPx() }
    private val initialOffsetPx = with(density) { initialOffset.toPx() }
    private var _offsetPx by mutableFloatStateOf(initialOffsetPx)
    override val offset: Dp
        get() = with(density) { _offsetPx.toDp() }

    override val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if ((available.y >= 0f) or (_offsetPx <= 0f)) return Offset.Zero
            val consumedY = doScroll(available.y)
            return Offset(0f, consumedY)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
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