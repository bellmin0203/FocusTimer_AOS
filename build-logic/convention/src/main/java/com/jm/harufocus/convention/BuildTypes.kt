package com.jm.harufocus.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
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
                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
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