import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/18 19:56
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "lyzmusic.android.library")
            apply(plugin = "lyzmusic.android.mvvm")
            apply(plugin = "lyzmusic.hilt")
            dependencies {
                "implementation"(project(":core:core_tools"))
                "implementation"(project(":core:core_mvvm"))
                "implementation"(project(":core:core_common"))
            }
        }
    }
}
