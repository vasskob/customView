package com.example.vasskob.customview;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickUpListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next one after the initialInterval, and subsequent
 * ones after the normalInterval.
 * <p>
 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks. Can be rewritten to
 * achieve this.
 */
class RepeatListener implements OnTouchListener {

    private Handler handlerUp = new Handler();
    private Handler handlerDn = new Handler();

    private int initialInterval;
    private final int normalInterval;
    private final OnClickListener clickUpListener;
    private final OnClickListener clickDnListener;

    private Runnable handlerUpRunnable = new Runnable() {
        @Override
        public void run() {
            handlerUp.postDelayed(this, normalInterval);
            clickUpListener.onClick(downView);
        }
    };

    private Runnable handlerDownRunnable = new Runnable() {
        @Override
        public void run() {
            handlerDn.postDelayed(this, normalInterval);
            clickDnListener.onClick(downView);
        }
    };

    private View downView;

    /**
     * @param initialInterval The interval after first click event
     * @param normalInterval  The interval after second and subsequent click
     *                        events
     * @param clickUpListener The OnClickListener, that will be called
     *                        periodically
     */
    RepeatListener(int initialInterval, int normalInterval,
                   OnClickListener clickUpListener, OnClickListener clickDnListener) {
        if (clickUpListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.clickUpListener = clickUpListener;
        this.clickDnListener = clickDnListener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handlerUp.removeCallbacks(handlerUpRunnable);
                handlerDn.removeCallbacks(handlerDownRunnable);
                handlerUp.postDelayed(handlerUpRunnable, initialInterval);

                downView = view;
                downView.setPressed(true);
                clickUpListener.onClick(view);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handlerUp.removeCallbacks(handlerUpRunnable);
                handlerDn.removeCallbacks(handlerDownRunnable);
                handlerDn.postDelayed(handlerDownRunnable, initialInterval);

                downView.setPressed(false);
                downView = null;
                clickDnListener.onClick(view);
                return true;
        }
        return false;
    }
}