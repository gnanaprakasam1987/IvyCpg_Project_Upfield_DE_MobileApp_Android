package com.ivy.sd.png.bo;

import java.util.Comparator;

public class RetailerSotringIsDone implements Comparator<RetailerMasterBO> {

	@Override
	public int compare(RetailerMasterBO lhs, RetailerMasterBO rhs) {
		// TODO Auto-generated method stub

		int attendCondition=lhs.getIsAttended().toString().compareTo(rhs.getIsAttended().toString());
		return attendCondition ;
	}

}
