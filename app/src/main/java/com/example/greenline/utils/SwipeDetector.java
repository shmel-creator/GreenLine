package com.example.greenline.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private SwipeListener listener;

    public interface SwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }

    public SwipeDetector(SwipeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > 100) {
            if (diffX > 0) listener.onSwipeRight();
            else listener.onSwipeLeft();
            return true;
        }
        return false;
    }
}