plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.room)
}

android {
    namespace = "yz.li.domain.paging"
}

dependencies {
    implementation(libs.androidx.paging.paging.runtime)
    implementation(projects.core.coreWidget.corePaging)
}