plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rudra.lifeledge"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rudra.lifeledge"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Room KSP configuration
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
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
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}

dependencies {
    // ------------------------------------------------------------
    // Core Android
    // ------------------------------------------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    
    // ------------------------------------------------------------
    // Kotlin
    // ------------------------------------------------------------
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    
    // ------------------------------------------------------------
    // Jetpack Compose BOM
    // ------------------------------------------------------------
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // ------------------------------------------------------------
    // Lifecycle & ViewModel
    // ------------------------------------------------------------
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    
    // ------------------------------------------------------------
    // Navigation
    // ------------------------------------------------------------
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ------------------------------------------------------------
    // Backupsystem
    // ------------------------------------------------------------
    implementation("com.google.code.gson:gson:2.10.1")
// For Kotlin, you might also want:
    implementation("com.google.code.gson:gson:2.10.1")
    // ------------------------------------------------------------
    // Room Database (KSP ONLY)
    // ------------------------------------------------------------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // KSP annotation processing
    testImplementation("androidx.room:room-testing:2.6.1")
    
    // ------------------------------------------------------------
    // DataStore (Settings)
    // ------------------------------------------------------------
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // ------------------------------------------------------------
    // WorkManager (Background tasks)
    // ------------------------------------------------------------
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // ------------------------------------------------------------
    // Splash Screen
    // ------------------------------------------------------------
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // ------------------------------------------------------------
    // Coil (Image Loading)
    // ------------------------------------------------------------
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // ------------------------------------------------------------
    // Charts
    // ------------------------------------------------------------
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Alternative: Pure Compose charts
    implementation("co.yml:ycharts:2.1.0")

    // ------------------------------------------------------------
    // Icone
    // ------------------------------------------------------------
    implementation("androidx.compose.material:material-icons-extended")
    implementation("br.com.devsrsouza.compose.icons:feather:1.1.0")
    implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.0")
    implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.0")
    // ------------------------------------------------------------
    // Accompanist (Utilities)
    // ------------------------------------------------------------
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.36.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")
    
    // ------------------------------------------------------------
    // Lottie Animations
    // ------------------------------------------------------------
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    
    // ------------------------------------------------------------
    // Security & Biometric
    // ------------------------------------------------------------
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    
    // ------------------------------------------------------------
    // PDF Generation
    // ------------------------------------------------------------
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // ------------------------------------------------------------
    // CSV Export
    // ------------------------------------------------------------
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")

    // ------------------------------------------------------------
    // Koin Dependency Injection
    // ------------------------------------------------------------
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
    
    // ------------------------------------------------------------
    // Testing
    // ------------------------------------------------------------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // ------------------------------------------------------------
    // Debug
    // ------------------------------------------------------------
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}