package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;

/**
 * Created by ivyuser on 3/1/18.
 */

public class DisplaySchemeSlabFragment extends IvyBaseFragment {

    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display_scheme_slab, container,
                false);


        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
