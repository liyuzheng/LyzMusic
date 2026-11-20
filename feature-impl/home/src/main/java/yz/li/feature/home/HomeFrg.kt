package yz.li.feature.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.KeepAliveTabFragment
import yz.li.feature.home.databinding.HomeFrgBinding
import kotlin.random.Random
import androidx.core.graphics.createBitmap

/**
 * 首页 Fragment。
 *
 * 这里生成一个可上下滚动的随机图片列表，用于验证底部液态玻璃导航
 * 在内容穿过时产生的放大和模糊效果。
 */
@AndroidEntryPoint
class HomeFrg : KeepAliveTabFragment<HomeFrgBinding>(HomeFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /** 向首页容器中添加测试图片；图片由代码生成，不依赖网络资源。 */
    override fun initViewAndLoadData() {
        repeat(24) { index ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(140)
                ).apply {
                    topMargin = dp(12)
                    marginStart = dp(16)
                    marginEnd = dp(16)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(createRandomImage(index))
            }
            binding.homeContent.addView(imageView)
        }
    }

    /**
     * 生成一张随机渐变图片。
     * 先画线性渐变背景，再叠加若干半透明圆形，让玻璃透视效果更容易观察。
     */
    private fun createRandomImage(seed: Int): Bitmap {
        val random = Random(seed * 31L + 7)
        val width = 600
        val height = 280
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val colorA = Color.rgb(random.nextInt(60, 220), random.nextInt(60, 220), random.nextInt(60, 220))
        val colorB = Color.rgb(random.nextInt(60, 220), random.nextInt(60, 220), random.nextInt(60, 220))
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                colorA, colorB, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        repeat(9) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(
                    random.nextInt(60, 150),
                    random.nextInt(0, 256),
                    random.nextInt(0, 256),
                    random.nextInt(0, 256)
                )
                style = Paint.Style.FILL
            }
            val cx = random.nextInt(0, width).toFloat()
            val cy = random.nextInt(0, height).toFloat()
            val radius = random.nextInt(30, 120).toFloat()
            canvas.drawCircle(cx, cy, radius, paint)
        }
        return bitmap
    }

    /** 将 dp 转换为像素，保证不同密度屏幕上尺寸一致。 */
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
