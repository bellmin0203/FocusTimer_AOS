plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
}

android {
    namespace = "com.jm.focustimer.designsystem"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material.icons.extended)
}