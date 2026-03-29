package android.project.trackcryptoapp.domain.repository

import android.project.trackcryptoapp.model.StockPrice
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    /**
     * Returns a flow of the most recent stock prices for the given symbols.
     * This should be the single source of truth, combining local and remote data.
     */
    fun getStockPrices(symbols: List<String>): Flow<Map<String, List<StockPrice>>>
    
    /**
     * Starts the remote data sync and saves updates to local storage.
     */
    fun startRealtimeSync(symbols: List<String>): Flow<Unit>

    fun disconnect()
}
