package com.ivy.sd.png.view.reports;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.maplib.OnInfoWindowElemTouchListener;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.profile.ProfileActivity;

/**
 * Created by rajkumar.s on 10/27/2017.
 */

public class SellerMapViewReportFragment extends SupportMapFragment {

    View view;
    BusinessModel bmodel;

    MapWrapperLayout mainLayout;
    GoogleMap mMap;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_seller_mapview, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainLayout = (MapWrapperLayout) getView()
                .findViewById(R.id.planningmapnew);

    }

    @Override
    public void onStart() {
        super.onStart();

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        try {
            final ViewGroup nullParent = null;
            if (mMap == null) {
                mMap = this.getMap();
                float pxlDp = 39 + 20;
                mainLayout.init(mMap, getPixelsFromDp(SellerMapViewReportFragment.this.getActivity(), pxlDp));
              /*  this.infoWindow = (ViewGroup) layInflater.inflate(
                        R.layout.custom_info_window, nullParent);
                this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
                this.infoSnippet = (TextView) infoWindow
                        .findViewById(R.id.snippet);
                this.infoDistance = (TextView) infoWindow.findViewById(R.id.distance_txt);
                startVisitLty = (LinearLayout) infoWindow.findViewById(R.id.start_visit_lty);
                startVisitBtn = (Button) infoWindow.findViewById(R.id.start_visitbtn);
                this.infoTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                this.infoSnippet.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                this.infoDistance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
//

                startVisitLty.setOnTouchListener(infoButtonListener);*/
                setUpMap();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void setUpMap() {
        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMyLocationEnabled(false);
         //   mMap.setInfoWindowAdapter(new PlanningMapFragment.CustomInfoWindowAdapter());
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
