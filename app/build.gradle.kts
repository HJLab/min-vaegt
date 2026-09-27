plugins {
    id("com.android.application")
}

android {
    namespace = "dk.hjlab.minvaegtclean"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.hjlab.minvaegtclean"
        minSdk = 26
        targetSdk = 35
        versionCode = 200
        versionName = "2.0.0"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
