package com.jm.focustimer.convention

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.configureKotest() {
    configureJUnit()

    dependencies {
        // Kotlin Reflection - Kotest가 내부적으로 사용
        "testImplementation"(libs.findLibrary("kotlin.reflect").get())

        // Kotest Runner - JUnit5 플랫폼에서 Kotest 실행
        "testImplementation"(libs.findLibrary("kotest.runner.junit5").get())

        // Kotest Assertions - shouldBe, shouldNotBe 등의 풍부한 어설션 API
        "testImplementation"(libs.findLibrary("kotest.assertions.core").get())

        // Kotest Property Testing - 속성 기반 테스트 (선택사항)
        "testImplementation"(libs.findLibrary("kotest.property").get())
    }

    println(">>> configureKotest done - JUnit5 platform enabled")
}

internal fun Project.configureJUnit() {
    // JUnit5 플랫폼 설정 (Kotest는 JUnit5 위에서 실행됨)
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    println(">>> configureJUnit done")
}