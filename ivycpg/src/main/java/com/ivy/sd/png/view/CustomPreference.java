package com.ivy.sd.png.view;

import android.content.Context;
import android.preference.Preference;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

public class CustomPreference extends Preference{

    public CustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        //To set ripple effect for the list item
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);

        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        titleView.setTypeface(FontUtils.getFontRoboto(getContext(), FontUtils.FontType.LIGHT));
        TextView summaryView = view.findViewById(android.R.id.summary);
        summaryView.setTypeface(FontUtils.getFontRoboto(getContext(), FontUtils.FontType.MEDIUM));
        summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }
}
