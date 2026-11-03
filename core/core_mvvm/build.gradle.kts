plugins {
    alias(libs.plugins.lyzmusic.android.mvvm.library)
}

android {
    namespace = "yz.li.core_mvvm"
}

dependencies {
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreTools)
}