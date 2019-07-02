package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.ivy.cpg.view.order.productdetails.ProductDetailsFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeBO;

import java.util.List;

@SuppressLint("ValidFragment")
public class SchemeDialog extends DialogFragment {

    private List<SchemeBO> schemes;
    private String pdname;
    private String prodId;
    private String mProductID;
    private ProductMasterBO productObj;
    private int flag;
    private int mTotalScreenWidth;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.scheme, container,
                false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, new ProductDetailsFragment())
                .commit();

        return rootView;
    }


    @SuppressLint("ValidFragment")
    public SchemeDialog(final Context context, List<SchemeBO> schemes,
                        String pdname, String prodId, ProductMasterBO productObj, int flag, int totalScreenSize) {
        this.prodId = prodId;
        this.mProductID = prodId;
        this.mTotalScreenWidth = totalScreenSize;
        this.schemes = schemes;
        this.productObj = productObj;
        this.flag = flag;
        this.pdname = pdname;

    }

}
