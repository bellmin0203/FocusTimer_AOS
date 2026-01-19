plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.harufocus.util"
}

dependencies {
    api(libs.jmlog)
    implementation(projects.core.common)

    // Firebase Crashlytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}