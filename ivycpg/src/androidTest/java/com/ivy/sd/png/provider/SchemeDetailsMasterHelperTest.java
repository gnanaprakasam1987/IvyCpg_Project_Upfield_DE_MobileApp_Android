package com.ivy.sd.png.provider;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import org.junit.Before;

import java.util.ArrayList;
import java.util.Vector;

import static org.junit.Assert.*;

public class SchemeDetailsMasterHelperTest {

    private  Context mContext;
    private  DBUtil db;
    private  SchemeDetailsMasterHelper schemeDetailsMasterHelper;

    @Before
    public void initialize(){
        mContext = InstrumentationRegistry.getContext();

        db=new DBUtil(mContext, DataMembers.DB_NAME);
        schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);

    }


    @org.junit.Test
    public void downloadSchemeParentDetails() {

        db.openDataBase();

        //schemeDetailsMasterHelper.downloadSchemeParentDetails(db,0,"0",819,0,0,0,new ArrayList<String>());
        ArrayList<Integer> originalList=new ArrayList<>();
        originalList.add(12);
        originalList.add(13);
        originalList.add(14);
        originalList.add(15);
        originalList.add(16);
        originalList.add(17);

        db.closeDB();

        assertEquals(originalList,schemeDetailsMasterHelper.getParentIDList());


    }

    @org.junit.Test
    public void downloadBuySchemeDetails() {

        db.openDataBase();
        BusinessModel businessModel=new BusinessModel();
        SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);
        //schemeDetailsMasterHelper.downloadBuySchemeDetails(db,"0",226,0,819,0,0,0,new ArrayList<String>());

        db.closeDB();

        assertEquals(22,schemeDetailsMasterHelper.getSchemeList());
    }

   /* public void checkFreeProducts(){

        mContext = InstrumentationRegistry.getContext();
       // BusinessModel businessModel=new BusinessModel();//(BusinessModel)InstrumentationRegistry.getTargetContext();
       // businessModel.schemeDetailsMasterHelper= SchemeDetailsMasterHelper.getInstance(mContext);

        DBUtil db=new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();

        SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);
       // schemeDetailsMasterHelper.downloadf


    }*/


    @org.junit.Test
   public void schemeApply(){
       SchemeDetailsMasterHelper schemeDetailsMasterHelper=SchemeDetailsMasterHelper.getInstance(mContext);

       Vector<ProductMasterBO> productList=new Vector<>();
       ProductMasterBO productMasterBO=new ProductMasterBO();

       productMasterBO.setProductID("859");
       productMasterBO.setProductShortName("A");
       productMasterBO.setOrderedPcsQty(10);
       productMasterBO.setPcUomid(609);
       productMasterBO.setCaseUomId(607);
       productList.add(productMasterBO);

        productMasterBO=new ProductMasterBO();
       productMasterBO.setProductID("35");
       productMasterBO.setProductShortName("B");
       productMasterBO.setOrderedPcsQty(20);
       productMasterBO.setPcUomid(609);
       productMasterBO.setCaseUomId(607);
       productList.add(productMasterBO);


       schemeDetailsMasterHelper.schemeApply(productList);

       ArrayList<String> appliedSlabIds=new ArrayList<>();
       for(SchemeBO schemeBO:schemeDetailsMasterHelper.getAppliedSchemeList()){
           appliedSlabIds.add(schemeBO.getSchemeId());
       }

       assertEquals(1,schemeDetailsMasterHelper.getAppliedSchemeList().size());
       assertTrue(appliedSlabIds.contains("20964"));
   }
}