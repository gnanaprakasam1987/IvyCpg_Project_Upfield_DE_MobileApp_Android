package com.ivy.sd.png.bo;

import java.io.Serializable;

public class ParentLevelBo implements Serializable {
	private int plProductid;
	private String plLevelName;
	private String plProductLevel;
	private boolean mchecked;
	
	public int getPl_productid() {
		return plProductid;
	}
	public void setPl_productid(int plProductid) {
		this.plProductid = plProductid;
	}
	public String getPl_levelName() {
		return plLevelName;
	}
	public void setPl_levelName(String plLevelName) {
		this.plLevelName = plLevelName;
	}
	public String getPl_productLevel() {
		return plProductLevel;
	}
	public void setPl_productLevel(String plProductLevel) {
		this.plProductLevel = plProductLevel;
	}

	public boolean isMchecked() {
		return mchecked;
	}

	public void setMchecked(boolean mchecked) {
		this.mchecked = mchecked;
	}

}
