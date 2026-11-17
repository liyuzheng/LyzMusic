package yz.li.music

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import yz.li.core_mvvm.BaseBindingAct
import yz.li.core_mvvm.exts.binding
import yz.li.core_tools.exts.getPlatformProxy
import yz.li.music.databinding.MainActBinding
import yz.li.music.platformlog.ILogPlatformAction
import yz.li.music.splash.SplashManager

class MainAct : BaseBindingAct<MainActBinding>() {
    companion object {
        const val TAG = "MainAct"
        fun launch(
            context: Context,
        ) {
            context.startActivity(Intent(context, MainAct::class.java))
        }
    }

    private val mSplashViewModel by viewModels<SplashViewModel>()
    private val logProxy by lazy {
        getPlatformProxy<ILogPlatformAction>()
    }
    override val mBinding by binding<MainActBinding>(R.layout.main_act)
    var isLightStatusBar = true
    override fun onCreate(savedInstanceState: Bundle?) {
        SplashManager(this).setup { mSplashViewModel.isReady.get() }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        mBinding.img1.setOnClickListener {
            val statusBarStyle = if (isLightStatusBar) {
                isLightStatusBar = false
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            } else {
                isLightStatusBar = true
                SystemBarStyle.dark(Color.TRANSPARENT)
            }
            enableEdgeToEdge(statusBarStyle = statusBarStyle)
        }
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