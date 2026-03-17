package android.project.trackcryptoapp.data.repository

import android.project.trackcryptoapp.domain.repository.StockRepository
import android.project.trackcryptoapp.model.StockPrice
import android.project.trackcryptoapp.network.BinanceWebSocketListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class BinanceStockRepositoryImpl(
    private val client: OkHttpClient
) : StockRepository {
    private var webSocket: WebSocket? = null

    override fun getStockPriceStream(symbols: List<String>): Flow<StockPrice> = callbackFlow {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws")
            .build()

        val listener = BinanceWebSocketListener(
            symbols = symbols,
            onMessageReceived = { text ->
                parseMessage(text)?.let { trySend(it) }
            },
            onError = { error ->
                close(Exception(error))
            }
        )

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            disconnect()
        }
    }

    private fun parseMessage(text: String): StockPrice? {
        return try {
            val json = JSONObject(text)
            if (json.has("e") && json.getString("e") == "aggTrade") {
                val ticker = json.getString("s")
                val price = json.getDouble("p")
                val timeMillis = json.getLong("T")
                
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timestamp = sdf.format(Date(timeMillis))

                StockPrice(ticker, price, timestamp)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun disconnect() {
        webSocket?.close(1000, "Disconnected by repository")
        webSocket = null
    }
}
