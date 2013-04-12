package islam.adhanalarm.view;

import java.text.DecimalFormat;

import uz.efir.muazzin.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class QiblaCompassView extends View {
    private float directionNorth = 0;
    private float directionQibla = 0;
    private TextView bearingNorth;
    private String bearingNorthString;
    private TextView bearingQibla;
    private String bearingQiblaString;
    private DecimalFormat df = new DecimalFormat("0.000");
    private Bitmap compassBackground;
    private Bitmap compassNeedle;
    private Matrix rotateNeedle = new Matrix();
    private int width = 240;
    private int height = 240;
    private float centre_x = width * 0.5f;
    private float centre_y = height * 0.5f;

    public QiblaCompassView(Context context) {
        super(context);
        initCompassView();
    }
    public QiblaCompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCompassView();
    }
    public QiblaCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    private void initCompassView() {
        compassNeedle = BitmapFactory.decodeResource(getResources(), R.drawable.compass_needle);
        compassBackground = BitmapFactory.decodeResource(getResources(), R.drawable.compass_background);
        width = getWidth(compassBackground, 240);
        height = getHeight(compassBackground, 240);
        centre_x = width  * 0.5f;
        centre_y = height * 0.5f;
        rotateNeedle.postTranslate(centre_x - getWidth(compassNeedle, 23) + 10, centre_y - getHeight(compassNeedle, 142) + 10);
        invalidate();
    }

    public void setConstants(TextView bearingNorth, CharSequence bearingNorthString, TextView bearingQibla, CharSequence bearingQiblaString) {
        this.bearingNorth = bearingNorth;
        this.bearingNorthString = bearingNorthString.toString();
        this.bearingQibla = bearingQibla;
        this.bearingQiblaString = bearingQiblaString.toString();
        compassBackground = BitmapFactory.decodeResource(getResources(), R.drawable.compass_background);
        compassNeedle = BitmapFactory.decodeResource(getResources(), R.drawable.compass_needle);
    }

    public void setDirections(float directionNorth, float directionQibla) {
        this.directionNorth = directionNorth;
        this.directionQibla = directionQibla;
        rotateNeedle = new Matrix();
        rotateNeedle.postRotate(-directionQibla, getWidth(compassNeedle, 23) * 0.5f, getHeight(compassNeedle, 142));
        rotateNeedle.postTranslate(centre_x - getWidth(compassNeedle, 23), centre_y - getHeight(compassNeedle, 142) + 5);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        bearingNorth.setText(bearingNorthString.replace("(1)", df.format(directionNorth)));
        bearingQibla.setText(bearingQiblaString.replace("(+/-)", directionQibla >= 0 ? " +" : " -").replace("(2)", df.format(Math.abs(directionQibla))).replace("(3)",  df.format(directionNorth + directionQibla)));

        Paint p = new Paint();
        canvas.rotate(-directionNorth, centre_x, centre_y);
        canvas.drawBitmap(compassBackground, 0, 0, p);
        canvas.drawBitmap(compassNeedle, rotateNeedle, p);
    }

    private int getHeight(Bitmap bitmap, int def) {
        if (isInEditMode()) {
            return def;
        }

        return bitmap.getHeight();
    }

    private int getWidth(Bitmap bitmap, int def) {
        if (isInEditMode()) {
            return def;
        }

        return bitmap.getWidth();
    }
}