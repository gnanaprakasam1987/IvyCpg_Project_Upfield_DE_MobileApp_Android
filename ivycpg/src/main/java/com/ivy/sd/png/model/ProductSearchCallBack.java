package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.ProductMasterBO;

import java.util.Vector;

public interface ProductSearchCallBack {

    void productSearchResult(Vector<ProductMasterBO> searchedList);
}
