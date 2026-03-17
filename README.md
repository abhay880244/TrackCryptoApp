# TrackCryptoApp

Tracking the crypto market - all in one

### Features
- Real-time cryptocurrency price tracking via Binance WebSocket
- Live price updates for BTC, ETH, BNB, SOL, ADA
- Interactive price history graphs with last 50 data points
- Toggle between real-time and throttled (500ms) updates
- Session statistics (high/low prices)
- Material Design 3 UI with dark/light theme support

### Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM with Clean Architecture
- **Dependency Injection:** Hilt
- **Networking:** OkHttp WebSocket
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
│   └── repository/
│       └── BinanceStockRepositoryImpl.kt  # Repository impl
├── network/
│   ├── BinanceWebSocketListener.kt  # WebSocket handler
│   └── TiingoWebSocketListener.kt   # Alternative data source
├── model/
│   └── StockPrice.kt          # Data model
├── viewmodel/
│   └── StockViewModel.kt      # UI state management
└── ui/
    ├── PriceGraph.kt          # Price chart component
    └── theme/                 # Theme configuration
```

### Building & Running

**Prerequisites:**
- Android Studio Hedgehog (2023.1.1) or newer
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
2. Select a device (emulator or physical)
3. Click Run (Shift+F10) or use the Run button

### Testing
```bash
./gradlew test                       # Run unit tests
./gradlew connectedAndroidTest       # Run instrumentation tests
./gradlew lint                       # Run lint checks
```

### API Integration
The app connects to Binance's public WebSocket stream:
- **Endpoint:** `wss://stream.binance.com:9443/ws`
- **Subscription format:** `{symbol.lowercase()}@aggTrade`
- **Message fields:** `s` (symbol), `p` (price), `T` (timestamp)

No API key required for public trade data.

### Key Architecture Patterns
- **Flow-based data:** Repository emits `Flow<StockPrice>`; ViewModel transforms with operators (shareIn, sample)
- **State management:** Immutable `StockUiState` exposed via `StateFlow`
- **Lifecycle-aware:** `collectAsStateWithLifecycle()` for Compose UI
- **WebSocket lifecycle:** Tied to ViewModel; disconnected in `onCleared()`
- **History tracking:** Maintains last 50 price points per ticker

### Configuration
- **Default symbols:** BTCUSDT, ETHUSDT, BNBUSDT, SOLUSDT, ADAUSDT
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Compose BOM:** 2024.12.01
- **Kotlin:** 2.0.21

### Notes
- The app uses a throttling mode to sample updates every 500ms (toggle via settings icon)
- Price graph displays normalized data using Canvas drawing
- All dependencies managed via version catalog (`gradle/libs.versions.toml`)

### License
[Add license information here]

### Contributing
[Add contribution guidelines here]
