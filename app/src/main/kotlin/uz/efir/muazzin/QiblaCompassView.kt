package uz.efir.muazzin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources

class QiblaCompassView : View {
    private var directionNorth = 0f
    private var directionQibla = 0f
    private val background: Drawable
    private val needle: Drawable
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        background = requireNotNull(
            AppCompatResources.getDrawable(context, R.drawable.compass_background)
        )
        needle = requireNotNull(
            AppCompatResources.getDrawable(context, R.drawable.compass_needle)
        )
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE_DP, resources.displayMetrics
        ).toInt()
        val w = resolveSize(defaultSizePx, widthMeasureSpec)
        val h = resolveSize(defaultSizePx, heightMeasureSpec)
        val side = minOf(w, h)
        setMeasuredDimension(side, side)
    }

    fun setDirections(directionNorth: Float, directionQibla: Float) {
        this.directionNorth = directionNorth
        this.directionQibla = directionQibla
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val side = width
        val centerX = side / 2f
        val centerY = side / 2f

        canvas.save()
        canvas.rotate(-directionNorth, centerX, centerY)

        background.setBounds(0, 0, side, side)
        background.draw(canvas)

        labelPaint.textSize = side * LABEL_TEXT_RATIO
        val labelRadius = side * LABEL_RADIUS_RATIO
        val baselineOffset = labelPaint.textSize * 0.36f
        canvas.drawText("N", centerX, centerY - labelRadius + baselineOffset, labelPaint)
        canvas.drawText("E", centerX + labelRadius, centerY + baselineOffset, labelPaint)
        canvas.drawText("S", centerX, centerY + labelRadius + baselineOffset, labelPaint)
        canvas.drawText("W", centerX - labelRadius, centerY + baselineOffset, labelPaint)

        canvas.save()
        canvas.rotate(directionQibla, centerX, centerY)
        val needleHalfW = side * NEEDLE_HALF_W_RATIO
        val needleHalfH = side * NEEDLE_HALF_H_RATIO
        needle.setBounds(
            (centerX - needleHalfW).toInt(),
            (centerY - needleHalfH).toInt(),
            (centerX + needleHalfW).toInt(),
            (centerY + needleHalfH).toInt()
        )
        needle.draw(canvas)
        canvas.restore()

        canvas.restore()
    }

    companion object {
        // The compass renders at 240dp by default
        private const val DEFAULT_SIZE_DP = 240f

        // Cardinal pip centers sit at radius 102 from the disc's center in the 240 viewport.
        private const val LABEL_RADIUS_RATIO = 102f / 240f
        private const val LABEL_TEXT_RATIO = 14f / 240f

        // Needle is 24x144 in source coordinates on the 240x240 disc; pivot is at the geometric center.
        private const val NEEDLE_HALF_W_RATIO = 12f / 240f
        private const val NEEDLE_HALF_H_RATIO = 72f / 240f
    }
}
