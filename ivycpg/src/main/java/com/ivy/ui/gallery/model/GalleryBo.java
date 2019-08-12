package com.ivy.ui.gallery.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GalleryBo implements Parcelable {
    private String filePath;
    private String imageName;
    private String source;
    private String date;
    private String name;
    private String retailerName;

    public GalleryBo() {
    }

    protected GalleryBo(Parcel in) {
        filePath = in.readString();
        imageName = in.readString();
        source = in.readString();
        date = in.readString();
        name = in.readString();
        retailerName = in.readString();
    }

    public static final Creator<GalleryBo> CREATOR = new Creator<GalleryBo>() {
        @Override
        public GalleryBo createFromParcel(Parcel in) {
            return new GalleryBo(in);
        }

        @Override
        public GalleryBo[] newArray(int size) {
            return new GalleryBo[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(imageName);
        dest.writeString(source);
        dest.writeString(date);
        dest.writeString(name);
        dest.writeString(retailerName);
    }
}
