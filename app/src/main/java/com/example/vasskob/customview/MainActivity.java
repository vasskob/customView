package com.example.vasskob.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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
        ImageButton accelerateButton = (ImageButton) findViewById(R.id.button_accelerate);
        accelerateButton.setOnTouchListener(new RepeatListener(0, 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        speedometerView.acceleratorPressed();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        speedometerView.acceleratorReleased();
                    }
                }
        ));
        ImageButton brakeButton = (ImageButton) findViewById(R.id.button_brake);
        brakeButton.setOnTouchListener(new RepeatListener(0, 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        speedometerView.brakePressed();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        speedometerView.brakeReleased();
                    }
                }
        ));

    }
}

//       implements OnTouchListener()
//        Button accelerateButton = (Button) findViewById(R.id.button_accelerate);
//        accelerateButton.setOnTouchListener(this);
//        Button brakeButton = (Button) findViewById(R.id.button_brake);
//        brakeButton.setOnTouchListener(this);
//    }
//    @Override
//    public boolean onTouch (View v, MotionEvent event){
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
//                        speedometerView.acceleratorReleased();
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
//                        speedometerView.brakeReleased();
//                        break;
//                }
//                break;
//        }
//        return true;
//    }
// }
