package com.ivy.cpg.view.supervisor.dummyadapter;


import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import java.util.HashMap;

class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mymarkerview;
    private TextView tvUserName , tvTimeIn , tvTimeOut , tvAddress ;
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
        tvAddress = mymarkerview.findViewById(R.id.tv_address);

        try{
            int userId = Integer.valueOf(marker.getSnippet().toString());

            for(final DetailsBo detailsBo : userHashmap.values()){
                if(detailsBo.getUserId() == userId){
                    tvUserName.setText(detailsBo.getUserName());



                    break;
                }
            }

        }catch(Exception e){
            Commons.printException("Error",e);
        }
        return mymarkerview;
    }

}