package com.ivy.sd.png.bo;

import com.ivy.cpg.view.promotion.PromotionBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;



import java.util.ArrayList;

public class StandardListBO {

    private String ListID;
    private String listCode;
    private String listName;
    private boolean checked;
    private ArrayList<PromotionBO> promotionTrackingList;
    private ArrayList<AssetTrackingBO> assetTrackingList, allassetTrackingList;
    private String status;

    private String userChildName;
    private int userChildId;

    public StandardListBO(StandardListBO item) {
        this.ListID = item.ListID;
        this.listCode = item.listCode;
        this.listName = item.listName;
    }

    public StandardListBO(String ListID, String listName) {
        this.ListID = ListID;
        this.listName = listName;
    }

    public StandardListBO() {

    }

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

    public ArrayList<AssetTrackingBO> getAllAssetTrackingList() {
        return allassetTrackingList;
    }

    public void setAllAssetTrackingList(ArrayList<AssetTrackingBO> allassetTrackingList) {
        this.allassetTrackingList = allassetTrackingList;
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

    public void setStatus( String status) {
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
