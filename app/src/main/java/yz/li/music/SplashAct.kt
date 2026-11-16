package yz.li.music

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import yz.li.core_common.BaseAct

/**
 * desc:
 * createed by liyuzheng on 2023/8/28 21:05
 */
class SplashAct : BaseAct() {
    override fun getLayoutId() = 0
    private val mViewModel by viewModels<SplashViewModel>()
    override fun setup() {
        if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) || !isTaskRoot) {
            finish()
            return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setupExitSplashScreenAnim(splashScreen)
        splashScreen.setKeepOnScreenCondition {
            // 只要返回 true，启动画面就会一直停留在屏幕上；一旦变为 false 立即平滑消失
            !mViewModel.isReady
        }
        mViewModel.init()
    }


    private fun setupExitSplashScreenAnim(splashScreen: SplashScreen) {
        splashScreen.setOnExitAnimationListener { provider ->
            val splashScreenView = provider.view
            SpringAnimation(
                splashScreenView,
                SpringAnimation.TRANSLATION_X,
                splashScreenView.x
            ).apply {
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                setStartValue(splashScreenView.x)
                setStartVelocity(-2000f)
                addEndListener { _, _, _, _ ->
                    jumpToMain()
                    provider.remove()
                }
            }.start()
        }
    }

    private fun jumpToMain() {
        MainAct.launch(this@SplashAct)
        finish()
    }
}