plugins {
    alias(libs.plugins.lyzmusic.android.library)
}

android {
    namespace = "yz.li.data.profile"
}

dependencies {
    compileOnly(libs.com.tencent.mmkv.static)
    compileOnly(projects.domain.database)
    compileOnly(projects.domain.network)
    compileOnly(projects.core.coreNetwork)
    compileOnly(projects.core.coreTools)
}