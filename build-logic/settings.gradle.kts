/**
 * 일반적으로 Gradle 빌드 스크립트에서 프로젝트 객체의 속성에 접근할 때,
 * Groovy 스크립트 언어의 동적 타입 시스템으로 인해 컴파일 타임에는 타입 검사가 이루어지지 않습니다.
 * 이것은 오타나 잘못된 속성 이름을 사용할 경우 런타임 오류가 발생할 수 있다는 의미입니다.
 * 이 기능을 사용한다면 기존의 ':'를 사용한 project(":client")와 같은 방식이 아닌
 * implementation projects.client처럼 '.'을 사용해 객체의 속성에 접근할 수 있습니다.
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")