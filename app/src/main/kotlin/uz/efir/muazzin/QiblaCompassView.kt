package uz.efir.muazzin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class QiblaCompassView : View {
    private var directionNorth = 0f
    private var directionQibla = 0f
    private var compassBackground: Bitmap? = null
    private var compassNeedle: Bitmap? = null
    private var rotateNeedle = Matrix()
    private var width = BACKGROUND_SOURCE_SIZE
    private var height = BACKGROUND_SOURCE_SIZE
    private var centerX = width * 0.5f
    private var centerY = height * 0.5f
    private val paint = Paint()

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        compassNeedle = BitmapFactory.decodeResource(resources, R.drawable.compass_needle)
        compassBackground = BitmapFactory.decodeResource(
            resources, R.drawable.compass_background
        )
        width = getWidth(compassBackground, BACKGROUND_SOURCE_SIZE)
        height = getHeight(compassBackground, BACKGROUND_SOURCE_SIZE)
        centerX = width * 0.5f
        centerY = height * 0.5f

        val needleWidth = getWidth(compassNeedle, NEEDLE_SOURCE_WIDTH)
        val needleHeight = getHeight(compassNeedle, NEEDLE_SOURCE_HEIGHT)

        rotateNeedle.postTranslate(
            centerX - needleWidth + 10, centerY - needleHeight + 10
        )
        invalidate()
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(width, widthMeasureSpec),
            resolveSize(height, heightMeasureSpec)
        )
    }

    fun setDirections(directionNorth: Float, directionQibla: Float) {
        this.directionNorth = directionNorth
        this.directionQibla = directionQibla
        rotateNeedle = Matrix()

        val needleWidth = getWidth(compassNeedle, NEEDLE_SOURCE_WIDTH)
        val needleHeight = getHeight(compassNeedle, NEEDLE_SOURCE_HEIGHT)

        // The silver pivot ball is not at the bitmap's geometric center: it sits
        // at ~(14.6, 131.4) in the 23x142 source. Using ratios keeps the pivot
        // aligned if the bitmap is swapped for a different density.
        val pivotX = needleWidth * NEEDLE_PIVOT_X_RATIO
        val pivotY = needleHeight * NEEDLE_PIVOT_Y_RATIO

        rotateNeedle.postRotate(directionQibla, pivotX, pivotY)
        rotateNeedle.postTranslate(centerX - pivotX, centerY - pivotY)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(-directionNorth, centerX, centerY)
        compassBackground?.let { canvas.drawBitmap(it, 0f, 0f, paint) }
        compassNeedle?.let { canvas.drawBitmap(it, rotateNeedle, paint) }
    }

    private fun getHeight(bitmap: Bitmap?, def: Int): Int {
        if (isInEditMode || bitmap == null) {
            return def
        }

        return bitmap.height
    }

    private fun getWidth(bitmap: Bitmap?, def: Int): Int {
        if (isInEditMode || bitmap == null) {
            return def
        }

        return bitmap.width
    }

    companion object {
        // Source dimensions of compass_background.png (drawable-mdpi).
        private const val BACKGROUND_SOURCE_SIZE = 240

        // Source dimensions of compass_needle.png (drawable-mdpi).
        private const val NEEDLE_SOURCE_WIDTH = 23
        private const val NEEDLE_SOURCE_HEIGHT = 142

        // Centroid of the silver pivot ball within compass_needle.png, in source pixels.
        private const val NEEDLE_PIVOT_X_RATIO = 14.6f / NEEDLE_SOURCE_WIDTH
        private const val NEEDLE_PIVOT_Y_RATIO = 131.4f / NEEDLE_SOURCE_HEIGHT
    }
}
