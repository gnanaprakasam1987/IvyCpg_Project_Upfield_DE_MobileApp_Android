package com.ivy.cpg.view.stockcheck;

import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;

/**
 * Created by mansoor on 03/10/2018
 */
public class StockCheckHelper {

    private static StockCheckHelper instance = null;
    private final BusinessModel bmodel;

    private StockCheckHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static StockCheckHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StockCheckHelper(context);
        }
        return instance;
    }

}
