# Chhotu OneTap

A one-tap Android app that performs auto drag-up gesture with configurable trigger mode and speed.

## Features

- **Floating Overlay Icon**: Shows a floating icon on top of all apps
- **One-Tap Drag Up**: Tap or long-press the icon to automatically perform a drag-up gesture
- **Configurable Trigger Mode**: Choose between Tap or Long-Press to trigger the gesture
- **Custom Speed Slider**: Adjust drag speed from 100ms to 2000ms
- **Foreground Service**: Runs in background with notification

## How It Works

1. Grant **Overlay Permission** (SYSTEM_ALERT_WINDOW)
2. Enable **Accessibility Service** for Chhotu OneTap
3. Tap **Start** - floating icon appears on screen
4. Tap or long-press the **floating icon** - performs auto drag-up
5. Customize speed and trigger mode in **Settings**

## Architecture

| Component | Purpose |
|-----------|---------|
| `MainActivity` | Start/stop service, open settings |
| `OverlayService` | Floating icon on screen (SYSTEM_ALERT_WINDOW) |
| `DragService` | AccessibilityService to dispatch drag-up gesture |
| `SettingsActivity` | Trigger mode toggle + custom speed slider |
| `SettingsManager` | SharedPreferences helper |

## Trigger Modes

- **Tap**: Single tap on floating icon triggers drag-up
- **Long Press**: Long press (500ms) on floating icon triggers drag-up

## Speed Configuration

Custom slider from **100ms** to **2000ms** (default: 500ms)

| Speed | Duration | Use Case |
|-------|----------|----------|
| Fast | 100-300ms | Quick gestures |
| Normal | 400-600ms | Default usage |
| Slow | 700-1000ms | Smooth animations |
| Very Slow | 1000-2000ms | Accessibility |

## Build Requirements

- Android SDK 26+ (Android 8.0+)
- Gradle 8.1.0
- Java 8

## Permissions Required

- `SYSTEM_ALERT_WINDOW` - Show floating overlay
- `FOREGROUND_SERVICE` - Run as foreground service
- `BIND_ACCESSIBILITY_SERVICE` - Perform gesture dispatch

## License

MIT
