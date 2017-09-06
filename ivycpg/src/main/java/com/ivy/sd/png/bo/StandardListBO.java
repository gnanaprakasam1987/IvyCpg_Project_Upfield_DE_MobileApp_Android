package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class StandardListBO {

    private String ListID;
    private String listCode;
    private String listName;
    private boolean checked;
    private ArrayList<PromotionBO> promotionTrackingList;
    private ArrayList<AssetTrackingBO> assetTrackingList;
    private String status;

    private String userChildName;
    private int userChildId;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public ArrayList<PromotionBO> getPromotionTrackingList() {
        return promotionTrackingList;
    }

    public void setPromotionTrackingList(ArrayList<PromotionBO> promotionTrackingList) {
        this.promotionTrackingList = promotionTrackingList;
    }

    public ArrayList<AssetTrackingBO> getAssetTrackingList() {
        return assetTrackingList;
    }

    public void setAssetTrackingList(ArrayList<AssetTrackingBO> assetTrackingList) {
        this.assetTrackingList = assetTrackingList;
    }

    public String getListID() {
        return ListID;
    }

    public void setListID(String listID) {
        ListID = listID;
    }

    public String getListCode() {
        return listCode;
    }

    public void setListCode(String listCode) {
        this.listCode = listCode;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    @Override
    public String toString() {
        return listName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChildUserName() {
        return userChildName;
    }

    public void setChildUserName(String userChildName) {
        this.userChildName = userChildName;
    }

    public int getChildUserId() {
        return userChildId;
    }

    public void setChildUserId(int userChildId) {
        this.userChildId = userChildId;
    }
}
