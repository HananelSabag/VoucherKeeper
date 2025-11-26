# Voucher Keeper - Project Configuration

## Technology Stack

### Platform
- **Language:** Kotlin 1.9.x
- **Min SDK:** 33 (Android 13)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Architecture
- **Pattern:** MVVM (Model-View-ViewModel)
- **UI Framework:** Jetpack Compose
- **Dependency Injection:** Hilt (Dagger)
- **Database:** Room
- **Async:** Coroutines + Flow

### Core Libraries
```gradle
// Jetpack Compose
androidx.compose.ui:*:1.5.x
androidx.compose.material3:material3:1.1.x
androidx.activity:activity-compose:1.8.x
androidx.navigation:navigation-compose:2.7.x

// Room Database
androidx.room:room-runtime:2.6.x
androidx.room:room-ktx:2.6.x
kapt androidx.room:room-compiler:2.6.x

// Hilt DI
com.google.dagger:hilt-android:2.48
kapt com.google.dagger:hilt-compiler:2.48
androidx.hilt:hilt-navigation-compose:1.1.x

// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-viewmodel-compose:2.6.x
androidx.lifecycle:lifecycle-runtime-compose:2.6.x

// DataStore (for settings)
androidx.datastore:datastore-preferences:1.0.x

// Testing
junit:junit:4.13.2
androidx.test.ext:junit:1.1.5
androidx.test.espresso:espresso-core:3.5.1
androidx.compose.ui:ui-test-junit4:1.5.x

