plugins {
    alias(libs.plugins.lyzmusic.android.library)
}

android {
    namespace = "yz.li.core_network"
}

dependencies {
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    compileOnly(projects.core.coreTools)
    compileOnly(libs.kotlinx.serialization.json)
}