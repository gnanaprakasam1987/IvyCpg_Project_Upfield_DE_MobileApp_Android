package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerKPIBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by rajesh.k on 21-01-2016.
 */
public class RetailerKpiFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private View view;
    private RelativeLayout mMonthRL1TV,mMonthRL2TV,mMonthRL3TV;
    private TextView mMonth1TitleTV,  mMon1_Ach1TV,  mMon1_Ach2TV,  mMon1_Ach3TV;
    private TextView mMonth2TitleTV,  mMon2_Ach1TV,  mMon2_Ach2TV,  mMon2_Ach3TV;
    private TextView mMonth3TitleTV,   mMon3_Ach1TV, mMon3_Ach2TV,  mMon3_Ach3TV;
    private TextView mVolumeTV,mLineTV,mNoOfInvoiceTV;
    private SparseArray<ArrayList<RetailerKPIBO>> mRetailerKpiListByMonth;
    private static final String VOLUME="SVOL";
    private static final String LINES="LINES";
    private static final String NO_OF_INVOICE="NUMINV";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        view = inflater.inflate(R.layout.fragment_retailer_kpi, container,
                false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initialization();
        bmodel.dashBoardHelper.downloadRetailerKpi();
        updateData();


    }

    private void initialization() {
        mVolumeTV=(TextView)view.findViewById(R.id.volume);
        mLineTV=(TextView)view.findViewById(R.id.line);
        mNoOfInvoiceTV=(TextView)view.findViewById(R.id.no_of_invoice);

        // Month1 inititalization
        mMonth1TitleTV = (TextView) view.findViewById(R.id.month1);

        mMon1_Ach1TV = (TextView) view.findViewById(R.id.month1_ach1);
        mMon1_Ach2TV = (TextView) view.findViewById(R.id.month1_ach2);
        mMon1_Ach3TV = (TextView) view.findViewById(R.id.month1_ach3);

        mMonthRL1TV=(RelativeLayout)view.findViewById(R.id.rl1);


        // Month2 initialization

        mMonth2TitleTV = (TextView) view.findViewById(R.id.month2);

        mMon2_Ach1TV = (TextView) view.findViewById(R.id.month2_ach1);
        mMon2_Ach2TV = (TextView) view.findViewById(R.id.month2_ach2);
        mMon2_Ach3TV = (TextView) view.findViewById(R.id.month2_ach3);

        mMonthRL2TV=(RelativeLayout)view.findViewById(R.id.rl2);

        // Month3 initialization


        mMonth3TitleTV = (TextView) view.findViewById(R.id.month3);

        mMon3_Ach1TV = (TextView) view.findViewById(R.id.month3_ach1);
        mMon3_Ach2TV = (TextView) view.findViewById(R.id.month3_ach2);
        mMon3_Ach3TV = (TextView) view.findViewById(R.id.month3_ach3);

        mMonthRL3TV=(RelativeLayout)view.findViewById(R.id.rl3);
    }
    private void updateData(){
        mRetailerKpiListByMonth=bmodel.dashBoardHelper.getRetailerKpiListByMonth();
        LinkedHashSet<Integer> monthList=bmodel.dashBoardHelper.getRetailerKpiMonthList();
        if(monthList!=null&&mRetailerKpiListByMonth!=null){
            Iterator itr = monthList.iterator();
       int i=-1;
            while(itr.hasNext()){
               i=i+1;

                int key = (Integer)itr.next();
                if(key!=0) {
                    String monthName = bmodel.dashBoardHelper.MONTH_NAME[key - 1];
                    ArrayList<RetailerKPIBO> retailerKpiList = mRetailerKpiListByMonth.get(key);
                    if (retailerKpiList != null) {

                        if (i == 0) {
                            mMonthRL1TV.setVisibility(View.VISIBLE);


                            String year="";
                            for (RetailerKPIBO retailerKPIBO : retailerKpiList) {

                                if(year.equals("")) {
                                    String fromDate = retailerKPIBO.getFromDate();
                                    String split[] = fromDate.split("/");
                                    if (split != null && split.length > 0) {
                                         year = split[0];
                                        mMonth1TitleTV.setText(monthName.substring(0, 3) + "'" + year.substring(2, 4));


                                    }
                                }


                                if (retailerKPIBO.getListCode().equals(VOLUME)) {
                                    mMon1_Ach1TV.setText(retailerKPIBO.getAchievement() + "");
                                    mVolumeTV.setText(retailerKPIBO.getListCode());


                                } else if (retailerKPIBO.getListCode().equals(LINES)) {
                                    mMon1_Ach2TV.setText(retailerKPIBO.getAchievement() + "");
                                    mLineTV.setText(retailerKPIBO.getListName());


                                } else if (retailerKPIBO.getListCode().equals(NO_OF_INVOICE)) {
                                    mMon1_Ach3TV.setText(retailerKPIBO.getAchievement() + "");
                                    mNoOfInvoiceTV.setText(retailerKPIBO.getListName());

                                }
                            }


                        } else if (i == 1) {
                            mMonthRL2TV.setVisibility(View.VISIBLE);
                            String year="";

                            for (RetailerKPIBO retailerKPIBO : retailerKpiList) {
                                if(year.equals("")) {
                                    String fromDate = retailerKPIBO.getFromDate();
                                    String split[] = fromDate.split("/");
                                    if (split != null && split.length > 0) {
                                        year = split[0];
                                        mMonth2TitleTV.setText(monthName.substring(0, 3) + "'" + year.substring(2, 4));


                                    }
                                }
                                if (retailerKPIBO.getListCode().equals(VOLUME)) {
                                    mMon2_Ach1TV.setText(retailerKPIBO.getAchievement() + "");


                                } else if (retailerKPIBO.getListCode().equals(LINES)) {
                                    mMon2_Ach2TV.setText(retailerKPIBO.getAchievement() + "");


                                } else if (retailerKPIBO.getListCode().equals(NO_OF_INVOICE)) {
                                    mMon2_Ach3TV.setText(retailerKPIBO.getAchievement() + "");

                                }
                            }

                        } else if (i == 2) {
                            mMonthRL3TV.setVisibility(View.VISIBLE);
                            String year="";

                            for (RetailerKPIBO retailerKPIBO : retailerKpiList) {
                                if(year.equals("")) {
                                    String fromDate = retailerKPIBO.getFromDate();
                                    String split[] = fromDate.split("/");
                                    if (split != null && split.length > 0) {
                                        year = split[0];
                                        mMonth3TitleTV.setText(monthName.substring(0, 3) + "'" + year.substring(2, 4));


                                    }
                                }
                                if (retailerKPIBO.getListCode().equals(VOLUME)) {
                                    mMon3_Ach1TV.setText(retailerKPIBO.getAchievement() + "");


                                } else if (retailerKPIBO.getListCode().equals(LINES)) {
                                    mMon3_Ach2TV.setText(retailerKPIBO.getAchievement() + "");


                                } else if (retailerKPIBO.getListCode().equals(NO_OF_INVOICE)) {
                                    mMon3_Ach3TV.setText(retailerKPIBO.getAchievement() + "");

                                }
                            }

                        }
                    }
                }






                // get the object by the key.

            }
        }

    }
}



