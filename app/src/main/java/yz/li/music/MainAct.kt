package yz.li.music

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import yz.li.core_mvvm.BaseBindingAct
import yz.li.core_mvvm.exts.binding
import yz.li.core_tools.exts.getPlatformProxy
import yz.li.domain.network.auth.AuthRepo
import yz.li.music.databinding.MainActBinding
import yz.li.music.platformlog.ILogPlatformAction
import yz.li.music.splash.SplashManager

/**
 * 应用主 Activity。
 *
 * 除了承载主页面外，这里还关闭了 Android 10+ 的系统导航栏对比蒙层，
 * 避免底部液态玻璃导航下面出现一条灰色横带。
 */
@AndroidEntryPoint
class MainAct : BaseBindingAct<MainActBinding>() {
    override val mBinding by binding(MainActBinding::inflate)
    private val mSplashViewModel by viewModels<SplashViewModel>()
    private val logProxy by lazy {
        getPlatformProxy<ILogPlatformAction>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SplashManager(this).setup { mSplashViewModel.isReady.get() }
        enableEdgeToEdge()
        // 关闭系统为手势导航栏增加的对比蒙层，让玻璃效果保持通透。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        setIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        // 校验是否匹配你的 scheme、host 和 path
        if (uri.scheme == "lyzmusic" && uri.host == "oauth" && uri.path?.startsWith("/callback") == true) {
            Timber.v("handleOAuthCallback ${intent.data}")
            // 提取回调参数，例如 lyzmusic://oauth/callback?code=xxx&state=yyy
            val state = uri.getQueryParameter("state")
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            Timber.v("recive code: $code state: ${state == AuthRepo.AUTH_STATE} error: $error")
            if (!code.isNullOrEmpty()) {
                AuthRepo.exchangeCodeForToken(code, lifecycleScope)
            } else if (!error.isNullOrEmpty()) {
                //授权失败
            }
            // ⚠️ 防止屏幕旋转或配置变更（如深浅色切换）重建 Activity 时重复消费同一个回调
            intent.data = null
        }
    }

    override fun setup() {
        super.setup()
        logProxy.log()
        mSplashViewModel.init()
    }
}
