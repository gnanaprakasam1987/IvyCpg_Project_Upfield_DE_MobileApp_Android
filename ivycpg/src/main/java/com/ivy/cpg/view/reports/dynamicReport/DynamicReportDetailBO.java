package com.ivy.cpg.view.reports.dynamicReport;

import android.util.SparseArray;

import java.util.TreeSet;

/**
 * Created by karthikeyan.a on 3/16/2016.
 */
public class DynamicReportDetailBO {

    private TreeSet<Integer> recordSet;
    private SparseArray<SparseArray<String>> detailsSparseArray;

    public SparseArray<SparseArray<String>> getDetailsSparseArray() {
        return detailsSparseArray;
    }

    public void setDetailsSparseArray(SparseArray<SparseArray<String>> detailsSparseArray) {
        this.detailsSparseArray = detailsSparseArray;
    }

    public TreeSet<Integer> getRecordSet() {
        return recordSet;
    }

    public void setRecordSet(TreeSet<Integer> recordSet) {
        this.recordSet = recordSet;
    }
}
