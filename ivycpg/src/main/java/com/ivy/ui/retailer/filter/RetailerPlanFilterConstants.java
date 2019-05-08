package com.ivy.ui.retailer.filter;

import java.util.ArrayList;

public class RetailerPlanFilterConstants {

    public static final String CODE_IS_NOT_VISITED = "not_visited";
    public static final String CODE_LAST_VISIT_DATE = "last_visit_date";
    public static final String CODE_TASK_DUE_DATE = "task_due_date";

    public static final ArrayList<String> hhtCodeList = new ArrayList<String>(){{
        add(CODE_IS_NOT_VISITED);
        add(CODE_LAST_VISIT_DATE);
        add(CODE_TASK_DUE_DATE);
    }};

}
