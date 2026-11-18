package yz.li.core_mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
) : BaseBindingFragment<VB>(inflate) {
    // 缓存根视图实例
    private var cachedRootView: View? = null

    // 标记是否已完成首次初始化
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currentView = cachedRootView
        if (currentView != null) {
            // 关键保护：如果缓存的 View 仍挂载在旧的父容器上，先将其移除，防止抛出 IllegalStateException
            (currentView.parent as? ViewGroup)?.removeView(currentView)
            return currentView
        }

        // 首次进入时正常创建 Binding 并缓存根视图
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

    /**
     * 子类重写此方法，编写控件监听设置与首次网络请求逻辑。
     * 切换 Tab 回来时不会重复触发此方法。
     */
    protected abstract fun initViewAndLoadData()

    override fun onDestroy() {
        super.onDestroy()
        // 注意：必须在 onDestroy（Fragment 彻底销毁）而非 onDestroyView 时置空
        _binding = null
        cachedRootView = null
        isInitialized = false
    }
}