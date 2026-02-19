import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.feature.onboarding"
}

dependencies {
    implementation(projects.core.common)
}
