package com.ivy.sd.png.bo.asset;

public class AssetAddDetailBO {
	private String mPOSMId;
	private String mposmdesc;
	private String mAssetBrandId;
	private String mAssetBrandName;
	private String vendorId;
	private String modelId;
	private String capacity;
	private String typeId;

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

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	@Override
	public String toString(){
		return mposmdesc;
	}
}
