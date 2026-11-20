package yz.li.music.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yz.li.core_mvvm.BaseBindingFragment
import yz.li.music.R
import yz.li.music.constant.BottonNavConstants.NAV_ITEMS
import yz.li.music.databinding.MainFrgBinding
import kotlin.time.Duration.Companion.seconds

/**
 * 首页容器 Fragment。
 *
 * 负责：
 * 1. 获取内嵌的 NavHostFragment 与 NavController；
 * 2. 配置底部液态玻璃导航的四个入口；
 * 3. 把导航切换、选中态同步和玻璃效果采样源连接起来。
 */
@AndroidEntryPoint
class MainFrg : BaseBindingFragment<MainFrgBinding>(MainFrgBinding::inflate) {
    private val navController
        get() = (childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun auth() {
        val request = NavDeepLinkRequest.Builder
            .fromUri("lyzmusic://app/auth".toUri())
            .build()

        // 可选：配置滑入滑出转场动画
        val navOptions = NavOptions.Builder()
            .build()
        navController.navigate(request, navOptions)
    }

    private fun setup() {
        setupBackPress()
        setupBottomNav()
        lifecycleScope.launch {
            delay(5.seconds)
            auth()
        }
    }

    /**
     * 解决 NavHostFragment 嵌套在 Fragment 内部时，系统返回键不自动回退子栈的问题
     */
    private fun setupBackPress() {
        // 定义所有一级底部 Tab 的 ID 集合
        val topLevelDestinations = NAV_ITEMS.mapTo(HashSet()) { it.itemId }

        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                // 仅在二级页面或 AuthFragment 时触发，弹出当前页退回上一级 Tab
                navController.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // 实时监听目的地变化：
        // 当前页面【不在】一级 Tab 集合中（说明在 AuthFragment 或二级页面）-> 开启拦截（按返回退回上一页）
        // 当前页面【在】一级 Tab 集合中 -> 关闭拦截（按返回直接退回桌面）
        navController.addOnDestinationChangedListener { _, destination, _ ->
            callback.isEnabled = destination.id !in topLevelDestinations
        }
    }

    private fun setupBottomNav() {
        // 配置底部导航的菜单项：目标 id 需要和 navigation 图中的 id 一致。
        binding.glassNav.setItems(NAV_ITEMS)

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true) // 👈 恢复 Fragment 实例与缓存视图
            .setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = true // 👈 保护 Fragment 实例不被 onDestroy 销毁
            )
            .build()

        // 点击或横滑选中菜单后，由 NavController 切换到对应页面。
        binding.glassNav.setOnItemSelectedListener { itemId ->
            navController.navigate(itemId, null, options)
        }

        // 让导航控件跟随目的地变化自动同步选中态与显隐。
        binding.glassNav.setNavController(navController)

        // 把页面容器作为采样源，玻璃导航会读取其下方内容做放大和模糊。
        binding.glassNav.setContentSource(binding.navHostFragment)
    }
}
