package yz.li.core_mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * @Description:
 * 适用于底部导航栏一级 Tab 页面的视图缓存基类
 * 特性：
 * 1. 切换 Tab 时复用已创建的 rootView，避免重复 inflate 布局与列表滚动位置丢失
 * 2. 提供 initViewAndLoadData() 回调，确保数据请求与控件初始化仅在首次进入时执行一次
 * 3. 零 !! 断言，在 Fragment 彻底销毁 (onDestroy) 时自动释放视图引用防泄漏
 * @Author: yuzhe
 * @CreateDate: 2026/11/18 12:58
 */
abstract class KeepAliveTabFragment<VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) {
            "ViewBinding 尚未初始化或 Fragment 已经彻底销毁（onDestroy）。"
        }

    private var cachedRootView: View? = null
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currentView = cachedRootView
        if (currentView != null) {
            (currentView.parent as? ViewGroup)?.removeView(currentView)
            return currentView
        }

        val newBinding = inflate(inflater, container, false)
        _binding = newBinding
        cachedRootView = newBinding.root
        return newBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            initViewAndLoadData()
        }
    }

    protected abstract fun initViewAndLoadData()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        cachedRootView = null
        isInitialized = false
    }
}
