package yz.li.music

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yz.li.core_mvvm.BaseViewModel
import kotlin.time.Duration.Companion.milliseconds

/**
 * desc:
 * createed by liyuzheng on 2023/8/31 14:04
 */
class SplashViewModel : BaseViewModel() {
    var isReady = false

    fun init() {
        viewModelScope.launch {
            //do something init
            //模拟初始化
            delay(2000.milliseconds)
            isReady = true
        }
    }
}