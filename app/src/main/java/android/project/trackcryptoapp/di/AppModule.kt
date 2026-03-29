package android.project.trackcryptoapp.di

import android.app.Application
import android.project.trackcryptoapp.data.local.StockDao
import android.project.trackcryptoapp.data.local.StockDatabase
import android.project.trackcryptoapp.data.repository.BinanceStockRepositoryImpl
import android.project.trackcryptoapp.domain.repository.StockRepository
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideStockDatabase(app: Application): StockDatabase {
        return Room.databaseBuilder(
            app,
            StockDatabase::class.java,
            "stock_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStockDao(db: StockDatabase): StockDao {
        return db.dao
    }

    @Provides
    @Singleton
    fun provideStockRepository(client: OkHttpClient, dao: StockDao): StockRepository {
        return BinanceStockRepositoryImpl(client, dao)
    }
}
