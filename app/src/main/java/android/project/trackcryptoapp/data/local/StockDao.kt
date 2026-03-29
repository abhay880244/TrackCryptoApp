package android.project.trackcryptoapp.data.local

import android.project.trackcryptoapp.model.StockPrice
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_prices WHERE ticker = :ticker ORDER BY timestampMillis DESC LIMIT 50")
    fun getRecentPrices(ticker: String): Flow<List<StockPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(stockPrice: StockPrice)

    @Query("DELETE FROM stock_prices WHERE ticker = :ticker AND id NOT IN (SELECT id FROM stock_prices WHERE ticker = :ticker ORDER BY timestampMillis DESC LIMIT 50)")
    suspend fun deleteOldPrices(ticker: String)
}
