package android.project.trackcryptoapp.domain.usecase

import android.project.trackcryptoapp.domain.repository.StockRepository
import javax.inject.Inject

class DisconnectUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke() {
        repository.disconnect()
    }
}
