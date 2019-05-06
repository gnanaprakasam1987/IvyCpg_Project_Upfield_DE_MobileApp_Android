package com.ivy.ui.filter;

public class FilterObjectBo {

    private String stringOne;
    private String stringTwo;
    private String stringThree;

    public FilterObjectBo(String strOne, String strTwo){
        this.stringOne = strOne;
        this.stringTwo = strTwo;
    }

    public String getStringOne() {
        return stringOne;
    }

    public void setStringOne(String stringOne) {
        this.stringOne = stringOne;
    }

    public String getStringTwo() {
        return stringTwo;
    }

    public void setStringTwo(String stringTwo) {
        this.stringTwo = stringTwo;
    }
}
