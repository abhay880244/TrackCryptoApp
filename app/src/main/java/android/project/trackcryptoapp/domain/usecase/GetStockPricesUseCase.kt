package android.project.trackcryptoapp.domain.usecase

import android.project.trackcryptoapp.domain.repository.StockRepository
import android.project.trackcryptoapp.model.StockPrice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockPricesUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(symbols: List<String>): Flow<Map<String, List<StockPrice>>> {
        return repository.getStockPrices(symbols)
    }
}
