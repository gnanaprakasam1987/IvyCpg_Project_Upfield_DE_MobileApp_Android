package com.ivy.cpg.view.sync.largefiledownload;

import android.os.Parcel;
import android.os.Parcelable;

public class DigitalContentModel implements Parcelable {

    private int productID, imageID,percent,downloadId,userId;
    private String fileName,description,imgUrl,downloadDetail,contentFrom,status,fileSize,signedUrl;
    private boolean isDownloaded;

    public DigitalContentModel(){

    }

    DigitalContentModel(Parcel in) {
        productID = in.readInt();
        imageID = in.readInt();
        percent = in.readInt();
        downloadId = in.readInt();
        userId = in.readInt();
        fileName = in.readString();
        description = in.readString();
        imgUrl = in.readString();
        downloadDetail = in.readString();
        contentFrom = in.readString();
        status = in.readString();
        fileSize = in.readString();
        signedUrl = in.readString();
        isDownloaded = in.readByte() != 0;
    }

    public static final Creator<DigitalContentModel> CREATOR = new Creator<DigitalContentModel>() {
        @Override
        public DigitalContentModel createFromParcel(Parcel in) {
            return new DigitalContentModel(in);
        }

        @Override
        public DigitalContentModel[] newArray(int size) {
            return new DigitalContentModel[size];
        }
    };

    public String getSignedUrl() {
        return signedUrl;
    }

    public void setSignedUrl(String signedUrl) {
        this.signedUrl = signedUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDownloadDetail() {
        return downloadDetail;
    }

    public void setDownloadDetail(String downloadDetail) {
        this.downloadDetail = downloadDetail;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getContentFrom() {
        return contentFrom;
    }

    public void setContentFrom(String contentFrom) {
        this.contentFrom = contentFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(productID);
        dest.writeInt(imageID);
        dest.writeInt(percent);
        dest.writeInt(downloadId);
        dest.writeInt(userId);
        dest.writeString(fileName);
        dest.writeString(description);
        dest.writeString(imgUrl);
        dest.writeString(downloadDetail);
        dest.writeString(contentFrom);
        dest.writeString(status);
        dest.writeString(fileSize);
        dest.writeString(signedUrl);
        dest.writeByte((byte) (isDownloaded ? 1 : 0));
    }
}
