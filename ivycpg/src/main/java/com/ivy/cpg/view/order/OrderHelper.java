package com.ivy.cpg.view.order;

import android.content.Context;

import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by rajkumar on 30/1/18.
 */

public class OrderHelper {

    private static OrderHelper instance = null;
    private Context mContext;
    private BusinessModel businessModel;

    private Vector<ProductMasterBO> mSortedOrderedProducts;

    private OrderHelper(Context context) {
        this.mContext = context;
        this.businessModel = (BusinessModel) context;
    }


    public static OrderHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderHelper(context);
        }
        return instance;
    }

    public Vector<ProductMasterBO> getSortedOrderedProducts() {
        return mSortedOrderedProducts;
    }

    public void setSortedOrderedProducts(Vector<ProductMasterBO> mSortedList) {
        this.mSortedOrderedProducts = mSortedList;
    }

}
