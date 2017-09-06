package com.ivy.sd.png.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

/**
 * Created by nivetha.s on 16-09-2015.
 */
public class CustomTextView extends TextView {
    TypedArray a;
    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public CustomTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
             a = getContext().obtainStyledAttributes(attrs, R.styleable.MyTextView);



        }
    }

}
