package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.LevelBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public interface BrandDialogInterface {
	void updateMultiSelectionBrand(List<String> mFilterName,List<Integer> mFilterId);
	void updateMultiSelectionCategory(List<Integer> mCategory);
	void updateBrandText(String mFilterText, int id);

	void updateGeneralText(String mFilterText);

	void updateCancel();

	void loadStartVisit();
	
	void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList);

	void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText );

}
