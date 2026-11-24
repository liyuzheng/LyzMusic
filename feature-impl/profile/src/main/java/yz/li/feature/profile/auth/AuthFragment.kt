package yz.li.feature.profile.auth

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.toColorInt
import yz.li.core_mvvm.BaseBindingFragment
import yz.li.domain.network.auth.AuthRepo
import yz.li.feature.profile.databinding.AuthFrgBinding

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:59
 */
class AuthFragment : BaseBindingFragment<AuthFrgBinding>(AuthFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = AuthRepo.buildAuthUri()
        val darkColorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor("#121212".toColorInt())
            .setNavigationBarColor("#121212".toColorInt())
            .build()
        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(darkColorParams)
            // 显示网页标题（默认只显示域名）
            .setShowTitle(true)
            // 隐藏 URL 地址栏的自动隐藏效果，保持顶部栏稳定
            .setUrlBarHidingEnabled(false)
            // 关闭默认分享按钮，保持界面极简干净
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()

        // 3. 启动网页
        customTabsIntent.launchUrl(requireContext(), uri)
    }
}