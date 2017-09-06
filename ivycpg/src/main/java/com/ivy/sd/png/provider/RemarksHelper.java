package com.ivy.sd.png.provider;

import com.ivy.sd.png.bo.RemarksBO;

public class RemarksHelper {

	private final RemarksBO remarksBO;
	private static RemarksHelper instance = null;

	private RemarksHelper() {
		remarksBO = new RemarksBO();
	}

	public static RemarksHelper getInstance() {
		if (instance == null) {
			instance = new RemarksHelper();
		}
		return instance;
	}

	public RemarksBO getRemarksBO() {
		return remarksBO;
	}
}
