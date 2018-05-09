package com.ivy.sd.png.provider;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SchemeDetailsMasterHelperTest {

    private Context mContext;
    @org.junit.Test
    public void downloadSchemeParentDetails() {

        mContext = InstrumentationRegistry.getContext();
       // BusinessModel businessModel=new BusinessModel();//(BusinessModel)InstrumentationRegistry.getTargetContext();
       // businessModel.schemeDetailsMasterHelper= SchemeDetailsMasterHelper.getInstance(mContext);

        DBUtil db=new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();


        SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);
        schemeDetailsMasterHelper.downloadSchemeParentDetails(db,0,"0",618,0,0,0,new ArrayList<String>());
        ArrayList<Integer> originalList=new ArrayList<>();
        originalList.add(12);
        originalList.add(13);
        originalList.add(14);
        originalList.add(15);
        originalList.add(16);
        originalList.add(17);

        assertEquals(originalList,schemeDetailsMasterHelper.getParentIDList());


    }

    @org.junit.Test
    public void downloadBuySchemeDetails() {


        mContext = InstrumentationRegistry.getContext();
       // BusinessModel businessModel=new BusinessModel();//(BusinessModel)InstrumentationRegistry.getTargetContext();
       // businessModel.schemeDetailsMasterHelper= SchemeDetailsMasterHelper.getInstance(mContext);

        DBUtil db=new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();


        SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);
        schemeDetailsMasterHelper.downloadBuySchemeDetails(db,"0",12,0,618,0,0,0,new ArrayList<String>());
        ArrayList<Integer> originalList=new ArrayList<>();
        originalList.add(12);
        originalList.add(13);
        originalList.add(14);
        originalList.add(15);
        originalList.add(16);
        originalList.add(17);
        assertEquals(originalList,schemeDetailsMasterHelper.getSchemeList());
    }

    public void checkFreeProducts(){

        mContext = InstrumentationRegistry.getContext();
       // BusinessModel businessModel=new BusinessModel();//(BusinessModel)InstrumentationRegistry.getTargetContext();
       // businessModel.schemeDetailsMasterHelper= SchemeDetailsMasterHelper.getInstance(mContext);

        DBUtil db=new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();

        SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);
       // schemeDetailsMasterHelper.downloadf


    }
}