import com.jm.harufocus.convention.ExtensionType
import com.jm.harufocus.convention.configureBuildTypes
import com.jm.harufocus.convention.configureCoroutineAndroid
import com.jm.harufocus.convention.configureKotest
import com.jm.harufocus.convention.configureKotlinAndroid
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