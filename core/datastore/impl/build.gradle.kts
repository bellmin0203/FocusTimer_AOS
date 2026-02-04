import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.core.datastore.impl"
}

dependencies {
    implementation(libs.androidx.dataStore)
    implementation(projects.core.datastore.api)
    implementation(projects.core.common)
}