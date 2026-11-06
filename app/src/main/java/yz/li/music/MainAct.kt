package yz.li.music

import yz.li.core_mvvm.BaseBindingAct
import yz.li.core_mvvm.exts.binding
import yz.li.core_tools.exts.getPlatformProxy
import yz.li.music.databinding.MainActBinding
import yz.li.music.platformlog.ILogPlatformAction

class MainAct : BaseBindingAct<MainActBinding>() {
    private val logProxy by lazy {
        getPlatformProxy<ILogPlatformAction>()
    }
    override val mBinding by binding<MainActBinding>(R.layout.main_act)

    override fun setup() {
        super.setup()
        logProxy.log()
    }
}