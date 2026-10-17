# FieldShift - Cross-Platform iOS Deployment

This directory contains the native Apple iOS integration for **FieldShift**, allowing the shared Compose Multiplatform application to run on iPhones, iPads, and macOS.

## Architecture

```
                    ┌──────────────────────────────────────────────┐
                    │               FieldShift Core                │
                    │   - 3D Globe & NASA Earth Observations       │
                    │   - Soil Studio Physics Engine               │
                    │   - Multi-Year Crop Rotation Simulator       │
                    │   - Real-Time Environmental Alert System     │
                    │   - Gemini 3.8 Flash Agronomist Chatbot      │
                    └──────────────────────┬───────────────────────┘
                                           │
                 ┌─────────────────────────┴─────────────────────────┐
                 │                                                   │
                 ▼                                                   ▼
       ┌─────────────────────┐                             ┌─────────────────────┐
       │   Android Runtime   │                             │     iOS Runtime     │
       │   (MainActivity)    │                             │  (MainViewController│
       │   Android 12+ M3    │                             │   & SwiftUI Wrapper)│
       └─────────────────────┘                             └─────────────────────┘
```

## Running on iOS (Mac + Xcode)

1. **Export the Project**:
   Click the **Settings** menu in AI Studio and choose **Export as ZIP** or **Push to GitHub**.

2. **Open in Xcode**:
   Open Xcode on macOS and select `iosApp/iosApp.xcodeproj` (or open the root directory in Android Studio with the Kotlin Multiplatform plugin).

3. **Build & Run**:
   - Target any iOS Simulator (e.g., iPhone 16 Pro) or a physical iOS device connected via USB.
   - Press **Cmd + R** to build and launch.

The entire UI, 3D Canvas globe, live NASA telemetry, soil studio, and Gemini AI agronomist execute seamlessly on iOS.
