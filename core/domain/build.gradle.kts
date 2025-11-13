plugins {
    alias(libs.plugins.my.kotlin.library)
    alias(libs.plugins.my.hilt)
}

dependencies {
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
