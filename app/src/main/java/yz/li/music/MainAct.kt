package yz.li.music

import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import yz.li.core_mvvm.BaseBindingAct
import yz.li.core_mvvm.exts.binding
import yz.li.core_tools.exts.getPlatformProxy
import yz.li.music.databinding.MainActBinding
import yz.li.music.platformlog.ILogPlatformAction

class MainAct : BaseBindingAct<MainActBinding>() {
    companion object {
        const val TAG = "MainAct"
        fun launch(
            context: Context,
        ) {
            context.startActivity(Intent(context, MainAct::class.java))
        }
    }

    private val logProxy by lazy {
        getPlatformProxy<ILogPlatformAction>()
    }
    override val mBinding by binding<MainActBinding>(R.layout.main_act)

    override fun setup() {
        super.setup()
        logProxy.log()
        registerEvent()
    }

    private fun registerEvent() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(false)
            }
        })
    }
}