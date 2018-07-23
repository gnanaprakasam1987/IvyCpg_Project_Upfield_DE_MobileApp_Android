package com.ivy.sd.png.bo.asset;

import com.ivy.sd.png.bo.ProductMasterBO;

import java.util.Map;
import java.util.Vector;

public class ProductMasterPair {
    public Vector<ProductMasterBO> productMaster;
    public Map<String, ProductMasterBO> productMasterById;

    public ProductMasterPair(Vector<ProductMasterBO> productMaster, Map<String, ProductMasterBO> productMasterById) {
        this.productMaster = productMaster;
        this.productMasterById = productMasterById;
    }
}
