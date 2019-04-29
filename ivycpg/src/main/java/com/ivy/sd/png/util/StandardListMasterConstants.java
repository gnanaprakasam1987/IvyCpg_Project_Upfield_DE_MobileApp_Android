package com.ivy.sd.png.util;

import java.util.HashMap;

public class StandardListMasterConstants {
	public static final String CASH = "CA";
	public static final String CREDIT_NOTE = "CN";
	public static final String DISCOUNT = "CD";
	public static final String CHEQUE = "CQ";
	public static final String MOBILE_PAYMENT = "CM";
	public static final String RTGS = "RTGS";
	public static final String DEMAND_DRAFT = "DD";
	public static final String COUPON="CP";
	public static final String ADVANCE_PAYMENT="AP";

	public static final String COLLECTION_PAY_TYPE = "COLLECTION_PAY_TYPE";
	public static final String CREDIT_NOTE_TYPE="CREDIT_NOTE_TYPE";
	public static final String MENU_ORDER = "MENU_ORDER";
	public static final String MENU_COLLECTION = "MENU_COLLECTION";
	public static final String MENU_STK_ORD = "MENU_STK_ORD";
	public static final String MENU_REV = "MENU_REV";

	// Extra
	public static final String MONTH = "MONTH";
	public static final String MENU_COLLECTION_VIEW = "MENU_COLLECTION_VIEW";

	public static final String MENU_DAMAGE_STOCK = "MENU_DAMAGE_STOCK";
	public static final String MENU_STOCK_REPLACEMENT = "MENU_STOCK_REPLACEMENT";

	public static final String SLM_RET_GPS_CODE = "LMM";
	public static final String SLM_RET_SEQ_CODE = "SEQSKP";
	public static final String OTP_LIST_CODE = "OTP_ACTIVITY_TYPE";
	public static final String MENU_CURRENT_STOCK_VIEW_BATCH = "MENU_CUR_STK_BATCH";

	public static final String COLLECTION_REASON_TYPE = "COLLR";
	public static final String MUSTSELL_REASON_TYPE = "RMSL";

	public static final String STOCK_TYPE_REASON = "STOCK_TYPE_REASON";

	public static final String SALES_RETURN_SALABLE_REASON_TYPE = "SRS";
	public static final String SALES_RETURN_NONSALABLE_REASON_TYPE = "SR";
	public static final String NON_PLANNED_REASON_TYPE = "NFA_DEVIATION";
	public static final String OTP_REASON_TYPE = "RTRSKP";
	public static final String COLLECTION_TRANSACTION_PAYMENT_TYPE = "COLL_TRAN_PAY_TYPE";
	public static final String COLLECTION_NORMAL_PAYMENT = "CNP";
	public static final String COLLECTION_ADVANCED_PAYMENT = "CAP";
	public static final String COLLECTION_DEBIT_NOTE_PAYMENT="CDP";

	public static final String COUNTER_SALES_AGE_GROUP_TYPE="AGE_GROUP_TYPE";

	public static final String REPORT_MENU = "MENU_REPORT";
	public static final String MENU_ORDER_REPORT = "MENU_ORDER_REPORT";
	public static final String MENU_DAY_REPORT = "MENU_DAY_REPORT";
	public static final String MENU_INVOICE_REPORT = "MENU_INVOICE_REPORT";
	public static final String MENU_PND_INVOICE_REPORT = "MENU_PND_INV_RPT";
	public static final String MENU_SKU_REPORT = "MENU_SKU_REPORT";
	public static final String MENU_CURRENT_STOCK_REPORT = "MENU_CURRENT_STOCK_REPORT";

	public static final String MENU_COLLECTION_REPORT = "MENU_COLLECTION_REPORT";
	public static final String MENU_TASK_EXECUTION_REPORT = "MENU_TASK_EXEC_REPORT";
	public static final String MENU_EOD_STOCK_REPORT = "MENU_EOD_STOCK_REPORT";
	public static final String MENU_TASK_REPORT = "MENU_TASK_REPORT";
	public static final String MENU_QUESTION_REPORT = "MENU_QUESTION_REPORT";

