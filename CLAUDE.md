# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TrackCryptoApp is an Android application that tracks cryptocurrency prices in real-time using Binance WebSocket API. The app displays live price updates for major cryptocurrencies (BTC, ETH, BNB, SOL, ADA) with historical price graphs.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material3
- **Architecture:** MVVM with Clean Architecture principles
- **Dependency Injection:** Hilt
- **Networking:** OkHttp WebSocket
- **Navigation:** Jetpack Navigation Compose
- **Build System:** Gradle with Kotlin DSL (AGP 8.7.3)
- **Minimum SDK:** 24, Target SDK:** 35
- **Coroutines/Flow:** For reactive data streams

## Project Structure

```
app/src/main/java/android/project/trackcryptoapp/
├── MainActivity.kt              # Entry point, sets up Compose UI and navigation
├── TrackCryptoApplication.kt   # Application class with @HiltAndroidApp
├── di/
│   └── AppModule.kt            # Hilt dependency injection module
├── domain/
│   └── repository/
│       └── StockRepository.kt  # Repository interface (domain layer)
├── data/
│   └── repository/
│       └── BinanceStockRepositoryImpl.kt  # Repository implementation (data layer)
├── network/
│   ├── BinanceWebSocketListener.kt  # OkHttp WebSocket listener
│   └── TiingoWebSocketListener.kt   # Placeholder for alternative data source
├── model/
│   └── StockPrice.kt          # Data model: ticker, price, timestamp
├── viewmodel/
│   └── StockViewModel.kt      # Manages UI state and business logic
└── ui/
    ├── PriceGraph.kt          # Custom Composable for price chart
    └── theme/                 # Material3 theme configuration
```

## Architecture

**Presentation Layer:** MainActivity with Composable UI screens (StockList, StockDetailsScreen), ViewModel (StockViewModel) manages UI state via StateFlow.

**Domain Layer:** Repository interface defines contracts for data operations (StockRepository).

**Data Layer:** BinanceStockRepositoryImpl implements repository using OkHttp WebSocket to connect to Binance API (wss://stream.binance.com:9443/ws). Parses aggTrade messages into StockPrice objects.

**Dependency Injection:** Hilt provides singleton OkHttpClient and StockRepository implementation.

## Common Development Tasks

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug on connected device/emulator
./gradlew installDebug
```

### Testing

```bash
# Run all unit tests
./gradlew test

# Run all instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "android.project.trackcryptoapp.ExampleUnitTest"

# Run instrumentation test class
./gradlew connectedAndroidTest --tests "android.project.trackcryptoapp.ExampleInstrumentedTest"
```

### Code Quality

```bash
# Lint check
./gradlew lint

# Lint with auto-fix (where applicable)
./gradlew lintFix
```

### Android Studio Shortcuts

- Build APK: Build > Build Bundle(s) / APK(s) > Build APK(s)
- Run app: Shift+F10 (macOS: Ctrl+R)
- Open project structure: Cmd+; (macOS)
- Lint: Analyze > Inspect Code

## Key Implementation Details

**WebSocket Data Flow:** BinanceWebSocketListener receives trade messages, parses JSON to StockPrice, repository emits to Flow, ViewModel collects and maintains last 50 price points per ticker, UI recomposes.

**Throttling Feature:** ViewModel supports optional 500ms sampling via `sample(500)` operator to reduce update frequency. Toggle via settings icon in top app bar.

**Navigation:** Uses NavHost with two routes: "stock_list" (main) and "stock_details/{ticker}" (detail view with price graph).

**State Management:** StockUiState immutable data class; ViewModel exposes StateFlow that UI collects with `collectAsStateWithLifecycle()`.

**Graph Rendering:** PriceGraph uses Canvas to draw line chart; normalizes prices to canvas height.

## Important Conventions

- Use Kotlin with explicit types for public declarations
- Repository methods return Flow for streaming data
- UI state is immutable; updates via copy()
- WebSocket lifecycle tied to ViewModel (disconnect in onCleared)
- Hilt for DI; @Singleton for OkHttpClient and Repository
- Compose UI built with @Composable functions, prefer parameters over state hoisting

## API Integration

- **Endpoint:** wss://stream.binance.com:9443/ws
- **Subscription:** Method SUBSCRIBE with params like "btcusdt@aggTrade"
- **Message format:** aggTrade events with fields: s (symbol), p (price), T (timestamp)
- **No authentication required** for public trade stream

## Testing Notes

Unit tests run on JVM (no Android dependencies). For repository/network tests, consider mocking OkHttp with MockWebServer. ViewModel tests should use runTest and TestDispatcher. Instrumented tests run on device/emulator.

## Gradle Configuration

- Uses version catalog (gradle/libs.versions.toml) for dependency versions
- Kotlin 2.0.21, Compose BOM 2024.12.01, Hilt 2.54
- Java 11 compatibility
- BuildFeatures.compose = true
