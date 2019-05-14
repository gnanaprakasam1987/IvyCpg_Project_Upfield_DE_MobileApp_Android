package com.ivy.ui.retailer;

import java.util.ArrayList;

public class RetailerConstants {

    public static final String CODE_IS_NOT_VISITED = "not_visited";
    public static final String CODE_LAST_VISIT_DATE = "last_visit_date";
    public static final String CODE_TASK_DUE_DATE = "task_due_date";

    public static final ArrayList<String> hhtCodeList = new ArrayList<String>(){{
        add(CODE_IS_NOT_VISITED);
        add(CODE_LAST_VISIT_DATE);
        add(CODE_TASK_DUE_DATE);
    }};

    public static final String PLANNED = "PLANNED";
    public static final String NOT_COMPLETED = "NOT COMPLETED";
    public static final String COMPLETED = "COMPLETED";
    public static final String APPROVED = "APPROVED";
    public static final String PENDING = "PENDING";
    public static final String REJECTED = "REJECTED";

}
