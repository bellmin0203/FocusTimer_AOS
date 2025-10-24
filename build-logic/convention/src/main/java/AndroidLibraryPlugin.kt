import com.jm.focustimer.convention.ExtensionType
import com.jm.focustimer.convention.configureBuildTypes
import com.jm.focustimer.convention.configureCoroutineAndroid
import com.jm.focustimer.convention.configureKotest
import com.jm.focustimer.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidLibraryPlugin applying...")

            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            configureKotlinAndroid()
            configureBuildTypes(extensionType = ExtensionType.LIBRARY)
            configureCoroutineAndroid()
            configureKotest()

            println(">>> AndroidLibraryPlugin done")
        }
    }
}