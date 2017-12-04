package com.ivy.cpg.view.sf;

import java.util.ArrayList;
import java.util.HashMap;

public class ShelfShareHelper {

    public static final String BLOCK_COUNT = "block_count";
    public static final String SHELF_COUNT = "shelf_count";
    public static final String SHELF_LENGTH = "shelf_length";
    public static final String EXTRA_LENGTH = "extra_length";
    public static final String SHARE = "share";
    public static final String BRAND_DETAIL_HASH_MAP = "brand_details_hashmap";
    public static final String LOADING_FROM_DB = "loading_from_db";

    public static final int SOS = 0;
    public static final int SOD = 1;
    private static ShelfShareHelper mShelfShareHelper = null;
    private HashMap<String, HashMap<String, Object>> mShelfDetailForSOD = null;
    private ArrayList<SFLocationBO> locations;

    public static ShelfShareHelper getInstance() {
        if (mShelfShareHelper == null)
            mShelfShareHelper = new ShelfShareHelper();

        return mShelfShareHelper;
    }

    private ShelfShareHelper() {
        mShelfDetailForSOD = new HashMap<>();
    }

    public boolean containsKeySOD(String key) {
        return this.mShelfDetailForSOD.containsKey(key);
    }

    public ArrayList<SFLocationBO> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<SFLocationBO> locations) {
        this.locations = locations;
    }
}