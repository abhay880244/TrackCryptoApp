package android.project.trackcryptoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.project.trackcryptoapp.model.StockPrice
import android.project.trackcryptoapp.ui.PriceGraph
import android.project.trackcryptoapp.ui.theme.TrackCryptoAppTheme
import android.project.trackcryptoapp.viewmodel.StockViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val symbols = listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "ADAUSDT")

        setContent {
            TrackCryptoAppTheme {
                val navController = rememberNavController()
                val viewModel: StockViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.observeStocks(symbols)
                }

                NavHost(navController = navController, startDestination = "stock_list") {
                    composable("stock_list") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                @OptIn(ExperimentalMaterial3Api::class)
                                CenterAlignedTopAppBar(
                                    title = { Text("Crypto Market") },
                                    actions = {
                                        IconButton(onClick = { viewModel.toggleThrottling() }) {
                                            Icon(
                                                Icons.Default.Settings, 
                                                contentDescription = "Throttling",
                                                tint = if (uiState.isThrottlingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Column(modifier = Modifier.padding(innerPadding)) {
                                if (uiState.isThrottlingEnabled) {
                                    Text(
                                        "Throttling enabled (500ms updates)",
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                StockList(
                                    stockHistoryMap = uiState.stockPrices,
                                    isLoading = uiState.isLoading,
                                    error = uiState.error,
                                    onStockClick = { ticker ->
                                        navController.navigate("stock_details/$ticker")
                                    }
                                )
                            }
                        }
                    }
                    composable(
                        route = "stock_details/{ticker}",
                        arguments = listOf(navArgument("ticker") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
                        StockDetailsScreen(
                            ticker = ticker,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StockList(
    stockHistoryMap: Map<String, List<StockPrice>>, 
    isLoading: Boolean,
    error: String?,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && stockHistoryMap.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null && stockHistoryMap.isEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(stockHistoryMap.keys.toList()) { ticker ->
                    val history = stockHistoryMap[ticker] ?: emptyList()
                    if (history.isNotEmpty()) {
                        StockListItem(history.last(), onClick = { onStockClick(ticker) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun StockListItem(stock: StockPrice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stock.ticker,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "View details",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = "$${String.format("%.2f", stock.price)}",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsScreen(
    ticker: String,
    viewModel: StockViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history = uiState.stockPrices[ticker] ?: emptyList()
    val currentPrice = history.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ticker) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (currentPrice != null) {
                Text(
                    text = "$${String.format("%.2f", currentPrice.price)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (uiState.isThrottlingEnabled) "Sampled updates (500ms)" else "Real-time updates",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Price History (Last 50 points)",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PriceGraph(
                    prices = history,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    graphColor = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Statistics", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("High (Session)")
                            Text("$${String.format("%.2f", history.maxOfOrNull { it.price } ?: 0.0)}")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Low (Session)")
                            Text("$${String.format("%.2f", history.minOfOrNull { it.price } ?: 0.0)}")
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
