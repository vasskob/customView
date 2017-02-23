package com.example.vasskob.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class SpeedometerView extends View {

    private static final String SPEED_ARROW_RADIUS_WARN = "Speed arrow radius must be in range [0,100]";
    private static final String INNER_SECTOR_RADIUS_WARN = "Inner radius must be in range [0,99] &  < than outer radius.";
    private static final String OUTER_SECTOR_RADIUS_WARN = "Outer radius must be in range [1,100] & > than inner radius.";
    private static final String MAX_SPEED_WARN = "Max speed must be in range [0,400] & be divisible by 10";

    private static final double MATH_PI = Math.PI;
    private static final int MAX_SPEED_DIVISIBLE_BY = 10;
    private static final int MIN_VALUE_OF_MAX_SPEED = 60;
    private static final int MAX_VALUE_OF_RADIUS = 100;
    private static final int HALF_CIRCLE_DEGREES = 180;
    private static final int MAX_VALUE_OF_MAX_SPEED = 400;
    private static final float FUEL_ICON_WIDTH = 0.1f;
    private static final float SCALE_WIDTH = 1.5f;
    private static final float DIGITS_WIDTH = 2;
    private static final float SCALE_SIZE = 5;
    private static final float SPEED_ARROW_WIDTH = 65;
    private static final float FUEL_ARROW_WIDTH = 80;
    private static final float BORDER_WIDTH = 90;
    private static final int MSG_INVALIDATE = 0;
    private static final long INVALIDATE_DELAY = 1000 / 24;

    private static final float MIN_INC_VALUE = 0.08f;
    private static final int DEFAULT_SPEED_ARROW_RADIUS = 50;
    private static final int DEFAULT_INNER_SECTOR_RADIUS = 30;
    private static final int DEFAULT_OUTER_SECTOR_RADIUS = 40;
    private static final int DEFAULT_MAX_SPEED = 100;
    private static final float MAX_FUEL_LEVEL = 100;
    private static final int ROTATION_COEFFICIENT = 400;
    private static final float DIGITS_VERTICAL_OFFSET = -10;

    private static final float FUEL_ARROW_RADIUS = 10;
    private static final float MAX_FUEL_ARROW_ANGLE = 60;
    private static final float FUEL_ARROW_START_ANGLE = 30;

    private static final long BLINKING_TIME = 500;
    private static final float WARNING_FUEL_LEVEL = 30;
    //  private static final String TAG = SpeedometerView.class.getSimpleName();
    private static boolean braking = false;
    private static boolean accelerating = false;
    private boolean blinking;
    private boolean noFuel = false;

    private int viewBackgroundColor;
    private int digitsColor;
    private int sectorBeforeSpeedArrowColor;
    private int sectorAfterSpeedArrowColor;
    private int speedArrowColor;
    private int borderColor;

    private int speedArrowRadius;
    private int innerSectorRadius;
    private int outerSectorRadius;
    private int maxSpeed;

    private Paint speedArrowPaint;
    private Paint borderPaint;
    private Paint scalePaint;
    private Paint backgroundPaint;
    private Paint digitsPaint;
    private Paint sectorBeforeSpeedArrowPaint;
    private Paint sectorAfterSpeedArrowPaint;
    private Bitmap mMask;
    private Paint maskPaint;
    private Paint fuelArrowPaint;


    private float centerX;
    private float centerY;
    private float angle;
    private float currentSpeed;
    private float incCoefficient = 0;
    private float currentFuelLevel = 100;
    private float fuelConsumption = 0.5f;
    private long lastTrueTime;

    private Path borderPath;
    private Path path;
    private Matrix matrix;
    private float[] src;
    private float[] dst;
    private RectF oval;
    private OnSpeedChangedListener mOnSpeedChangedListener;


    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //      Log.d(TAG, "handleMessage: msg: " + msg.what);
            if (msg.what == MSG_INVALIDATE) {
                invalidate();
                sendEmptyMessageDelayed(MSG_INVALIDATE, INVALIDATE_DELAY);
                if (mOnSpeedChangedListener != null)
                    mOnSpeedChangedListener.onSpeedChanged((int) currentSpeed);
            }
        }
    };


    public SpeedometerView(Context context) {
        super(context);
        speedArrowRadius = DEFAULT_SPEED_ARROW_RADIUS;
        innerSectorRadius = DEFAULT_INNER_SECTOR_RADIUS;
        outerSectorRadius = DEFAULT_OUTER_SECTOR_RADIUS;
        maxSpeed = DEFAULT_MAX_SPEED;
        currentSpeed = 0;
        viewBackgroundColor = ContextCompat.getColor(context, R.color.gray_100);
        digitsColor = ContextCompat.getColor(context, R.color.gray_900);
        sectorBeforeSpeedArrowColor = ContextCompat.getColor(context, R.color.lightBlue_300);
        sectorAfterSpeedArrowColor = ContextCompat.getColor(context, R.color.cyan_500);
        speedArrowColor = ContextCompat.getColor(context, R.color.black);
        borderColor = ContextCompat.getColor(context, R.color.gray_900);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, 0, 0);
        try {

            speedArrowRadius = a.getInteger(R.styleable.SpeedometerView_speedArrowRadius, 0);
            setSpeedArrowRadius(speedArrowRadius);
            innerSectorRadius = a.getInteger(R.styleable.SpeedometerView_innerSectorRadius, 0);
            setInnerSectorRadius(innerSectorRadius);
            outerSectorRadius = a.getInteger(R.styleable.SpeedometerView_outerSectorRadius, 0);
            setOuterSectorRadius(outerSectorRadius);
            maxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 0);
            setMaxSpeed(maxSpeed);
            currentSpeed = a.getFloat(R.styleable.SpeedometerView_currentSpeed, 0);
            setCurrentSpeed(currentSpeed);
            if (currentSpeed > 0)
                acceleratorReleased();
            viewBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, 0);

            digitsColor = a.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            sectorBeforeSpeedArrowColor = a.getColor(R.styleable.SpeedometerView_sectorBeforeSpeedArrowColor, 0);
            sectorAfterSpeedArrowColor = a.getColor(R.styleable.SpeedometerView_sectorAfterSpeedArrowColor, 0);
            speedArrowColor = a.getColor(R.styleable.SpeedometerView_speedArrowColor, 0);

            borderColor = a.getColor(R.styleable.SpeedometerView_borderColor, 0);

        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        speedArrowPaint = new Paint();
        speedArrowPaint.setColor(speedArrowColor);

        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setShadowLayer(5f, 0f, 0f, borderColor);
        borderPaint.setAntiAlias(true);

        scalePaint = new Paint();
        scalePaint.setColor(Color.CYAN);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(viewBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        digitsPaint = new Paint();
        digitsPaint.setColor(digitsColor);
        digitsPaint.setStyle(Paint.Style.FILL);
        digitsPaint.setStrokeWidth(DIGITS_WIDTH);
        digitsPaint.setShadowLayer(5f, 0f, 0f, digitsColor);
        digitsPaint.setAntiAlias(true);

        sectorBeforeSpeedArrowPaint = new Paint();
        sectorBeforeSpeedArrowPaint.setColor(sectorBeforeSpeedArrowColor);
        sectorBeforeSpeedArrowPaint.setStyle(Paint.Style.STROKE);

        sectorAfterSpeedArrowPaint = new Paint();
        sectorAfterSpeedArrowPaint.setColor(sectorAfterSpeedArrowColor);
        sectorAfterSpeedArrowPaint.setStyle(Paint.Style.STROKE);

        borderPath = new Path();
        path = new Path();
        matrix = new Matrix();

        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.ic_oil);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight());

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        fuelArrowPaint = new Paint();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
        } else {
            width = -1;
        }

        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width / 2;
        } else if (width >= 0) {
            height = width / 2;
        } else if (height >= 0) {
            width = height * 2;
        } else {
            width = 0;
            height = 0;
        }
        //      Log.d(TAG, "onMeasure: width: " + width + " height: " + height);
        setMeasuredDimension(width, height);
    }


    private void startInvalidateAnimation() {
//      Log.d(TAG, "startInvalidateAnimation: ");
        handler.removeMessages(MSG_INVALIDATE);
        handler.sendEmptyMessage(MSG_INVALIDATE);
    }

    /**
     * method handle view destroy when detached from window
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeMessages(MSG_INVALIDATE);
    }

    public void acceleratorPressed() {
        accelerating = true;
        if (!braking && !noFuel) {
            incCoefficient = 8 * MIN_INC_VALUE;
        }
        startInvalidateAnimation();
    }


    public void acceleratorReleased() {
        accelerating = false;
        if (!braking) {
            incCoefficient = -1 * MIN_INC_VALUE;
        }
        startInvalidateAnimation();
    }

    public void brakePressed() {
        braking = true;
        incCoefficient = -16 * MIN_INC_VALUE;
        startInvalidateAnimation();
    }

    public void brakeReleased() {
        braking = false;
        if (!accelerating) {
            incCoefficient = -1 * MIN_INC_VALUE;
            startInvalidateAnimation();
        } else {
            acceleratorPressed();
        }
    }

    /**
     * Handle view state & draw all elements
     *
     * @param canvas 0f drawing view element
     */
    @Override
    protected void onDraw(Canvas canvas) {

        updateCurrentSpeed();
        updateCurrentFuelLevel();
        drawBackground(canvas);
        drawBorder(canvas);
//        drawDigits(canvas);
//        drawSectorAfterSpeedArrow(canvas);
//        drawSectorBeforeSpeedArrow(canvas);
//        drawFuelState(canvas);
//        drawSpeedArrow(canvas);

    }

    private void updateCurrentSpeed() {
        if (currentSpeed < 30 && currentSpeed > 0 && incCoefficient > 0) {
            currentSpeed += incCoefficient;
        } else if (currentSpeed < 70 && currentSpeed >= 30 && incCoefficient > 0) {
            currentSpeed += incCoefficient * 0.8f;
        } else if (currentSpeed < maxSpeed && incCoefficient > 0) {
            currentSpeed += incCoefficient * 0.6f;
        } else if (currentSpeed + incCoefficient < 0) {
            currentSpeed = 0;
        } else if (currentSpeed + incCoefficient > maxSpeed) {
            currentSpeed = maxSpeed;
        } else {
            currentSpeed += incCoefficient;
        }
    }

    private void updateCurrentFuelLevel() {

        if (currentFuelLevel < 0) {
            currentFuelLevel = 0;
        } else if (accelerating) {
            currentFuelLevel -= MAX_FUEL_LEVEL / 100 * fuelConsumption;
        }
        updateFuelIconColor();
    }

    private void updateFuelIconColor() {
        float[] cmRed = new float[]{
                0, 0, 0, 0, 211,
                0, 0, 0, 0, 47,
                0, 0, 0, 0, 47,
                0, 0, 0, 1, 0};
        float[] cmGreen = new float[]{
                0, 0, 0, 0, 76,
                0, 0, 0, 0, 175,
                0, 0, 0, 0, 80,
                0, 0, 0, 1, 0};

        if (currentFuelLevel < WARNING_FUEL_LEVEL) {
            ColorMatrixColorFilter colorMatrixFilterRed = new ColorMatrixColorFilter(new ColorMatrix(cmRed));
            long now = System.currentTimeMillis();
            if (blinking) {
                maskPaint.setColorFilter(null);
                fuelArrowPaint.setColorFilter(null);
                lastTrueTime = now;
                blinking = false;
            } else {
                maskPaint.setColorFilter(colorMatrixFilterRed);
                fuelArrowPaint.setColorFilter(colorMatrixFilterRed);
                if (lastTrueTime + BLINKING_TIME < now) {
                    blinking = true;
                }

//                if (currentFuelLevel==0) {
//                    acceleratorReleased();
//                    noFuel=true;
//                }
            }
        } else {
            ColorMatrixColorFilter colorMatrixFilterGreen = new ColorMatrixColorFilter(new ColorMatrix(cmGreen));
            fuelArrowPaint.setColorFilter(colorMatrixFilterGreen);
            maskPaint.setColorFilter(colorMatrixFilterGreen);
        }

    }

    private void drawBackground(Canvas canvas) {
        backgroundPaint.setColor(viewBackgroundColor);
        oval = getOval(canvas, 1f);
        canvas.drawArc(oval, HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, true, backgroundPaint);
        centerX = oval.centerX();
        centerY = oval.centerY();

    }

    private void drawBorder(Canvas canvas) {
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(oval.width() / BORDER_WIDTH);
        RectF ovalScale = getOval(canvas, 0.95f);
        RectF ovalBorder = getOval(canvas, 0.99f);
        canvas.drawArc(ovalBorder, -HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, false, borderPaint);


        int step = HALF_CIRCLE_DEGREES * MAX_SPEED_DIVISIBLE_BY / maxSpeed;
//        for (int i = -HALF_CIRCLE_DEGREES + step; i < 0; i += step) {
//            borderPath.addArc(ovalScale, i, SCALE_WIDTH);
//        }
//        canvas.drawPath(borderPath, borderPaint);

        borderPath.reset();
        borderPath.addRect(0, 0, 20, 40, Path.Direction.CW);
        scalePaint.setColor(Color.CYAN);
        canvas.drawPath(borderPath, scalePaint);

        for (int i = 0; i < maxSpeed; i += step) {
            matrix.reset();
            matrix.setTranslate(
                    (float) (centerX + (oval.width() / 2 - 20) * Math.cos(Math.toRadians(180 - i))-10),
                    (float) (centerY + (oval.width() / 2 - 20) * Math.cos(Math.toRadians(180 - i))));
            matrix.setRotate(-i,
                    (float) (centerX + (oval.width() / 2 - 20) * Math.cos(Math.toRadians(180 - i))-10),
                    (float) (centerY + (oval.width() / 2 - 20) * Math.cos(Math.toRadians(180 - i))));
//                    (float) (centerX + oval.width() / 2 * Math.cos(Math.toRadians(180-i))),
//                    (float) (centerY + oval.width() / 2 * Math.cos(Math.toRadians(i))));
            // matrix.setSinCos((float) (Math.toRadians(180 - i)), (float) (Math.toRadians(180-i)));

            borderPath.transform(matrix);
            scalePaint.setColor(Color.GREEN);
            canvas.drawPath(borderPath, scalePaint);
        }

    }


    private void drawDigits(Canvas canvas) {
        digitsPaint.setColor(digitsColor);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(-(HALF_CIRCLE_DEGREES + ROTATION_COEFFICIENT / maxSpeed), centerX, centerY);
        digitsPaint.setColor(digitsColor);
        Path circle = new Path();
        double halfCircumference = (oval.width() / 2 - oval.width() / 8) * MATH_PI;
        int increments = MAX_SPEED_DIVISIBLE_BY;
        for (int i = increments; i < maxSpeed; i += increments) {
            circle.addCircle(centerX, centerY, (oval.width() / 2 - oval.width() / 8), Path.Direction.CW);
            digitsPaint.setTextSize(SCALE_SIZE * oval.width() / maxSpeed);
            canvas.drawTextOnPath(String.format(Locale.getDefault(), "%d", i),
                    circle,
                    (float) (i * halfCircumference / maxSpeed),
                    DIGITS_VERTICAL_OFFSET,
                    digitsPaint);
        }
        canvas.restore();
    }

    private synchronized void drawSectorAfterSpeedArrow(Canvas canvas) {
        sectorAfterSpeedArrowPaint.setColor(sectorAfterSpeedArrowColor);
        sectorAfterSpeedArrowPaint.setStrokeWidth((outerSectorRadius - innerSectorRadius) * oval.width() / 140);
        RectF rect = getOval(canvas, outerSectorRadius / (float) MAX_VALUE_OF_RADIUS);
        canvas.drawArc(rect, angle, -HALF_CIRCLE_DEGREES, false, sectorAfterSpeedArrowPaint);
    }

    private synchronized void drawSectorBeforeSpeedArrow(Canvas canvas) {
        sectorBeforeSpeedArrowPaint.setColor(sectorBeforeSpeedArrowColor);
        sectorBeforeSpeedArrowPaint.setStrokeWidth((outerSectorRadius - innerSectorRadius) * oval.width() / 140);
        RectF rect = getOval(canvas, outerSectorRadius / (float) MAX_VALUE_OF_RADIUS);
        canvas.drawArc(rect, -HALF_CIRCLE_DEGREES, angle, false, sectorBeforeSpeedArrowPaint);
    }

    private void drawFuelState(Canvas canvas) {
        fuelArrowPaint.setStrokeWidth(oval.width() / FUEL_ARROW_WIDTH);
        float fuelIconX = centerX - oval.width() / 8;
        float fuelIconY = centerY - oval.width() / 3;
        float fuelArrowAngle = currentFuelLevel / MAX_FUEL_LEVEL * MAX_FUEL_ARROW_ANGLE;
        float fuelArrowRadius = oval.width() / FUEL_ARROW_RADIUS;
        canvas.drawLine(
                fuelIconX + oval.width() / 11, fuelIconY + oval.width() / 22,
                (float) (fuelIconX + oval.width() / 11 + fuelArrowRadius * Math.cos(Math.toRadians(FUEL_ARROW_START_ANGLE - fuelArrowAngle))),
                (float) (fuelIconY + oval.width() / 22 + fuelArrowRadius * Math.sin(Math.toRadians(FUEL_ARROW_START_ANGLE - fuelArrowAngle))),
                fuelArrowPaint
        );
        Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) (oval.width() * FUEL_ICON_WIDTH), (int) (oval.height() * FUEL_ICON_WIDTH), true);
        canvas.drawBitmap(mask, fuelIconX, fuelIconY, maskPaint);
    }

    private void drawSpeedArrow(Canvas canvas) {
        speedArrowPaint.setColor(speedArrowColor);
        speedArrowPaint.setStrokeWidth(oval.width() / SPEED_ARROW_WIDTH);
        float radius = speedArrowRadius * oval.width() / (2 * MAX_VALUE_OF_RADIUS);
        RectF smallOval = getOval(canvas, 0.1f);
        angle = currentSpeed / maxSpeed * HALF_CIRCLE_DEGREES;
        canvas.drawLine(
                (float) (centerX + Math.cos(Math.toRadians(HALF_CIRCLE_DEGREES - angle)) * smallOval.width() * 0.5f),
                (float) (centerY - Math.sin(Math.toRadians(angle)) * smallOval.width() * 0.5f),
                (float) (centerX + radius * Math.cos(Math.toRadians(HALF_CIRCLE_DEGREES - angle))),
                (float) (centerY - radius * Math.sin(Math.toRadians(angle))),
                speedArrowPaint
        );
        canvas.drawArc(smallOval, HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, true, speedArrowPaint);
    }

    private RectF getOval(Canvas canvas, float factor) {

        RectF oval;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight * 2 >= canvasWidth) {
            oval = new RectF(0, 0, canvasWidth * factor, canvasWidth * factor);

        } else {
            oval = new RectF(0, 0, canvasHeight * 2 * factor, canvasHeight * 2 * factor);
        }

        oval.offset((canvasWidth - oval.width()) / 2 + getPaddingLeft(), (canvasHeight * 2 - oval.height()) / 2);

        return oval;
    }


    // Getters & Setters
    public float getSpeedArrowRadius() {
        return speedArrowRadius;
    }

    public void setSpeedArrowRadius(int speedArrowRadius) {
        if (speedArrowRadius < 0 || speedArrowRadius > MAX_VALUE_OF_RADIUS)
            throw new IllegalArgumentException(SPEED_ARROW_RADIUS_WARN);
        this.speedArrowRadius = speedArrowRadius;
        invalidate();
    }

    public float getInnerSectorRadius() {
        return innerSectorRadius;
    }

    public void setInnerSectorRadius(int innerSectorRadius) {
        if (innerSectorRadius < 0 || innerSectorRadius > MAX_VALUE_OF_RADIUS - 1)
            throw new IllegalArgumentException(INNER_SECTOR_RADIUS_WARN);
        this.innerSectorRadius = innerSectorRadius;
        invalidate();
    }

    public float getOuterSectorRadius() {

        return outerSectorRadius;
    }

    public void setOuterSectorRadius(int outerSectorRadius) {

        if (outerSectorRadius < innerSectorRadius || outerSectorRadius > MAX_VALUE_OF_RADIUS)
            throw new IllegalArgumentException(OUTER_SECTOR_RADIUS_WARN);
        this.outerSectorRadius = outerSectorRadius;
        invalidate();
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        if (maxSpeed < MIN_VALUE_OF_MAX_SPEED || (maxSpeed % MAX_SPEED_DIVISIBLE_BY) != 0 || maxSpeed > MAX_VALUE_OF_MAX_SPEED)
            throw new IllegalArgumentException(MAX_SPEED_WARN);
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public int getViewBackgroundColor() {
        return viewBackgroundColor;
    }

    public void setViewBackgroundColor(int backgroundColor) {
        this.viewBackgroundColor = backgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return digitsColor;
    }

    public void setDigitsColor(int digitsColor) {
        this.digitsColor = digitsColor;
        invalidate();
    }

    public int getSectorBeforeSpeedArrowColor() {
        return sectorBeforeSpeedArrowColor;
    }

    public void setSectorBeforeSpeedArrowColor(int sectorBeforeSpeedArrowColor) {
        this.sectorBeforeSpeedArrowColor = sectorBeforeSpeedArrowColor;
        invalidate();
    }

    public int getSectorAfterSpeedArrowColor() {
        return sectorAfterSpeedArrowColor;
    }

    public void setSectorAfterSpeedArrowColor(int sectorAfterSpeedArrowColor) {
        this.sectorAfterSpeedArrowColor = sectorAfterSpeedArrowColor;
        invalidate();
    }

    public int getSpeedArrowColor() {
        return speedArrowColor;
    }

    public void setSpeedArrowColor(int speedArrowColor) {
        this.speedArrowColor = speedArrowColor;
        invalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {

        if (currentSpeed < 0)
            currentSpeed = 0;
        if (currentSpeed > maxSpeed)
            currentSpeed = maxSpeed;
        this.currentSpeed = currentSpeed;
        invalidate();
    }

    public void setOnSpeedChangedListener(OnSpeedChangedListener mOnSpeedChangedListener) {
        this.mOnSpeedChangedListener = mOnSpeedChangedListener;
    }

    public void setCurrentFuelLevel(float fuelLevel) {
        if (fuelLevel < 0) {
            this.currentFuelLevel = 0;
        } else if (fuelLevel > MAX_FUEL_LEVEL) {
            this.currentFuelLevel = MAX_FUEL_LEVEL;
        } else {
            this.currentFuelLevel = fuelLevel;
        }
    }

    public void setFuelConsumption(float fuelConsumption) {
        if (fuelConsumption > 0) {
            this.fuelConsumption = fuelConsumption;
        } else {
            this.fuelConsumption = 1;
        }
    }

    public interface OnSpeedChangedListener {
        void onSpeedChanged(int value);
    }

}

//        src = new float[]{100, 100, 100, 120};
//        dst = new float[]{
//                (float) (centerX + oval.width() / 11 + oval.width()/2 * Math.cos(Math.toRadians(-180 - angle))),
//                (float) (centerY + oval.width() / 22 + oval.width()/2 * Math.sin(Math.toRadians(-180 - angle))),
//                (float) (centerX + oval.width() / 11 + oval.width()/2 * Math.cos(Math.toRadians(-180 - angle))),
//                (float) (centerY + oval.width() / 22 + oval.width()/2 * Math.sin(Math.toRadians(-180 - angle))),
//        };
//        for (int i = 0; i < maxSpeed; i += step) {
//            dst = new float[]{100+step, 100+step, 200-step, 200-step};
//            matrix.setPolyToPoly(src, 0, dst, 0, 1);
//            canvas.drawLine(
//                    dst[0]+i*2 ,
//                    //+ (float) (oval.width() / 2 * Math.cos(Math.toRadians( i))),
//                    dst[1]+i,
//                    //+ (float) (oval.width() / 2 * Math.sin(Math.toRadians( i))),
//                    dst[2]+i*2,
//                    //+ (float) (oval.width() / 2 * Math.cos(Math.toRadians( i))),
//                    dst[3]+i,
//                    //+ (float) (oval.width() / 2 * Math.cos(Math.toRadians( i))),
//                    borderPaint);
//
//        }