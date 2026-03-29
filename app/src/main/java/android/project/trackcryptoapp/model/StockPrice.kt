package android.project.trackcryptoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_prices")
data class StockPrice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val price: Double,
    val timestamp: String,
    val timestampMillis: Long
)
