plugins {
    alias(libs.plugins.lyzmusic.feature.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "yz.li.domain.network"
}

dependencies {
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    compileOnly(projects.core.coreTools)
    compileOnly(projects.core.coreNetwork)
    compileOnly(libs.com.tencent.mmkv.static)
}