package android.project.trackcryptoapp.viewmodel

import android.project.trackcryptoapp.domain.repository.StockRepository
import android.project.trackcryptoapp.model.StockPrice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    private val repository: StockRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    fun toggleThrottling() {
        _uiState.update { it.copy(isThrottlingEnabled = !it.isThrottlingEnabled) }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun observeStocks(symbols: List<String>) {
        // Create a shared flow that stays active even when switching between throttled/unthrottled modes.
        // WhileSubscribed(5000) keeps the connection alive for 5 seconds after the last subscriber leaves,
        // which prevents reconnection during the brief toggle switch.
        val sharedPriceFlow = repository.getStockPriceStream(symbols)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 0
            )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            _uiState
                .map { it.isThrottlingEnabled }
                .distinctUntilChanged()
                .flatMapLatest { enabled ->
                    if (enabled) {
                        sharedPriceFlow.sample(500)
                    } else {
                        sharedPriceFlow
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { stockPrice ->
                    _uiState.update { state ->
                        val currentHistory = state.stockPrices[stockPrice.ticker] ?: emptyList()
                        val newHistory = (currentHistory + stockPrice).takeLast(50)
                        state.copy(
                            stockPrices = state.stockPrices + (stockPrice.ticker to newHistory),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
