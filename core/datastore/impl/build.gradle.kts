plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.harufocus.core.datastore.impl"
}

dependencies {
    implementation(libs.androidx.dataStore)
    implementation(projects.core.datastore.api)
    implementation(projects.core.common)
}