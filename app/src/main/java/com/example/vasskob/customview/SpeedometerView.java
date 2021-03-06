package com.example.vasskob.customview;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

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
    private static final int ROTATION_COEFFICIENT = 350;
    private static final float DIGITS_VERTICAL_OFFSET = -10;

    private static final float FUEL_ARROW_RADIUS = 10;
    private static final float MAX_FUEL_ARROW_ANGLE = 60;
    private static final float FUEL_ARROW_START_ANGLE = 25;

    private static final long BLINKING_TIME = 500;
    private static final float WARNING_FUEL_LEVEL = 30;
    private static final String TAG = SpeedometerView.class.getSimpleName();
    private static final long SECONDS_TO_MILLISECONDS = 100;
    private static boolean braking = false;
    private static boolean accelerating = false;

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
    private Bitmap fuelIconMask;

    private Paint fuelIconPaint;
    private Paint fuelArrowPaint;


    private float centerX;
    private float centerY;
    private float angle;
    private float currentSpeed;
    private float incCoefficient = 0;
    private float currentFuelLevel = 100;
    private float fuelConsumption = 0.5f;


    private Path fuelArrowPath;
    private Path borderPath;
    private Path speedArrowPath;
    private Matrix matrix;
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
    private ValueAnimator colorAnimation;
    private ValueAnimator blinkAnimator;


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
                brakeReleased();
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
        scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        scalePaint.setColor(borderColor);

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
        speedArrowPath = new Path();
        fuelArrowPath = new Path();
        matrix = new Matrix();

        fuelIconMask = BitmapFactory.decodeResource(getResources(), R.drawable.ic_oil);
        fuelIconMask = Bitmap.createBitmap(fuelIconMask, 0, 0, fuelIconMask.getWidth(), fuelIconMask.getHeight());

        fuelIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        fuelArrowPaint = new Paint();
        fuelArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        startFuelColorAnimation();
        colorAnimation.pause();
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
        Log.d(TAG, "onMeasure: width: " + width + " height: " + height);
        setMeasuredDimension(width, height);
    }


    private void startInvalidateAnimation() {
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
        if (!braking) {
            incCoefficient = 8 * MIN_INC_VALUE;
        }
        startInvalidateAnimation();
        startFuelColorAnimation();
    }


    public void acceleratorReleased() {
        accelerating = false;
        if (!braking) {
            incCoefficient = -1 * MIN_INC_VALUE;
        }
        startInvalidateAnimation();
        colorAnimation.pause();
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
        drawDigits(canvas);
        drawSectorAfterSpeedArrow(canvas);
        drawSectorBeforeSpeedArrow(canvas);
        drawFuelIcon(canvas);
        drawSpeedArrow(canvas);

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
        if (currentFuelLevel < WARNING_FUEL_LEVEL) {
            startFuelBlinkingAnimation();
        } else if (blinkAnimator != null) {
            blinkAnimator.pause();
        }

    }

    private void startFuelBlinkingAnimation() {

        if (blinkAnimator == null) {
            blinkAnimator = ValueAnimator.ofInt(0, 255);
            blinkAnimator.setDuration(BLINKING_TIME);
            blinkAnimator.setRepeatCount(Animation.INFINITE);
            blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
            blinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    fuelArrowPaint.setAlpha((int) animator.getAnimatedValue());
                    fuelIconPaint.setAlpha((int) animator.getAnimatedValue());
                }
            });
            blinkAnimator.start();
        } else {
            blinkAnimator.resume();
        }
    }

    private void startFuelColorAnimation() {

        int colorFrom = ContextCompat.getColor(getContext(), R.color.green_500);
        int colorTo = ContextCompat.getColor(getContext(), R.color.red_700);
        if (colorAnimation == null || currentFuelLevel == MAX_FUEL_LEVEL) {
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration((long) (MAX_FUEL_LEVEL / 2f * fuelConsumption * SECONDS_TO_MILLISECONDS)); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    updateFuelColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
        } else {
            colorAnimation.resume();
        }

    }

    private void updateFuelColor(int color) {
        float r = Color.red(color);
        float g = Color.green(color);
        float b = Color.blue(color);
        final float[] cmDefault = new float[]{
                0, 0, 0, 0, r,
                0, 0, 0, 0, g,
                0, 0, 0, 0, b,
                0, 0, 0, 1, 0};
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(new ColorMatrix(cmDefault));
        fuelArrowPaint.setColorFilter(colorMatrixFilter);
        fuelIconPaint.setColorFilter(colorMatrixFilter);
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
        float borderWidth = oval.width() / BORDER_WIDTH;
        borderPaint.setStrokeWidth(borderWidth);
        RectF ovalBorder = getOval(canvas, 0.99f);
        canvas.drawArc(ovalBorder, -HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, false, borderPaint);
        scalePaint.setColor(borderColor);

        int step = 1 + HALF_CIRCLE_DEGREES * MAX_SPEED_DIVISIBLE_BY / maxSpeed;
        borderPath.reset();
        borderPath.addRect(0, 3 * borderWidth, borderWidth, 0, Path.Direction.CW);
        matrix.reset();
        matrix.setTranslate(centerX, 0);
        matrix.postRotate(step - 90, centerX, centerY);
        borderPath.transform(matrix);
        canvas.drawPath(borderPath, scalePaint);
        matrix.reset();
        matrix.setRotate(step - 1, centerX, centerY);

        for (int i = -HALF_CIRCLE_DEGREES + step; i < 0; i += step) {
            borderPath.transform(matrix);
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

    private void drawFuelIcon(Canvas canvas) {
        fuelArrowPaint.setStrokeWidth(oval.width() / FUEL_ARROW_WIDTH);
        float fuelIconX = centerX - oval.width() / 8;
        float fuelIconY = centerY - oval.width() / 3;
        float fuelArrowAngle = currentFuelLevel / MAX_FUEL_LEVEL * MAX_FUEL_ARROW_ANGLE;
        float fuelArrowRadius = oval.width() / FUEL_ARROW_RADIUS;

        fuelArrowPath.reset();
        int point1X = 0;
        int point1Y = 0;

        Point point1_draw = new Point(point1X, point1Y);
        Point point2_draw = new Point((int) (point1X + fuelArrowRadius), point1Y);
        Point point3_draw = new Point((int) (point1X + fuelArrowRadius), (int) (point1Y + fuelArrowRadius / 20));
        Point point4_draw = new Point(point1X, (int) (point1Y + fuelArrowRadius / 20));

        fuelArrowPath.moveTo(point1_draw.x, point1_draw.y);
        fuelArrowPath.lineTo(point2_draw.x, point2_draw.y);
        fuelArrowPath.lineTo(point3_draw.x, point3_draw.y);
        fuelArrowPath.lineTo(point4_draw.x, point4_draw.y);
        fuelArrowPath.lineTo(point1_draw.x, point1_draw.y);

        matrix.reset();
        matrix.setTranslate(fuelIconX + oval.width() / 11 - point1X, fuelIconY + oval.width() / 22 - point1Y);
        matrix.postRotate(FUEL_ARROW_START_ANGLE - fuelArrowAngle, fuelIconX + oval.width() / 11, fuelIconY + oval.width() / 22);
        fuelArrowPath.transform(matrix);
        canvas.drawPath(fuelArrowPath, fuelArrowPaint);

        Bitmap mask = Bitmap.createScaledBitmap(fuelIconMask, (int) (oval.width() * FUEL_ICON_WIDTH), (int) (oval.height() * FUEL_ICON_WIDTH), true);
        canvas.drawBitmap(mask, fuelIconX, fuelIconY, fuelIconPaint);
    }

    private void drawSpeedArrow(Canvas canvas) {
        speedArrowPaint.setColor(speedArrowColor);
        speedArrowPaint.setStrokeWidth(oval.width() / SPEED_ARROW_WIDTH);
        float radius = speedArrowRadius * oval.width() / (2 * MAX_VALUE_OF_RADIUS);
        int point1X = 0;
        int point1Y = 0;
        int speedArrowTopWidth = (int) (oval.width() / SPEED_ARROW_WIDTH);
        angle = currentSpeed / maxSpeed * HALF_CIRCLE_DEGREES;
        speedArrowPath.reset();

        Point point1_draw = new Point(point1X, point1Y);
        Point point2_draw = new Point(point1X + speedArrowTopWidth, point1Y);
        Point point3_draw = new Point(point1X + 2 * speedArrowTopWidth, (int) radius);
        Point point4_draw = new Point(point1X - speedArrowTopWidth, (int) radius);

        speedArrowPath.moveTo(point1_draw.x, point1_draw.y);
        speedArrowPath.lineTo(point2_draw.x, point2_draw.y);
        speedArrowPath.lineTo(point3_draw.x, point3_draw.y);
        speedArrowPath.lineTo(point4_draw.x, point4_draw.y);
        speedArrowPath.lineTo(point1_draw.x, point1_draw.y);

        matrix.reset();
        matrix.setTranslate(centerX - (2 * point1X + speedArrowTopWidth) / 2, centerY - radius);
        matrix.postRotate(angle - HALF_CIRCLE_DEGREES / 2, centerX, centerY);
        speedArrowPath.transform(matrix);
        canvas.drawPath(speedArrowPath, speedArrowPaint);
        invalidate();

        RectF smallOval = getOval(canvas, 0.1f);
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
            currentFuelLevel = 0;
        } else if (fuelLevel > MAX_FUEL_LEVEL) {
            currentFuelLevel = MAX_FUEL_LEVEL;
        } else {
            currentFuelLevel = fuelLevel;
        }
        colorAnimation.start();
        colorAnimation.pause();
        if (currentFuelLevel > WARNING_FUEL_LEVEL && blinkAnimator != null) {
            blinkAnimator.end();
            blinkAnimator = null;
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