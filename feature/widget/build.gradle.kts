plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.harufocus.widget"
}

dependencies {
    // Core modules
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.common)
    implementation(projects.core.util)
    implementation(projects.core.designsystem)

    // Glance (Widget)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    debugImplementation(libs.glance.preview)
    debugImplementation(libs.glance.appwidget.preview)

    // Work Manager for periodic updates
    implementation(libs.work.runtime.ktx)
}
