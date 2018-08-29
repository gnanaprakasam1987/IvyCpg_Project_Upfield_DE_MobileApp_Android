package com.ivy.cpg.view.van.vanunload;

import com.ivy.sd.png.bo.LoadManagementBO;

import java.util.ArrayList;

public interface VanUnloadInterface {
    void setProductName(String pName);
    void hideViewFlipper();
    void hideKeyboard();
    void updateTotalQtyDetails(ArrayList<LoadManagementBO> filterList);
}
