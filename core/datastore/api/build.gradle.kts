plugins {
    alias(libs.plugins.my.android.library)
}

android {
    namespace = "com.jm.teumtimer.core.datastore.api"
}

dependencies {
    implementation(projects.core.common)
}