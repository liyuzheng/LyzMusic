plugins {
    alias(libs.plugins.lyzmusic.feature.library)
    alias(libs.plugins.lyzmusic.android.mvvm)
}

android {
    namespace = "yz.li.feature.profile"
}

dependencies {
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    compileOnly(libs.androidx.browser)
    compileOnly(projects.data.profile)
}