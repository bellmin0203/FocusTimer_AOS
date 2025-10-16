plugins {
    alias(libs.plugins.my.android.application)
    alias(libs.plugins.my.android.compose)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.focustimer"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(projects.core.designsystem)
    implementation(projects.core.util)

    implementation(projects.feature.timer)
    implementation(projects.feature.stats)
    implementation(projects.feature.setting)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}