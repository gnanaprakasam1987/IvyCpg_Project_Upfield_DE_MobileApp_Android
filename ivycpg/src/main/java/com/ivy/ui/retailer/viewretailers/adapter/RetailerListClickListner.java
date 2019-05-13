package com.ivy.ui.retailer.viewretailers.adapter;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.List;

/**
 * Created by mansoor on 01/04/2019
 */
public interface RetailerListClickListner {
    void onRetailerSelected(RetailerMasterBO retailerMasterBO);
}
