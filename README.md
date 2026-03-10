# Macro Lens

A food tracking Android app that uses AI to analyze photos of your meals and estimate nutritional content.

The name is a double meaning: **macro** as in macronutrients (calories, protein, carbs, fat) and **macro lens** as in close-up photography—capturing both core features in two words.

## Features

- **Camera Capture**: Take photos of your food using CameraX
- **AI Analysis**: GPT-4o vision analyzes images to estimate calories, protein, carbs, and fat
- **Daily Log**: View all food entries for the day with running macro totals
- **Inspirational Messages**: AI-generated encouragement based on your daily progress
- **Customizable Prompts**: Personalize the tone of your daily messages

## Screenshots

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  📷 Camera      │    │  📋 Daily Log   │    │  ⚙️ Settings    │
│                 │    │                 │    │                 │
│   [viewfinder]  │    │ "Great job..."  │    │ API Key: ****   │
│                 │    │ ─────────────── │    │                 │
│                 │    │ Totals: 1200cal │    │ Custom Prompt:  │
│                 │    │ ─────────────── │    │ [textarea]      │
│      (○)        │    │ • chicken salad │    │                 │
│                 │    │ • coffee        │    │                 │
│  [←]      [⚙]  │    │                 │    │           [←]   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Requirements

- Android 8.0 (API 26) or higher
- OpenAI API key with GPT-4o access

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on device or emulator
5. Open Settings and enter your OpenAI API key

## Architecture

Single-activity Compose app with three screens managed by Navigation Compose.

```
com.phood
├── data
│   ├── local/          # Room database (FoodEntry, DAO)
│   ├── remote/         # OpenAI API client
│   └── settings/       # DataStore preferences
├── repository/         # FoodRepository, LlmRepository
├── di/                 # Manual dependency injection
└── ui
    ├── camera/         # CameraScreen + ViewModel
    ├── dailylog/       # DailyLogScreen + ViewModel
    ├── settings/       # SettingsScreen + ViewModel
    └── theme/          # Material3 theming
```

## Tech Stack

- **UI**: Jetpack Compose + Material3
- **Navigation**: Navigation Compose
- **Camera**: CameraX
- **Database**: Room
- **Preferences**: DataStore
- **Networking**: OkHttp + kotlinx.serialization
- **Async**: Kotlin Coroutines + Flow

## License

MIT
