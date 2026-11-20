package yz.li.domain.common_ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import androidx.core.view.isVisible
import androidx.navigation.NavController
import yz.li.domain.common_ui.databinding.GlassBottomNavBinding
import yz.li.domain.common_ui.databinding.ItemGlassNavBinding
import kotlin.math.PI
import kotlin.math.sin

/**
 * 液态玻璃底部导航控件。
 *
 * 职责：
 * 1. 展示四个首页入口，并把点击/横滑结果交给外部导航；
 * 2. 跟随 NavController 自动同步选中态与显隐；
 * 3. 采样页面容器在导航下方的内容，做放大、模糊和镜面高光，模拟液态玻璃；
 * 4. 选中态不使用圆框，只通过图标/文字颜色渐变和图标弹跳动画表达。
 */
class GlassBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = GlassBottomNavBinding.inflate(LayoutInflater.from(context), this)

    // 当前已创建的导航项，以及选中态与布局延迟标记。
    private val items = mutableListOf<NavItemView>()
    private var selectedId: Int = View.NO_ID
    private var listener: OnItemSelectedListener? = null
    private var layoutPending = true

    // 前景色策略：选中项固定深色；未选中项根据玻璃下方内容明暗自适应。
    private var isLightSurface = true
    private val selectedColor = "#111827".toColorInt()
    private val lightInactiveColor = "#4B5563".toColorInt()
    private val darkInactiveColor = "#D7DCE8".toColorInt()
    private val inactiveColor: Int
        get() = if (isLightSurface) lightInactiveColor else darkInactiveColor

    // 与 NavController 联动：非首页目的地自动隐藏，首页目的地自动同步选中。
    private var navController: NavController? = null
    private val destinationListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            val handled = items.any { it.itemId == destination.id }
            isVisible = handled
            if (handled) {
                selectedItemId = destination.id
            }
        }

    // 玻璃参数统一按屏幕密度换算为像素，保证不同设备观感一致。
    private val density = context.resources.displayMetrics.density
    private val cornerRadius = 28f * density
    private val baseScale = 1.42f
    private val touchExtraScale = 0.22f
    private val highlightRadius = 160f * density
    private val bandHalfWidth = 100f * density

    // 绘制层：底层渐变、透镜内容、触摸高光、顶部镜面线和描边。
    private val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 230
    }
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#08FFFFFF".toColorInt()
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = "#A6FFFFFF".toColorInt()
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }
    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }
    private val specularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val highlightGradient = RadialGradient(
        0f, 0f, highlightRadius,
        0xB3FFFFFF.toInt(), 0x00FFFFFF.toInt(),
        Shader.TileMode.CLAMP
    )
    private val bandGradient = LinearGradient(
        -bandHalfWidth, 0f, bandHalfWidth, 0f,
        intArrayOf(0x00FFFFFF, 0xB3FFFFFF.toInt(), 0x00FFFFFF),
        floatArrayOf(0f, 0.5f, 1f),
        Shader.TileMode.CLAMP
    )
    private val gradientMatrix = Matrix()
    private val bandMatrix = Matrix()
    private val shaderMatrix = Matrix()
    private var specularGradient: LinearGradient? = null
    private var baseGradient: LinearGradient? = null

    // 离屏采样与模糊缓冲区：避免每帧创建 Bitmap 和 IntArray。
    private var bufferBitmap: Bitmap? = null
    private var bufferCanvas: Canvas? = null
    private var blurredBitmap: Bitmap? = null
    private var blurPixels: IntArray? = null
    private var blurScratch: IntArray? = null
    private val blurRadiusPx = (16f * density).toInt().coerceAtLeast(2)
    private var bitmapShader: BitmapShader? = null
    private var contentSource: View? = null
    private var activeObserver: ViewTreeObserver? = null
    private var isCapturing = false
    private var lastCaptureTime = 0L

    // 页面滚动时主动请求重绘，让玻璃内容跟随列表实时变化。
    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        postInvalidateOnAnimation()
    }

    // 触摸高光状态：记录手指位置、强度，以及选中切换动画状态。
    private var touchX = -1f
    private var touchY = -1f
    private var touchStrength = 0f
    private var touchAnimator: ValueAnimator? = null

    // 选中扫光状态：从旧选中项中心移动到新选中项中心。
    private var sweepX = -1f
    private var sweepStrength = 0f
    private var sweepAnimator: ValueAnimator? = null

    private var previousSelectedId: Int = View.NO_ID
    private var selectionAnimator: ValueAnimator? = null
    private var iconAnimator: ValueAnimator? = null
    private val colorEvaluator = ArgbEvaluator()

    // 开启 ViewGroup 自绘，并把内容裁剪成圆角胶囊形状。
    init {
        setWillNotDraw(false)
        clipToPadding = false
        clipToOutline = true
        highlightPaint.shader = highlightGradient
        bandPaint.shader = bandGradient
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
    }

    /** 绑定导航控制器；当用户到达非首页目的地时隐藏导航，回到首页时同步选中。 */
    fun setNavController(controller: NavController) {
        navController?.removeOnDestinationChangedListener(destinationListener)
        navController = controller
        controller.addOnDestinationChangedListener(destinationListener)
        controller.currentDestination?.let { destination ->
            destinationListener.onDestinationChanged(controller, destination, null)
        }
    }

    /** 根据 [NavItem] 列表创建导航项；itemId 必须与 navigation 图中的目的地 id 一致。 */
    fun setItems(navItems: List<NavItem>) {
        items.clear()
        binding.navItems.removeAllViews()
        navItems.forEach { item ->
            val itemViewId = item.itemId
            val itemBinding = ItemGlassNavBinding.inflate(
                LayoutInflater.from(context),
                binding.navItems,
                true
            ).apply {
                root.id = itemViewId
                navIcon.setImageResource(item.iconRes)
                navLabel.setText(item.labelRes)
                navBadge.text = item.badge
                navBadge.isVisible = !item.badge.isNullOrEmpty()
            }
            items += NavItemView(item.itemId, itemBinding)
        }
        layoutPending = true
        if (selectedId != View.NO_ID) {
            updateSelection(animated = false)
        }
    }

    /** 设置菜单选择回调；通常由外部调用 NavController.navigate。 */
    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        this.listener = listener
    }

    /** 设置选中项；[animated] 为 true 时播放颜色渐变与图标弹跳动画。 */
    fun setSelectedItemId(@IdRes itemId: Int, animated: Boolean = true) {
        if (selectedId == itemId && !layoutPending) return
        previousSelectedId = selectedId
        selectedId = itemId
        updateSelection(animated)
    }

    var selectedItemId: Int
        @IdRes get() = selectedId
        set(@IdRes value) = setSelectedItemId(value)

    /** 给指定菜单设置角标；传 null 或空字符串时隐藏角标。 */
    fun setBadge(@IdRes itemId: Int, badge: String?) {
        items.firstOrNull { it.itemId == itemId }?.itemBinding?.navBadge?.apply {
            text = badge
            isVisible = !badge.isNullOrEmpty()
        }
    }

    /**
     * 设置玻璃效果的采样源。
     * 通常传入铺满屏幕的 NavHostFragment，导航会截取其在自身区域下方的内容并做放大模糊。
     */
    fun setContentSource(view: View?) {
        if (contentSource === view) return
        activeObserver?.let { observer ->
            if (observer.isAlive) {
                observer.removeOnScrollChangedListener(scrollListener)
            }
        }
        contentSource = view
        activeObserver = view?.viewTreeObserver?.also { observer ->
            observer.addOnScrollChangedListener(scrollListener)
        }
        invalidate()
    }

    /**
     * 统一在这里处理触摸：
     * 按下/移动更新高光位置，抬起时按手指停留位置选中菜单。
     * 返回 super 保证事件继续分发给子 View，不拦截列表或点击。
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> updateTouchPoint(event.x, event.y)
            MotionEvent.ACTION_UP -> {
                val index = indexAt(event.x)
                if (index >= 0) {
                    val itemId = items[index].itemId
                    if (itemId != selectedId) {
                        listener?.onItemSelected(itemId)
                    } else {
                        // 点击当前项时，在原位置闪一次高光作为反馈。
                        playSelectionSweep(itemId, itemId)
                    }
                }
                releaseTouchPoint()
            }

            MotionEvent.ACTION_CANCEL -> releaseTouchPoint()
        }
        return super.dispatchTouchEvent(event)
    }

    /** 根据横坐标找到手指所在的导航项索引。 */
    private fun indexAt(x: Float): Int {
        return items.indexOfFirst { item ->
            x >= item.itemBinding.root.left && x <= item.itemBinding.root.right
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) releaseTouchPoint()
    }

    /** 尺寸变化时重建离屏位图、像素缓冲区和渐变，避免每帧分配内存。 */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        bufferBitmap?.recycle()
        blurredBitmap?.recycle()
        bufferBitmap = createBitmap(w, h)
        blurredBitmap = createBitmap(w, h)
        bufferCanvas = Canvas(bufferBitmap!!)
        blurPixels = IntArray(w * h)
        blurScratch = IntArray(w * h)
        bitmapShader = BitmapShader(blurredBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        lensPaint.shader = bitmapShader

        baseGradient = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(0x80FFFFFF.toInt(), 0x66DDE5F0.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        specularGradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            intArrayOf(0x00FFFFFF, 0x99FFFFFF.toInt(), 0x00FFFFFF),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (layoutPending && selectedId != View.NO_ID && items.isNotEmpty()) {
            updateSelection(animated = false)
        }
    }

    /**
     * 绘制顺序：先采样并模糊底层内容，再依次画玻璃底色、放大透镜和表面高光。
     * 采样做了时间节流，避免每帧重绘整棵页面树。
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val now = SystemClock.uptimeMillis()
        if (bufferBitmap == null ||
            contentSource != null && (touchStrength > 0.01f || now - lastCaptureTime >= 24L)
        ) {
            captureContentBelow()
            lastCaptureTime = now
        }

        drawBase(canvas)
        drawMagnifiedContent(canvas)
        drawGlassSurface(canvas)
    }

    /** 视图移除时释放位图、解绑滚动监听并取消动画，防止内存泄漏。 */
    override fun onDetachedFromWindow() {
        touchAnimator?.cancel()
        selectionAnimator?.cancel()
        iconAnimator?.cancel()
        sweepAnimator?.cancel()
        activeObserver?.let { observer ->
            if (observer.isAlive) {
                observer.removeOnScrollChangedListener(scrollListener)
            }
        }
        activeObserver = null
        contentSource = null
        bitmapShader = null
        lensPaint.shader = null
        bufferBitmap?.recycle()
        blurredBitmap?.recycle()
        bufferBitmap = null
        blurredBitmap = null
        bufferCanvas = null
        blurPixels = null
        blurScratch = null
        super.onDetachedFromWindow()
    }

    /**
     * 把采样源绘制到离屏 Bitmap。
     * 通过两个 View 的屏幕坐标差值做平移，只截取导航下方那一块内容。
     */
    private fun captureContentBelow() {
        val source = contentSource ?: return
        val bitmap = bufferBitmap ?: return
        val targetCanvas = bufferCanvas ?: return
        if (isCapturing) return

        val locationSelf = IntArray(2)
        val locationSource = IntArray(2)
        getLocationOnScreen(locationSelf)
        source.getLocationOnScreen(locationSource)

        isCapturing = true
        targetCanvas.withSave {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            translate(
                (locationSource[0] - locationSelf[0]).toFloat(),
                (locationSource[1] - locationSelf[1]).toFloat()
            )
            source.draw(this)
        }
        isCapturing = false
        bitmap.density = source.resources.displayMetrics.densityDpi
        blurCapturedContent()
    }

    /** 对采样结果做两轮可分离盒式模糊，近似高斯模糊且无需 RenderScript。 */
    private fun blurCapturedContent() {
        val source = bufferBitmap ?: return
        val output = blurredBitmap ?: return
        val pixels = blurPixels ?: return
        val scratch = blurScratch ?: return
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0 || blurRadiusPx < 1) return

        source.getPixels(pixels, 0, w, 0, 0, w, h)
        boxBlurHorizontal(pixels, scratch, w, h, blurRadiusPx)
        boxBlurVertical(scratch, pixels, w, h, blurRadiusPx)
        boxBlurHorizontal(pixels, scratch, w, h, blurRadiusPx / 2)
        boxBlurVertical(scratch, pixels, w, h, blurRadiusPx / 2)
        output.setPixels(pixels, 0, w, 0, 0, w, h)
        updateSurfaceTheme(pixels)
    }

    /** 抽样计算模糊内容的平均亮度，超过阈值时切换未选中项的前景色。 */
    private fun updateSurfaceTheme(pixels: IntArray) {
        var sum = 0L
        var count = 0
        var i = 0
        while (i < pixels.size) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            sum += (r * 299 + g * 587 + b * 114) / 1000
            count++
            i += 4
        }
        if (count == 0) return
        val light = (sum / count).toInt() > 140
        if (light != isLightSurface) {
            isLightSurface = light
            applyItemColors()
        }
    }

    /** 水平方向盒式模糊：用滑动窗口累加 ARGB，避免重复计算。 */
    private fun boxBlurHorizontal(
        src: IntArray,
        dst: IntArray,
        width: Int,
        height: Int,
        radius: Int
    ) {
        val window = radius * 2 + 1
        for (y in 0 until height) {
            val row = y * width
            var a = 0
            var r = 0
            var g = 0
            var b = 0
            for (k in -radius..radius) {
                val color = src[row + k.coerceIn(0, width - 1)]
                a += color ushr 24
                r += (color shr 16) and 0xFF
                g += (color shr 8) and 0xFF
                b += color and 0xFF
            }
            for (x in 0 until width) {
                dst[row + x] = pack(a / window, r / window, g / window, b / window)
                val removed = src[row + (x - radius).coerceIn(0, width - 1)]
                val added = src[row + (x + radius + 1).coerceIn(0, width - 1)]
                a += (added ushr 24) - (removed ushr 24)
                r += ((added shr 16) and 0xFF) - ((removed shr 16) and 0xFF)
                g += ((added shr 8) and 0xFF) - ((removed shr 8) and 0xFF)
                b += (added and 0xFF) - (removed and 0xFF)
            }
        }
    }

    /** 垂直方向盒式模糊，与水平模糊组合成二维模糊。 */
    private fun boxBlurVertical(
        src: IntArray,
        dst: IntArray,
        width: Int,
        height: Int,
        radius: Int
    ) {
        val window = radius * 2 + 1
        for (x in 0 until width) {
            var a = 0
            var r = 0
            var g = 0
            var b = 0
            for (k in -radius..radius) {
                val color = src[(k.coerceIn(0, height - 1)) * width + x]
                a += color ushr 24
                r += (color shr 16) and 0xFF
                g += (color shr 8) and 0xFF
                b += color and 0xFF
            }
            for (y in 0 until height) {
                dst[y * width + x] = pack(a / window, r / window, g / window, b / window)
                val removed = src[(y - radius).coerceIn(0, height - 1) * width + x]
                val added = src[(y + radius + 1).coerceIn(0, height - 1) * width + x]
                a += (added ushr 24) - (removed ushr 24)
                r += ((added shr 16) and 0xFF) - ((removed shr 16) and 0xFF)
                g += ((added shr 8) and 0xFF) - ((removed shr 8) and 0xFF)
                b += (added and 0xFF) - (removed and 0xFF)
            }
        }
    }

    /** 把四个通道重新打包成 ARGB 颜色整数。 */
    private fun pack(a: Int, r: Int, g: Int, b: Int): Int {
        return (a.coerceIn(0, 255) shl 24) or
                (r.coerceIn(0, 255) shl 16) or
                (g.coerceIn(0, 255) shl 8) or
                b.coerceIn(0, 255)
    }

    /** 绘制浅色半透明玻璃底。 */
    private fun drawBase(canvas: Canvas) {
        val gradient = baseGradient ?: return
        basePaint.shader = gradient
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerRadius, cornerRadius, basePaint
        )
    }

    /**
     * 用 BitmapShader 绘制放大后的模糊内容。
     * 焦点默认在导航中心，触摸时平滑移向手指位置。
     */
    private fun drawMagnifiedContent(canvas: Canvas) {
        val shader = bitmapShader ?: return
        val centerX = width / 2f
        val centerY = height / 2f
        val useSweep = sweepStrength > touchStrength
        val activeStrength = if (useSweep) sweepStrength else touchStrength
        val focalX = when {
            useSweep -> sweepX
            touchX >= 0f -> centerX + (touchX - centerX) * touchStrength
            else -> centerX
        }
        val scale = baseScale + touchExtraScale * activeStrength

        shaderMatrix.setScale(scale, scale, focalX, centerY)
        shader.setLocalMatrix(shaderMatrix)
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerRadius, cornerRadius, lensPaint
        )
    }

    /**
     * 绘制玻璃表面效果：触摸径向高光、跟随手指的光带、
     * 常驻顶部镜面反光以及圆角描边。
     */
    private fun drawGlassSurface(canvas: Canvas) {
        val right = width.toFloat()
        val bottom = height.toFloat()

        canvas.drawRoundRect(0f, 0f, right, bottom, cornerRadius, cornerRadius, tintPaint)

        val useTouch = touchStrength >= sweepStrength
        val glowX = if (useTouch) touchX else sweepX
        val glowY = if (useTouch) touchY else height / 2f
        val glowStrength = maxOf(touchStrength, sweepStrength)
        if (glowStrength > 0.01f && glowX >= 0f) {
            gradientMatrix.setTranslate(glowX, glowY)
            highlightGradient.setLocalMatrix(gradientMatrix)
            highlightPaint.alpha = (glowStrength * 255).toInt()
            canvas.drawRoundRect(0f, 0f, right, bottom, cornerRadius, cornerRadius, highlightPaint)

            bandMatrix.setTranslate(glowX, 0f)
            bandGradient.setLocalMatrix(bandMatrix)
            bandPaint.alpha = (glowStrength * 230).toInt()
            canvas.drawRoundRect(0f, 0f, right, bottom, cornerRadius, cornerRadius, bandPaint)
        }

        specularGradient?.let { gradient ->
            specularPaint.shader = gradient
            specularPaint.alpha = if (glowStrength > 0.01f) {
                (glowStrength * 210).toInt().coerceAtLeast(70)
            } else {
                70
            }
            canvas.drawRoundRect(3f, 1f, right - 6f, 3f, 2f, 2f, specularPaint)
        }

        canvas.drawRoundRect(0f, 0f, right, bottom, cornerRadius, cornerRadius, strokePaint)
    }

    /** 记录新的手指位置并立即把高光强度拉满。 */
    private fun updateTouchPoint(x: Float, y: Float) {
        touchAnimator?.cancel()
        touchX = x.coerceIn(0f, width.toFloat())
        touchY = y.coerceIn(0f, height.toFloat())
        touchStrength = 1f
        invalidate()
    }

    /** 手指离开后让高光渐隐，避免光斑突然消失或残留。 */
    private fun releaseTouchPoint() {
        val start = touchStrength
        touchAnimator?.cancel()
        if (start <= 0.01f) {
            touchX = -1f
            touchY = -1f
            invalidate()
            return
        }
        touchAnimator = ValueAnimator.ofFloat(start, 0f).apply {
            duration = 220L
            interpolator = DecelerateInterpolator(1f)
            addUpdateListener { animator ->
                touchStrength = animator.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    touchX = -1f
                    touchY = -1f
                    invalidate()
                }
            })
            start()
        }
    }

    /** 立即按当前选中态刷新所有菜单项颜色，供背景明暗变化时调用。 */
    private fun applyItemColors() {
        items.forEach { item ->
            val isSelected = item.itemId == selectedId
            item.itemBinding.navLabel.setTextColor(if (isSelected) selectedColor else inactiveColor)
            tintIcon(item.itemBinding.navIcon, if (isSelected) selectedColor else inactiveColor)
        }
    }

    /**
     * 处理选中态变化。
     * 不使用圆框背景，而是对旧选中项和新选中项做颜色渐变，
     * 并让新选中项图标做一次弹跳动画。
     */
    private fun updateSelection(animated: Boolean) {
        if (items.isEmpty()) {
            layoutPending = true
            return
        }
        layoutPending = false

        val fromId = previousSelectedId
        val toId = selectedId
        selectionAnimator?.cancel()
        iconAnimator?.cancel()

        if (!animated || fromId == View.NO_ID || fromId == toId) {
            applyItemColors()
            return
        }

        selectionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300L
            interpolator = DecelerateInterpolator(1.4f)
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                items.forEach { item ->
                    val color = when (item.itemId) {
                        fromId -> colorEvaluator.evaluate(
                            progress,
                            selectedColor,
                            inactiveColor
                        ) as Int

                        toId -> colorEvaluator.evaluate(
                            progress,
                            inactiveColor,
                            selectedColor
                        ) as Int

                        else -> inactiveColor
                    }
                    item.itemBinding.navLabel.setTextColor(color)
                    tintIcon(item.itemBinding.navIcon, color)
                }
            }
            start()
        }

        val targetIcon = items.firstOrNull { it.itemId == toId }?.itemBinding?.navIcon ?: return
        iconAnimator = ValueAnimator.ofFloat(1f, 1.22f, 1f).apply {
            duration = 340L
            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                targetIcon.scaleX = scale
                targetIcon.scaleY = scale
            }
            start()
        }

        // 高光从旧选中项扫到新选中项，强化切换反馈。
        playSelectionSweep(fromId, toId)
    }

    /** 获取指定菜单项在当前控件中的中心横坐标。 */
    private fun centerXOf(itemId: Int): Float? {
        val root = items.firstOrNull { it.itemId == itemId }?.itemBinding?.root ?: return null
        return (root.left + root.right) / 2f
    }

    /**
     * 播放选中扫光：高光从旧选中项中心移动到新选中项中心，
     * 强度按正弦曲线渐入渐出，结束时自动清除光斑。
     */
    private fun playSelectionSweep(fromId: Int, toId: Int) {
        val startX = centerXOf(fromId) ?: centerXOf(toId) ?: return
        val endX = centerXOf(toId) ?: return

        sweepAnimator?.cancel()
        sweepX = startX
        sweepStrength = 0f
        sweepAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 380L
            interpolator = DecelerateInterpolator(1f)
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                sweepX = startX + (endX - startX) * progress
                sweepStrength = sin(progress * PI).toFloat()
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    sweepStrength = 0f
                    invalidate()
                }
            })
            start()
        }
    }

    /** 用 ColorFilter 给矢量图标着色，并清除 tintList 避免互相覆盖。 */
    private fun tintIcon(imageView: ImageView, color: Int) {
        imageView.setColorFilter(color)
        imageView.imageTintList = null
    }

    /** 外部配置单个菜单项所需的数据。 */
    data class NavItem(
        @IdRes val itemId: Int,
        val iconRes: Int,
        val labelRes: Int,
        val badge: String? = null
    )

    /** 菜单被点击或横滑选中后的回调。 */
    fun interface OnItemSelectedListener {
        fun onItemSelected(@IdRes itemId: Int)
    }

    /** 内部持有的菜单项视图引用。 */
    private data class NavItemView(
        val itemId: Int,
        val itemBinding: ItemGlassNavBinding
    )
}
