package yz.li.music.splash

import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * @Description: 启动屏管理类
 * @Author: yuzhe
 * @CreateDate: 2026/11/17 17:36
 */
class SplashManager(private val activity: ComponentActivity) {
    fun setup(isReadyCondition: () -> Boolean) {
        val window = activity.window
        // 允许内容延伸到系统栏区域（Edge-to-Edge），防止隐藏/显示系统栏时页面跳动
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 1. 启动期间：隐藏状态栏和导航栏
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val splashScreen = activity.installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            !isReadyCondition()
        }

        splashScreen.setOnExitAnimationListener { provider ->
            val splashScreenView = provider.view
            val iconView = provider.iconView

            // 仅让中央图标做弹性晃动，避免黑底边缘移出屏幕露底
            SpringAnimation(iconView, SpringAnimation.TRANSLATION_X, 0f).apply {
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                setStartValue(0f)
                setStartVelocity(-2000f)
                addEndListener { _, _, _, _ ->
                    // 2. 动画结束准备进入白底主页：恢复显示状态栏和导航栏
                    insetsController.apply {
                        show(WindowInsetsCompat.Type.systemBars())
                        // 确保白底状态下，状态栏时间和导航栏指示条是黑色（深色图标）
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }

                    // 3. 黑底启动页平滑淡出并移除，露出下方的白底主页
                    splashScreenView.animate()
                        .setDuration(250L)
                        .withEndAction { provider.remove() }
                        .start()
                }
            }.start()
        }
    }
}
