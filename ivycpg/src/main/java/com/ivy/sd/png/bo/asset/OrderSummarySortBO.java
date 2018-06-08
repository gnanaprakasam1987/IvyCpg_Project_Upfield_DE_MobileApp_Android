package com.ivy.sd.png.bo.asset;

import java.util.Comparator;

/**
 * Created by anandasir on 8/6/18.
 */

public class OrderSummarySortBO {

    private String levelID;
    private String SKUID;

    public String getLevelID() {
        return levelID;
    }

    public void setLevelID(String levelID) {
        this.levelID = levelID;
    }

    public String getSKUID() {
        return SKUID;
    }

    public void setSKUID(String SKUID) {
        this.SKUID = SKUID;
    }

    public static Comparator<OrderSummarySortBO> COMPARE_BY_ID = new Comparator<OrderSummarySortBO>() {
        public int compare(OrderSummarySortBO one, OrderSummarySortBO two) {
            String levelIDOne = one.getLevelID();
            String levelIDTwo = two.getLevelID();
            int sComp = levelIDOne.compareTo(levelIDTwo);
            if (sComp != 0) {
                return sComp;
            }
            String skuIDOne = one.getSKUID();
            String skuIDTwo = two.getSKUID();
            return skuIDOne.compareTo(skuIDTwo);
        }
    };
}
