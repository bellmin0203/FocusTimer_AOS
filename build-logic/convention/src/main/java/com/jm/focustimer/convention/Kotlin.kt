package com.jm.focustimer.convention

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid() {
    // Android settings
    androidExtension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 26
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }
    println(">>> configureKotlinAndroid done")

    configureKotlin()
}

internal fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())
            freeCompilerArgs.set(
                freeCompilerArgs.get() + listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    /**
                     * data class의 copy() 함수 가시성을 생성자와 일치시키기 위해서 추가
                     *
                     * Remove this args after Phase 3.
                     * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-consistent-copy-visibility/#deprecation-timeline
                     *
                     * Deprecation timeline
                     * Phase 3. (Supposedly Kotlin 2.2 or Kotlin 2.3).
                     * The default changes.
                     * Unless ExposedCopyVisibility is used, the generated 'copy' method has the same visibility as the primary constructor.
                     * The binary signature changes. The error on the declaration is no longer reported.
                     * '-Xconsistent-data-class-copy-visibility' compiler flag and ConsistentCopyVisibility annotation are now unnecessary.
                     */
                    "-Xconsistent-data-class-copy-visibility",
                )
            )
        }
    }
    println(">>> configureKotlin done")
}

internal fun Project.configureCoroutineAndroid() {
    configureCoroutineKotlin()

    dependencies {
        "implementation"(libs.findLibrary("kotlinx.coroutines.android").get())
    }

    println(">>> configureCoroutineAndroid done")
}

internal fun Project.configureCoroutineKotlin() {
    dependencies {
        "implementation"(libs.findLibrary("kotlinx.coroutines.core").get())
        "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
    }

    println(">>> configureCoroutineKotlin done")
}