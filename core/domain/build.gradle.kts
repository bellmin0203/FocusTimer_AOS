plugins {
    alias(libs.plugins.my.kotlin.library)
    alias(libs.plugins.my.hilt)
}

dependencies {
    implementation(projects.core.common)

    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
