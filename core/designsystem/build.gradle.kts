import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.android.compose)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.designsystem"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(projects.core.common)
    api(libs.androidx.compose.material.icons.extended)
    api(projects.core.domain)
}