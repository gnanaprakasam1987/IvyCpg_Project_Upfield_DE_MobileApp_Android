package com.ivy.sd.png.bo;

/**
 * Created by anandasir.v on 9/27/2017.
 */

public class WeightageBO {

    private int HeaderID, ProductID, FromNorm, Score;

    public int getHeaderID() {
        return HeaderID;
    }

    public void setHeaderID(int headerID) {
        HeaderID = headerID;
    }

    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    public int getFromNorm() {
        return FromNorm;
    }

    public void setFromNorm(int fromNorm) {
        FromNorm = fromNorm;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }
}
