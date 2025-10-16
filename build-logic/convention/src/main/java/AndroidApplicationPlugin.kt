import com.android.build.api.dsl.ApplicationExtension
import com.jm.focustimer.convention.ExtensionType
import com.jm.focustimer.convention.configureBuildTypes
import com.jm.focustimer.convention.configureKotlinAndroid
import com.jm.focustimer.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidApplicationPlugin applying...")

            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                    versionName = libs.findVersion("projectVersionName").get().toString()
                }

                configureKotlinAndroid()

                configureBuildTypes(ExtensionType.APPLICATION)
            }

            println(">>> AndroidApplicationPlugin done")
        }
    }
}