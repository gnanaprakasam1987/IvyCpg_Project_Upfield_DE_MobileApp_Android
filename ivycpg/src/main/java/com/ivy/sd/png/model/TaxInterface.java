package com.ivy.sd.png.model;

import android.util.SparseArray;

import com.ivy.sd.png.bo.TaxBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by mansoor on 18/1/18.
 */

public interface TaxInterface {

   void updateBillTaxList(ArrayList<TaxBO> mBillTaxList);

   void updateTaxListByProductId(HashMap<String, ArrayList<TaxBO>> mTaxListByProductId);

   void updateProductIdbyTaxGroupId (LinkedHashMap<String, HashSet<String>> mProductIdByTaxGroupId);

   void updateGroupIdList(ArrayList<TaxBO> mGroupIdList);

    void updateTaxPercentageListByGroupID(LinkedHashMap<Integer, HashSet<Double>> mTaxPercentagerListByGroupId);

    void updateTaxBoByGroupId(SparseArray<LinkedHashSet<TaxBO>> mTaxBOByGroupId);

    void updateTaxBoBatchProduct(HashMap<String, ArrayList<TaxBO>> mTaxBoBatchProduct);

}
