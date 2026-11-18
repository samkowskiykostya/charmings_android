import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Secrets live in local.properties (git-ignored) or environment variables, never in source.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(propKey: String, envKey: String): String =
    localProps.getProperty(propKey) ?: System.getenv(envKey) ?: ""

val petImageBaseUrl: String = (project.findProperty("petImageBaseUrl") as String?)
    ?: "https://cdn.jsdelivr.net/gh/samkowskiykostya/charmings_android@main/pet-images/v2/"

android {
    namespace = "com.charmings.app"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("../charmings-release.keystore")
            storePassword = secret("release.keystore.key", "KEYSTORE_PASSWORD")
            keyAlias = "charmings"
            keyPassword = secret("release.key.password", "KEY_PASSWORD").ifEmpty { secret("release.keystore.key", "KEYSTORE_PASSWORD") }
        }
    }

    defaultConfig {
        applicationId = "com.charmings.app"
        minSdk = 30  // Android 11
        targetSdk = 36
        versionCode = 5
        versionName = "1.3"

        buildConfigField("String", "WEATHER_API_KEY", "\"${secret("weather.apiKey", "WEATHER_API_KEY")}\"")
        buildConfigField("String", "PET_IMAGE_BASE_URL", "\"$petImageBaseUrl\"")
        // Multiplies every detected step. Keep at 1; raise only temporarily for emulator testing.
        buildConfigField("int", "STEP_MULTIPLIER", "1")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose (icons: core set only — the "extended" artifact added tens of MB of dex)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // OkHttp for weather API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coil: downloads + disk-caches full pet images (they are not bundled in the APK)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore for persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation("junit:junit:4.13.2")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
