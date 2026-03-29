package android.project.trackcryptoapp.network

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class BinanceWebSocketListener(
    private val symbols: List<String>,
    private val onMessageReceived: (String) -> Unit,
    private val onError: (Throwable) -> Unit
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        val subscribeMessage = JSONObject().apply {
            put("method", "SUBSCRIBE")
            put("params", JSONArray(symbols.map { "${it.lowercase()}@aggTrade" }))
            put("id", 1)
        }
        webSocket.send(subscribeMessage.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onMessageReceived(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onError(t)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }
}
