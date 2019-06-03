package com.ivy.cpg.view.homescreen;

import com.ivy.sd.png.asean.view.R;

import java.util.HashMap;

public class HomeMenuConstants {

    public static final String MENU_PLANNING_CONSTANT = "Day Planning";
    public static final String MENU_VISIT_CONSTANT = "Trade Coverage";

    public static final String MENU_PLANNING = "MENU_PLANNING";
    public static final String MENU_VISIT = "MENU_VISIT";
    public static final String MENU_EXPENSE = "MENU_EXPENSE";
    public static final String MENU_NEW_RETAILER = "MENU_NEW_RET";
    public static final String MENU_REPORT = "MENU_REPORT";
    public static final String MENU_SYNC = "MENU_SYNC";
    public static final String MENU_LOAD_MANAGEMENT = "MENU_LOAD_MANAGEMENT";
    public static final String MENU_PLANNING_SUB = "MENU_PLANNING_SUB";
    public static final String MENU_LOAD_REQUEST = "MENU_STK_PRO";
    public static final String MENU_PRIMARY_SALES = "MENU_PRIMARY_SALES";
    public static final String MENU_JOINT_CALL = "MENU_JOINT_CALL";
    public static final String MENU_SURVEY_SW = "MENU_SURVEY_SW";
    public static final String MENU_SURVEY01_SW = "MENU_SURVEY01_SW";
    public static final String MENU_SURVEY_BA_CS = "MENU_SURVEY_BA_CS";
    public static final String MENU_SKUWISESTGT = "MENU_SKUWISESTGT";
    public static final String MENU_DASH_KPI = "MENU_DASH_KPI";
    public static final String MENU_DASH = "MENU_DASH";
    public static final String MENU_DASH_DAY = "MENU_DASH_DAY";
    public static final String MENU_DASH_INC = "MENU_DASH_INCENTIVE";
    public static final String MENU_DIGITIAL_SELLER = "MENU_DGT_SW";
    public static final String MENU_ATTENDANCE = "MENU_ATTENDANCE";
    public static final String MENU_PRESENCE = "MENU_PRESENCE";
    public static final String MENU_IN_OUT = "MENU_IN_OUT";
    public static final String MENU_LEAVE_APR = "MENU_LEAVE_APR";
    public static final String MENU_REALLOCATION = "MENU_REALLOCATION";
    public static final String MENU_EMPTY_RECONCILIATION = "MENU_EMPTY_RECONCILIATION";
    public static final String MENU_ORDER_FULLFILLMENT = "MENU_FULLFILMENT";
    public static final String MENU_ROAD_ACTIVITY = "MENU_ROAD_ACTIVITY";
    public static final String MENU_COUNTER = "MENU_COUNTER";
    public static final String MENU_MVP = "MENU_MVP";
    public static final String MENU_WVW_PLAN = "MENU_WVW_PLAN";
    public static final String MENU_WEB_VIEW = "MENU_WEB_VIEW";
    public static final String MENU_WEB_VIEW_APPR = "MENU_WVW_APPR";
    public static final String MENU_WEB_VIEW_PLAN = "MENU_WVW_PLAN_REQ";
    public static final String MENU_NEWRET_EDT = "MENU_NEWRET_EDT";
    public static final String MENU_TASK_NEW = "MENU_TASK_NEW";
    public static final String MENU_RTR_TASK_PENDING = "MENU_RTR_TASK_PENDING";
    public static final String MENU_PLANE_MAP = "MENU_PLANE_MAP";
    public static final String MENU_BACKUP_SELLER = "MENU_BACKUP_SELLER";
    public static final String MENU_SUPERVISOR_REALTIME = "MENU_SUPERVISOR_REALTIME";
    public static final String MENU_SUPERVISOR_MOVEMENT = "MENU_SUPERVISOR_MOVEMENT";
    public static final String MENU_SUPERVISOR_CALLANALYSIS = "MENU_SUPERVISOR_ACTIVITY";
    public static final String MENU_DENOMINATION = "MENU_DENOMINATION";
    public static final String MENU_ROUTE_KPI = "MENU_ROUTE_KPI";
    public static final String MENU_JOINT_ACK = "MENU_JOINT_ACK";
    public static final String MENU_NON_FIELD = "MENU_NON_FIELD";
    public static final String MENU_DELMGMT_RET = "MENU_DELMGMT_RET"; //Deleiver Management
    public static final String MENU_OFLNE_PLAN = "MENU_OFLNE_PLAN"; //Offline Planning
    public static final String MENU_SUBD = "MENU_SUBD";
    public static final String MENU_Q_CALL = "MENU_QUICK_CALL";
    public static final String MENU_NOTES_SW = "MENU_NOTES_SW";
    public static final String MENU_MAP_PLAN = "MENU_MAP_PLAN";
    public static final String TASK_NOTIFICATION = "taskNotificationFlag";



