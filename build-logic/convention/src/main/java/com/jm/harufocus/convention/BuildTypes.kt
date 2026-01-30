package com.jm.harufocus.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

enum class ExtensionType {
    APPLICATION,
    LIBRARY
}

internal fun Project.configureBuildTypes(
    extensionType: ExtensionType,
) {
    androidExtension.buildFeatures.buildConfig = true

    when (extensionType) {
        ExtensionType.APPLICATION -> {

            extensions.configure<ApplicationExtension> {
                signingConfigs {
                    create("release") {
                        storeFile = file(
                            System.getenv("RELEASE_STORE_FILE") ?: gradleLocalProperties(
                                rootDir,
                                providers
                            ).getProperty("RELEASE_STORE_FILE")
                        )
                        storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: gradleLocalProperties(
                            rootDir,
                            providers
                        ).getProperty("RELEASE_STORE_PASSWORD")
                        keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: gradleLocalProperties(
                            rootDir,
                            providers
                        ).getProperty("RELEASE_KEY_ALIAS")
                        keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: gradleLocalProperties(
                            rootDir,
                            providers
                        ).getProperty("RELEASE_KEY_PASSWORD")
                    }
                }

                buildTypes {
                    release {
                        signingConfig = signingConfigs.getByName("release")
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }

                    create("benchmark") {
                        matchingFallbacks.add("release")
                        signingConfig = signingConfigs.getByName("debug")
                        isDebuggable = false
                    }
                }
            }
        }
        ExtensionType.LIBRARY -> {
            extensions.configure<LibraryExtension> {
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
            }

        }
    }


    println(">>> configureBuildTypes done")
}