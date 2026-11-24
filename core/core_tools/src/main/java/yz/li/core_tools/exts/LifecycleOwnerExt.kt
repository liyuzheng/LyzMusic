package yz.li.core_tools.exts

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * desc:
 * createed by liyuzheng on 2023/8/29 0:35
 */
inline fun LifecycleOwner.launchWhenResumed(
    retryTime: Int = 1,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        var retryCount = 0
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                block()
                this@launch.cancel()
            } finally {
                if (retryTime != -1) {
                    retryCount += 1
                    if (retryCount >= retryTime) {
                        this@launch.cancel()
                    }
                }
            }
        }
    }
}

inline fun LifecycleOwner.launchWhenCreated(
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            block()
            this@launch.cancel()
        }
    }
}

suspend inline fun runSafely(taskName: String, crossinline block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e, "启动任务 [$taskName] 执行失败，已安全跳过")
    }
}