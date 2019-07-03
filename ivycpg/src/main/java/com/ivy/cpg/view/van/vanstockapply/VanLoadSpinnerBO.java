package com.ivy.cpg.view.van.vanstockapply;

public class VanLoadSpinnerBO {

    private String uid, spinnerTxt;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSpinnerTxt() {
        return spinnerTxt;
    }

    public void setSpinnerTxt(String spinnerTxt) {
        this.spinnerTxt = spinnerTxt;
    }

    public VanLoadSpinnerBO(String uid, String spinnerTxt) {
        super();
        this.uid = uid;
        this.spinnerTxt = spinnerTxt;
    }

    @Override
    public String toString() {
        return spinnerTxt;
    }

}
