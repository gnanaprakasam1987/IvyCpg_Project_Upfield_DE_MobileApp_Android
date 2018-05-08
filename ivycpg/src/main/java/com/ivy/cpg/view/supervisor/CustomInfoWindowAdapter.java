package com.ivy.cpg.view.supervisor;


import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import java.util.HashMap;

class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mymarkerview;
    private TextView tvUserName , tvTimeIn , tvTimeOut , tvBattery , tvActivity, tvAddress, tvLastSync ;
    private ImageView sellerInfoNavigate;
    private LinearLayout startSellerMap,timeLayout;
    private HashMap<String, DetailsBo> userHashmap = new HashMap<>();
    private boolean isMovementTrack;
    private Context context;

    CustomInfoWindowAdapter(Context context,View mymarkerview,HashMap<String, DetailsBo> userHashmap,boolean isMovementTrack) {
        this.mymarkerview = mymarkerview;
        this.userHashmap = userHashmap;
        this.isMovementTrack = isMovementTrack;
        this.context = context;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {

        tvUserName = mymarkerview.findViewById(R.id.tv_user_name);
        tvTimeIn = mymarkerview.findViewById(R.id.tv_time_in);
        tvTimeOut = mymarkerview.findViewById(R.id.tv_time_out);
        tvBattery = mymarkerview.findViewById(R.id.tv_battery);
        tvActivity = mymarkerview.findViewById(R.id.tv_activity);
        tvAddress = mymarkerview.findViewById(R.id.tv_address);
        tvLastSync = mymarkerview.findViewById(R.id.tv_last_sync);
        sellerInfoNavigate = mymarkerview.findViewById(R.id.user_in_work);
        startSellerMap = mymarkerview.findViewById(R.id.btn_layout);
        timeLayout = mymarkerview.findViewById(R.id.time_layout);

        try{
            int userId = Integer.valueOf(marker.getSnippet().toString());

            for(final DetailsBo detailsBo : userHashmap.values()){
                if(detailsBo.getUserId() == userId){
                    tvUserName.setText(detailsBo.getUserName());

                    String activity = "Activity <b>"+detailsBo.getActivityName()+"</b>";
                    tvActivity.setText(Html.fromHtml(activity));

                    String battery = "Battery <b>"+detailsBo.getBatterStatus()+"% </b>";
                    tvBattery.setText(Html.fromHtml(battery));

                    String syncTime = "Last Sync <b>" + SupervisorActivityHelper.getInstance().getTimeFromMillis(detailsBo.getTime()) + "</b>";
                    tvLastSync.setText(Html.fromHtml(syncTime));

                    if(isMovementTrack) {
                        sellerInfoNavigate.setImageResource(R.drawable.ic_double_right_arrow);
                        timeLayout.setVisibility(View.GONE);

                        String address = "Address <b>" +
                                SupervisorActivityHelper.getInstance().getAddressLatLong(context,detailsBo.getMarker().getPosition())+ " </b>";
                        tvAddress.setText(Html.fromHtml(address));

                        mymarkerview.findViewById(R.id.view_dotted_line).setVisibility(View.GONE);
                        mymarkerview.findViewById(R.id.view_dotted_line_end).setVisibility(View.GONE);

                    }else{

                        tvTimeIn.setText(SupervisorActivityHelper.getInstance().getTimeFromMillis(detailsBo.getInTime()));
                        tvTimeOut.setText(SupervisorActivityHelper.getInstance().getTimeFromMillis(detailsBo.getOutTime()));

                        tvAddress.setVisibility(View.GONE);

                        if (detailsBo.getStatus().equalsIgnoreCase("IN"))
                            sellerInfoNavigate.setImageResource(R.drawable.ic_in_work);
                        else
                            sellerInfoNavigate.setImageResource(R.drawable.ic_day_closed);
                    }

                    break;
                }
            }

        }catch(Exception e){
            Commons.printException("Error",e);
        }
        return mymarkerview;
    }

}