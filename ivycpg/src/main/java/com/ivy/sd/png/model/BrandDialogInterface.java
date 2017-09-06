package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.LevelBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public interface BrandDialogInterface {
	void updateMultiSelectionBrand(List<String> filtername,List<Integer> filterid); 
	void updateMultiSelectionCatogry(List<Integer> mcatgory);
	void updatebrandtext(String filtertext, int id);

	void updategeneraltext(String filtertext);

	void updateCancel();

	void loadStartVisit();
	
	void updatefromFiveLevelFilter(Vector<LevelBO> parentidList);

	void updatefromFiveLevelFilter(Vector<LevelBO> parentidList,HashMap<Integer, Integer> mSelectedIdByLevelId,ArrayList<Integer> mAttributeProducts,String filtertext );
}
