package uz.efir.muazzin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation

class QiblaCompassView : View {
    private var directionNorth = 0f
    private var directionQibla = 0f
    private val compassBackground: Drawable
    private val compassNeedle: Drawable
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        labelPaint.color = ContextCompat.getColor(context, R.color.text_primary)

        compassBackground = requireNotNull(
            ContextCompat.getDrawable(context, R.drawable.compass_background)
        )
        compassNeedle = requireNotNull(
            ContextCompat.getDrawable(context, R.drawable.compass_needle)
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

        val compassSide = side * 0.8f
        val compassOffset = (side - compassSide) / 2f

        canvas.save()
        canvas.rotate(-directionNorth, centerX, centerY)

        compassBackground.setBounds(
            compassOffset.toInt(),
            compassOffset.toInt(),
            (compassOffset + compassSide).toInt(),
            (compassOffset + compassSide).toInt()
        )
        compassBackground.draw(canvas)

        labelPaint.textSize = side * LABEL_TEXT_RATIO
        val labelRadius = side * LABEL_RADIUS_RATIO
        val baselineOffset = labelPaint.textSize * 0.35f

        val labels = arrayOf("N", "E", "S", "W")
        for (i in labels.indices) {
            canvas.withRotation(i * 90f, centerX, centerY) {
                drawText(labels[i], centerX, centerY - labelRadius + baselineOffset, labelPaint)
            }
        }

        canvas.save()
        canvas.rotate(directionQibla, centerX, centerY)
        compassNeedle.setBounds(
            compassOffset.toInt(),
            compassOffset.toInt(),
            (compassOffset + compassSide).toInt(),
            (compassOffset + compassSide).toInt()
        )
        compassNeedle.draw(canvas)
        canvas.restore()

        canvas.restore()
    }

    companion object {
        // Render compass at 280dp by default
        private const val DEFAULT_SIZE_DP = 280f
        private const val LABEL_TEXT_RATIO = 18f / 240f

        // Push labels as far from center as possible while keeping the text inside the View bounds
        private const val LABEL_RADIUS_RATIO = 0.5f - LABEL_TEXT_RATIO / 2f
    }
}
