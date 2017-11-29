package com.ivy.cpg.view.sf;

import com.ivy.sd.png.bo.ShelfShareBO;

import java.util.HashMap;

/**
 * Created by ivyuser on 29/11/17.
 */

public class SFLocationBO {

    int audit = 2;
    private String actual = "0", parentTotal = "0";
    private String gap = "0", target = "0", percentage = "0";
    private String imageName = "";
    private int reasonId;
    private int locationId;
    private String locationName;
    private String imgName = "";
    private String remarks = "";

    private HashMap<String, HashMap<String, Object>> mShelfDetailForSOS = null;
    private HashMap<String, HashMap<String, ShelfShareBO>> mShelfBlockDetailForSOS = null;
    private HashMap<String, HashMap<String, Object>> mShelfDetailForSOD = null;
    private HashMap<String, HashMap<String, ShelfShareBO>> mShelfBlockDetailForSOD = null;

    public SFLocationBO(SFLocationBO locObj) {
        this.locationId = locObj.locationId;
        mShelfDetailForSOS = new HashMap<>();
        mShelfBlockDetailForSOS = new HashMap<>();
        mShelfDetailForSOD = new HashMap<>();
        mShelfBlockDetailForSOD = new HashMap<>();

    }

    public SFLocationBO() {

    }


    public int getAudit() {
        return audit;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getParentTotal() {
        return parentTotal;
    }

    public void setParentTotal(String parentTotal) {
        this.parentTotal = parentTotal;
    }

    public String getGap() {
        return gap;
    }

    public void setGap(String gap) {
        this.gap = gap;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setShelfDetailForSOS(String key,
                                     HashMap<String, Object> shelfDetailForSOS) {
        this.mShelfDetailForSOS.put(key, shelfDetailForSOS);
    }

    public HashMap<String, HashMap<String, Object>> getShelfDetailForSOS() {
        return this.mShelfDetailForSOS;
    }

    public boolean containsKeySOS(String key) {
        return this.mShelfDetailForSOS.containsKey(key);
    }

    public HashMap<String, HashMap<String, ShelfShareBO>> getmShelfBlockDetailForSOS() {
        return mShelfBlockDetailForSOS;
    }

    public void setmShelfBlockDetailForSOS(String key,
                                           HashMap<String, ShelfShareBO> mShelfBlockDetailForSOS) {
        this.mShelfBlockDetailForSOS.put(key, mShelfBlockDetailForSOS);
    }

    public void setShelfDetailForSOD(String key,
                                     HashMap<String, Object> shelfDetailForSOD) {
        this.mShelfDetailForSOD.put(key, shelfDetailForSOD);
    }

    public HashMap<String, HashMap<String, Object>> getShelfDetailForSOD() {
        return this.mShelfDetailForSOD;
    }


    public HashMap<String, HashMap<String, ShelfShareBO>> getmShelfBlockDetailForSOD() {
        return mShelfBlockDetailForSOD;
    }

    public void setmShelfBlockDetailForSOD(String key,
                                           HashMap<String, ShelfShareBO> mShelfBlockDetailForSOD) {
        this.mShelfBlockDetailForSOD.put(key, mShelfBlockDetailForSOD);
    }


    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
