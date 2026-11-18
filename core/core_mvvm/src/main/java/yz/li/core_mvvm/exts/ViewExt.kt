package yz.li.core_mvvm.exts

import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * desc:
 * created by liyuzheng on 2023/9/2 15:30
 */
inline fun <T : ViewBinding> View.binding(
    crossinline inflater: (LayoutInflater) -> T
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    inflater(LayoutInflater.from(context))
}