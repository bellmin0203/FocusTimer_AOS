plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

android {
    namespace = "com.jm.teumtimer.setting"
}

dependencies {
    testImplementation(projects.core.testing)
}