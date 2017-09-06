package com.ivy.sd.png.bo;

public class LevelBO { 
	
	private int  parentID = 0;
	private int  productID = 0;
	private String levelName;
	private int sequence ;

	public LevelBO(){
		//to avoid compile time error when object with no parameter/s created
	}

	public LevelBO(String lvName,int prodId,int seq){
		this.levelName=lvName;
		this.productID=prodId;
		this.sequence=seq;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	@Override
	public String toString() {
		return levelName;
	}
}
