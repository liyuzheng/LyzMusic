plugins {
    alias(libs.plugins.lyzmusic.android.application)
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
}

dependencies {
    implementation(projects.core.coreTools)
}