plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
}

android {
    namespace = "com.jm.teumtimer.designsystem"
}

dependencies {
    implementation(libs.androidx.appcompat)
    api(libs.androidx.compose.material.icons.extended)
    api(projects.core.domain)
}