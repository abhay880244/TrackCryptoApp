package android.project.trackcryptoapp.di

import android.project.trackcryptoapp.data.repository.BinanceStockRepositoryImpl
import android.project.trackcryptoapp.domain.repository.StockRepository
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
    fun provideStockRepository(client: OkHttpClient): StockRepository {
        return BinanceStockRepositoryImpl(client)
    }
}
