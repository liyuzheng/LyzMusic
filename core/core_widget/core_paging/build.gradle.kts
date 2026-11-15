plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.android.mvvm)
}

android {
    namespace = "yz.li.core_paging"
}

dependencies {
    implementation(libs.androidx.paging.paging.runtime)
}