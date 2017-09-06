package com.ivy.sd.png.view;

import android.content.Context;
import android.preference.Preference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

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
        BusinessModel bmodel = (BusinessModel) getContext().getApplicationContext();
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        titleView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }
}
