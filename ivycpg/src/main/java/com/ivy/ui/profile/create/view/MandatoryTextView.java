package com.ivy.ui.profile.create.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

@SuppressLint("AppCompatCustomView")
public class MandatoryTextView extends TextView {


    public MandatoryTextView(Context context) {
        super(context);
        /*Typeface face = Typeface.createFromAsset(context.getAssets(), "Helvetica_Neue.ttf");
        this.setTypeface(face);*/
        this.setText("*");
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        this.setTextColor(Color.RED);
    }

    public MandatoryTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setText("*");
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        this.setTextColor(Color.RED);
    }

    public MandatoryTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setText("*");
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        this.setTextColor(Color.RED);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


}