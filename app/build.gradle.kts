plugins {
    alias(libs.plugins.lyzmusic.android.application)
    alias(libs.plugins.lyzmusic.hilt)
}

android {
    namespace = "yz.li.music"

    defaultConfig {
        applicationId = "yz.li.music"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    val autoIncludePrefixes = listOf(":core", ":data", ":domain", ":feature-api", ":feature-impl")
    val excludeModules = setOf("") // 按需排除测试模块

    rootProject.subprojects
        .filter { project ->
            project.childProjects.isEmpty() &&
                    autoIncludePrefixes.any { prefix -> project.path.startsWith(prefix) } &&
                    project.path !in excludeModules
        }
        .forEach { implementation(it) }

    implementation(libs.autoService.annotations)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.lottie)
    implementation(libs.com.tencent.mmkv.static)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.bundles.navigation)
    implementation(libs.androidx.browser)
    // 可选：如果需要在该模块的 Fragment 中使用 hiltNavGraphViewModels()
    implementation(libs.androidx.hilt.navigation.fragment)
    ksp(libs.autoService.ksp)
    api(libs.timber)
}