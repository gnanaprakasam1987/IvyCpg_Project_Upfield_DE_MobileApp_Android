package com.ivy.cpg.view.mvp;

import android.graphics.Bitmap;

import java.net.URL;

class MVPBadgeBO {

    private int badgeID;
    private String badgeName;
    private String badgeURL;
    private String badgeCount = "0";
    private Bitmap badgeBitmap;
    private String imageName;
    private String mTimeStamp;
    private URL imageUrl;

    URL getImageUrl() {
        return imageUrl;
    }

    void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    int getBadgeID() {
        return badgeID;
    }

    void setBadgeID(int badgeID) {
        this.badgeID = badgeID;
    }

    String getBadgeName() {
        return badgeName;
    }

    void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    String getBadgeURL() {
        return badgeURL;
    }

    void setBadgeURL(String badgeURL) {
        this.badgeURL = badgeURL;
    }

    String getBadgeCount() {
        return badgeCount;
    }

    void setBadgeCount(String badgeCount) {
        this.badgeCount = badgeCount;
    }

    Bitmap getBadgeBitmap() {
        return badgeBitmap;
    }

    void setBadgeBitmap(Bitmap badgeBitmap) {
        this.badgeBitmap = badgeBitmap;
    }

    String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    String getTimeStamp() {
        return mTimeStamp;
    }

    void setTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }
}

