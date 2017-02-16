package com.example.vasskob.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private SpeedometerView speedometerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        speedometerView = (SpeedometerView) findViewById(R.id.speedometer_view);
        Button accelerateButton = (Button) findViewById(R.id.button_accelerate);
        accelerateButton.setOnTouchListener(new RepeatListener(100, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.setCurrentSpeed(speedometerView.getCurrentSpeed() + 0.8f, 0, 0);
            }
        }));
        Button brakeButton = (Button) findViewById(R.id.button_brake);
        brakeButton.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.setCurrentSpeed(speedometerView.getCurrentSpeed() - 3f, 0, 0);
            }
        }));

    }
}
