plugins {
    id("com.android.application")
}

android {
    namespace = "dk.hjlab.minvaegt"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.hjlab.minvaegttest301"
        minSdk = 26
        targetSdk = 35
        versionCode = 301
        versionName = "3.0.1"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
    }
}
