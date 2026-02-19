import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

@Suppress("UnstableApiUsage")
configure<LibraryExtension> {
    namespace = "com.jm.harufocus.setting"

    testFixtures.enable = true
}

dependencies {
    testImplementation(projects.core.testing)

    implementation(libs.play.services.oss.licenses)

    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(projects.core.common)
    testFixturesImplementation(libs.kotlinx.coroutines.core)
}