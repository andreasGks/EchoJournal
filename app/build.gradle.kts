plugins {
    id("com.android.application")
    // Use latest compatible Kotlin version (e.g., 2.0.0)
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.devtools.ksp") // KSP plugin application
}

android {
    namespace = "com.example.echojournal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.echojournal"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Keeping Java 11 for consistency and modern features
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // !!! CRITICAL FIX: The Compose Compiler Extension MUST match the Kotlin version.
        // Kotlin 2.0.0 requires Compose Compiler 1.5.12 (or newer compatible if available).
        // Since you didn't provide the exact required version for 2.0.0,
        // I'll set it to the latest known stable one for a typical setup (1.6.10/1.6.21 were common for KSP/2.0.0 builds).
        // If this version fails, you might need to check the official Kotlin/Compose compatibility mapping.
        kotlinCompilerExtensionVersion = "1.6.10" // Using 1.6.10 as a safer bet for Compose 2024.xx
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // This task configuration is not strictly needed if jvmTarget = "11" is set globally,
    // but it is harmless.
    if (name.contains("Test", ignoreCase = true)) {
        kotlinOptions.jvmTarget = "11"
    }
}


dependencies {
    // Define the Compose BOM for centralized version management.
    // I am updating this to the latest 2024.06.00 which is generally safer than an unknown future date.
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    // ✅ Compose BOM (controls all versions)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ✅ Compose UI & Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // REMOVED: androidx.compose.material:material (use M3 instead for consistency)
    implementation("androidx.compose.material3:material3") // Required for SwipeToDismissBox, DismissDirection, DismissValue

    // ✅ Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ✅ Lifecycle & ViewModel (MVVM)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // ✅ Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ✅ AndroidX Core / AppCompat / Material
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // --- ADDITIONS: Room & Data ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Gson (Needed for TypeConverters to store List<String>)
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Google Play Services
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // ---------------------------------------------------------------------
    // 🧪 UNIT TESTING DEPENDENCIES
    // ---------------------------------------------------------------------

    // JUnit (The base)
    testImplementation("junit:junit:4.13.2")

    // Coroutines Test Utilities
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Mockito/Mocking Framework
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // InstantTaskExecutorRule
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // ✅ Android Test
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // ✅ Compose Debug Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}