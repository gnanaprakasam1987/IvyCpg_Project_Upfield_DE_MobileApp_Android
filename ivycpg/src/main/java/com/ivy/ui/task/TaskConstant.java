package com.ivy.ui.task;

import android.os.Environment;

import com.ivy.sd.png.util.DataMembers;

public class TaskConstant {

    public static final String RETAILER_WISE_TASK = "isRetailerWiseTask";

    public static final String FORM_CHANNEL_WISE = "FromChannelWise";

    public static final String FROM_PROFILE_SCREEN = "fromProfileScreen";

    public static final String FROM_HOME_SCREEN = "fromHomeScreen";

    public static final String CURRENT_ACTIVITY_CODE = "CurrentActivityCode";

    public static final String SCREEN_TITLE = "screenTitle";

    public static final String MOVE_NEXT_ACTIVITY = "IsMoveNextActivity";

    public static final int SERVER_TASK = 1;

    public static final int TASK_TITLE_ASC = 0;//Order By Ascending for Task Title Name
    public static final int TASK_TITLE_DESC = 1;//Order By Descending  for Task Title Name
    public static final int PRODUCT_LEVEL_ASC = 2;// Order By Ascending for product level
    public static final int PRODUCT_LEVEL_DESC = 3;//Order By Descending for product level

    public static final String EVIDENCE_IMAGE = "evidenceImg";

    public static final String MENU_CODE = "menuCode";

    public static final String TASK_OBJECT = "taskObj";

    public static final String TASK_SCREEN_MODE = "isType";

    public static final String FILE_PATH = "path";

    public static final String MODULE_NAME = "modulename";

    public static final String PHOTO_CAPTURE_DIALOG_TAG = "ReasonDialogFragment";

    public static final int TASK_DETAIL = 0;

    public static final int TASK_EDIT = 1;

    public static final String SELLER_WISE = "seller";

    public static final String RETAILER_WISE = "retailer";

    public static final String CHANNEL_WISE = "channel";

    public static final String PRODUCT_LEVEL_WISE = "Category";

    public static final String ASC_ORD = "(A—Z)";

    public static final String DESC_ORD = "(Z—A)";

    public static final String ASC_ORD_DATE = "(old—new)";

    public static final String DESC_ORD_DATE = "(new—old)";

    public static String TASK_SERVER_IMG_PATH ;

    public static final String TAB_SELECTION = "tab_selection";


    public static enum SOURCE {
        HOME_SCREEN(0),
        RETAILER(1),
        PROFILE_SCREEN(2);
        private int value;

        SOURCE(int value) {
            this.value = value;
        }
    }


}
