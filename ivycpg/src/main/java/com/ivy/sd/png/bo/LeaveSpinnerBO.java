package com.ivy.sd.png.bo;

public class LeaveSpinnerBO {

    private int id;
    private String spinnerTxt, flex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpinnerTxt() {
        return spinnerTxt;
    }

    public void setSpinnerTxt(String spinnerTxt) {
        this.spinnerTxt = spinnerTxt;
    }

    @Override
    public String toString() {
        return spinnerTxt;
    }

    public String getFlex() {
        return flex;
    }

    public void setFlex(String flex) {
        this.flex = flex;
    }
}
