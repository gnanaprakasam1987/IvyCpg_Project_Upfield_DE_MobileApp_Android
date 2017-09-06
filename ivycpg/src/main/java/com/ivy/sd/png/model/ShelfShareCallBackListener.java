package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.SOSBO;

import java.util.List;

public interface ShelfShareCallBackListener {
	void SOSBOCallBackListener(List<SOSBO> sosBOList);
	void SODDOCallBackListener(List<SODBO> sodBOList);
	void handleDialogClose();
}
