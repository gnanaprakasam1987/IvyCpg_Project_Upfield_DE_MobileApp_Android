package com.ivy.sd.png.bo;

import java.util.List;

/**
 * Created by mayuri.v on 7/12/2017.
 */
public class CustomerVisitTableBO {
    private String Master;
    private List<String> Field;
    private List<List<String>> Data;
    private String ErrorCode;

    public String getMasterString() {
        return Master;
    }

    public void setMasterString(String Master) {
        this.Master = Master;
    }

    public List<String> getFieldJsonArray() {
        return Field;
    }

    public void setFieldJsonArray(List<String> Field) {
        this.Field = Field;
    }

    public List<List<String>> getDataJsonArray() {
        return Data;
    }

    public void setDataJsonArray(List<List<String>> Data) {
        this.Data = Data;
    }

    public String getErrorCodeString() {
        return ErrorCode;
    }

    public void setErrorCodeString(String ErrorCode) {
        this.ErrorCode = ErrorCode;
    }
}
