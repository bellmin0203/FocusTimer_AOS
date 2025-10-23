import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidPresentationUiPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidPresentationUiPlugin applying...")

            with(pluginManager) {
                apply("my.android.library")
                apply("my.android.compose")
                apply("my.hilt")
            }

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:domain"))
            }
        }
        println(">>> AndroidPresentationUiPlugin done")
    }
}