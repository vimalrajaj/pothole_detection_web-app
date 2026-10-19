# RoadSense Project Analysis

## Project Overview

**RoadSense** is an Android mobile application designed to detect potholes on roads using device sensors and GPS, reporting them to a cloud database in real-time. The app provides a crowdsourced solution for road damage detection with gamification features (leaderboard, trip history, user profiles).

---

## Tech Stack

### Core Android
- **Android API Level**: 27 (Min SDK) → 36 (Target SDK)
- **Kotlin**: 2.0.21 with Compose support
- **Build System**: Gradle 9.0.0

### UI Framework
- **Jetpack Compose**: Material Design 3 (compose-bom: 2024.09.00)
- **Navigation**: Compose Navigation (2.7.7) for multi-screen navigation
- **Theme**: Custom dark/light theme implementation with theme mode settings

### Backend & Database
- **Firebase**: 
  - Firestore (Cloud database for potholes, users, leaderboard)
  - Firebase Authentication (Phone-based OTP authentication)
  - Firebase Analytics
- **Data Persistence**: DataStore Preferences (1.1.1) for local settings

### Location & Sensors
- **Google Play Services Location**: 21.3.0 (FusedLocationProviderClient for GPS)
- **Accelerometer**: Built-in sensor for pothole detection algorithm
- **Foreground Service**: Continuous location tracking and sensor monitoring

### Maps
- **OSMDroid**: 6.1.18 (OpenStreetMap integration for map visualization)

### Additional Features
- **Material Icons Extended**: 1.6.0 (rich icon support)
- **Glance**: 1.1.0 (Home screen widget)
- **Google Services Plugin**: 4.4.4 (Firebase integration)

---

## Project Structure

```
RoadSense/
├── app/
│   ├── src/main/
│   │   ├── java/com/vimal/roadsense/
│   │   │   ├── MainActivity.kt              # Entry point with navigation
│   │   │   ├── data/
│   │   │   │   ├── AuthRepository.kt        # Firebase Auth & User management
│   │   │   │   ├── AuthState.kt             # Auth state definitions
│   │   │   │   ├── PotholeRepository.kt     # Firestore pothole CRUD
│   │   │   │   ├── PotholeData.kt           # Pothole & detection data models
│   │   │   │   ├── TripData.kt              # Trip history & leaderboard models
│   │   │   │   └── UserData.kt              # User profile & vehicle types
│   │   │   ├── service/
│   │   │   │   └── PotholeDetectionService.kt  # Foreground service with sensors
│   │   │   ├── viewmodel/
│   │   │   │   ├── AuthViewModel.kt         # Auth state management
│   │   │   │   └── MainViewModel.kt         # Detection & data state
│   │   │   ├── ui/
│   │   │   │   ├── screens/                 # UI screens (Home, Profile, Settings, etc)
│   │   │   │   ├── components/              # Reusable UI components
│   │   │   │   └── theme/                   # Theme definitions & colors
│   │   │   └── widget/
│   │   │       └── RoadSenseWidgetReceiver  # Home screen widget
│   │   ├── AndroidManifest.xml              # App permissions & components
│   │   └── res/                             # Resources (strings, drawables, colors)
│   ├── build.gradle.kts                     # App-level build configuration
│   └── google-services.json                 # Firebase configuration
├── gradle/libs.versions.toml                # Centralized dependency versioning
├── build.gradle.kts                         # Root build configuration
├── settings.gradle.kts                      # Project setup & repos
├── firestore.rules                          # Firestore security rules
├── firestore.indexes.json                   # Firestore index configuration
└── local.properties                         # Local build properties
```

---

## Key Components & Architecture

### 1. **Authentication Flow (AuthRepository & AuthViewModel)**
- **Phone OTP Authentication**: Firebase Phone Auth
- **Data Persistence**: Local user ID stored in DataStore
- **Profile Management**: First-time setup with vehicle type selection
- **Sign Out**: Clear authentication and local data

### 2. **Pothole Detection System (PotholeDetectionService)**
- **Foreground Service**: Runs continuously with GPS & accelerometer
- **Sensor Processing**: 
  - Accelerometer readings analyzed for impact detection
  - Sensitivity adjustable per vehicle type
  - Real-time detection algorithm with configurable thresholds
- **Location Integration**: FusedLocationProviderClient for accurate GPS
- **Notification Channel**: User-visible foreground service notification

### 3. **Data Management (Repositories)**

#### PotholeRepository
- **Real-time Sync**: Firestore listener for all potholes
- **Offline Support**: Pending writes queue for offline scenarios
- **User Potholes**: Flow-based user detection history
- **Nearby Potholes**: Location-based filtering
- **Statistics**: User pothole count & global stats (totalPotholes, totalTrips)
- **Duplicate Prevention**: Connection monitoring for offline detection

#### AuthRepository
- **Phone OTP**: sendOtp() → verifyOtp() workflow
- **Profile Persistence**: Firestore user documents
- **Vehicle Selection**: Type-based threshold adjustment
- **Auth State Observing**: Real-time auth state changes

### 4. **UI Architecture (Jetpack Compose)**

**Navigation Routes**:
- `onboarding` → Initial setup screen
- `home` → Main map/detection screen
- `profile` → User profile & statistics
- `settings` → App configuration
- `trip_history` → Past trips & detections
- `leaderboard` → Global user rankings

**State Management**:
- **AppSettings**: Theme mode, language (enum-based)
- **UserSettings**: Vehicle type, detection sensitivity
- **DetectionState**: Current detection status & stats
- **AuthState**: Sealed class (Unauthenticated, Authenticated, Error)

### 5. **Firestore Database Schema**

