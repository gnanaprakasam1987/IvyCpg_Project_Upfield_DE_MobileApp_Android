package com.ivy.sd.png.bo;

public class AssetAddDetailBO {
	private String mPOSMId;
	private String mposmdesc;
	private String mAssetBrandId;
	private String mAssetBrandName;

	public String getAssetBrandId() {
		return mAssetBrandId;
	}

	public void setAssetBrandId(String assetBrandId) {
		this.mAssetBrandId = assetBrandId;
	}

	public String getAssetBrandName() {
		return mAssetBrandName;
	}

	public void setAssetBrandName(String mAssetBrandName) {
		this.mAssetBrandName = mAssetBrandName;
	}

	public String getPOSMId() {
		return mPOSMId;
	}

	public void setPOSMId(String mPOSMId) {
		this.mPOSMId = mPOSMId;
	}

	public String getPOSMDescription() {
		return mposmdesc;
	}

	public void setPOSMDescription(String mPOSMDescription) {
		this.mposmdesc = mPOSMDescription;
	}
}
