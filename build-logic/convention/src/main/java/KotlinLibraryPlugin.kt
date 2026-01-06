import com.jm.teumtimer.convention.configureCoroutineKotlin
import com.jm.teumtimer.convention.configureKotest
import com.jm.teumtimer.convention.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> KotlinLibraryPlugin applying...")
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")

                configureKotlinJvm()
                configureCoroutineKotlin()
                configureKotest()
            }
            println(">>> KotlinLibraryPlugin done")
        }
    }
}