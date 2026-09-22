plugins {
    id("com.android.application")
}

android {
    namespace = "dk.hjlab.minvaegt"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.hjlab.minvaegt"
        minSdk = 26
        targetSdk = 35
        versionCode = 110
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
