package com.ivy.cpg.view.serializedAsset;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SerializedAssetBO implements Parcelable {
    private int assetID;
    private int Target;
    private int audit = 2;
    private int competitorQty;
    private int executorQty;
    private String reason1ID = "0";
    private String reasonDesc;
    private String conditionID = "0";
    private String mInstallDate;
    private String mLastInstallDate = "";
    private String mServiceDate;
    private String mPOSM;
    private String mBrand;
    private String mNewInstallDate;
    private String mSNO, mReasonId = "0", mRemarks = "", mToRetailerId;
    private String mPOSMName;
    private String mFlag;
    private String mSBDId;
    private String groupLevelName;
    private String mNFCTagId;
    private int groupLevelId;
    private int availQty, scanComplete = 0;
    private String imageName = "";
    private String AssetName;
    private String SerialNo = "0";
    private String newSerialNo = "0";
    //private int mProductId;
    private boolean isSelectedToRemove;
    private int targetLocId;
    private String locationName;
    private ArrayList<String> imageList;
    private int capacity;
    private String vendorId;
    private String vendorName;
    private String modelId;
    private String modelName;
    private String assetType;
    private String assetTypeId;
    private String assetBarCodeId;
    private String assetBarCodeReason;
    private String deliveryDate;
    private int sihQty = 0;
    private double assetPrice;
    private String assetImage;
    private String requestedDate;
    private String approvalStatus;
    private boolean isChecked;
    private String transferType;
    private double rentalPrice;
    private String effectiveToDate;
    private String effectiveFromDate;


    protected SerializedAssetBO(Parcel in) {
        assetID = in.readInt();
        Target = in.readInt();
        audit = in.readInt();
        competitorQty = in.readInt();
        executorQty = in.readInt();
        reason1ID = in.readString();
        reasonDesc = in.readString();
        conditionID = in.readString();
        mInstallDate = in.readString();
        mLastInstallDate = in.readString();
        mServiceDate = in.readString();
        mPOSM = in.readString();
        mBrand = in.readString();
        mNewInstallDate = in.readString();
        mSNO = in.readString();
        mReasonId = in.readString();
        mRemarks = in.readString();
        mToRetailerId = in.readString();
        mPOSMName = in.readString();
        mFlag = in.readString();
        mSBDId = in.readString();
        groupLevelName = in.readString();
        mNFCTagId = in.readString();
        groupLevelId = in.readInt();
        availQty = in.readInt();
        scanComplete = in.readInt();
        imageName = in.readString();
        AssetName = in.readString();
        SerialNo = in.readString();
        newSerialNo = in.readString();
        isSelectedToRemove = in.readByte() != 0;
        targetLocId = in.readInt();
        locationName = in.readString();
        imageList = in.createStringArrayList();
        capacity = in.readInt();
        vendorId = in.readString();
        vendorName = in.readString();
        modelId = in.readString();
        modelName = in.readString();
        assetType = in.readString();
        assetTypeId = in.readString();
        assetBarCodeId = in.readString();
        assetBarCodeReason = in.readString();
        deliveryDate = in.readString();
        sihQty = in.readInt();
        assetPrice = in.readDouble();
        assetImage = in.readString();
        requestedDate = in.readString();
        approvalStatus = in.readString();
        isChecked = in.readByte() != 0;
        flag = in.readInt();
        referenceId = in.readString();
        isPromo = in.readString();
        isDisplay = in.readString();
        reasonID = in.readInt();
        locationID = in.readInt();
        actual = in.readInt();
        imgName = in.readString();
        parentHierarchy = in.readString();
        isSelectedReason = in.readByte() != 0;
        transferType = in.readString();
        rentalPrice = in.readDouble();
        effectiveFromDate = in.readString();
        effectiveToDate = in.readString();
    }

    public static final Creator<SerializedAssetBO> CREATOR = new Creator<SerializedAssetBO>() {
        @Override
        public SerializedAssetBO createFromParcel(Parcel in) {
            return new SerializedAssetBO(in);
        }

        @Override
        public SerializedAssetBO[] newArray(int size) {
            return new SerializedAssetBO[size];
        }
    };

    public String getRField() {
        return RField;
    }

    public void setRField(String RField) {
        this.RField = RField;
    }

    private String RField;

    public String getAssetServiceReqStatus() {
        return assetServiceReqStatus;
    }

    public void setAssetServiceReqStatus(String assetServiceReqStatus) {
        this.assetServiceReqStatus = assetServiceReqStatus;
    }

    private String assetServiceReqStatus="Pending";

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public int getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(int serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    private int serviceProviderId;
    private String serviceProvider;

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    private String issueDescription;

    public String getAssetBarCodeId() {
        return assetBarCodeId;
    }

    public void setAssetBarCodeId(String assetBarCodeId) {
        this.assetBarCodeId = assetBarCodeId;
    }

    public String getAssetBarCodeReason() {
        return assetBarCodeReason;
    }

    public void setAssetBarCodeReason(String assetBarCodeReason) {
        this.assetBarCodeReason = assetBarCodeReason;
    }

    private int flag = 0;


    private String referenceId = "0";


    public SerializedAssetBO() {

    }

    public SerializedAssetBO(int flag) {
        this.flag = flag;
    }

    public SerializedAssetBO(SerializedAssetBO serializedAssetBO) {
        this.assetID = serializedAssetBO.getAssetID();
        this.Target = serializedAssetBO.getTarget();
        this.AssetName = serializedAssetBO.getAssetName();
        this.audit = serializedAssetBO.getAudit();
        this.availQty = serializedAssetBO.getAvailQty();
        this.scanComplete = serializedAssetBO.getScanComplete();
        this.competitorQty = serializedAssetBO.getCompetitorQty();
        this.conditionID = serializedAssetBO.getConditionID();
        this.imageName = serializedAssetBO.getImageName();
        this.imgName = serializedAssetBO.getImgName();
        this.mBrand = serializedAssetBO.getBrand();
        this.mInstallDate = serializedAssetBO.getInstallDate();
        this.mNewInstallDate = serializedAssetBO.getNewInstallDate();
        this.mPOSM = serializedAssetBO.getPOSM();
        this.mSBDId = serializedAssetBO.getSBDId();
        this.mServiceDate = serializedAssetBO.getServiceDate();
        this.mPOSMName = serializedAssetBO.getPOSMName();
        this.mFlag = serializedAssetBO.getFlag();
        //this.mProductId = serializedAssetBO.getProductId();
        this.mSNO = serializedAssetBO.getSNO();
        this.reason1ID = serializedAssetBO.getReason1ID();
        this.reasonDesc = serializedAssetBO.getReasonDesc();
        this.groupLevelId = serializedAssetBO.getGroupLevelId();
        this.groupLevelName = serializedAssetBO.getGroupLevelName();
        this.executorQty = serializedAssetBO.getExecutorQty();
        this.SerialNo = serializedAssetBO.getSerialNo();
        this.mNFCTagId = serializedAssetBO.getNFCTagId();
        this.targetLocId = serializedAssetBO.getTargetLocId();
        this.locationName = serializedAssetBO.getLocationName();
        this.imageList = serializedAssetBO.getImageList();
        this.isSelectedReason = serializedAssetBO.isSelectedReason();
        this.capacity = serializedAssetBO.getCapacity();
        this.vendorId = serializedAssetBO.getVendorId();
        this.vendorName = serializedAssetBO.getVendorName();
        this.modelId = serializedAssetBO.getModelId();
        this.modelName = serializedAssetBO.getModelName();
        this.assetType = serializedAssetBO.getAssetType();
        this.assetTypeId = serializedAssetBO.getAssetTypeId();
        this.assetBarCodeId = serializedAssetBO.getAssetBarCodeId();
        this.assetBarCodeReason = serializedAssetBO.getAssetBarCodeReason();
        this.mLastInstallDate = serializedAssetBO.getmLastInstallDate();
        this.deliveryDate = serializedAssetBO.getDeliveryDate();
        this.sihQty = serializedAssetBO.getSihQty();
        this.assetPrice = serializedAssetBO.getAssetPrice();
        this.assetImage = serializedAssetBO.getAssetImage();
        this.newSerialNo = serializedAssetBO.getNewSerialNo();
        this.requestedDate = serializedAssetBO.getRequestedDate();
        this.approvalStatus = serializedAssetBO.getApprovalStatus();
        this.isChecked = serializedAssetBO.isChecked();
        this.rentalPrice = serializedAssetBO.getRentalPrice();
        this.effectiveFromDate = serializedAssetBO.getEffectiveFromDate();
        this.effectiveToDate = serializedAssetBO.getEffectiveToDate();
    }

    public int getExecutorQty() {
        return executorQty;
    }

    public void setExecutorQty(int executorQty) {
        this.executorQty = executorQty;
    }

    public int getCompetitorQty() {
        return competitorQty;
    }

    public void setCompetitorQty(int competitorQty) {
        this.competitorQty = competitorQty;
    }

    public String getNFCTagId() {
        return mNFCTagId;
    }

    public void setNFCTagId(String mNFCTagId) {
        this.mNFCTagId = mNFCTagId;
    }

    public int getGroupLevelId() {
        return groupLevelId;
    }

    public void setGroupLevelId(int groupLevelId) {
        this.groupLevelId = groupLevelId;
    }

    public String getGroupLevelName() {
        return groupLevelName;
    }

    public void setGroupLevelName(String groupLevelName) {
        this.groupLevelName = groupLevelName;
    }

    public String getSBDId() {
        return mSBDId;
    }

    public void setSBDId(String mSBDId) {
        this.mSBDId = mSBDId;
    }

    public String getPOSMName() {
        return mPOSMName;
    }

    public void setPOSMName(String mPOSMName) {
        this.mPOSMName = mPOSMName;
    }

    public String getFlag() {
        return mFlag;
    }

    public void setFlag(String mFlag) {
        this.mFlag = mFlag;
    }

    public String getConditionID() {
        return conditionID;
    }

    public void setConditionID(String conditionID) {
        this.conditionID = conditionID;
    }

    public String getPOSM() {
        return mPOSM;
    }

    public void setPOSM(String mPOSM) {
        this.mPOSM = mPOSM;
    }

    public void setBrand(String mBrand) {
        this.mBrand = mBrand;
    }

    public String getBrand() {
        return mBrand;
    }

    public String getNewInstallDate() {
        return mNewInstallDate;
    }

    public void setNewInstallDate(String mInstallDate) {
        this.mNewInstallDate = mInstallDate;
    }

    public String getSNO() {
        return mSNO;
    }

    public void setSNO(String mSNO) {
        this.mSNO = mSNO;
    }

    public String getReasonId() {
        return mReasonId;
    }

    public void setReasonId(String mReasonId) {
        this.mReasonId = mReasonId;
    }

    public String getRemarks() {
        return mRemarks;
    }

    public void setRemarks(String mRemarks) {
        this.mRemarks = mRemarks;
    }

    public String getToRetailerId() {
        return mToRetailerId;
    }

    public void setToRetailerId(String mToRetailerId) {
        this.mToRetailerId = mToRetailerId;
    }

    public String getInstallDate() {
        return mInstallDate;
    }

    public void setInstallDate(String mInstallDate) {
        this.mInstallDate = mInstallDate;
    }

    public String getServiceDate() {
        return mServiceDate;
    }

    public void setServiceDate(String mServiceDate) {
        this.mServiceDate = mServiceDate;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public String getReason1ID() {
        return reason1ID;
    }

    public void setReason1ID(String reason1id) {
        reason1ID = reason1id;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    public int getTarget() {
        return Target;
    }

    public void setTarget(int target) {
        Target = target;
    }

    public String getAssetName() {
        return AssetName;
    }

    public void setAssetName(String assetName) {
        AssetName = assetName;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public int getAvailQty() {
        return availQty;
    }

    public void setAvailQty(int availQty) {
        this.availQty = availQty;
    }

    public int getScanComplete() {
        return scanComplete;
    }

    public void setScanComplete(int scanComplete) {
        this.scanComplete = scanComplete;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getAudit() {
        return audit;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

//    public int getProductId() {
//        return mProductId;
//    }
//
//    public void setProductId(int mProductId) {
//        this.mProductId = mProductId;
//    }

    public boolean isSelectedToRemove() {
        return isSelectedToRemove;
    }

    public void setSelectedToRemove(boolean selectedToRemove) {
        isSelectedToRemove = selectedToRemove;
    }

    //SOD Asset Columns
    private String isPromo = "N", isDisplay = "N";
    private int reasonID, locationID, actual;

    public String getIsPromo() {
        return isPromo;
    }

    public void setIsPromo(String isPromo) {
        this.isPromo = isPromo;
    }

    public String getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(String isDisplay) {
        this.isDisplay = isDisplay;
    }

    public int getActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    public int getReasonID() {
        return reasonID;
    }

    public void setReasonID(int reasonID) {
        this.reasonID = reasonID;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    private String imgName = "";

    public int getTargetLocId() {
        return targetLocId;
    }

    public void setTargetLocId(int targetLocId) {
        this.targetLocId = targetLocId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public ArrayList<String> getImageList() {
        if (imageList == null)
            return new ArrayList<String>();
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    private String parentHierarchy;

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    public boolean isSelectedReason() {
        return isSelectedReason;
    }

    public void setSelectedReason(boolean selectedReason) {
        isSelectedReason = selectedReason;
    }

    private boolean isSelectedReason = false;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetTypeId() {
        return assetTypeId;
    }

    public void setAssetTypeId(String assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    public String getmLastInstallDate() {
        return mLastInstallDate;
    }

    public void setmLastInstallDate(String mLastInstallDate) {
        this.mLastInstallDate = mLastInstallDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }


    public String getmInstallDate() {
        return mInstallDate;
    }

    public void setmInstallDate(String mInstallDate) {
        this.mInstallDate = mInstallDate;
    }

    public int getSihQty() {
        return sihQty;
    }

    public void setSihQty(int sihQty) {
        this.sihQty = sihQty;
    }

    public double getAssetPrice() {
        return assetPrice;
    }

    public void setAssetPrice(double assetPrice) {
        this.assetPrice = assetPrice;
    }

    public String getAssetImage() {
        return assetImage;
    }

    public void setAssetImage(String assetImage) {
        this.assetImage = assetImage;
    }

    public String getNewSerialNo() {
        return newSerialNo;
    }

    public void setNewSerialNo(String newSerialNo) {
        this.newSerialNo = newSerialNo;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public String getEffectiveToDate() {
        return effectiveToDate;
    }

    public void setEffectiveToDate(String effectiveToDate) {
        this.effectiveToDate = effectiveToDate;
    }

    public String getEffectiveFromDate() {
        return effectiveFromDate;
    }

    public void setEffectiveFromDate(String effectiveFromDate) {
        this.effectiveFromDate = effectiveFromDate;
    }

    @Override
    public String toString() {
        if (flag == 1)
            return vendorName;
        else if (flag == 2)
            return modelName;
        else if (flag == 3)
            return assetType;
        else if (flag == 4)
            return assetBarCodeReason;
        else
            return AssetName;
    }

    protected SerializedAssetBO(Parcel in) {
        assetID = in.readInt();
        AssetName = in.readString();
        mNewInstallDate = in.readString();
        reasonDesc = in.readString();
        SerialNo = in.readString();
        issueDescription = in.readString();
        imageName = in.readString();
        mServiceDate = in.readString();
        reasonID=in.readInt();
        serviceRequestedRetailer = in.readString();
        RField=in.readString();
        serviceProviderId = in.readInt();
        mRemarks=in.readString();
        status=in.readString();

    }

    public static final Creator<SerializedAssetBO> CREATOR = new Creator<SerializedAssetBO>() {
        @Override
        public SerializedAssetBO createFromParcel(Parcel in) {
            return new SerializedAssetBO(in);
        }

        @Override
        public SerializedAssetBO[] newArray(int size) {
            return new SerializedAssetBO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(assetID);
        parcel.writeString(AssetName);
        parcel.writeString(mNewInstallDate);
        parcel.writeString(reasonDesc);
        parcel.writeString(SerialNo);
        parcel.writeString(issueDescription);
        parcel.writeString(imageName);
        parcel.writeString(mServiceDate);
        parcel.writeInt(reasonID);
        parcel.writeString(serviceRequestedRetailer);
        parcel.writeString(RField);
        parcel.writeInt(serviceProviderId);
        parcel.writeString(mRemarks);
        parcel.writeString(status);
    }

    public String getServiceRequestedRetailer() {
        return serviceRequestedRetailer;
    }

    public void setServiceRequestedRetailer(String serviceRequestedRetailer) {
        this.serviceRequestedRetailer = serviceRequestedRetailer;
    }

    private String serviceRequestedRetailer;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(assetID);
        dest.writeInt(Target);
        dest.writeInt(audit);
        dest.writeInt(competitorQty);
        dest.writeInt(executorQty);
        dest.writeString(reason1ID);
        dest.writeString(reasonDesc);
        dest.writeString(conditionID);
        dest.writeString(mInstallDate);
        dest.writeString(mLastInstallDate);
        dest.writeString(mServiceDate);
        dest.writeString(mPOSM);
        dest.writeString(mBrand);
        dest.writeString(mNewInstallDate);
        dest.writeString(mSNO);
        dest.writeString(mReasonId);
        dest.writeString(mRemarks);
        dest.writeString(mToRetailerId);
        dest.writeString(mPOSMName);
        dest.writeString(mFlag);
        dest.writeString(mSBDId);
        dest.writeString(groupLevelName);
        dest.writeString(mNFCTagId);
        dest.writeInt(groupLevelId);
        dest.writeInt(availQty);
        dest.writeInt(scanComplete);
        dest.writeString(imageName);
        dest.writeString(AssetName);
        dest.writeString(SerialNo);
        dest.writeString(newSerialNo);
        dest.writeByte((byte) (isSelectedToRemove ? 1 : 0));
        dest.writeInt(targetLocId);
        dest.writeString(locationName);
        dest.writeStringList(imageList);
        dest.writeInt(capacity);
        dest.writeString(vendorId);
        dest.writeString(vendorName);
        dest.writeString(modelId);
        dest.writeString(modelName);
        dest.writeString(assetType);
        dest.writeString(assetTypeId);
        dest.writeString(assetBarCodeId);
        dest.writeString(assetBarCodeReason);
        dest.writeString(deliveryDate);
        dest.writeInt(sihQty);
        dest.writeDouble(assetPrice);
        dest.writeString(assetImage);
        dest.writeString(requestedDate);
        dest.writeString(approvalStatus);
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeInt(flag);
        dest.writeString(referenceId);
        dest.writeString(isPromo);
        dest.writeString(isDisplay);
        dest.writeInt(reasonID);
        dest.writeInt(locationID);
        dest.writeInt(actual);
        dest.writeString(imgName);
        dest.writeString(parentHierarchy);
        dest.writeByte((byte) (isSelectedReason ? 1 : 0));
        dest.writeString(transferType);
        dest.writeDouble(rentalPrice);
        dest.writeString(effectiveFromDate);
        dest.writeString(effectiveToDate);
    }
}
