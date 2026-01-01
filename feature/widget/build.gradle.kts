plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.focustimer.widget"
}

dependencies {
    // Core modules
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)

    // Glance (Widget)
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // Work Manager for periodic updates
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}
