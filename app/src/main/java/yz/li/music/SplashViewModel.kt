package yz.li.music

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import yz.li.core_mvvm.BaseViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * desc:
 * createed by liyuzheng on 2023/8/31 14:04
 */
class SplashViewModel : BaseViewModel() {
    val isReady = AtomicBoolean(false)

    fun init() {
        viewModelScope.launch {
            try {
                val result = withTimeoutOrNull(5000.milliseconds) {
                    coroutineScope {
                        // 协程 1：保底 1500ms 计时器
                        launch {
                            delay(1500.milliseconds)
                            Timber.v("1500 milliseconds passed")
                        }
                        // 协程 2：实际执行的初始化任务
                        launch {
                            runSafely("test") {
                                Timber.v("test task ready")
                            }
                        }
                        launch {
                            delay(2000.milliseconds)
                            Timber.v("2000 milliseconds passed")
                        }
                    }
                    // 只有当上面两个协程【全部】完成后，才会执行到这里
                    Timber.v("all task passed")
                }
                // 2. 通过判空精确感知是否超时
                if (result == null) {
                    Timber.v("启动初始化已超过 5000ms，触发超时放行机制")
                    // 可在此处记录埋点、标记弱网状态或提示用户
                } else {
                    Timber.v("启动初始化在规定时间内正常完成")
                }
            } finally {
                isReady.set(true)
            }
        }
    }
}

private suspend inline fun runSafely(taskName: String, crossinline block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e, "启动任务 [$taskName] 执行失败，已安全跳过")
    }
}
