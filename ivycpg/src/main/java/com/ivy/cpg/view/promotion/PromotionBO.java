package com.ivy.cpg.view.promotion;

import java.util.ArrayList;

public class PromotionBO {

    private int promoId;
    private int productId;
    private String promoName;
    private String imageName = "";
    private String ReasonID = "0";
    private String ReasonDesc;
    private String Flag = "S";

    private String imagePath = "";
    private int isExecuted = 0;
    private int HasAnnouncer = 0;
    private String groupName;
    private String ratingId = "0";
    private String ratingDec;
    private int promoQty;
    private int MappingId;
    private String parentHierarchy;
    private String remarks;
    private String productPrice;
    private boolean isAccepted;
    private String promotionGroupName;
    private ArrayList<PromotionAttachmentBO> promotionAttchmentList;

    private boolean isAllowedtoExecute;

    PromotionBO() {

    }

    PromotionBO(PromotionBO promotionBO) {
        this.promoId = promotionBO.getPromoId();
        this.productId = promotionBO.getProductId();
        this.promoName = promotionBO.getPromoName();
        this.imageName = promotionBO.getImageName();
        this.ReasonID = promotionBO.getReasonID();
        this.ReasonDesc = promotionBO.getReasonDesc();
        this.Flag = promotionBO.getFlag();
        this.isExecuted = promotionBO.getIsExecuted();
        this.HasAnnouncer = promotionBO.getHasAnnouncer();
        this.ratingDec = promotionBO.getRatingDec();
        this.ratingId = promotionBO.getRatingId();
        this.groupName = promotionBO.getGroupName();
        this.pName = promotionBO.getpName();
        this.fromDate = promotionBO.getFromDate();
        this.toDate = promotionBO.getToDate();
        this.parentHierarchy = promotionBO.getParentHierarchy();
        this.remarks = promotionBO.getRemarks();
        this.productPrice = promotionBO.getProductPrice();
        this.isAccepted = promotionBO.isAccepted();
        this.promotionGroupName = promotionBO.getPromotionGroupName();
        this.promotionAttchmentList = promotionBO.getPromotionAttchmentList();
        this.isAllowedtoExecute = promotionBO.isAllowedtoExecute();
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    int getPromoQty() {
        return promoQty;
    }

    void setPromoQty(int promoQty) {
        this.promoQty = promoQty;
    }

    private String getRatingDec() {
        return ratingDec;
    }

    void setRatingDec(String ratingDec) {
        this.ratingDec = ratingDec;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    String getRatingId() {
        return ratingId;
    }

    void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    int getPromoId() {
        return promoId;
    }

    void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    String getPromoName() {
        return promoName;
    }

    void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    int getIsExecuted() {
        return isExecuted;
    }


    void setIsExecuted(int isExecuted) {
        this.isExecuted = isExecuted;
    }

    int getHasAnnouncer() {
        return HasAnnouncer;
    }

    void setHasAnnouncer(int hasAnnouncer) {
        HasAnnouncer = hasAnnouncer;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }


    public String getReasonID() {
        return ReasonID;
    }

    public void setReasonID(String reasonID) {
        ReasonID = reasonID;
    }

    private String getReasonDesc() {
        return ReasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        ReasonDesc = reasonDesc;
    }

    @Override
    public String toString() {
        return promoName;
    }

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String flag) {
        Flag = flag;
    }

    int getMappingId() {
        return MappingId;
    }

    void setMappingId(int mappingId) {
        MappingId = mappingId;
    }

    private String fromDate, toDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    private String pName;

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public String getPromotionGroupName() {
        return promotionGroupName;
    }

    public void setPromotionGroupName(String promotionGroupName) {
        this.promotionGroupName = promotionGroupName;
    }

    public ArrayList<PromotionAttachmentBO> getPromotionAttchmentList() {
        return promotionAttchmentList;
    }

    public void setPromotionAttchmentList(ArrayList<PromotionAttachmentBO> promotionAttchmentList) {
        this.promotionAttchmentList = promotionAttchmentList;
    }

    public boolean isAllowedtoExecute() {
        return isAllowedtoExecute;
    }

    public void setAllowedtoExecute(boolean allowedtoExecute) {
        isAllowedtoExecute = allowedtoExecute;
    }
}
