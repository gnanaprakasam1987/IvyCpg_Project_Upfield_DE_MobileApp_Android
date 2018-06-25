package com.ivy.cpg.view.reports.retaileractivity;

import android.util.Log;

import junit.framework.Assert;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by abbas.a on 25/06/18.
 */
public class RetailerActivityReportHelperTest {
    @Test
    public void timeDifference() throws Exception {
        RetailerActivityReportHelper retailerActivityReportHelper=new RetailerActivityReportHelper(null);
        String str=retailerActivityReportHelper.timeDifference("2018/06/18 20:07:12","2018/06/18 20:08:13");
        System.out.print(str);
        Assert.assertEquals("00:01",str);
    }

}