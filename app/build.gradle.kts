plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.vimal.roadsense"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.vimal.roadsense"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Google Maps API key placeholder (set MAPS_API_KEY in local.properties)
        manifestPlaceholders["MAPS_API_KEY"] = findProperty("MAPS_API_KEY") ?: ""

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    
    // Location Services (for GPS)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // DataStore for preferences
    implementation(libs.datastore.preferences)

    // OpenStreetMap
    implementation(libs.osmdroid)
    
    // Material Icons Extended (for more icon options)
    implementation(libs.material.icons.extended)
    
    // Glance (App Widget)
    implementation(libs.glance)
    implementation(libs.glance.appwidget)
    
    // Lottie for animations
    implementation(libs.lottie.compose)
    
    // Coil for image loading
    implementation(libs.coil.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}