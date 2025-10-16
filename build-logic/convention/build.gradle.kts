import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Kotlin DSL을 사용해서 Convention Plugin을 만들기 때문에 추가
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.my.android.application.get().pluginId
            implementationClass = "AndroidApplicationPlugin"
        }

        register("androidLibrary") {
            id = libs.plugins.my.android.library.get().pluginId
            implementationClass = "AndroidLibraryPlugin"
        }

        register("androidCompose") {
            id = libs.plugins.my.android.compose.get().pluginId
            implementationClass = "AndroidComposePlugin"
        }

        register("hilt") {
            id = libs.plugins.my.hilt.get().pluginId
            implementationClass = "HiltPlugin"
        }

        register("androidRoom") {
            id = libs.plugins.my.android.room.get().pluginId
            implementationClass = "AndroidRoomPlugin"
        }

        register("androidPresentationUi") {
            id = libs.plugins.my.android.presentation.ui.get().pluginId
            implementationClass = "AndroidPresentationUiPlugin"
        }

        register("kotlinLibrary") {
            id = libs.plugins.my.kotlin.library.get().pluginId
            implementationClass = "KotlinLibraryPlugin"
        }
    }
}