```
/potholes/{potholeId}
├── userId
├── latitude
├── longitude
├── timestamp
├── severity
└── vehicleType

/users/{userId}
├── displayName
├── phoneNumber
├── selectedVehicle
├── vehicleType
├── potholeCount
├── profileCompleted
└── userSettings

/globalStats/{docId}
├── totalPotholes
└── totalTrips
```

---

## Features

### ✅ Implemented Features
1. **Pothole Detection**: Real-time sensor-based detection with configurable sensitivity
2. **Real-time Mapping**: OSMDroid integration showing pothole locations
3. **User Authentication**: Phone OTP-based Firebase Auth
4. **Cloud Sync**: Firestore real-time database with offline support
5. **Trip History**: Track detection sessions with timestamps & locations
6. **Leaderboard**: Global user rankings based on detections
7. **User Profile**: Vehicle type selection, statistics display
8. **Settings Screen**: Theme mode, language, sensitivity adjustment
9. **Home Screen Widget**: Glance-based widget for quick access
10. **Offline Mode**: Pending writes queue for offline scenarios
11. **Foreground Service**: Background detection with persistent notification

### ⚙️ Configuration
- **MAPS_API_KEY**: Required in local.properties (Google Maps)
- **Theme Support**: Dark/Light mode with Material Design 3
- **Localization**: Enum-based language support (en, es, fr, de, etc.)
- **Vehicle Types**: Car, Motorcycle, Truck, Bus with custom thresholds

---

## Permissions & Sensors

### Requested Permissions
```xml
- INTERNET
- ACCESS_FINE_LOCATION (GPS)
- ACCESS_COARSE_LOCATION
- ACCESS_BACKGROUND_LOCATION
- FOREGROUND_SERVICE
- FOREGROUND_SERVICE_LOCATION
- POST_NOTIFICATIONS
- HIGH_SAMPLING_RATE_SENSORS
- WAKE_LOCK
```

### Required Hardware
- GPS sensor
- Accelerometer (required)

---

## Firebase Setup

### Collections & Security Rules
- **Public Read Access**: `/potholes/*` - Anyone can view pothole data
- **Authenticated Write**: Only authenticated users can create potholes
- **User-Owned Data**: Users can only modify their own data in `/users/*`
- **Development Mode**: Currently allows unauthenticated writes for testing

### Indexes
- Auto-generated index for pothole queries (geo-spatial queries)

---

## Build Configuration

### Gradle Setup
- **Java Compatibility**: Version 11
- **Proguard/R8**: Minification disabled for debug, enabled for release
- **Compose**: Enabled for UI rendering

### Dependencies (Key Versions)
| Library | Version |
|---------|---------|
| Android Gradle Plugin | 9.0.0 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.09.00 |
| Firebase BOM | 34.8.0 |
| AGP | 9.0.0 |

---

## Potential Improvements & Areas for Development

### Architecture & Code Quality
1. **Dependency Injection**: Consider Hilt for better dependency management
2. **Repository Pattern**: Already implemented but could use more abstraction layers
3. **Unit Tests**: No test files detected - add comprehensive test coverage
4. **Sealed Classes**: Good practice already in use for auth/trip states

### Features
1. **Image Capture**: Add camera integration for pothole verification
2. **Impact Severity Calculation**: More sophisticated algorithm for severity levels
3. **Batch Uploads**: Optimize data sync for better connectivity handling
4. **User Verification**: Rating system to validate pothole reports
5. **Municipality Integration**: API to notify local authorities
6. **Push Notifications**: Alert users about nearby potholes

### Performance & Analytics
1. **Firebase Analytics**: Currently included but not extensively used
2. **Crash Reporting**: Add Crashlytics for production monitoring
3. **Memory Optimization**: Service improvements for long-running detection
4. **Battery Optimization**: Implement smart location update intervals

### UI/UX
1. **Dark Mode**: Implemented with theme toggle
2. **Accessibility**: Add content descriptions and accessibility features
3. **Error Handling**: Improve error messages and user feedback
4. **Animations**: Enhance transitions between screens

---

## Current Local Setup Status

### Required Configuration
- **local.properties**: Must contain:
  ```properties
  MAPS_API_KEY=<your-google-maps-api-key>
  ```
- **google-services.json**: Firebase configuration (present at `app/google-services.json`)

### Build Ready
- ✅ All Gradle dependencies configured
- ✅ Firebase integrated
- ✅ Compose enabled
- ✅ Navigation setup complete

---

## Development Workflow

### To Run the App
1. Configure `MAPS_API_KEY` in `local.properties`
2. Sync Gradle: `./gradlew sync`
3. Build: `./gradlew assembleDebug`
4. Deploy to emulator/device with Android 8.1+ (API 27+)

### Key Files to Monitor
- [MainActivity.kt](app/src/main/java/com/vimal/roadsense/MainActivity.kt) - Navigation logic
- [PotholeDetectionService.kt](app/src/main/java/com/vimal/roadsense/service/PotholeDetectionService.kt) - Core detection
- [PotholeRepository.kt](app/src/main/java/com/vimal/roadsense/data/PotholeRepository.kt) - Data sync
- [firestore.rules](firestore.rules) - Database security

---

## Summary

RoadSense is a well-architected Android application leveraging modern Android technologies (Compose, Firestore, Foreground Services) to create a crowdsourced pothole detection system. The project demonstrates good practices in:
- ✅ Reactive programming with Kotlin Flows
- ✅ Firebase integration (Auth + Firestore)
- ✅ Jetpack Compose for modern UI
- ✅ Background service management
- ✅ Real-time data synchronization

Next steps for enhancement would include test coverage, enhanced analytics, image integration, and municipality reporting features.
