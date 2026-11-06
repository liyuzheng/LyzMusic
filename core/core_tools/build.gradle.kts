plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.ksp)
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
    implementation(libs.autoService.annotations)
    ksp(libs.autoService.ksp)
    api(libs.timber)
}