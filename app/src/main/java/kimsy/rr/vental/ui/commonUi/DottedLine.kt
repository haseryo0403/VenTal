package kimsy.rr.vental.ui.commonUi

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

@Composable
fun DottedLine(modifier: Modifier, color: Color) {
    Canvas(
        modifier = modifier
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 4f,
            pathEffect = pathEffect
        )
    }
}
