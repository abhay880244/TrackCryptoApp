package android.project.trackcryptoapp.network

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class TiingoWebSocketListener(
    private val apiKey: String,
    private val symbols: List<String>,
    private val onMessageReceived: (String) -> Unit,
    private val onError: (String) -> Unit
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Subscribe to symbols upon connection
        val subscribeMessage = JSONObject().apply {
            put("eventName", "subscribe")
            put("authorization", apiKey)
            val eventData = JSONObject().apply {
                // Removed thresholdLevel as it is restricted for certain tiers (like Free)
                put("tickers", JSONArray(symbols))
            }
            put("eventData", eventData)
        }
        webSocket.send(subscribeMessage.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onMessageReceived(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onError(t.message ?: "Unknown error")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }
}
