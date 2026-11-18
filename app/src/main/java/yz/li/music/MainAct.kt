package yz.li.music

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
        super.onCreate(savedInstanceState)
    }


    override fun setup() {
        super.setup()
        logProxy.log()
        registerEvent()
        mSplashViewModel.init()
    }

    private fun registerEvent() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(false)
            }
        })
    }
}