package kimsy.rr.vental.ui.CommonComposable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

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
