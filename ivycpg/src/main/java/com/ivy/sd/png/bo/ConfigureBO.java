package com.ivy.sd.png.bo;



import java.io.Serializable;

public class ConfigureBO implements Serializable {

    private String configCode, menuName, locationType, RField2, RField6, RField;
    private String menuNumber = "0";
    private int flag, hasLink;
    private int moduleOrder;
    private int Mandatory = 0;
    private int maxLengthNo = 0;
    private boolean isDone;
    private String index; // use in presentation summary screen
    private String menu_type;
    private String regex;

    private boolean isDeleteRow;
    private String retailerId;
    private String refId;

    public String getMenu_type() {
        return menu_type;
    }

    public void setMenu_type(String menu_type) {
        this.menu_type = menu_type;
    }

    public ConfigureBO() {

    }

    public ConfigureBO( String configcode,  String menuName, String menuNum,
                       int mOrder, int flag, int haslink) {
        this.configCode = configcode;
        this.menuName = menuName;
        this.menuNumber = menuNum;
        this.moduleOrder = mOrder;
        this.flag = flag;
        this.hasLink = haslink;
    }

    @Override
    public String toString() {
        return menuName;
    }

    public int getModule_Order() {
        return moduleOrder;
    }

    public void setModule_Order(int module_Order) {
        moduleOrder = module_Order;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }

    public int isFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuNumber() {
        return menuNumber;
    }

    public void setMenuNumber(String menuNumber) {
        this.menuNumber = menuNumber;
    }

    public int getHasLink() {
        return hasLink;
    }

    public void setHasLink(int hasLink) {
        this.hasLink = hasLink;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    public int getMandatory() {
        return Mandatory;
    }

    public void setMandatory(int RField1) {
        this.Mandatory = RField1;
    }

    public int getMaxLengthNo() {
        return maxLengthNo;
    }

    public void setMaxLengthNo(int maxLengthNo) {
        this.maxLengthNo = maxLengthNo;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getRField2() {
        return RField2;
    }

    public void setRField2(String rField2) {
        RField2 = rField2;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getKpiTarget() {
        return kpiTarget;
    }

    public void setKpiTarget(String kpiTarget) {
        this.kpiTarget = kpiTarget;
    }

    public String getKpiAchieved() {
        return kpiAchieved;
    }

    public void setKpiAchieved(String kpiAchieved) {
        this.kpiAchieved = kpiAchieved;
    }

    private String kpiTarget = "-1", kpiAchieved = "-1";

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public String getRField6() {
        return RField6;
    }

    public void setRField6(String RField6) {
        this.RField6 = RField6;
    }

    // used to load retailer address in order confirmation dialog based given address type List Code by comma separate
    public String getRField() {
        return RField;
    }

    public void setRField(String RField) {
        this.RField = RField;
    }

    public boolean isDeleteRow() {
        return isDeleteRow;
    }

    public void setDeleteRow(boolean deleteRow) {
        isDeleteRow = deleteRow;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }
}
