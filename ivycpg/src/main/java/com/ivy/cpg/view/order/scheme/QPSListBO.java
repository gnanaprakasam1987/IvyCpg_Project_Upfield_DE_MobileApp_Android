package com.ivy.cpg.view.order.scheme;

import java.util.Vector;

/**
 * Created by anandasir on 19/7/18.
 */

public class QPSListBO {

    private ParentSchemeBO parentScheme;
    private Vector<SchemeProductBO> schemeList;

    public ParentSchemeBO getParentScheme() {
        return parentScheme;
    }

    public void setParentScheme(ParentSchemeBO parentScheme) {
        this.parentScheme = parentScheme;
    }

    public Vector<SchemeProductBO> getSchemeList() {
        return schemeList;
    }

    public void setSchemeList(Vector<SchemeProductBO> schemeList) {
        this.schemeList = schemeList;
    }
}
