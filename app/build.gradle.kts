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
        dataBinding = true
    }
}

dependencies {
    implementation(projects.core.coreTools)
    implementation(projects.core.coreMvvm)
    implementation(projects.core.coreNetwork)
    implementation(projects.core.coreWidget.coreGlassview)
    implementation(projects.core.coreWidget.corePaging)
    implementation(projects.core.coreCommon)
    implementation(projects.domain.database)
    implementation(projects.domain.paging)
    implementation(projects.domain.commonUi)
    implementation(libs.autoService.annotations)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.lottie)
    ksp(libs.autoService.ksp)
    api(libs.timber)
}