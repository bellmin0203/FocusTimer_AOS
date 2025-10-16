import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.jm.focustimer.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidRoomPlugin applying...")

            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<KspExtension> {
                // Kotlin-only 프로젝트에서 컴파일 타임 최적화
                // Room의 KSP processor에게 Kotlin 소스코드를 생성하라고 지시하는 옵션
                arg("room.generateKotlin", "true")
            }

            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.findLibrary("room.runtime").get())
                "implementation"(libs.findLibrary("room.ktx").get())
                "ksp"(libs.findLibrary("room.compiler").get())
            }

            println(">>> AndroidRoomPlugin done")
        }
    }
}