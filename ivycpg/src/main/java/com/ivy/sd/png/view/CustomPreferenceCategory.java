package com.ivy.sd.png.view;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

public class CustomPreferenceCategory extends PreferenceCategory{

    public CustomPreferenceCategory(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);

    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        BusinessModel bmodel = (BusinessModel) getContext().getApplicationContext();
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
    }
}
