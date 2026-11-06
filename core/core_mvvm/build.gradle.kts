plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.android.mvvm)
}

android {
    namespace = "yz.li.core_mvvm"
}

dependencies {
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreTools)
}