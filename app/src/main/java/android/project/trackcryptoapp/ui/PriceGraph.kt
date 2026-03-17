package android.project.trackcryptoapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import android.project.trackcryptoapp.model.StockPrice

@Composable
fun PriceGraph(
    prices: List<StockPrice>,
    modifier: Modifier = Modifier,
    graphColor: Color = Color.Green
) {
    if (prices.size < 2) return

    val minPrice = prices.minOf { it.price }
    val maxPrice = prices.maxOf { it.price }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val spaceBetweenPoints = size.width / (prices.size - 1)
        val path = Path().apply {
            prices.forEachIndexed { index, stockPrice ->
                val x = index * spaceBetweenPoints
                val normalizedPrice = (stockPrice.price - minPrice) / priceRange
                val y = size.height - (normalizedPrice.toFloat() * size.height)
                
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        drawPath(
            path = path,
            color = graphColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
