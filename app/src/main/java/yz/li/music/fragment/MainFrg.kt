package yz.li.music.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.BaseBindingFragment
import yz.li.domain.common_ui.widget.GlassBottomNavView
import yz.li.music.R
import yz.li.music.databinding.MainFrgBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        setupBottomNav()
    }

    private fun setupBottomNav() {
        // 在 Fragment 中嵌套 NavHost，必须从 childFragmentManager 获取。
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 配置底部导航的菜单项：目标 id 需要和 navigation 图中的 id 一致。
        binding.glassNav.setItems(
            listOf(
                GlassBottomNavView.NavItem(
                    R.id.homeFragment,
                    R.drawable.ic_home,
                    R.string.main_nav_home
                ),
                GlassBottomNavView.NavItem(
                    R.id.discoverFragment,
                    R.drawable.ic_discover,
                    R.string.main_nav_discover
                ),
                GlassBottomNavView.NavItem(
                    R.id.libFragment,
                    R.drawable.ic_lib,
                    R.string.main_nav_lib
                ),
                GlassBottomNavView.NavItem(
                    R.id.mineFragment,
                    R.drawable.ic_profile,
                    R.string.main_nav_mine
                )
            )
        )

        // 点击或横滑选中菜单后，由 NavController 切换到对应页面。
        binding.glassNav.setOnItemSelectedListener { itemId ->
            navController.navigate(itemId)
        }

        // 让导航控件跟随目的地变化自动同步选中态与显隐。
        binding.glassNav.setNavController(navController)

        // 把页面容器作为采样源，玻璃导航会读取其下方内容做放大和模糊。
        binding.glassNav.setContentSource(binding.navHostFragment)
    }
}
