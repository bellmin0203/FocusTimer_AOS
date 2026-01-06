plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
}

android {
    namespace = "com.jm.teumtimer.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.core.designsystem)
}