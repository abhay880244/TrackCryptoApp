# TrackCryptoApp - Architecture Diagram Images

This directory contains the architecture diagrams for the TrackCryptoApp project.

## Files

### Generated Images (Ready to View)
- **`app_flow_diagram.svg`** - Primary flow diagram (vector, scalable)
- **`app_flow_diagram.png`** - Primary flow diagram (raster)
- **`layered_architecture.svg`** - Layered architecture view with color-coded layers

### Source Files (Mermaid Format)
- `app_flow_diagram.mmd` - Source for primary flow diagram
- `layered_architecture.mmd` - Source for layered architecture diagram
- `architecture_diagram.mmd` - Detailed comprehensive diagram (may need online renderer due to size)

### Documentation
- `architecture_overview.txt` - Comprehensive text-based diagram with explanations
- `README.md` - This file

## Generating Images

### Option 1: Mermaid CLI (Recommended)

1. Install Mermaid CLI:
```bash
npm install -g @mermaid-js/mermaid-cli
```

2. Generate PNG:
```bash
mmdc -i architecture_diagram.mmd -o architecture_diagram.png
mmdc -i app_flow_diagram.mmd -o app_flow_diagram.png
```

### Option 2: Mermaid Live Editor (Online)

1. Go to https://mermaid.live
2. Copy and paste the content from `.mmd` files
3. Download as PNG or SVG

### Option 3: VS Code Extension

1. Install "Markdown Preview Mermaid Support" extension
2. Create a `.md` file with:
   ` ```mermaid
   [paste diagram content]
   `
3. Preview and screenshot

## Architecture Overview

### Layers
1. **External API**: Binance WebSocket (wss://stream.binance.com:9443/ws)
2. **Data Layer**: BinanceWebSocketListener → BinanceStockRepositoryImpl
3. **Domain Layer**: StockRepository interface
4. **DI Layer**: Hilt modules (AppModule)
5. **Presentation Layer**: MainActivity, StockViewModel, Compose UI

### Key Data Flow
```
Binance API
  → WebSocket messages
  → BinanceWebSocketListener
  → parseMessage → StockPrice
  → callbackFlow
  → StockRepository
  → StockViewModel (sample/500ms if throttled)
  → StockUiState
  → Compose UI (StockList, StockDetailsScreen, PriceGraph)
```

### Components
- **MainActivity**: Navigation host (StockList ↔ StockDetails)
- **StockViewModel**: State management, throttling logic, maintains last 50 prices per ticker
- **PriceGraph**: Canvas-based line chart rendering
- **WebSocket**: Single shared connection via `shareIn(WhileSubscribed(5000))`

### Technologies
- Kotlin 2.0.21, Jetpack Compose (Material3), Hilt 2.54
- OkHttp WebSocket, Kotlin Coroutines/Flow
- MVVM + Clean Architecture
- Min SDK 24, Target SDK 35
