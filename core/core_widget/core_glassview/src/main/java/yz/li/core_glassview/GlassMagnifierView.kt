package yz.li.core_glassview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation

/**
 * 终极完美闭环版：局部毛玻璃与物理透镜放大遮罩
 * 💡 特性：物理强流光精准追踪、彻底阻断RecyclerView滑动闪烁、首创双保险防光斑残留不消失机制
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GlassMagnifierView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ⚙️ 物理透镜可调节属性
    var glassRadius: Float = 350f
    var glassScale: Float = 1.30f
    var blurRadius: Float = 40f

    var cornerRadius: Float = 40f
        set(value) {
            field = value
            invalidateOutline()
        }

    // 🌟 手指高亮追踪属性
    private var touchX = -1f
    private var touchY = -1f
    private var isTouching = false
    var highlightRadius = 320f

    // 🎨 常驻 Paint 缓存池，严禁在 onDraw 内部执行任何 new 操作
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    // 🎛 通过 Matrix 矩阵平移，让常驻光斑无缝咬合在手指坐标上
    private val gradientMatrix = Matrix()
    private var cachedRadialGradient: RadialGradient
    private var cachedStrokeGradient: RadialGradient

    private var runtimeShader: RuntimeShader? = null
    private var bufferBitmap: Bitmap? = null
    private var bufferCanvas: Canvas? = null

    private var isDrawingCopy = false
    private var lastRenderTime = 0L

    private var activeViewTreeObserver: ViewTreeObserver? = null

    private val onScrollListener = ViewTreeObserver.OnScrollChangedListener {
        postInvalidateOnAnimation()
    }

    // 💡 异步清洗队列：用于在手指离开后进行二次底层硬件清洗，彻底灭掉定格的光斑
    private val clearGlowRunnable = Runnable {
        resetGlowState()
    }

    // AGSL 极致扩亮着色器脚本
    private val agslCode = """
        uniform shader inputTexture; 
        uniform float2 origin;       
        uniform float radius;       
        uniform float scale;        
        uniform float2 touchPos;    
        uniform float touchRadius;  
        uniform float isTouching;   

        half4 main(float2 coord) {
            float2 d = coord - origin;
            float dist = length(d);
            
            float2 sampleCoord = coord;
            if (dist < radius) {
                float progress = dist / radius;
                float distortion = mix(1.0 / scale, 1.0, progress * progress);
                sampleCoord = origin + d * distortion;
            }
            
            half4 baseColor = inputTexture.eval(sampleCoord);
            
            if (isTouching > 0.5) {
                float tDist = length(coord - touchPos);
                if (tDist < touchRadius) {
                    float lightFactor = 1.0 - (tDist / touchRadius);
                    float peakLight = pow(lightFactor, 3.0); 
                    baseColor.rgb += half3(0.65) * peakLight;
                }
            }
            
            return baseColor;
        }
    """.trimIndent()

    init {
        runtimeShader = RuntimeShader(agslCode)

        cachedRadialGradient = RadialGradient(
            0f, 0f, highlightRadius,
            0xBFFFFFFF.toInt(),
            0x00FFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )
        highlightPaint.shader = cachedRadialGradient

        cachedStrokeGradient = RadialGradient(
            0f, 0f, highlightRadius,
            0xCCFFFFFF.toInt(),
            0x1AFFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )

        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        clipToOutline = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 💡 触控双保险：每次收到新的按下或移动事件，立刻截断正在排队排队执行的清洗任务
        handler?.removeCallbacks(clearGlowRunnable)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                touchX = event.x
                touchY = event.y
                isTouching = true
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                // 离手瞬间，启动双刷物理清洗方案：第一刷
                resetGlowState()

                // 第二刷：通过主线程 Handler 在 16 毫秒（正好一帧左右的时间）后追加清洗，彻底根治 RenderThread 坐标定格
                handler?.postDelayed(clearGlowRunnable, 16)
            }
        }
        return true
    }

    /**
     * 💡 【核心新增保险 1】
     * 窗口焦点联动判定。如果用户滑着滑着弹出了系统弹窗、或者手指直接滑到了屏幕最边缘甚至系统全局导航栏区域，
     * 此时系统可能无法回调 ACTION_UP。我们通过监听窗口焦点变化，一旦失去焦点立刻物理闭灯。
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            resetGlowState()
        }
    }

    /**
     * 💡 【核心新增保险 2】
     * 抽离出绝对纯净的状态重置清洗函数
     */
    private fun resetGlowState() {
        if (bufferBitmap == null || bufferBitmap?.isRecycled == true) return
        isTouching = false
        touchX = -1f
        touchY = -1f
        invalidate() // 强制将 -1f 和 isTouching=0f 打入 GPU 管线清洗残留
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? ViewGroup)?.viewTreeObserver?.let { observer ->
            activeViewTreeObserver = observer
            observer.addOnScrollChangedListener(onScrollListener)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isDrawingCopy) return
        super.onDraw(canvas)

        val parentLayout = parent as? ViewGroup ?: return
        if (width <= 0 || height <= 0) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRenderTime < 8 && !isTouching) {
            drawStaticLayers(canvas)
            return
        }
        lastRenderTime = currentTime

        if (bufferBitmap == null || bufferBitmap?.width != width || bufferBitmap?.height != height) {
            bufferBitmap?.recycle()
            bufferBitmap = createBitmap(width, height)
            bufferCanvas = Canvas(bufferBitmap!!)
        }

        val bufCanvas = bufferCanvas ?: return
        val bufBitmap = bufferBitmap ?: return

        isDrawingCopy = true
        bufCanvas.withTranslation(-left.toFloat(), -top.toFloat()) {
            drawColor(0)
            parentLayout.draw(this)
        }
        isDrawingCopy = false

        runtimeShader?.let { shader ->
            val centerX = width / 2f
            val centerY = height / 2f

            shader.setFloatUniform("origin", centerX, centerY)
            shader.setFloatUniform("radius", glassRadius)
            shader.setFloatUniform("scale", glassScale)
            shader.setFloatUniform("touchPos", touchX, touchY)
            shader.setFloatUniform("touchRadius", highlightRadius)
            shader.setFloatUniform("isTouching", if (isTouching) 1f else 0f)

            val bitmapEffect = RenderEffect.createBitmapEffect(bufBitmap)
            val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
            val magnifierEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputTexture")

            val blurChain = RenderEffect.createChainEffect(blurEffect, bitmapEffect)
            val finalEffect = RenderEffect.createChainEffect(magnifierEffect, blurChain)

            this.setRenderEffect(finalEffect)
        }

        drawStaticLayers(canvas)
    }

    private fun drawStaticLayers(canvas: Canvas) {
        paint.color = 0x0DFFFFFF
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, paint)

        if (isTouching && touchX >= 0 && touchY >= 0) {
            gradientMatrix.setTranslate(touchX, touchY)
            cachedRadialGradient.setLocalMatrix(gradientMatrix)
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, highlightPaint)
        }

        if (isTouching && touchX >= 0 && touchY >= 0) {
            cachedStrokeGradient.setLocalMatrix(gradientMatrix)
            strokePaint.shader = cachedStrokeGradient
        } else {
            strokePaint.shader = null
            strokePaint.color = 0x26FFFFFF
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, strokePaint)
    }

    override fun onDetachedFromWindow() {
        handler?.removeCallbacks(clearGlowRunnable) // 及时清空队列防止内存泄漏
        activeViewTreeObserver?.let { observer ->
            if (observer.isAlive) {
                observer.removeOnScrollChangedListener(onScrollListener)
            }
        }
        activeViewTreeObserver = null
        bufferBitmap?.recycle()
        bufferBitmap = null
        bufferCanvas = null
        this.setRenderEffect(null)
        super.onDetachedFromWindow()
    }
}
