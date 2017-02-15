package com.example.vasskob.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        final SpeedometerView speedometerView = new SpeedometerView(this);
        Button accelerateButton = (Button) findViewById(R.id.button_accelerate);
        accelerateButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.onSpeedChanged(speedometerView.getCurrentSpeed() + 8);
            }
        }));
        Button brakeButton = (Button) findViewById(R.id.button_brake);
        brakeButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.onSpeedChanged(speedometerView.getCurrentSpeed() - 8);
            }
        }));
    }
}
