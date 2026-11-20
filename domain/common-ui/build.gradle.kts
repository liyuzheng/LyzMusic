plugins {
    alias(libs.plugins.lyzmusic.android.library)
}

android {
    namespace = "yz.li.domain.common_ui"

    buildFeatures {
        // GlassBottomNavView 使用 ViewBinding 加载自身布局和导航项布局。
        viewBinding = true
    }
}

dependencies {
    // 控件需要与 NavController 联动，自动同步选中态和显隐。
    implementation(libs.bundles.navigation)
}
