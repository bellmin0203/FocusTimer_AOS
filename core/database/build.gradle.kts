plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.room)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.teumtimer.core.database"
}

dependencies {
    implementation(projects.core.domain)
}