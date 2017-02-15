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

public class SpeedometerView extends View implements SpeedChangeListener {

    private static final float SCALE_SIZE = 7000f;

    private float pointerRadius;
    private float innerSectorRadius;
    private float outerSectorRadius;
    private int maxSpeed;
    private float currentSpeed;
    private int backgroundColor;
    private int digitsColor;
    private int sectorBeforePointerColor;
    private int sectorAfterPointerColor;
    private int pointerColor;
    private int borderColor;
    private Paint pointerPaint;
    private Paint borderPaint;
    private Paint backgroundPaint;
    private Paint digitsPaint;
    private Paint sectorBeforePointerPaint;
    private Paint sectorAfterPointerPaint;

    private float centerX;
    private float centerY;
    private float digitsRadius;
    private float sectorRadius = 500;
    //   private float radius = SpeedometerView.get;
    private float angle;


    private Path onPath;
    private Path borderPath;
    private RectF oval;
    private RectF ovalDigits;


    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, 0, 0);
        try {

            this.pointerRadius = a.getFloat(R.styleable.SpeedometerView_pointerRadius, 0);
            this.innerSectorRadius = a.getFloat(R.styleable.SpeedometerView_innerSectorRadius, 0);
            this.outerSectorRadius = a.getFloat(R.styleable.SpeedometerView_outerSectorRadius, 0);
            int maxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 0);
            setMaxSpeed(maxSpeed);
            this.currentSpeed = a.getFloat(R.styleable.SpeedometerView_currentSpeed, 0);

            this.backgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, 0);
            this.digitsColor = a.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            this.sectorBeforePointerColor = a.getColor(R.styleable.SpeedometerView_sectorBeforePointerColor, 0);
            this.sectorAfterPointerColor = a.getColor(R.styleable.SpeedometerView_sectorBeforePointerColor, 0);
            this.pointerColor = a.getColor(R.styleable.SpeedometerView_pointerColor, 0);
            this.borderColor = a.getColor(R.styleable.SpeedometerView_borderColor, 0);

        } finally {
            a.recycle();
        }
        init();
    }

    public void accelerate() {
        setCurrentSpeed(33,1000,300);

    }

    public void brake() {

    }

    private void init() {
        pointerPaint = new Paint();
        pointerPaint.setColor(pointerColor);
        pointerPaint.setStrokeWidth(20);

        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(15f);
        borderPaint.setShadowLayer(5f, 0f, 0f, borderColor);
        borderPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        digitsPaint = new Paint();
        digitsPaint.setColor(digitsColor);
        digitsPaint.setStyle(Paint.Style.FILL);
        digitsPaint.setStrokeWidth(2f);
        digitsPaint.setTextSize(SCALE_SIZE/getMaxSpeed());
        digitsPaint.setShadowLayer(5f, 0f, 0f, digitsColor);
        digitsPaint.setAntiAlias(true);


        sectorBeforePointerPaint = new Paint();
        sectorBeforePointerPaint.setColor(sectorBeforePointerColor);

        sectorAfterPointerPaint = new Paint();
        sectorAfterPointerPaint.setColor(sectorAfterPointerColor);

        onPath = new Path();
        borderPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        int chosenDimension = Math.min(chosenWidth, chosenHeight);
        centerX = chosenDimension / 2;
        centerY = chosenDimension / 2;
        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int widthMode, int widthSize) {
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) {
            return widthSize;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredSize();
        }

    }

    private int getPreferredSize() {
        return 250;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawPointer(canvas);
        drawBorder(canvas);
        drawDigits(canvas);
//        drawSectorBeforePointer(canvas);
//        drawSectorAfterPointer(canvas);
    }

    private void drawDigits(Canvas canvas) {


        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(-195, centerX, centerY);
        Path circle = new Path();
        double halfCircumference = (canvas.getWidth() / 2 - canvas.getWidth() / 8) * Math.PI;
        double increments = 10;
        for (int i = 10; i <= this.maxSpeed; i += increments) {
            circle.addCircle(centerX, centerY, (canvas.getWidth() / 2 - canvas.getWidth() / 8), Path.Direction.CW);
            canvas.drawTextOnPath(String.format("%d", i),
                    circle,
                    (float) (i * halfCircumference / this.maxSpeed),
                    -10f,
                    digitsPaint);
        }

        canvas.restore();
        System.out.println("Width of Canvas is  " + canvas.getWidth());


    }

    private void drawBorder(Canvas canvas) {
        RectF ovalBorder = getOval(canvas, 0.95f);
        borderPath.reset();
        for (int i = -170; i < 0; i += 170 * 10 / maxSpeed) {
            borderPath.addArc(ovalBorder, i, 1.5f);
        }
        //borderPath.addOval(0,0,180,180, Path.Direction.CW);
        borderPath.addCircle(centerX, centerY, canvas.getWidth() / 2 - 10, Path.Direction.CW);
        canvas.drawPath(borderPath, borderPaint);


    }

    private void drawPointer(Canvas canvas) {

        //RectF oval = getOval(canvas, 1);
        float radius = oval.width() * 0.35f + 10;
        RectF smallOval = getOval(canvas, 0.1f);

        angle = 5 + getCurrentSpeed() / getMaxSpeed() * 160;
        canvas.drawLine(
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * smallOval.width() * 0.5f),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * smallOval.width() * 0.5f),
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                pointerPaint
        );
        canvas.drawArc(smallOval, 180, 180, true, pointerPaint);

        // canvas.drawLine(oval.centerX(),oval.centerY(),oval.centerX()-sectorRadius + correction, oval.centerY() + correction,mPointerPaint);

    }


    private void drawBackground(Canvas canvas) {
        //  factor mast be between 0 and 1
//        oval = getOval(canvas, 1);
//        canvas.drawOval(oval, mBackgroundPaint);

        oval = getOval(canvas, 1);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);
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

    public void setPointerRadius(float mPointerRadius) {
        this.pointerRadius = mPointerRadius;
        invalidate();
    }

    public float getInnerSectorRadius() {
        return innerSectorRadius;
    }

    public void setInnerSectorRadius(float mInnerSectorRadius) {
        this.innerSectorRadius = mInnerSectorRadius;
        invalidate();
    }

    public float getOuterSectorRadius() {
        return outerSectorRadius;
    }

    public void setOuterSectorRadius(float mOuterSectorRadius) {
        this.outerSectorRadius = mOuterSectorRadius;
        invalidate();
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        if (maxSpeed < 60)
            throw new IllegalArgumentException("Max speed must be > 60");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.backgroundColor = mBackgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return digitsColor;
    }

    public void setDigitsColor(int mDigitsColor) {
        this.digitsColor = mDigitsColor;
        invalidate();
    }

    public int getSectorBeforePointerColor() {
        return sectorBeforePointerColor;
    }

    public void setSectorBeforePointerColor(int mSectorBeforePointerColor) {
        this.sectorBeforePointerColor = mSectorBeforePointerColor;
        invalidate();
    }

    public int getSectorAfterPointerColor() {
        return sectorAfterPointerColor;
    }

    public void setSectorAfterPointerColor(int mSectorAfterPointerColor) {
        this.sectorAfterPointerColor = mSectorAfterPointerColor;
        invalidate();
    }

    public int getPointerColor() {
        return pointerColor;
    }

    public void setPointerColor(int mPointerColor) {
        this.pointerColor = mPointerColor;
        invalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int mBorderColor) {
        this.borderColor = mBorderColor;
        invalidate();
    }

    public Paint getPointerPaint() {
        return pointerPaint;
    }

    public void setPointerPaint(Paint mPointerPaint) {
        this.pointerPaint = mPointerPaint;
        invalidate();
    }

    public Paint getBorderPaint() {
        return borderPaint;
    }

    public void setBorderPaint(Paint mBorderPaint) {
        this.borderPaint = mBorderPaint;
        invalidate();
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    public void setBackgroundPaint(Paint mBackgroundPaint) {
        this.backgroundPaint = mBackgroundPaint;
        invalidate();
    }

    public Paint getDigitsPaint() {
        return digitsPaint;
    }

    public void setDigitsPaint(Paint mDigitsPaint) {
        this.digitsPaint = mDigitsPaint;
        invalidate();
    }

    public Paint getSectorBeforePointerPaint() {
        return sectorBeforePointerPaint;
    }

    public void setSectorBeforePointerPaint(Paint mSectorBeforePointerPaint) {
        this.sectorBeforePointerPaint = mSectorBeforePointerPaint;
        invalidate();
    }

    public Paint getSectorAfterPointerPaint() {
        return sectorAfterPointerPaint;
    }

    public void setSectorAfterPointerPaint(Paint mSectorAfterPointerPaint) {
        this.sectorAfterPointerPaint = mSectorAfterPointerPaint;
        invalidate();
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {

            if (currentSpeed < 0)
                throw new IllegalArgumentException("Non-positive value specified as a speed.");
            if (currentSpeed > maxSpeed)
                currentSpeed = maxSpeed;
            this.currentSpeed = currentSpeed;
            invalidate();

    }

    public ValueAnimator setCurrentSpeed(float progress, long duration, long startDelay) {
        if (progress < 0)
            throw new IllegalArgumentException("Negative value specified as a speed.");

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


    @Override
    public void onSpeedChanged(float newSpeedValue) {
        this.setCurrentSpeed(33,1000,300);
        this.invalidate();

    }
}
