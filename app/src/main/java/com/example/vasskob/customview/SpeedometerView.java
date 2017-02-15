package com.example.vasskob.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class SpeedometerView extends View implements SpeedChangeListener {

    private static final float SCALE_SIZE = 80f;

    private float mPointerRadius;
    private float mInnerSectorRadius;
    private float mOuterSectorRadius;
    private int mMaxSpeed;
    private float mCurrentSpeed;
    private int mBackgroundColor;
    private int mDigitsColor;
    private int mSectorBeforePointerColor;
    private int mSectorAfterPointerColor;
    private int mPointerColor;
    private int mBorderColor;
    private Paint mPointerPaint;
    private Paint mBorderPaint;
    private Paint mBackgroundPaint;
    private Paint mDigitsPaint;
    private Paint mSectorBeforePointerPaint;
    private Paint mSectorAfterPointerPaint;

    private float centerX;
    private float centerY;
    private float digitsRadius;
    private float sectorRadius = 500;
    //   private float radius = SpeedometerView.get;
    private float angle;


    private Path onPath;
    private Path offPath;
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

            this.mPointerRadius = a.getFloat(R.styleable.SpeedometerView_pointerRadius, 0);
            this.mInnerSectorRadius = a.getFloat(R.styleable.SpeedometerView_innerSectorRadius, 0);
            this.mOuterSectorRadius = a.getFloat(R.styleable.SpeedometerView_outerSectorRadius, 0);
            this.mMaxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 0);
            this.mCurrentSpeed = a.getFloat(R.styleable.SpeedometerView_currentSpeed, 0);

            this.mBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, 0);
            this.mDigitsColor = a.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            this.mSectorBeforePointerColor = a.getColor(R.styleable.SpeedometerView_sectorBeforePointerColor, 0);
            this.mSectorAfterPointerColor = a.getColor(R.styleable.SpeedometerView_sectorBeforePointerColor, 0);
            this.mPointerColor = a.getColor(R.styleable.SpeedometerView_pointerColor, 0);
            this.mBorderColor = a.getColor(R.styleable.SpeedometerView_borderColor, 0);

        } finally {
            a.recycle();
        }
        init();
    }

    public void accelerate() {
        setCurrentSpeed(0);
        setMaxSpeed(300);

    }

    public void brake() {

    }

    private void init() {
        mPointerPaint = new Paint();
        mPointerPaint.setColor(mPointerColor);
        mPointerPaint.setStrokeWidth(20);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(80f);
        mBorderPaint.setShadowLayer(5f, 0f, 0f, mBorderColor);
        mBorderPaint.setAntiAlias(true);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);

        mDigitsPaint = new Paint();
        mDigitsPaint.setColor(mDigitsColor);
        mDigitsPaint.setStyle(Paint.Style.FILL);
        mDigitsPaint.setStrokeWidth(2f);
        mDigitsPaint.setTextSize(SCALE_SIZE);
        mDigitsPaint.setShadowLayer(5f, 0f, 0f, mDigitsColor);
        mDigitsPaint.setAntiAlias(true);


        mSectorBeforePointerPaint = new Paint();
        mSectorBeforePointerPaint.setColor(mSectorBeforePointerColor);

        mSectorAfterPointerPaint = new Paint();
        mSectorAfterPointerPaint.setColor(mSectorAfterPointerColor);

        onPath = new Path();
        offPath = new Path();
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
        double halfCircumference = (canvas.getWidth() / 2 - 170) * Math.PI;
        double increments = 10;
        for (int i = 10; i <= this.mMaxSpeed; i += increments) {
            circle.addCircle(centerX, centerY, (canvas.getWidth() / 2 - 170), Path.Direction.CW);
            canvas.drawTextOnPath(String.format("%d", i),
                    circle,
                    (float) (i * halfCircumference / this.mMaxSpeed),
                    -30f,
                    mDigitsPaint);
        }

        canvas.restore();


    }

    private void drawBorder(Canvas canvas) {
        //RectF oval = getOval(canvas, 1);
        offPath.reset();
        for (int i = -170; i < 0; i += 170 * 10 / mMaxSpeed) {
            offPath.addArc(oval, i, 1f);
        }
        canvas.drawPath(offPath, mBorderPaint);
        System.out.println(" !!!!!!!!! MAX SPEED = " + mMaxSpeed);
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
                mPointerPaint
        );
        canvas.drawArc(smallOval, 180, 180, true, mPointerPaint);

        // canvas.drawLine(oval.centerX(),oval.centerY(),oval.centerX()-sectorRadius + correction, oval.centerY() + correction,mPointerPaint);

    }


    private void drawBackground(Canvas canvas) {
        //  factor mast be between 0 and 1
//        oval = getOval(canvas, 1);
//        canvas.drawOval(oval, mBackgroundPaint);

        oval = getOval(canvas, 1);
        canvas.drawArc(oval, 180, 180, true, mBackgroundPaint);
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
            //  oval=new Rect
        } else {
            oval = new RectF(0, 0, canvasHeight * 2 * factor, canvasHeight * 2 * factor);
        }

        oval.offset((canvasWidth - oval.width()) / 2 + getPaddingLeft(), (canvasHeight * 2 - oval.height()) / 2);

        return oval;
    }


    // Getters & Setters


    public float getPointerRadius() {
        return mPointerRadius;
    }

    public void setPointerRadius(float mPointerRadius) {
        this.mPointerRadius = mPointerRadius;
        invalidate();
    }

    public float getInnerSectorRadius() {
        return mInnerSectorRadius;
    }

    public void setInnerSectorRadius(float mInnerSectorRadius) {
        this.mInnerSectorRadius = mInnerSectorRadius;
        invalidate();
    }

    public float getOuterSectorRadius() {
        return mOuterSectorRadius;
    }

    public void setOuterSectorRadius(float mOuterSectorRadius) {
        this.mOuterSectorRadius = mOuterSectorRadius;
        invalidate();
    }

    public float getMaxSpeed() {
        return mMaxSpeed;
    }

    public void setMaxSpeed(int mMaxSpeed) {
        this.mMaxSpeed = mMaxSpeed;
        invalidate();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return mDigitsColor;
    }

    public void setDigitsColor(int mDigitsColor) {
        this.mDigitsColor = mDigitsColor;
        invalidate();
    }

    public int getSectorBeforePointerColor() {
        return mSectorBeforePointerColor;
    }

    public void setSectorBeforePointerColor(int mSectorBeforePointerColor) {
        this.mSectorBeforePointerColor = mSectorBeforePointerColor;
        invalidate();
    }

    public int getSectorAfterPointerColor() {
        return mSectorAfterPointerColor;
    }

    public void setSectorAfterPointerColor(int mSectorAfterPointerColor) {
        this.mSectorAfterPointerColor = mSectorAfterPointerColor;
        invalidate();
    }

    public int getPointerColor() {
        return mPointerColor;
    }

    public void setPointerColor(int mPointerColor) {
        this.mPointerColor = mPointerColor;
        invalidate();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
        invalidate();
    }

    public Paint getPointerPaint() {
        return mPointerPaint;
    }

    public void setPointerPaint(Paint mPointerPaint) {
        this.mPointerPaint = mPointerPaint;
        invalidate();
    }

    public Paint getBorderPaint() {
        return mBorderPaint;
    }

    public void setBorderPaint(Paint mBorderPaint) {
        this.mBorderPaint = mBorderPaint;
        invalidate();
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    public void setBackgroundPaint(Paint mBackgroundPaint) {
        this.mBackgroundPaint = mBackgroundPaint;
        invalidate();
    }

    public Paint getDigitsPaint() {
        return mDigitsPaint;
    }

    public void setDigitsPaint(Paint mDigitsPaint) {
        this.mDigitsPaint = mDigitsPaint;
        invalidate();
    }

    public Paint getSectorBeforePointerPaint() {
        return mSectorBeforePointerPaint;
    }

    public void setSectorBeforePointerPaint(Paint mSectorBeforePointerPaint) {
        this.mSectorBeforePointerPaint = mSectorBeforePointerPaint;
        invalidate();
    }

    public Paint getSectorAfterPointerPaint() {
        return mSectorAfterPointerPaint;
    }

    public void setSectorAfterPointerPaint(Paint mSectorAfterPointerPaint) {
        this.mSectorAfterPointerPaint = mSectorAfterPointerPaint;
        invalidate();
    }

    public float getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setCurrentSpeed(float mCurrentSpeed) {
        if (mCurrentSpeed > this.mMaxSpeed)
            this.mCurrentSpeed = mMaxSpeed;
        else if (mCurrentSpeed < 0)
            this.mCurrentSpeed = 0;
        else this.mCurrentSpeed = mCurrentSpeed;
        invalidate();
    }

    @Override
    public void onSpeedChanged(float newSpeedValue) {
        this.setCurrentSpeed(newSpeedValue);
        this.invalidate();

    }
}
