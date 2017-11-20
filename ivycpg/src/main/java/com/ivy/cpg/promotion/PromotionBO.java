package com.ivy.cpg.promotion;

public class PromotionBO {

	private int promoId;
	private int productId;
	private String promoName;
	private String imageName = "";
	private String ReasonID = "0";
	private String ReasonDesc;
	private String Flag = "S";

	private String imagePath="";
	private int isExecuted = 0;
	private int HasAnnouncer = 0;
	private String groupName;
	private String ratingId = "0";
	private String ratingDec;
	private int promoQty;
	private  int MappingId;

	public PromotionBO(){

	}

	public PromotionBO(PromotionBO promotionBO){
		this.promoId=promotionBO.getPromoId();
		this.productId=promotionBO.getProductId();
		this.promoName=promotionBO.getPromoName();
		this.imageName=promotionBO.getImageName();
		this.ReasonID=promotionBO.getReasonID();
		this.ReasonDesc=promotionBO.getReasonDesc();
		this.Flag=promotionBO.getFlag();
		this.isExecuted=promotionBO.getIsExecuted();
		this.HasAnnouncer = promotionBO.getHasAnnouncer();
		this.ratingDec=promotionBO.getRatingDec();
		this.ratingId=promotionBO.getRatingId();
		this.groupName=promotionBO.getGroupName();
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}


	public int getPromoQty() {
		return promoQty;
	}

	public void setPromoQty(int promoQty) {
		this.promoQty = promoQty;
	}

	private String getRatingDec() {
		return ratingDec;
	}

	public void setRatingDec(String ratingDec) {
		this.ratingDec = ratingDec;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRatingId() {
		return ratingId;
	}

	public void setRatingId(String ratingId) {
		this.ratingId = ratingId;
	}

	public int getPromoId() {
		return promoId;
	}

	public void setPromoId(int promoId) {
		this.promoId = promoId;
	}

	public String getPromoName() {
		return promoName;
	}

	public void setPromoName(String promoName) {
		this.promoName = promoName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public int getIsExecuted() {
		return isExecuted;
	}


	public void setIsExecuted(int isExecuted) {
		this.isExecuted = isExecuted;
	}

	public int getHasAnnouncer() {
		return HasAnnouncer;
	}

	public void setHasAnnouncer(int hasAnnouncer) {
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

	public int getMappingId() {
		return MappingId;
	}

	public void setMappingId(int mappingId) {
		MappingId = mappingId;
	}
	
}
