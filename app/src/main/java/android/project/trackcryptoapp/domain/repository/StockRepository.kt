package android.project.trackcryptoapp.domain.repository

import android.project.trackcryptoapp.model.StockPrice
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStockPriceStream(symbols: List<String>): Flow<StockPrice>
    fun disconnect()
}
