package com.jm.harufocus.convention

import org.gradle.api.Project

internal fun Project.configureKotestAndroid() {
    configureKotest()
    configureJUnitAndroid()
}

internal fun Project.configureJUnitAndroid() {
    androidExtension.testOptions.unitTests.all { it.useJUnitPlatform() }
}