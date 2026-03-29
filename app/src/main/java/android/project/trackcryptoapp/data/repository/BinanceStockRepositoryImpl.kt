package android.project.trackcryptoapp.data.repository

import android.project.trackcryptoapp.data.local.StockDao
import android.project.trackcryptoapp.domain.repository.StockRepository
import android.project.trackcryptoapp.model.StockPrice
import android.project.trackcryptoapp.network.BinanceWebSocketListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinanceStockRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val dao: StockDao
) : StockRepository {
    private var webSocket: WebSocket? = null
    private val messageCounters = ConcurrentHashMap<String, Int>()

    override fun getStockPrices(symbols: List<String>): Flow<Map<String, List<StockPrice>>> {
        if (symbols.isEmpty()) return flowOf(emptyMap())

        val flows = symbols.map { symbol ->
            dao.getRecentPrices(symbol)
                .distinctUntilChanged()
                .map { symbol to it.reversed() }
        }
        return combine(flows) { array ->
            array.toMap()
        }
    }

    override fun startRealtimeSync(symbols: List<String>): Flow<Unit> = callbackFlow {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws")
            .build()

        val listener = BinanceWebSocketListener(
            symbols = symbols,
            onMessageReceived = { text ->
                parseMessage(text)?.let { stockPrice ->
                    trySend(stockPrice)
                }
            },
            onError = { error ->
                close(error)
            }
        )

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            disconnect()
        }
    }
    .onEach { stockPrice ->
        dao.insertPrice(stockPrice)
        
        // Optimize: Only delete old prices every 20 messages per ticker to reduce DB overhead
        val currentCount = messageCounters.getOrDefault(stockPrice.ticker, 0) + 1
        if (currentCount >= 20) {
            dao.deleteOldPrices(stockPrice.ticker)
            messageCounters[stockPrice.ticker] = 0
        } else {
            messageCounters[stockPrice.ticker] = currentCount
        }
    }
    .map { }

    private fun parseMessage(text: String): StockPrice? {
        return try {
            val json = JSONObject(text)
            if (json.has("e") && json.getString("e") == "aggTrade") {
                val ticker = json.getString("s")
                val price = json.getDouble("p")
                val timeMillis = json.getLong("T")
                
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timestamp = sdf.format(Date(timeMillis))

                StockPrice(
                    ticker = ticker,
                    price = price,
                    timestamp = timestamp,
                    timestampMillis = timeMillis
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun disconnect() {
        webSocket?.close(1000, "Disconnected by repository")
        webSocket = null
        messageCounters.clear()
    }
}
