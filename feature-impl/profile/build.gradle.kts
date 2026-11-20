plugins {
    alias(libs.plugins.lyzmusic.feature.library)
}

android {
    namespace = "yz.li.feature.profile"
}

dependencies {
    implementation(libs.androidx.browser)
}