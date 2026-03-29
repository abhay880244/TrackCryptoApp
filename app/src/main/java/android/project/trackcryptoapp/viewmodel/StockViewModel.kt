package android.project.trackcryptoapp.viewmodel

import android.project.trackcryptoapp.domain.repository.StockRepository
import android.project.trackcryptoapp.model.StockPrice
import android.project.trackcryptoapp.network.NetworkMonitor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockUiState(
    val stockPrices: Map<String, List<StockPrice>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isThrottlingEnabled: Boolean = false
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    fun toggleThrottling() {
        _uiState.update { it.copy(isThrottlingEnabled = !it.isThrottlingEnabled) }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun observeStocks(symbols: List<String>) {
        // Start synchronization when online
        viewModelScope.launch {
            networkMonitor.isOnline
                .flatMapLatest { isOnline ->
                    if (isOnline) {
                        repository.startRealtimeSync(symbols)
                            .retry(3) { e ->
                                (e is Exception).also { if (it) delay(2000) }
                            }
                    } else {
                        flow { 
                            _uiState.update { it.copy(error = "No internet connection") }
                        }
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect()
        }

        // Observe data from Single Source of Truth (Database)
        viewModelScope.launch {
            _uiState
                .map { it.isThrottlingEnabled }
                .distinctUntilChanged()
                .flatMapLatest { enabled ->
                    val dataFlow = repository.getStockPrices(symbols)
                    if (enabled) {
                        dataFlow.sample(500)
                    } else {
                        dataFlow
                    }
                }
                .collect { pricesMap ->
                    _uiState.update { it.copy(
                        stockPrices = pricesMap,
                        isLoading = false,
                        error = if (it.error == "No internet connection") it.error else null
                    ) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
