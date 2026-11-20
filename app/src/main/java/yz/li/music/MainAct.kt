package yz.li.music

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.BaseBindingAct
import yz.li.core_mvvm.exts.binding
import yz.li.core_tools.exts.getPlatformProxy
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

    override fun setup() {
        super.setup()
        logProxy.log()
        registerEvent()
        mSplashViewModel.init()
    }

    /** 主页面按返回键时退到后台，而不是直接结束 Activity。 */
    private fun registerEvent() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(false)
            }
        })
    }
}
