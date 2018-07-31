package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.LevelBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public interface FiveLevelFilterCallBack {

	void updateCancel();
	void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText);

}
