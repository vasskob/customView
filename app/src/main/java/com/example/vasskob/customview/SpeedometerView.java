package com.example.vasskob.customview;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SpeedometerView extends View {

    private static final String POINTER_RADIUS_WARN = "Pointer radius must be > 0";
    private static final String INNER_SECTOR_RADIUS_WARN = "Inner radius must be > 0 than outer radius.";
    private static final String OUTER_SECTOR_RADIUS_WARN = "Outer radius must be > than inner radius.";
    private static final String MAX_SPEED_WARN = "Max speed must be > 60 & divisible by 10";
    private static final String NEGATIVE_SPEED = "Non-positive value specified as a speed.";

    private static final double MATH_PI = Math.PI;
    private static final int MIN_VALUE_OF_MAX_SPEED = 60;
    private static final int MAX_SPEED_DIVISIBLE_BY = 10;
    private static final int HALF_CIRCLE_DEGREES = 180;
    private static final float SCALE_SIZE = 7000f;
    private static final float BORDER_WIDTH = 15f;
    private static final float POINTER_WIDTH = 20f;
    private static final float DIGITS_WIDTH = 2f;
    private static final float SCALE_WIDTH = 1.5f;

    private int spBackgroundColor;
    private int digitsColor;
    private int sectorBeforePointerColor;
    private int sectorAfterPointerColor;
    private int pointerColor;
    private int borderColor;

    private float pointerRadius;
    private float innerSectorRadius;
    private float outerSectorRadius;
    private int maxSpeed;

    private Paint pointerPaint;
    private Paint borderPaint;
    private Paint backgroundPaint;
    private Paint digitsPaint;
    private Paint sectorBeforePointerPaint;
    private Paint sectorAfterPointerPaint;

    private float centerX;
    private float centerY;
    private float angle;
    private float currentSpeed;

    private Path borderPath;
    private RectF oval;


    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, 0, 0);
        try {

            pointerRadius = a.getFloat(R.styleable.SpeedometerView_pointerRadius, 0);
            setPointerRadius(pointerRadius);
            innerSectorRadius = a.getFloat(R.styleable.SpeedometerView_innerSectorRadius, 0);
            setInnerSectorRadius(innerSectorRadius);
            outerSectorRadius = a.getFloat(R.styleable.SpeedometerView_outerSectorRadius, 0);
            setOuterSectorRadius(outerSectorRadius);
            maxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 0);
            setMaxSpeed(maxSpeed);
            currentSpeed = a.getFloat(R.styleable.SpeedometerView_currentSpeed, 0);
            setCurrentSpeed(currentSpeed);
            spBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, 0);
            digitsColor = a.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            sectorBeforePointerColor = a.getColor(R.styleable.SpeedometerView_sectorBeforePointerColor, 0);
            sectorAfterPointerColor = a.getColor(R.styleable.SpeedometerView_sectorAfterPointerColor, 0);
            pointerColor = a.getColor(R.styleable.SpeedometerView_pointerColor, 0);
            borderColor = a.getColor(R.styleable.SpeedometerView_borderColor, 0);

        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        pointerPaint = new Paint();
        pointerPaint.setColor(pointerColor);
        pointerPaint.setStrokeWidth(POINTER_WIDTH);

        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_WIDTH);
        borderPaint.setShadowLayer(5f, 0f, 0f, borderColor);
        borderPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(spBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        digitsPaint = new Paint();
        digitsPaint.setColor(digitsColor);
        digitsPaint.setStyle(Paint.Style.FILL);
        digitsPaint.setStrokeWidth(DIGITS_WIDTH);
        digitsPaint.setTextSize(SCALE_SIZE / getMaxSpeed());
        digitsPaint.setShadowLayer(5f, 0f, 0f, digitsColor);
        digitsPaint.setAntiAlias(true);

        sectorBeforePointerPaint = new Paint();
        sectorBeforePointerPaint.setColor(sectorBeforePointerColor);
        sectorBeforePointerPaint.setStyle(Paint.Style.STROKE);
        sectorBeforePointerPaint.setStrokeWidth(outerSectorRadius - innerSectorRadius);

        sectorAfterPointerPaint = new Paint();
        sectorAfterPointerPaint.setColor(sectorAfterPointerColor);
        sectorAfterPointerPaint.setStyle(Paint.Style.STROKE);
        sectorAfterPointerPaint.setStrokeWidth(outerSectorRadius - innerSectorRadius);

        borderPath = new Path();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        int chosenWidth = chooseDimension(widthMode, widthSize);
//        int chosenHeight = chooseDimension(heightMode, heightSize);
//
//        int chosenDimension = Math.min(chosenWidth, chosenHeight);
//        centerX = chosenDimension / 2;
//        centerY = chosenDimension / 2;
//        setMeasuredDimension(chosenDimension, chosenDimension);
//    }
//
//    private int chooseDimension(int widthMode, int widthSize) {
//        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) {
//            return widthSize;
//        } else {
//            return getPreferredSize();
//        }
//
//    }
//
//    private int getPreferredSize() {
//        return 250;
//    }

    public void acceleratorPressed() {
        setCurrentSpeed(getCurrentSpeed() + 0.8f, 0, 0);
    }

    public void acceleratorRelease() {
        setCurrentSpeed(getCurrentSpeed() - 0.8f, 0, 0);
    }

    public void brakePressed() {
        setCurrentSpeed(getCurrentSpeed() - 0.8f, 0, 0);
    }

    public void brakeRelease() {
        setCurrentSpeed(getCurrentSpeed() - 0.2f, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawBorder(canvas);
        drawDigits(canvas);
        drawSectorAfterPointer(canvas);
        drawSectorBeforePointer(canvas);
        drawPointer(canvas);
    }

    private synchronized void drawSectorAfterPointer(Canvas canvas) {
//        sectorAfterPointerPath.reset();
//        sectorAfterPointerPath.addCircle(centerX, centerY, (getOuterSectorRadius()-getInnerSectorRadius())*5, Path.Direction.CW);
//        canvas.drawPath(sectorAfterPointerPath, sectorAfterPointerPaint);

        RectF rect = getOval(canvas,outerSectorRadius/oval.height() );
        canvas.drawArc(rect, angle, -HALF_CIRCLE_DEGREES, false, sectorAfterPointerPaint);
    }

    private synchronized void drawSectorBeforePointer(Canvas canvas) {
//      sectorBeforePointerPath.reset();
//      sectorBeforePointerPath.addCircle(centerX, centerY, getOuterSectorRadius(), Path.Direction.CW);
//      canvas.drawPath(sectorBeforePointerPath, sectorBeforePointerPaint);

        RectF rect = getOval(canvas, outerSectorRadius/oval.height());
        canvas.drawArc(rect, -HALF_CIRCLE_DEGREES, angle, false, sectorBeforePointerPaint);
    }

    private void drawDigits(Canvas canvas) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(-(HALF_CIRCLE_DEGREES + 400 / getMaxSpeed()), centerX, centerY);

        Path circle = new Path();
        double halfCircumference = (canvas.getHeight() - canvas.getHeight() / 4) * Math.PI;
        int increments = MAX_SPEED_DIVISIBLE_BY;
        for (int i = increments; i < maxSpeed; i += increments) {
            circle.addCircle(centerX, centerY, (canvas.getHeight() - canvas.getHeight() / 4), Path.Direction.CW);
            canvas.drawTextOnPath(String.format("%d", i),
                    circle,
                    (float) (i * halfCircumference / maxSpeed),
                    -10f,
                    digitsPaint);
        }
        canvas.restore();
    }

    private void drawBorder(Canvas canvas) {

        RectF ovalScale = getOval(canvas, 0.95f);
        RectF ovalBorder = getOval(canvas, 0.99f);
        borderPath.reset();
        int step = HALF_CIRCLE_DEGREES * MAX_SPEED_DIVISIBLE_BY / maxSpeed;
        for (int i = -HALF_CIRCLE_DEGREES + step; i < 0; i += step) {

            borderPath.addArc(ovalScale, i, SCALE_WIDTH);
        }
//      borderPath.addCircle(centerX, centerY, canvas.getWidth() / 2 - 10, Path.Direction.CW);
        canvas.drawArc(ovalBorder, -HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, false, borderPaint);
        canvas.drawPath(borderPath, borderPaint);

    }

    private void drawPointer(Canvas canvas) {

        float radius = pointerRadius * 1.4f;
        RectF smallOval = getOval(canvas, 0.1f);
        angle = getCurrentSpeed() / getMaxSpeed() * HALF_CIRCLE_DEGREES;
        canvas.drawLine(
                (float) (centerX + Math.cos((HALF_CIRCLE_DEGREES - angle) / HALF_CIRCLE_DEGREES * MATH_PI) * smallOval.width() * 0.5f),
                (float) (centerY - Math.sin(angle / HALF_CIRCLE_DEGREES * MATH_PI) * smallOval.width() * 0.5f),
                (float) (centerX + Math.cos((HALF_CIRCLE_DEGREES - angle) / HALF_CIRCLE_DEGREES * MATH_PI) * radius),
                (float) (centerY - Math.sin(angle / HALF_CIRCLE_DEGREES * MATH_PI) * radius),
                pointerPaint
        );
        canvas.drawArc(smallOval, HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, true, pointerPaint);
    }


    private void drawBackground(Canvas canvas) {
//        factor mast be between 0 and 1
        oval = getOval(canvas, 1f);
        canvas.drawArc(oval, HALF_CIRCLE_DEGREES, HALF_CIRCLE_DEGREES, true, backgroundPaint);
        centerX = oval.centerX();
        centerY = oval.centerY();


    }

    private RectF getOval(Canvas canvas, float factor) {
        // factor mast be between 0 and 1
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

    public float getPointerRadius() {
        return pointerRadius;
    }

    public void setPointerRadius(float pointerRadius) {
        if (pointerRadius < 0)
            throw new IllegalArgumentException(POINTER_RADIUS_WARN);
        this.pointerRadius = pointerRadius;
        invalidate();
    }

    public float getInnerSectorRadius() {
        return innerSectorRadius;
    }

    public void setInnerSectorRadius(float innerSectorRadius) {
        if (innerSectorRadius < 0)
            throw new IllegalArgumentException(INNER_SECTOR_RADIUS_WARN);
        this.innerSectorRadius = innerSectorRadius;
        invalidate();
    }

    public float getOuterSectorRadius() {

        return outerSectorRadius;
    }

    public void setOuterSectorRadius(float outerSectorRadius) {

        if (outerSectorRadius < getInnerSectorRadius())
            throw new IllegalArgumentException(OUTER_SECTOR_RADIUS_WARN);
        this.outerSectorRadius = outerSectorRadius;
        invalidate();
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        if (maxSpeed < MIN_VALUE_OF_MAX_SPEED || (maxSpeed % MAX_SPEED_DIVISIBLE_BY) != 0)
            throw new IllegalArgumentException(MAX_SPEED_WARN);
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public int getSpBackgroundColor() {
        return spBackgroundColor;
    }

    public void setSpBackgroundColor(int backgroundColor) {
        this.spBackgroundColor = backgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return digitsColor;
    }

    public void setDigitsColor(int digitsColor) {
        this.digitsColor = digitsColor;
        invalidate();
    }

    public int getSectorBeforePointerColor() {
        return sectorBeforePointerColor;
    }

    public void setSectorBeforePointerColor(int sectorBeforePointerColor) {
        this.sectorBeforePointerColor = sectorBeforePointerColor;
        invalidate();
    }

    public int getSectorAfterPointerColor() {
        return sectorAfterPointerColor;
    }

    public void setSectorAfterPointerColor(int sectorAfterPointerColor) {
        this.sectorAfterPointerColor = sectorAfterPointerColor;
        invalidate();
    }

    public int getPointerColor() {
        return pointerColor;
    }

    public void setPointerColor(int pointerColor) {
        this.pointerColor = pointerColor;
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
            throw new IllegalArgumentException(NEGATIVE_SPEED);
        if (currentSpeed > maxSpeed)
            currentSpeed = maxSpeed;
        this.currentSpeed = currentSpeed;
        invalidate();

    }

    public ValueAnimator setCurrentSpeed(float progress, long duration, long startDelay) {
        if (progress < 0)
            progress = 0;

        // throw new IllegalArgumentException("Negative value specified as a speed.");

        if (progress > maxSpeed)
            progress = maxSpeed;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Float>() {
            @Override
            public Float evaluate(float fraction, Float startValue, Float endValue) {
                return startValue + fraction * (endValue - startValue);
            }

        }, getCurrentSpeed(), progress);

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (value != null)
                    setCurrentSpeed(value);
            }
        });
        va.start();
        return va;
    }


}
