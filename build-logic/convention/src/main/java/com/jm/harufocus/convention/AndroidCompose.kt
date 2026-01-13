package com.jm.harufocus.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureAndroidCompose() {
    androidExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            "implementation"(platform(bom))
            "androidTestImplementation"(platform(bom))
            "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())

            "implementation"(libs.findBundle("compose").get())
            "debugImplementation"(libs.findBundle("compose.debug").get())
        }
    }

    extensions.getByType<ComposeCompilerGradlePluginExtension>().apply {
        /**
         * Compose의 recomposition skipping 기능을 강화하는 설정
         * 더 엄격하게 UI가 변하지 않은 컴포저블은 recomposition skip
         * 기본값 켜짐
         */
//        featureFlags.add(ComposeFeatureFlag.StrongSkipping)

        // Compose가 생성한 코드에 원본 소스의 디버깅 정보를 포함
        includeSourceInformation.set(true)
    }

    println(">>> configureAndroidCompose done")
}