package com.ivy.sd.png.bo.asset;

/**
 * Created by anandasir.v on 8/31/2017.
 *
 */

public class AssetTrackingBrandBO {

    private int brandID;
    private String brandName;

    public AssetTrackingBrandBO() {

    }

    public AssetTrackingBrandBO(int brandID, String brandName) {
        super();
        this.brandID = brandID;
        this.brandName = brandName;
    }

    public int getBrandID() {
        return brandID;
    }

    public void setBrandID(int brandID) {
        this.brandID = brandID;
    }


    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public String toString() {
        return brandName;
    }
}
