import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.impl.VariantOutputImpl

plugins {
    alias(libs.plugins.my.android.application)
    alias(libs.plugins.my.android.compose)
    alias(libs.plugins.my.hilt)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.play.services.oss.licenses)
}

configure<BasePluginExtension> {
    archivesName.set("HaruFocus-v${libs.versions.projectVersionName.get()}")
}

configure<ApplicationExtension> {
    namespace = "com.jm.harufocus"

    defaultConfig {
        testInstrumentationRunner = "com.jm.harufocus.HiltTestRunner"
    }

    ndkVersion = libs.versions.ndk.get()

    buildTypes {
        release {
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                output.outputFileName = "HaruFocus-v${output.versionName.get()}-${variant.buildType}.apk"
            }
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
    implementation(projects.core.data)

    implementation(projects.feature.timer)
    implementation(projects.feature.stats)
    implementation(projects.feature.setting)
    implementation(projects.feature.widget)
    implementation(projects.feature.onboarding)

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