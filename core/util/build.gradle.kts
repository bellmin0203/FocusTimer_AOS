plugins {
    alias(libs.plugins.my.android.library)
}

android {
    namespace = "com.jm.focustimer.util"
}

dependencies {
    api(libs.jmlog)
}