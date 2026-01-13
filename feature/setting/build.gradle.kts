plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

android {
    namespace = "com.jm.harufocus.setting"
}

dependencies {
    testImplementation(projects.core.testing)
}