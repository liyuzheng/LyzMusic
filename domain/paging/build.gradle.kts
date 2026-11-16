plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.room)
    alias(libs.plugins.lyzmusic.hilt)
}

android {
    namespace = "yz.li.domain.paging"
}

dependencies {
    implementation(libs.androidx.paging.paging.runtime)
    implementation(projects.core.coreWidget.corePaging)
    implementation(projects.core.coreNetwork)
    implementation(projects.domain.database)
}