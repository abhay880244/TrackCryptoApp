# TrackCryptoApp

Tracking the crypto market - all in one

### Features
- Real-time cryptocurrency price tracking via Binance WebSocket
- Live price updates for BTC, ETH, BNB, SOL, ADA
- **Local caching** with Room database (last 50 prices per ticker)
- **Offline support** - view cached prices without internet connection
- **Network-aware** - automatically syncs when connection restored
- Interactive price history graphs
- Toggle between real-time and throttled (500ms) updates
- Session statistics (high/low prices)
- Material Design 3 UI with dark/light theme support


Solarized dark             |  Solarized Ocean
:-------------------------:|:-------------------------:
![](https://github.com/user-attachments/assets/c192c7a8-7a17-4a7d-b9be-5db71786df51)  |  ![](https://github.com/user-attachments/assets/d0d504e4-d39f-4a1f-9c77-1d7b75e5cb01)

### Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM with Clean Architecture
- **Dependency Injection:** Hilt
- **Networking:** OkHttp WebSocket
- **Local Storage:** Room Database
- **Navigation:** Navigation Compose
- **Build:** Gradle with Kotlin DSL

### Project Structure
```
app/src/main/java/android/project/trackcryptoapp/
├── MainActivity.kt              # Entry point, Compose UI & navigation
├── TrackCryptoApplication.kt   # Hilt Application class
├── di/
│   └── AppModule.kt            # Dependency injection module
├── domain/
│   └── repository/
│       └── StockRepository.kt  # Repository interface
├── data/
│   ├── repository/
│   │   └── BinanceStockRepositoryImpl.kt  # Repository impl with DB cache
│   └── local/
│       ├── StockDatabase.kt    # Room database
│       └── StockDao.kt         # Data access object
├── network/
│   ├── BinanceWebSocketListener.kt  # WebSocket handler
│   └── NetworkMonitor.kt      # Network connectivity monitor
├── model/
│   └── StockPrice.kt          # Data model (Room entity)
├── viewmodel/
│   └── StockViewModel.kt      # UI state management
└── ui/
    ├── PriceGraph.kt          # Price chart component
    └── theme/                 # Theme configuration
```

### Building & Running

**Prerequisites:**
- Android Studio Iguana (2023.3.1) or newer
- Android SDK with API level 35
- JDK 11 or newer

**Build commands:**
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew installDebug       # Install on connected device
./gradlew clean build        # Clean and build
```

**Run from Android Studio:**
1. Open the project in Android Studio
2. Let Gradle sync and download dependencies
3. Select a device (emulator or physical)
4. Click Run (Shift+F10) or use the Run button

### Testing
```bash
./gradlew test                       # Run unit tests
./gradlew connectedAndroidTest       # Run instrumentation tests
./gradlew lint                       # Run lint checks
./gradlew lintFix                    # Auto-fix lint issues
```

### API Integration

The app connects to Binance's public WebSocket stream for real-time trade data:
- **Endpoint:** `wss://stream.binance.com:9443/ws`
- **Subscription:** One stream per symbol (e.g., `btcusdt@aggTrade`)
- **Message format:** `aggTrade` events with `s` (symbol), `p` (price), `T` (timestamp)
- **No authentication required** for public trade data

All messages are cached in Room for offline viewing and graph rendering.

### Key Architecture Patterns

**Data Flow:**
- **Single Source of Truth:** Room database caches all price data
- **Repository pattern:** `BinanceStockRepositoryImpl` coordinates WebSocket sync and DB operations
- **Flow-based:** Repository exposes `Flow<StockPrice>`; ViewModel transforms with `sample()`, `combine()`
- **Optimistic DB writes:** WebSocket messages → Room DB → UI updates

**State Management:**
- Immutable `StockUiState` exposed via `StateFlow`
- `collectAsStateWithLifecycle()` for Compose UI
- Network monitoring with `NetworkMonitor` - auto-reconnects when online

**Performance Optimizations:**
- Throttling mode: 500ms sampling via `Flow.sample()`
- DB cleanup: Background deletion of old prices (keeps last 50 per ticker)
- `distinctUntilChanged()` prevents unnecessary UI recompositions

**Offline Support:**
- Room persists historical prices
- `NetworkMonitor` tracks connectivity state
- Repository reads from DB even when offline
- Auto-reconnect when network restored

### Configuration
- **Default symbols:** BTCUSDT, ETHUSDT, BNBUSDT, SOLUSDT, ADAUSDT
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Compose BOM:** 2024.12.01
- **Kotlin:** 2.0.21
- **Room:** 2.6.1
- **OkHttp:** 4.12.0
- **Hilt:** 2.54
- **Lifecycle Runtime:** 2.8.7
- **Navigation Compose:** 2.8.5
- **Activity Compose:** 1.9.3

### Notes
- **Throttling mode:** Toggle to sample updates every 500ms (reduces UI updates while preserving data integrity)
- **Price graph:** Custom Canvas-based line chart with last 50 data points per ticker
- **Network-aware:** Automatically fetches live data when online, shows cached data when offline
- **All dependencies** managed via version catalog (`gradle/libs.versions.toml`)

https://github.com/user-attachments/assets/b59f5a4d-2187-465a-8bc6-530d8bd49a6b

