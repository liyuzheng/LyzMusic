plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.room)
}

android {
    namespace = "yz.li.domain.database"
}

dependencies {
    implementation(projects.core.coreTools)
}