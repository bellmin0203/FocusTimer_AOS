import com.jm.focustimer.convention.configureCoroutineKotlin
import com.jm.focustimer.convention.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> KotlinLibraryPlugin applying...")
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")

                configureKotlin()
                configureCoroutineKotlin()
            }
            println(">>> KotlinLibraryPlugin done")
        }
    }
}