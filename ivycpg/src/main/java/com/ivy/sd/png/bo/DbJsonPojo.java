package com.ivy.sd.png.bo;

import java.util.List;


/**
 * Created by mayuri.v on 7/12/2017.
 */
public class DbJsonPojo {
    private List<CustomerVisitTableBO> Tables;

    public List<CustomerVisitTableBO> getTableJsonArray() {
        return Tables;
    }

    public void setTableJsonArray(List<CustomerVisitTableBO> Tables) {
        this.Tables = Tables;
    }

}
