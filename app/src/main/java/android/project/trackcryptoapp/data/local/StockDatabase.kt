package android.project.trackcryptoapp.data.local

import android.project.trackcryptoapp.model.StockPrice
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StockPrice::class], version = 1, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract val dao: StockDao
}
