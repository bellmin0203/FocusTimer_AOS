import com.android.build.api.dsl.ApplicationExtension
import com.jm.harufocus.convention.ExtensionType
import com.jm.harufocus.convention.configureBuildTypes
import com.jm.harufocus.convention.configureKotestAndroid
import com.jm.harufocus.convention.configureKotlinAndroid
import com.jm.harufocus.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            println(">>> AndroidApplicationPlugin applying...")

            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()

                    // Semantic Versioning Mapping (Automated)
                    val major = libs.findVersion("appVersion-major").get().toString().toInt()
                    val minor = libs.findVersion("appVersion-minor").get().toString().toInt()
                    val patch = libs.findVersion("appVersion-patch").get().toString().toInt()
                    val build = libs.findVersion("appVersion-build").get().toString().toInt()
                    versionCode = (major * 1_000_000) + (minor * 10_000) + (patch * 100) + build
                    versionName = libs.findVersion("projectVersionName").get().toString()
                }

                configureKotlinAndroid()
                configureBuildTypes(ExtensionType.APPLICATION)
                configureKotestAndroid()
            }

            println(">>> AndroidApplicationPlugin done")
        }
    }
}
