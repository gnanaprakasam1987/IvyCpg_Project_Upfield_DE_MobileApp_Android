package com.ivy.ui.DisplayAsset;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.CompanyBO;

import java.util.ArrayList;

public class DisplayAssetTestDataFactory {

    public static ArrayList<AssetTrackingBO> getDisplayAssetList() {

        ArrayList<CompanyBO> companyList=new ArrayList<>();
        CompanyBO comp1=new CompanyBO();
        comp1.setCompetitorid(1);
        comp1.setCompetitorName("comp 1");
        comp1.setIsOwn(1);
        comp1.setQuantity(5);
        companyList.add(comp1);

        CompanyBO comp2=new CompanyBO();
        comp2.setCompetitorid(2);
        comp2.setCompetitorName("comp 2");
        comp2.setIsOwn(0);
        comp2.setQuantity(7);
        companyList.add(comp2);

        AssetTrackingBO assetTrackingBO=new AssetTrackingBO();
        assetTrackingBO.setDisplayAssetId("1");
        assetTrackingBO.setAssetName("Asset 1");
        assetTrackingBO.setWeightage(10);
        assetTrackingBO.setCompanyList(companyList);

        AssetTrackingBO assetTrackingBO2=new AssetTrackingBO();
        assetTrackingBO2.setDisplayAssetId("2");
        assetTrackingBO2.setAssetName("Asset 2");
        assetTrackingBO2.setWeightage(5);
        assetTrackingBO2.setCompanyList(companyList);

        ArrayList<AssetTrackingBO> list = new ArrayList<>();
        list.add(assetTrackingBO);
        list.add(assetTrackingBO2);
        return list;
    }
}
