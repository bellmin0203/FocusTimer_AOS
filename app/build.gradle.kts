import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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

    signingConfigs {
        create("release") {
            storeFile = file(
                System.getenv("RELEASE_STORE_FILE") ?: gradleLocalProperties(
                    rootDir,
                    providers
                ).getProperty("RELEASE_STORE_FILE")
            )
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: gradleLocalProperties(
                rootDir,
                providers
            ).getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: gradleLocalProperties(
                rootDir,
                providers
            ).getProperty("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: gradleLocalProperties(
                rootDir,
                providers
            ).getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }

        create("benchmark") {
            matchingFallbacks.add("release")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
        }
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