	public static final String MENU_PS_ORD_RPT="MENU_PS_ORD_RPT";
	public static final String MENU_DYN_REPORT = "MENU_DYN_RPT"; // Dynamic Report
	public static final String MENU_LOG = "MENU_LOG"; // Activity Log
	public static final String MENU_RTR_RPT = "MENU_RTR_RPT"; // Retailer Report
	public static final String MENU_WEBVIEW_RPT01 = "MENU_WVW_RPT01";
	public static final String MENU_WEBVIEW_RPT02 = "MENU_WVW_RPT02";
	public static final String MENU_CREDIT_NOTE_REPORT = "MENU_CREDIT_RPT";
	public static final String MENU_ATTENDANCE_REPORT = "MENU_ATTENDANCE_RPT";
	public static final String MENU_DELIVERY_STOCK_REPORT = "MENU_DELIVERYSTK";
	public static final String MENU_CONTRACT_REPORT = "MENU_CONTRACT_RPT";
	public static final String MENU_SFG_REPORT = "MENU_SFPJ_RPT";
	public static final String MENU_PROMO_REPORT = "MENU_PRMPJ_RPT";
	public static final String MENU_ASSET_REPORT = "MENU_ASTPJ_RPT";
	public static final String MENU_SALES_REPORT = "MENU_SELRET_RPT";
	public static final String MENU_SELLER_MAPVIEW_REPORT = "MENU_SELLERMAP_RPT";
	public static final String MENU_SELLER_PERFOMANCE_REPORT = "MENU_SELPERFO_RPT";
	public static final String MENU_RETPERFO_RPT="MENU_RETPERFO_RPT";
	public static final String MENU_ARCHV_RPT = "MENU_ARCHV_RPT";
	public static final String MENU_DELIVERY_RPT = "MENU_DELIVERY_RPT";

	public static final String VISITCONFIG_PLANNING = "MENU_PLANNING";
	public static final String VISITCONFIG_COVERAGE = "MENU_VISIT";

	public static final String MENU_INVENTORY_RPT = "MENU_INVPJ_RPT";
    public static final String MENU_RETAILER_ACTIVITY_REPORT = "MENU_RETACT_RPT";

	// For Piramal
	public static final String MENU_BRAND_PERFORMANCE_REPORT = "MENU_BRAND_RPT";
	public static final String MENU_OPPORTUNITIES_REPORT = "MENU_OPPORTUNITIES_REPORT";
	public static final String MENU_TIMEANDTRAVEL_REPORT = "MENU_TIMEANDTRAVEL_REPORT";
	public static final String MENU_PRODUCTIVITY_REPORT = "MENU_PRODUCTIVITY_REPORT";

	public static final String FEEDBACK_TYPE = "VFDK";
	public static final String PRINT_FILE_INVOICE = "PF_INV_";
	public static final String PRINT_FILE_COLLECTION = "PF_COL_";
	public static final String PRINT_FILE_ORDER = "PF_ORD_";
	public static final String PRINT_FILE_UNLOAD = "PF_UNLOAD_";
	public static final String PRINT_FILE_PATH = "PrintFile/";

	public static final String MENU_CLOSING_STK_RPT = "MENU_CLOSING_STK_RPT";
	public static final String MENU_ORD_STAT_RPT = "MENU_ORD_STAT_RPT";
	public static final String MENU_INV_STAT_RPT = "MENU_INV_STAT_RPT";
	public static final String MENU_ORDER_FULFILL_REPORT = "MENU_ORDERFULFILL_REPORT";

	public static final String MENU_INV_SALES_RETURN_REPORT = "MENU_SALES_RET_REPORT";
	public static final String OTP_SKIP_REASON_TYPE = "GPS_VIOLATION";

	public static final HashMap<String,String> mActivityCodeByMenuCode=new HashMap<>();

	static {
		mActivityCodeByMenuCode.put("MENU_STK_ORD", "ORDER");
		mActivityCodeByMenuCode.put("MENU_SUBD_ORD", "ORDER");
		mActivityCodeByMenuCode.put("MENU_SALES_RET", "SALESRET");
		mActivityCodeByMenuCode.put("MENU_NEAREXPIRY", "NEAREXPIRY");
		mActivityCodeByMenuCode.put("MENU_PRICE", "PRICE");
		mActivityCodeByMenuCode.put("MENU_STOCK_PROPOSAL", "STKPROPOSAL");
		mActivityCodeByMenuCode.put("MENU_MANUAL_VAN_LOAD", "MVANLOAD");
		mActivityCodeByMenuCode.put("MENU_VAN_UNLOAD", "VUNLOAD");
		mActivityCodeByMenuCode.put("MENU_PS_STKORD", "PSSTKORD");
	}

	public static final String NO_COLLECTION_REASON_TYPE = "COLLECTION_DUE";
}
