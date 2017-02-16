package com.example.vasskob.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
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
//      accelerateButton.setOnTouchListener(this);

        accelerateButton.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.acceleratorPressed();
            }

        }));
        Button brakeButton = (Button) findViewById(R.id.button_brake);

        brakeButton.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.brakePressed();
            }
        }));

    }
//    brakeButton.setOnTouchListener(this);
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        switch (v.getId()) {
//            case R.id.button_accelerate:
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: // нажатие
//                        speedometerView.acceleratorPressed();
//                        break;
//                    case MotionEvent.ACTION_MOVE: // движение
//
//                        break;
//                    case MotionEvent.ACTION_UP: // отпускание
//                    case MotionEvent.ACTION_CANCEL:
//                        speedometerView.acceleratorRelease();
//                        break;
//                }
//                break;
//            case R.id.button_brake:
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: // нажатие
//                        speedometerView.brakePressed();
//                        break;
//                    case MotionEvent.ACTION_MOVE: // движение
//                        break;
//                    case MotionEvent.ACTION_UP: // отпускание
//                    case MotionEvent.ACTION_CANCEL:
//                        speedometerView.brakeRelease();
//                        break;
//                }
//                break;
//        }
//        return true;
//    }
}
