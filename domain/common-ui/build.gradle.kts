plugins {
    alias(libs.plugins.lyzmusic.android.library)
    alias(libs.plugins.lyzmusic.android.mvvm)
}

android {
    namespace = "yz.li.domain.common_ui"
}

dependencies {
    // 控件需要与 NavController 联动，自动同步选中态和显隐。
    implementation(libs.bundles.navigation)
}
