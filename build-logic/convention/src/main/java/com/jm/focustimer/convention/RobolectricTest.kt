package com.jm.focustimer.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureRobolectric() {
    dependencies {
        // Robolectric - Android 컴포넌트를 JVM에서 테스트
        "testImplementation"(libs.findLibrary("robolectric").get())
    }

    println(">>> configureRobolectric done")
}