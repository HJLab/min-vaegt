plugins {
    id("com.android.application")
}

android {
    namespace = "dk.hjlab.minvaegt"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.hjlab.minvaegt.test"
        minSdk = 26
        targetSdk = 35
        versionCode = 122
        versionName = "1.2.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
