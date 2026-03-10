# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/claude-code) when working with this codebase.

## Project Overview

Phood is an Android food tracking app that uses OpenAI's GPT-4o vision model to analyze photos of food and estimate nutritional content (calories, protein, carbs, fat).

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Architecture

**Single-Activity Compose Architecture** with Navigation Compose managing three screens:
- `CameraScreen` → captures food photos, sends to OpenAI
- `DailyLogScreen` → displays entries and totals for today
- `SettingsScreen` → API key and custom prompt configuration

**Data Flow:**
```
CameraScreen → CameraViewModel → LlmRepository → OpenAiClient
                                       ↓
                              FoodRepository → Room DB
                                       ↓
                              DailyLogViewModel → DailyLogScreen
```

## Key Files

| File | Purpose |
|------|---------|
| `PhoodApp.kt` | NavHost and screen routing |
| `OpenAiClient.kt` | GPT-4o API calls (vision + chat) |
| `FoodEntry.kt` | Room entity for food entries |
| `DailyLogDao.kt` | Database queries with Flow support |
| `AppContainer.kt` | Manual DI wiring |

## Conventions

- **ViewModels** use `StateFlow` for UI state and `SharedFlow` for one-time events
- **Repositories** return `Result<T>` for operations that can fail
- **Composables** receive callbacks for navigation (e.g., `onFoodCaptured`, `onSettingsClick`)
- **No Hilt/Dagger** — manual DI via `AppContainer` singleton in `PhoodApplication`

## Common Tasks

### Adding a new food entry field

1. Add column to `FoodEntry.kt`
2. Update `DailyLogDao.kt` queries if needed
3. Update `FoodEstimate.kt` (API response model)
4. Update `OpenAiClient.FOOD_ANALYSIS_PROMPT` to request the new field
5. Update `CameraViewModel` to map the field
6. Update `DailyLogScreen` UI to display it

### Changing the AI prompt

Edit `OpenAiClient.kt`:
- `FOOD_ANALYSIS_PROMPT` — for food image analysis
- `getInspirationalMessage()` — for daily encouragement messages

### Modifying the theme

Edit `ui/theme/Theme.kt` — uses Material3 color schemes.

## API Integration

The app uses OpenAI's Chat Completions API with vision:
- Model: `gpt-4o`
- Endpoint: `https://api.openai.com/v1/chat/completions`
- Image format: base64-encoded JPEG in `image_url` content block

Response format is strictly JSON:
```json
{"found":true,"calories":300,"protein_g":25,"carbs_g":30,"fat_g":12,"description":"grilled chicken salad"}
```

## Gotchas

- CameraX requires `extractNativeLibs="true"` in manifest for 16KB page alignment on Android 15+
- Room's `Flow` queries auto-update when data changes — no manual refresh needed
- DataStore operations are async — use `first()` to get current value synchronously
- The OpenAI response may be wrapped in markdown code blocks — `extractJson()` handles this
