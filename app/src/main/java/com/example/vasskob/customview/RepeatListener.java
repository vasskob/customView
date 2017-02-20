package com.example.vasskob.customview;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a onClickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next one after the initialInterval, and subsequent
 * ones after the normalInterval.
 * <p>
 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks. Can be rewritten to
 * achieve this.
 */
class RepeatListener implements OnTouchListener {

    private Handler onPressHandler = new Handler();
    private Handler unPressHandler = new Handler();

    private final int initialInterval;
    private final int normalInterval;
    private final OnClickListener onClickListener;
    private final OnClickListener unClickListener;

    private Runnable onPressRunnable = new Runnable() {
        @Override
        public void run() {
            onPressHandler.postDelayed(this, normalInterval);
            onClickListener.onClick(downView);
        }
    };

    private Runnable unPressRunnable = new Runnable() {
        @Override
        public void run() {
            unPressHandler.postDelayed(this, normalInterval);
            unClickListener.onClick(downView);
        }
    };

    private View downView;

    /**
     * @param initialInterval The interval after first click event
     * @param normalInterval  The interval after second and subsequent click
     *                        events
     * @param onClickListener The OnClickListener, that will be called
     *                        periodically
     */
    RepeatListener(int initialInterval, int normalInterval,
                   OnClickListener onClickListener, OnClickListener unClickListener) {
        if (onClickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.onClickListener = onClickListener;
        this.unClickListener = unClickListener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onPressHandler.removeCallbacks(onPressRunnable);
                unPressHandler.removeCallbacks(unPressRunnable);
                onPressHandler.postDelayed(onPressRunnable, initialInterval);

//                downView = view;
//                downView.setPressed(true);
//                onClickListener.onClick(view);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onPressHandler.removeCallbacks(onPressRunnable);
                unPressHandler.removeCallbacks(unPressRunnable);
                unPressHandler.postDelayed(unPressRunnable, initialInterval);

//                downView.setPressed(false);
//                downView = null;
//                unClickListener.onClick(view);
                return true;
        }
        return false;
    }
}