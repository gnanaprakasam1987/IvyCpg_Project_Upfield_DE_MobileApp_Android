package com.ivy.sd.png.bo;

import com.ivy.countersales.bo.CS_StockReasonBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationBO {

	private int locationId;

	private int mShelfPiece=-1, mShelfCase=-1, mShelfOuter=-1;
	private int mWHPiece;
	private int mWHCase;
	private int mWHOuter;
	private int mwHFacing;
	private int mSIH;
	private int isPouring;
	private int cockTailQty;

	private ArrayList<NearExpiryDateBO> nearexpiryDate;
	private boolean hasData = false;
	private String shelfInventory = "";
	private int LocId;
	private int parentId;
	private String actual = "0", parentTotal = "0";
	private String gap = "0", target = "0", percentage = "0";
	private String imageName = "";
	private ArrayList<AssetTrackingBO> assetTrackingBOArrayList;

	public ArrayList<AssetTrackingBO> getAssetTrackingBOArrayList() {
		return assetTrackingBOArrayList;
	}


	public int getLocId() {
		return LocId;
	}

	public void setLocId(int locId) {
		LocId = locId;
	}

	public String getLocCode() {
		return LocCode;
	}

	public void setLocCode(String locCode) {
		LocCode = locCode;
	}

	public String getLocName() {
		return LocName;
	}

	public void setLocName(String locName) {
		LocName = locName;
	}

	private String LocCode, LocName;

	public LocationBO(LocationBO locObj) {
		this.locationId = locObj.locationId;
		this.mShelfPiece = locObj.mShelfPiece;
		this.mShelfCase = locObj.mShelfCase;
		this.mShelfOuter = locObj.mShelfOuter;
		this.mWHPiece = locObj.mWHPiece;
		this.mWHCase = locObj.mWHCase;
		this.mWHOuter = locObj.mWHOuter;
		mShelfDetailForSOS = new HashMap<String, HashMap<String, Object>>();
		mShelfBlockDetailForSOS = new HashMap<String, HashMap<String, ShelfShareBO>>();
		mShelfDetailForSOD = new HashMap<String, HashMap<String, Object>>();
		mShelfBlockDetailForSOD = new HashMap<String, HashMap<String, ShelfShareBO>>();
		this.fromDate = locObj.getFromDate();
		this.toDate = locObj.getToDate();
		this.productID = locObj.getProductID();
		this.productName = locObj.getProductName();
		this.imagepath = locObj.getImagepath();
		this.photoid=locObj.getPhotoid();
		this.skuname =locObj.getSkuname();
		this.abv = locObj.getAbv();
		this.lotcode = locObj.getLotcode();
		this.seqno = locObj.getSeqno();
		this.feedback = locObj.getFeedback();

		this.isPouring = locObj.getIsPouring();
		this.cockTailQty = locObj.getCockTailQty();
	}

	public LocationBO() {

	}
	public void addAssetList(ArrayList<AssetTrackingBO> assetList){
		this.assetTrackingBOArrayList=assetList;
	}

	public LocationBO(int locid, String LocName) {
		super();
		this.LocId = locid;
		this.LocName = LocName;
	}


	public int getmSIH() {
		return mSIH;
	}

	public void setmSIH(int mSIH) {
		this.mSIH = mSIH;
	}

	public int getFacingQty() {
		return mwHFacing;
	}

	public void setFacingQty(int mwHFacing) {
		this.mwHFacing = mwHFacing;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public int getShelfPiece() {
		return mShelfPiece;
	}

	public void setShelfPiece(int mShelfPiece) {
		this.mShelfPiece = mShelfPiece;
	}

	public int getShelfCase() {
		return mShelfCase;
	}

	public void setShelfCase(int mShelfCase) {
		this.mShelfCase = mShelfCase;
	}

	public int getShelfOuter() {
		return mShelfOuter;
	}

	public void setShelfOuter(int mShelfOuter) {
		this.mShelfOuter = mShelfOuter;
	}

	public int getWHPiece() {
		return mWHPiece;
	}

	public void setWHPiece(int mWHPiece) {
		this.mWHPiece = mWHPiece;
	}

	public int getWHCase() {
		return mWHCase;
	}

	public void setWHCase(int mWHCase) {
		this.mWHCase = mWHCase;
	}

	public int getWHOuter() {
		return mWHOuter;
	}

	public void setWHOuter(int mWHOuter) {
		this.mWHOuter = mWHOuter;
	}

	public ArrayList<NearExpiryDateBO> getNearexpiryDate() {
		return nearexpiryDate;
	}

	public void setNearexpiryDate(ArrayList<NearExpiryDateBO> nearexpiryDate) {
		this.nearexpiryDate = nearexpiryDate;
	}

	public boolean isHasData() {
		return hasData;
	}

	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}

	public String getShelfInventory() {
		return shelfInventory;
	}

	public void setShelfInventory(String shelfInventory) {
		this.shelfInventory = shelfInventory;
	}
	public String toString() {
		// TODO Auto-generated method stub
		return LocName;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	int audit = 2;

	public int getAudit() {
		return audit;
	}

	public void setAudit(int audit) {
		this.audit = audit;
	}
	public int getReasonId() {
		return reasonId;
	}

	public void setReasonId(int reasonId) {
		this.reasonId = reasonId;
	}
	private int reasonId;

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	private String remarks = "";
	public String getActual() {
		return actual;
	}

	public void setActual(String actual) {
		this.actual = actual;
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
	public String getGap() {
		return gap;
	}

	public void setGap(String gap) {
		this.gap = gap;
	}
	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getParentTotal() {
		return parentTotal;
	}

	public void setParentTotal(String parentTotal) {
		this.parentTotal = parentTotal;
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
	private HashMap<String, HashMap<String, Object>> mShelfDetailForSOS = null;

	private HashMap<String, HashMap<String, ShelfShareBO>> mShelfBlockDetailForSOS = null;
	private HashMap<String, HashMap<String, Object>> mShelfDetailForSOD = null;
	private HashMap<String, HashMap<String, ShelfShareBO>> mShelfBlockDetailForSOD = null;
	public HashMap<String, HashMap<String, ShelfShareBO>> getmShelfBlockDetailForSOS() {
		return mShelfBlockDetailForSOS;
	}

	public boolean containsBlockKeySOS(String key) {
		return this.mShelfBlockDetailForSOS.containsKey(key);
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

	public boolean containsKeySOD(String key) {
		return this.mShelfDetailForSOD.containsKey(key);
	}
	public HashMap<String, HashMap<String, ShelfShareBO>> getmShelfBlockDetailForSOD() {
		return mShelfBlockDetailForSOD;
	}

	public void setmShelfBlockDetailForSOD(String key,
										   HashMap<String, ShelfShareBO> mShelfBlockDetailForSOD) {
		this.mShelfBlockDetailForSOD.put(key, mShelfBlockDetailForSOD);
	}
	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	private String  imagepath = "";
	private String fromDate = "";
	private String toDate = "";
	private String productName;

	private String skuname="";
	private String abv="";
	private String lotcode="";
	private String seqno="";
	private String feedback="";
	private int productID;
	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getImagepath() {
		return imagepath;
	}

	public void setImagepath(String imagepath) {
		this.imagepath = imagepath;
	}
	public int getPhotoid() {
		return photoid;
	}

	public void setPhotoid(int photoid) {
		this.photoid = photoid;
	}
	private int photoid;

	public String getSkuname() {
		return skuname;
	}

	public void setSkuname(String skuname) {
		this.skuname = skuname;
	}

	public String getAbv() {
		return abv;
	}

	public void setAbv(String abv) {
		this.abv = abv;
	}

	public String getLotcode() {
		return lotcode;
	}

	public void setLotcode(String lotcode) {
		this.lotcode = lotcode;
	}

	public String getSeqno() {
		return seqno;
	}

	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public int getIsPouring() {
		return isPouring;
	}

	public void setIsPouring(int isPouring) {
		this.isPouring = isPouring;
	}

	public int getCockTailQty() {
		return cockTailQty;
	}

	public void setCockTailQty(int cockTailQty) {
		this.cockTailQty = cockTailQty;
	}


	public ArrayList<CS_StockReasonBO> getLstStockReasons() {
		return lstStockReasons;
	}

	public void setLstStockReasons(ArrayList<CS_StockReasonBO> lstStockReasons) {
		this.lstStockReasons = lstStockReasons;
	}

	ArrayList<CS_StockReasonBO> lstStockReasons;

	public String getImgName() {
		return imgName;
	}

	public void setImgName(String imgName) {
		this.imgName = imgName;
	}

	private String imgName = "";
}
