package yz.li.music.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.BaseBindingFragment
import yz.li.music.R
import yz.li.music.databinding.MainFrgBinding


/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/17 17:36
 */
@AndroidEntryPoint
class MainFrg : BaseBindingFragment<MainFrgBinding>(MainFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 关键修改：在 Fragment 中嵌套导航容器时，必须使用 childFragmentManager 获取
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. 将 BottomNavigationView 与内部的 NavController 绑定
        binding.bottomNav.setupWithNavController(navController)

        // 3. 监听内部目的地变化，控制底部导航栏显隐
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.discoverFragment,
                R.id.libFragment,
                R.id.mineFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }

                else -> {
                    binding.bottomNav.visibility = View.GONE
                }
            }
        }
    }
}