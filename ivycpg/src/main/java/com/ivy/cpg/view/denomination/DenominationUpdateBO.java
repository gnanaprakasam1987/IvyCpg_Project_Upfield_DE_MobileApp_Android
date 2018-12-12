package com.ivy.cpg.view.denomination;

/**
 * Created by murugan on 3/9/18.
 */

public class DenominationUpdateBO {

    private String uid;
    private String value;
    private String count;
    private String lineAmount;
    private int isCoin;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(String lineAmount) {
        this.lineAmount = lineAmount;
    }

    public int getIsCoin() {
        return isCoin;
    }

    public void setIsCoin(int isCoin) {
        this.isCoin = isCoin;
    }
}
