import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.library)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.testing"
}

dependencies {
    api(libs.junit4)
    api(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.engine)
    implementation(libs.junit.vintage.engine)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.truth)
    api(libs.robolectric)
}