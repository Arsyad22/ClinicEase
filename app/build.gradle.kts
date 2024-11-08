// Apply necessary plugins for Android application and Google services (Firebase)
plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Enables Google services such as Firebase
}

android {
    namespace = "com.example.clinicease" // Unique namespace for your app
    compileSdk = 34 // Compile against Android SDK version 34

    defaultConfig {
        applicationId = "com.example.clinicease" // Application package name
        minSdk = 24 // Minimum supported Android version
        targetSdk = 34 // Target Android SDK version
        versionCode = 1 // Version code for app updates
        versionName = "1.0" // Version name for app display

        // Android instrumentation test runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use support library for vector drawables on older Android versions
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Disable code shrinking for release builds
            // Apply ProGuard rules for code optimization (not active here)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Set Java version compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Exclude specific resources from packaging
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}") // Avoid conflicts with licensing files
        }
    }
}

dependencies {
    // AndroidX and Core Libraries

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Lifecycles for managing UI components
    implementation("androidx.appcompat:appcompat:1.6.1") // Support library for backward compatibility
    implementation("com.google.android.material:material:1.9.0")

    // Material Design for modern UI components
    implementation("com.google.android.material:material:1.9.0") // Google Material Design components

    // Firebase Platform for managing Firebase services versions centrally
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase Analytics for usage tracking and reporting
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Authentication for login and registration functionality
    implementation("com.google.firebase:firebase-auth")

    // Firebase Realtime Database for storing and syncing app data
    implementation("com.google.firebase:firebase-database")

    // Firebase Firestore for document-oriented NoSQL database
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Storage for storing user-generated content (e.g., images, files)
    implementation("com.google.firebase:firebase-storage")

    // ZXing for QR code generation and scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // Android embedded QR scanner
    implementation("com.google.zxing:core:3.3.0") // Core ZXing library for QR code generation

    // AndroidX ConstraintLayout for flexible and responsive UI layouts
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    // Glide for image loading and caching
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Corrected Picasso dependency
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Unit Testing dependencies
    testImplementation("junit:junit:4.13.2") // JUnit for unit tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // AndroidX extension for JUnit
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Espresso for UI testing
}
