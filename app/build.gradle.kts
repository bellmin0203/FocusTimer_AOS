plugins {
    alias(libs.plugins.my.android.application)
    alias(libs.plugins.my.android.compose)
    alias(libs.plugins.my.hilt)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.play.services.oss.licenses)
}

android {
    namespace = "com.jm.harufocus"

    defaultConfig {
        testInstrumentationRunner = "com.jm.harufocus.HiltTestRunner"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(projects.core.designsystem)
    implementation(projects.core.util)
    implementation(projects.core.common)
    implementation(projects.core.data)

    implementation(projects.feature.timer)
    implementation(projects.feature.stats)
    implementation(projects.feature.setting)
    implementation(projects.feature.widget)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.hilt.android.testing)
    ksp(libs.hilt.android.compiler)

    debugImplementation(projects.core.uiTestHilt)
    debugImplementation(libs.androidx.ui.test.manifest)

    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.profileinstaller)
}