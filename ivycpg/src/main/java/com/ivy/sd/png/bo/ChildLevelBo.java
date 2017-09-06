package com.ivy.sd.png.bo;

import java.io.Serializable;
import java.util.Comparator;

public class ChildLevelBo implements Serializable

{

	private int productid;
	private int parentid;
	private String plevelName;
	private String productLevel;
	private boolean mchecked;

	public ChildLevelBo() {

	}
	public ChildLevelBo(int productid,String plevelName) {
		this.productid = productid;
		this.plevelName = plevelName;
	}
	public static final Comparator<ChildLevelBo> LoadingOrder = new Comparator<ChildLevelBo>() {

		@Override
		public int compare(ChildLevelBo id1, ChildLevelBo id2) {

			int parentID1 = id1.getParentid();
			int parentID2 = id2.getParentid();
			// ascending order
			return parentID1 - parentID2;
		}

	};

	public ChildLevelBo(int productid, int parentid, String plevelName) {
		this.productid = productid;
		this.parentid = parentid;
		this.plevelName = plevelName;
	}

	public int getParentid() {
		return parentid;
	}

	public void setParentid(int parentid) {
		this.parentid = parentid;
	}

	public String getProductLevel() {
		return productLevel;
	}

	public void setProductLevel(String productLevel) {
		this.productLevel = productLevel;
	}

	public String getPlevelName() {
		return plevelName;
	}

	public void setPlevelName(String plevelName) {
		this.plevelName = plevelName;
	}

	public int getProductid() {
		return productid;
	}

	public void setProductid(int productid) {
		this.productid = productid;
	}

	@Override
	public String toString() {
		return plevelName;

	}

	public boolean isMchecked() {
		return mchecked;
	}

	public void setMchecked(boolean mchecked) {
		this.mchecked = mchecked;
	}

}
