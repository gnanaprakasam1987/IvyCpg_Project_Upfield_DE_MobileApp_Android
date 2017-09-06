package com.ivy.sd.png.commons;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by hanifa.m on 11/23/2016.
 */

public class MapWrapperLayout extends FrameLayout {

    public MapWrapperLayout(Context context) {
        super(context);
    }

    public MapWrapperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    private OnDragListener mOnDragListener;



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(ev);
        }
        return super.dispatchTouchEvent(ev);
    }


    public interface OnDragListener {
        public void onDrag(MotionEvent motionEvent);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }


}
