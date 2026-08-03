package android.project.trackcryptoapp.domain.usecase

import android.project.trackcryptoapp.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncStockPricesUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(symbols: List<String>): Flow<Unit> {
        return repository.startRealtimeSync(symbols)
    }
}
