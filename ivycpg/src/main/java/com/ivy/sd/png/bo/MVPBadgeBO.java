package com.ivy.sd.png.bo;

import android.graphics.Bitmap;

import java.net.URL;

public class MVPBadgeBO{

    private int badgeID;
    private String badgeName;
    private String badgeURL;
    private String badgeCount="0";
    private Bitmap badgeBitmap;
    private String imageName;
    private String mTimeStamp;
    private URL imageUrl;

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getBadgeID() {
        return badgeID;
    }

    public void setBadgeID(int badgeID) {
        this.badgeID = badgeID;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getBadgeURL() {
        return badgeURL;
    }

    public void setBadgeURL(String badgeURL) {
        this.badgeURL = badgeURL;
    }

    public String getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(String badgeCount) {
        this.badgeCount = badgeCount;
    }

    public Bitmap getBadgeBitmap() {
        return badgeBitmap;
    }

    public void setBadgeBitmap(Bitmap badgeBitmap) {
        this.badgeBitmap = badgeBitmap;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }
}

