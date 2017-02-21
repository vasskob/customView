package com.example.vasskob.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private SpeedometerView speedometerView, speedometerView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        speedometerView = (SpeedometerView) findViewById(R.id.speedometer_view);
        speedometerView2 = (SpeedometerView) findViewById(R.id.speedometer_view2);
        // TODO: 21.02.17 show that view can be customized from code
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
