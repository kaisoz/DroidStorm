package net.kaisoz.droidstorm.nxt.handler;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

/**
 * View that prints the radar-like screen in control by movement
 *
 * @author Tomás Tormo Franco
 */
public class SensorArrowView extends View {

    public float mOrientationValues[] = new float[3];
    private static final String TAG = SensorArrowView.class.getSimpleName();
    private final Paint mPaint = new Paint();
    private int mAngle;
    private int mTilt;
    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    public SensorArrowView(Activity activity) {
        super(activity);

        Log.d(TAG, "SensorArrowView()");
        setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setFocusable(true);
        mPaint.setTypeface(Typeface.MONOSPACE);
        mPaint.setAntiAlias(true);
        calcArrowAngle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        mRadius = (int) (Math.min(mWidth, mHeight) * 0.45);
    }

    /**
     * Sets orientation sensor returned mValues in order to draw the arrow
     *
     * @param mOrientationValues Converted orientation mValues
     * @param mTilt              Calculated tilt
     * @param mAngle             Calculated angle
     */
    public void setData(float mOrientationValues[], int mTilt, int mAngle) {
        this.mOrientationValues = mOrientationValues;
        this.mTilt = mTilt;
        this.mAngle = mAngle;
    }


    /**
     * The main draw method
     */
    @Override
    protected void onDraw(Canvas canvas) {

        // Draw the outer circle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);

        // Draw the inner circle
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(mCenterX, mCenterY, 10, mPaint);
        for (int marker = 0; marker < 360; marker += 10) {
            canvas.drawLine(mCenterX, mCenterY - mRadius, mCenterX,
                    (mCenterY - mRadius)
                            + ((marker % 90) == 0 ? 20 : 10), mPaint);
            canvas.rotate(10, mCenterX, mCenterY);
        }

        drawArrow(canvas, mPaint);
    }


    /**
     * Draws the arrow
     * The mTilt value is a percentage (0 -> 100) which is updated outside
     * this method and is used here to determine the arrows length
     * The mAngle value is in degrees (0 -> 360) and is updated on change
     *
     * @param canvas Canvas to use
     * @param paint  Paint to use
     */
    private void drawArrow(Canvas canvas, Paint paint) {
        if (mTilt == 0) return;

        int length = ((mRadius - 30) * mTilt) / 100;

        if (length < 0) length = 0;
        if (length > (mRadius - 30)) length = mRadius - 30;

        Path mPath = new Path();
        mPath.moveTo(mCenterX, mCenterY);
        mPath.rLineTo(10, 0);
        mPath.rLineTo(0, -length);
        mPath.rLineTo(10, 0);
        mPath.rLineTo(-20, -30);
        mPath.rLineTo(-20, 30);
        mPath.rLineTo(10, 0);
        mPath.rLineTo(0, length);
        mPath.rLineTo(10, 0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.GREEN);
        canvas.rotate(mAngle, mCenterX, mCenterY);
        canvas.drawPath(mPath, mPaint);
        canvas.rotate(-mAngle, mCenterX, mCenterY);
    }

    /**
     * Calculates final arrow angle as well as updates the tilt value
     */
    public void calcArrowAngle() {

        if ((mOrientationValues[1] >= 0)
                && (mOrientationValues[2] < 0)) {
            mAngle = -mAngle;
        } else {
            if ((mOrientationValues[1] <= 0)
                    && (mOrientationValues[2] < 0)) {
                mAngle = 180 - mAngle;
            } else {
                if ((mOrientationValues[1] <= 0)
                        && (mOrientationValues[2] > 0)) {
                    mAngle = 180 - mAngle;
                } else {
                    mAngle = 360 - mAngle;
                }
            }
        }
    }

}