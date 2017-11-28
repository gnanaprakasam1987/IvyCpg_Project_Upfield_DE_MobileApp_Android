package com.ivy.sd.png.bo;

/**
 * Created by dharmapriya.k on 11/10/2017,12:53 PM.
 */
public class CompetitorFilterLevelBO {
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    private String productId;
    private String productName;
    private String levelName;
}