    public static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>(){{
        put(MENU_PLANNING, R.drawable.ic_vector_planning);
        put(MENU_VISIT, R.drawable.ic_vector_tradecoverage);
        put(MENU_MVP, R.drawable.ic_mvp_icon);
        put(MENU_SUBD, R.drawable.ic_vector_gallery);
        put(MENU_Q_CALL, R.drawable.ic_vector_tradecoverage);
        put(MENU_LOAD_MANAGEMENT, R.drawable.ic_load_mgmt_icon);
        put(MENU_PLANNING_SUB, R.drawable.ic_action_icon_reports);
        put(MENU_NEW_RETAILER, R.drawable.ic_new_retailer_icon);
        put(MENU_LOAD_REQUEST, R.drawable.ic_stock_proposal_icon);
        put(MENU_REPORT, R.drawable.ic_vector_reports);
        put(MENU_SYNC, R.drawable.ic_vector_sync);
        put(MENU_DASH_KPI, R.drawable.ic_vector_dashboard);
        put(MENU_DASH, R.drawable.ic_vector_dashboard);
        put(MENU_DASH_DAY, R.drawable.ic_vector_dashboard);
        put(MENU_DASH_INC, R.drawable.ic_vector_dashboard);
        put(MENU_SKUWISESTGT, R.drawable.ic_vector_dashboard);
        put(MENU_JOINT_CALL, R.drawable.ic_vector_jointcall);
        put(MENU_EMPTY_RECONCILIATION, R.drawable.ic_empty_reconcilation_icon);
        put(MENU_ATTENDANCE, R.drawable.ic_vector_out_of_trade);
        put(MENU_REALLOCATION, R.drawable.ic_reallocation_icon);
        put(MENU_DIGITIAL_SELLER, R.drawable.ic_vector_gallery);
        put(MENU_ROAD_ACTIVITY, R.drawable.icon_reports);
        put(MENU_PRESENCE, R.drawable.ic_vector_out_of_trade);
        put(MENU_IN_OUT, R.drawable.ic_vector_out_of_trade);
        put(MENU_LEAVE_APR, R.drawable.ic_vector_out_of_trade);
        put(MENU_EXPENSE, R.drawable.ic_expense_icon);
        put(MENU_NEWRET_EDT, R.drawable.ic_new_retailer_icon);
        put(MENU_TASK_NEW, R.drawable.task);
        put(MENU_RTR_TASK_PENDING, R.drawable.task);
        put(MENU_SURVEY_SW, R.drawable.ic_survey_icon);
        put(MENU_SURVEY01_SW, R.drawable.ic_survey_icon);
        put(MENU_SURVEY_BA_CS, R.drawable.ic_survey_icon);
        put(MENU_JOINT_ACK, R.drawable.ic_survey_icon);
        put(MENU_OFLNE_PLAN, R.drawable.ic_expense_icon);
        put(MENU_NON_FIELD, R.drawable.ic_vector_planning);
        put(MENU_BACKUP_SELLER, R.drawable.ic_reallocation_icon);
        put(MENU_SUPERVISOR_REALTIME, R.drawable.ic_new_retailer_icon);
        put(MENU_SUPERVISOR_MOVEMENT, R.drawable.ic_new_retailer_icon);
        put(MENU_SUPERVISOR_CALLANALYSIS, R.drawable.ic_new_retailer_icon);
        put(MENU_ROUTE_KPI, R.drawable.ic_vector_dashboard);
        put(MENU_DENOMINATION, R.drawable.ic_vector_dashboard);
        put(MENU_MAP_PLAN, R.drawable.ic_show_me_map);
        put(MENU_NOTES_SW,R.drawable.task_add);

    }};


}
