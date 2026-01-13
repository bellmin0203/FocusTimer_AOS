plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

android {
    namespace = "com.jm.harufocus.timer"
}

dependencies {
    implementation(projects.core.common)
    // Widget 모듈 (위젯 업데이트를 위해)
    implementation(projects.feature.widget)
}
