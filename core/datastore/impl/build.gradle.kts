plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.focustimer.core.datastore.impl"
}

dependencies {
    implementation(libs.androidx.dataStore)
    implementation(projects.core.datastore.api)
}