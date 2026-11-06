plugins {
    alias(libs.plugins.lyzmusic.android.application)
    alias(libs.plugins.ksp)
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
        dataBinding = true
    }
}

dependencies {
    implementation(projects.core.coreTools)
    implementation(projects.core.coreMvvm)
    implementation(projects.core.coreCommon)
    implementation(libs.autoService.annotations)
    ksp(libs.autoService.ksp)
    api(libs.timber)
}