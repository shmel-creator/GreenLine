plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.greenline"
    compileSdk = 36  // ← ИЗМЕНЕНО С 34 НА 36

    defaultConfig {
        applicationId = "com.example.greenline"
        minSdk = 26
        targetSdk = 36  // ← ИЗМЕНЕНО С 33/34 НА 36
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {
    // NaviKit SDK (основной)
    implementation("com.yandex.android:maps.mobile:4.36.0-navikit")

    // Google Play Services (для геолокации)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Фирменные UI-элементы Яндекс Карт
    implementation("com.yandex.mapkit.styling:automotivenavigation:4.36.0")

    // Дорожные события (пробки, аварии)
    implementation("com.yandex.mapkit.styling:roadevents:4.36.0")

    // Shimmer (скелетон загрузки)
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Gson для истории
    implementation("com.google.code.gson:gson:2.10.1")

    // Preference (настройки)
    implementation("androidx.preference:preference:1.2.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // Core (с явной версией, совместимой с compileSdk 36)
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Стандартные библиотеки
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.runtime)
    implementation(libs.webkit)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}