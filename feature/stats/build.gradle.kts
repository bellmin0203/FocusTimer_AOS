plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

android {
    namespace = "com.jm.teumtimer.stats"
}

dependencies {
    // Vico Chart
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
}