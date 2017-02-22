package com.example.vasskob.customview;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private SpeedometerView speedometerView,
            speedometerView2;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        textView = (TextView) findViewById(R.id.speed_txt);
        speedometerView = (SpeedometerView) findViewById(R.id.speedometer_view);

        speedometerView2 = (SpeedometerView) findViewById(R.id.speedometer_view2);
        speedometerView2.setBorderColor(ContextCompat.getColor(this, R.color.gray_900));
        speedometerView2.setDigitsColor(ContextCompat.getColor(this, R.color.gray_900));
        speedometerView2.setSpeedArrowColor(ContextCompat.getColor(this, R.color.black));
        speedometerView2.setViewBackgroundColor(ContextCompat.getColor(this, R.color.gray_300));
        speedometerView2.setSectorAfterSpeedArrowColor(ContextCompat.getColor(this, R.color.lightBlue_900));
        speedometerView2.setSectorBeforeSpeedArrowColor(ContextCompat.getColor(this, R.color.cyan_500));
        speedometerView2.setMaxSpeed(160);
        speedometerView2.setCurrentSpeed(50);
        speedometerView2.setSpeedArrowRadius(75);
        speedometerView2.setInnerSectorRadius(20);
        speedometerView2.setOuterSectorRadius(35);
        speedometerView2.setCurrentFuelLevel(80);
        speedometerView2.setFuelConsumption(0.5f);
        speedometerView2.setOnSpeedChangedListener(new SpeedometerView.OnSpeedChangedListener() {
            @Override
            public void onSpeedChanged(int value) {
                textView.setText(String.valueOf(value));
            }
        });

        ImageButton accelerateButton = (ImageButton) findViewById(R.id.button_accelerate);
        accelerateButton.setOnTouchListener(this);
        ImageButton brakeButton = (ImageButton) findViewById(R.id.button_brake);
        brakeButton.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.button_accelerate:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speedometerView.acceleratorPressed();
                        speedometerView2.acceleratorPressed();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        speedometerView.acceleratorReleased();
                        speedometerView2.acceleratorReleased();
                        break;
                }
                break;
            case R.id.button_brake:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speedometerView.brakePressed();
                        speedometerView2.brakePressed();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        speedometerView.brakeReleased();
                        speedometerView2.brakeReleased();
                        break;
                }
                break;
        }
        return true;
    }
}
