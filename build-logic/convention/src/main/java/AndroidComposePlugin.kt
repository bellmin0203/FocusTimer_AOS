import com.jm.focustimer.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidComposePlugin applying...")

            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureAndroidCompose()

            println(">>> AndroidComposePlugin done")
        }
    }
}