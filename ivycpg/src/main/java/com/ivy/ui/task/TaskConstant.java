package com.ivy.ui.task;

public class TaskConstant {

    public static final String TASK_REMARK = "TASK";

    public static final String FILTER = "filter";

    public static final String SELECTED_FILTER_LIST = "SelectedFilterList";

    public static final String FILTER_LIST = "hashList";

    public static final String FILTER_MENU_LIST = "menuList";

    public static final String RETAILER_FILTER_MENU = "Retailer";

    public static final String RETAILER_WISE_TASK = "isRetailerWiseTask";

    public static final String FORM_CHANNEL_WISE = "FromChannelWise";

    public static final String FROM_PROFILE_SCREEN = "fromProfileScreen";

    public static final String FROM_HOME_SCREEN = "fromHomeScreen";

    public static final String CURRENT_ACTIVITY_CODE = "CurrentActivityCode";

    public static final String SCREEN_TITLE = "screenTitle";

    public static final String MOVE_NEXT_ACTIVITY = "IsMoveNextActivity";

    public static final String TASK_NOTIFICATION_SRC = "fromTaskNotification";

    public static final String TASK_DETAIL_SRC = "fromTaskDetailSrc";

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

    public static final int TASK_DELETE = 2;

    public static final String SELLER_WISE = "seller";

    public static final String PARENT_WISE = "parent";

    public static final String CHILD_WISE = "child";

    public static final String RETAILER_WISE = "retailer";

    public static final String PEERT_WISE = "peer";

    public static final String LINK_WISE = "link";

    public static final String PRODUCT_LEVEL_WISE = "Category";

    public static final String ASC_ORD = "(A—Z)";

    public static final String DESC_ORD = "(Z—A)";

    public static final String ASC_ORD_DATE = "(old—new)";

    public static final String DESC_ORD_DATE = "(new—old)";

    public static String TASK_SERVER_IMG_PATH;

    public static final String TAB_SELECTION = "tab_selection";

    public static final String TASK_DATE_FORMAT = "dd-MM-yyyy";

    public static final String TASK_NOT_COMPLETE_REASON = "NCTR";

    public static final String TASK_TITLE_LABEL = "task_title";

    public static final String TASK_DUE_DATE_LABEL = "task_due_date";

    public static final String TASK_APPLICABLE_FOR_LABEL = "task_applicable_for";

    public static final String TASK_PHOTO_CAPTURE_LABEL = "task_photo_capture";

    public static final String TASK_DESCRIPTION_LABEL = "task_description";

    public static final String TASK_CREATED_BY_LABEL = "task_created_by";

    public static final String TASK_EVIDENCE_LABEL = "task_evidence";

    public static final int CAMERA_REQUEST_CODE = 1;

    public static final int TASK_UPDATED_SUCCESS_CODE = 2;

    public static final int TASK_CREATED_SUCCESS_CODE = 3;

    public static final int NEW_TASK_CREATION = 0;

    public static final int EDIT_MODE_FROM_TASK_FRAGMENT_SRC = 1;

    public static final int EDIT_MODE_FROM_TASK_DETAIL_SRC = 2;

    public static final String TASK_PRE_VISIT = "PreVisit";

    public static final String TASK_EXECUTE_RESPONSE = "ExeResponse";

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
