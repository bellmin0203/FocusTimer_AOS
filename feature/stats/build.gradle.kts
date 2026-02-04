import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.stats"
}

dependencies {
    // Vico Chart
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
}