plugins {
    alias(libs.plugins.my.android.presentation.ui)
}

android {
    namespace = "com.jm.focustimer.timer"
}

dependencies {
    // Widget 모듈 (위젯 업데이트를 위해)
    implementation(projects.feature.widget)
}