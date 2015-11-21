package net.wtako.IIDXSPGuide.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerFixed extends android.support.v4.view.ViewPager {

    private boolean isPagingEnabled = true;

    public ViewPagerFixed(Context context) {
        super(context);
    }

    public ViewPagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void fakeDragBy(float xOffset) {
        try {
            super.fakeDragBy(xOffset);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endFakeDrag() {
        try {
            super.endFakeDrag();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return this.isPagingEnabled && super.onTouchEvent(ev);
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return this.isPagingEnabled && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}