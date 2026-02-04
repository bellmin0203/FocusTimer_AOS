import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.library)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.core.datastore.api"
}

dependencies {
    implementation(projects.core.common)
}