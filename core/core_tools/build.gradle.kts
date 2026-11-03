plugins {
    alias(libs.plugins.lyzmusic.android.library)
}

android {
    namespace = "yz.li.core_tools"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.tencent.mmkv.static)

    api(libs.timber)

}