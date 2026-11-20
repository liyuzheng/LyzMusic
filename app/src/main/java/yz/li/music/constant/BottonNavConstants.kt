package yz.li.music.constant

import yz.li.domain.common_ui.widget.GlassBottomNavView
import yz.li.music.R

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 18:22
 */
object BottonNavConstants {
    internal val NAV_ITEMS = listOf(
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
}