plugins {
    id("com.android.application")
}

android {
    namespace = "dk.hjlab.minvaegttest"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.hjlab.minvaegttest"
        minSdk = 26
        targetSdk = 35
        versionCode = 300
        versionName = "3.0.0"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
    }
}
