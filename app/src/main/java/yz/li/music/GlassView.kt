package yz.li.music

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.S) // 强制约束仅支持 Android 12 (API 31) 及以上
class GlassMagnifierView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 🔬 可调节参数
    var glassRadius: Float = 350f  // 放大镜的半径（单位：像素）
    var glassScale: Float = 1.30f  // 放大倍率（1.30 表示放大 1.3 倍）
    var blurRadius: Float = 20f    // 毛玻璃模糊度（数值越大越模糊）

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val runtimeShader: RuntimeShader

    // AGSL 顶点/片元着色器：实时计算像素点到圆心的距离，模拟玻璃透镜折射
    private val agslCode = """
        uniform shader inputTexture; // 捕获下方的视图流
        uniform float2 origin;       // 放大镜中心点
        uniform float radius;       // 作用半径
        uniform float scale;        // 放大系数

        half4 main(float2 coord) {
            float2 d = coord - origin;
            float dist = length(d);
            
            // 在玻璃透镜范围内，通过非线性插值改变采样坐标，实现中心放大、边缘平滑过渡
            if (dist < radius) {
                float progress = dist / radius;
                float distortion = mix(1.0 / scale, 1.0, progress * progress);
                float2 sampleCoord = origin + d * distortion;
                return inputTexture.eval(sampleCoord);
            }
            
            // 范围外保持原样
            return inputTexture.eval(coord);
        }
    """

    init {
        // 1. 初始化自定义着色器
        runtimeShader = RuntimeShader(agslCode)

        // 2. 创建高斯模糊效果
        val blurEffect =
            RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)

        // 3. 将自定义的放大着色器注入渲染管线
        val magnifierEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputTexture")

        // 4. 将【模糊】与【放大】级联（Chain）：下层内容先被模糊，再被放大折射
        val glassEffect = RenderEffect.createChainEffect(magnifierEffect, blurEffect)

        // 5. 应用到当前 View 上（由 GPU 硬件加速实时渲染）
        setRenderEffect(glassEffect)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 实时更新当前遮罩的中心点，确保列表滚动时折射位置绝对精准
        val centerX = width / 2f
        val centerY = height / 2f

        runtimeShader.setFloatUniform("origin", centerX, centerY)
        runtimeShader.setFloatUniform("radius", glassRadius)
        runtimeShader.setFloatUniform("scale", glassScale)

        // 绘制一层带有高级微白反光的玻璃底色（模拟真实玻璃的 10% 物理反光）
        paint.color = 0x1AFFFFFF
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
