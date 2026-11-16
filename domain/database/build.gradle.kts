plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.room)
    alias(libs.plugins.lyzmusic.hilt)
}

android {
    namespace = "yz.li.domain.database"
}

dependencies {
    implementation(projects.core.coreTools)
}