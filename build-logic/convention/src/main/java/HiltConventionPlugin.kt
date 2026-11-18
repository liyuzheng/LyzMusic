import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import yz.li.convention.exts.libs

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.devtools.ksp")
            dependencies {
                "ksp"(libs.findLibrary("hilt.compiler").get())
            }

            // 3. 场景 A：如果是纯 Kotlin/JVM 模块（如 core:model），只需引入 hilt-core
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                dependencies {
                    "implementation"(libs.findLibrary("hilt.core").get())
                }
            }

            // 4. 场景 B：如果是 Android 模块（app 或 library），应用 Hilt Android 插件并引入 hilt-android
            pluginManager.withPlugin("com.android.base") {
                apply(plugin = libs.findPlugin("hilt").get().get().pluginId)
                dependencies {
                    "implementation"(libs.findLibrary("hilt.android").get())
                }
            }
        }
    }
}
