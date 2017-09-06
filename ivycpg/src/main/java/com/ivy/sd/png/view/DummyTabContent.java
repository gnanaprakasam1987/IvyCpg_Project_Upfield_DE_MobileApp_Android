package com.ivy.sd.png.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class DummyTabContent implements TabContentFactory{
    private Context mContext;
    
    LayoutInflater inflater;
 
    public DummyTabContent(Context context){
        mContext = context;
    }
 
    @Override
    public View createTabContent(String tag) {
        View v = new View(mContext);
        return v;
    }
}