package yz.li.core_paging.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * desc:
 * createed by liyuzheng on 2023/3/30 15:59
 */
abstract class BasePagingViewHolder<M, T : ViewBinding>(protected val mBinding: T) :
    RecyclerView.ViewHolder(mBinding.root) {
    open fun setup() {}

    abstract fun bind(data: M)

    open fun recycle() {
    }

    inline fun <T : ViewBinding> viewHolderBinding(
        view: View,
        crossinline binder: (View) -> T
    ): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
        binder(view)
    }
}