package yz.li.music.initializer

import android.content.Context
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV
import timber.log.Timber
import yz.li.core_tools.exts.debug

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/18 9:50
 */
class CommonInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val appContext = context.applicationContext
        debug {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "${super.createStackElementTag(element)}.${element.methodName}:${element.lineNumber}"
                }

                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, tag, "$tag:$message", t)
                }
            })
        }
        MMKV.initialize(appContext)
        Timber.v("MMKV.initialize")
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}