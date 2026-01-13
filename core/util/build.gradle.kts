plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

android {
    namespace = "com.jm.harufocus.util"
}

dependencies {
    api(libs.jmlog)
    implementation(projects.core.common)
}