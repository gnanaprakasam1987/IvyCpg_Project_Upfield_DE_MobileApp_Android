package com.ivy.ui.retailer.presenter;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class RetailerPresenterImpl<V extends RetailerContract.RetailerView> extends BasePresenter<V> implements RetailerContract.RetailerPresenter<V> {

    private AppDataProvider appDataProvider;
    @Inject
    RetailerPresenterImpl(DataManager dataManager,
                          SchedulerProvider schedulerProvider,
                          CompositeDisposable compositeDisposable,
                          ConfigurationMasterHelper configurationMasterHelper,
                          V view,
                          AppDataProvider appDataProvider) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.appDataProvider = appDataProvider;
    }

    @Override
    public void fetchRetailerList() {

        getIvyView().populateRetailers(appDataProvider.getRetailerMasters());

    }

    @Override
    public void prepareRetailerMarker(GoogleMap map,List<RetailerMasterBO> retailerList) {

        ArrayList<MarkerOptions> retailerMarkerList = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (RetailerMasterBO retailerMasterBO : retailerList) {

            if (retailerMasterBO.getLatitude() != 0 && retailerMasterBO.getLongitude() != 0) {

                LatLng latLng = new LatLng(retailerMasterBO.getLatitude(), retailerMasterBO.getLongitude());
                MarkerOptions mMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(retailerMasterBO.getRetailerName() + "," + retailerMasterBO.getRetailerID())
                        .snippet(retailerMasterBO.getAddress1())
                        .icon(BitmapDescriptorFactory
                                .fromResource(getMarkerIcon(retailerMasterBO)));

                map.addMarker(mMarkerOptions);
                builder.include(latLng);

                retailerMarkerList.add(mMarkerOptions);
            }
        }

        if (retailerMarkerList.size() > 0)
            focusMarker(map,builder);

        getIvyView().populateRetailersMarker(retailerMarkerList);
    }

    @Override
    public void addRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void updateRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void deleteRetailerFromPlan(RetailerMasterBO retailerMasterBO) {

    }

    @Override
    public int getPixelsFromDpInt(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((39 + 20) * scale + 0.5f);
    }

    public void focusMarker(GoogleMap map,final LatLngBounds.Builder builder) {

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                if (checkAreaBoundsTooSmall(builder.build())) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                }
            }
        });
    }

    private boolean checkAreaBoundsTooSmall(LatLngBounds bounds) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < 300;
    }

    private int getMarkerIcon(RetailerMasterBO retailerMasterBO) {
        int drawable = R.drawable.marker_visit_unscheduled;

        if ("Y".equals(retailerMasterBO.getIsVisited())) {
            if (("N").equals(retailerMasterBO.isOrdered()))
                drawable = R.drawable.marker_visit_non_productive;
            else
                drawable = R.drawable.marker_visit_completed;
        } else if (retailerMasterBO.getIsToday() == 1 || "Y".equals(retailerMasterBO.getIsDeviated()))
            drawable = R.drawable.marker_visit_planned;


        if (retailerMasterBO.isHasNoVisitReason())
            drawable = R.drawable.marker_visit_cancelled;


        return drawable;
    }


}
