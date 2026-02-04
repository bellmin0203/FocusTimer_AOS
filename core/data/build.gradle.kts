import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.my.android.library)
    alias(libs.plugins.my.hilt)
}

configure<LibraryExtension> {
    namespace = "com.jm.harufocus.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.common)
    implementation(projects.core.util)

    // datastore.api는 Repository 구현에서 사용하므로 api로 노출
    api(projects.core.datastore.api)

    // datastore.impl은 Hilt 모듈을 위해 포함 (구현 세부사항)
    implementation(projects.core.datastore.impl)

    implementation(libs.androidx.dataStore)

    // Google Play In-App Review
    implementation(libs.play.review.ktx)
}