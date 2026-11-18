package yz.li.core_mvvm.exts

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * desc:
 * createed by liyuzheng on 2023/8/28 17:23
 */
inline fun <T : ViewBinding> AppCompatActivity.binding(
    crossinline inflater: (LayoutInflater) -> T
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    inflater(layoutInflater)
}