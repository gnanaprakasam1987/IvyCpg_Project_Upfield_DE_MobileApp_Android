package com.ivy.ui.DisplayAsset;

import android.content.Context;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.CompanyBO;

import java.util.HashMap;

public class DisplayAssetPresenterImpl implements DisplayAssetContractor.presenter{

    private DisplayAssetHelper displayAssetHelper;
    public DisplayAssetContractor.View assetView;
    private String displayAssetStatus="";

    private double ownCompanyScore,otherCompanyMaxScore;

    public DisplayAssetPresenterImpl(DisplayAssetHelper displayAssetHelper){

        this.displayAssetHelper=displayAssetHelper;

    }

    @Override
    public void setView(DisplayAssetContractor.View view) {
        assetView=view;
    }


    @Override
    public void refreshStatus() {

        HashMap<Integer,Double> totalWeightageCompanywise=new HashMap<>();
        // int totalOwnCompanyCount=0;
        double ownCompanyScore=0;
        String ownCompanyName="";

        for(AssetTrackingBO assetTrackingBO:displayAssetHelper.getDisplayAssetList()){
            for(CompanyBO companyBO:assetTrackingBO.getCompanyList()){
                if(companyBO.getIsOwn()==1){
                    // totalOwnCompanyCount+=companyBO.getQuantity();
                    ownCompanyScore+=(companyBO.getQuantity()*assetTrackingBO.getWeightage());

                    ownCompanyName=companyBO.getCompetitorName();
                }
                else {
                    double weightage=0;
                    if(totalWeightageCompanywise.get(companyBO.getCompetitorid())!=null)
                        weightage=totalWeightageCompanywise.get(companyBO.getCompetitorid());

                    totalWeightageCompanywise.put(companyBO.getCompetitorid(),weightage+(companyBO.getQuantity()*assetTrackingBO.getWeightage()));
                }
            }
        }

        double otherCompanyMaxScore=0;
        for(int companyId:totalWeightageCompanywise.keySet()){

            if(totalWeightageCompanywise.get(companyId)>otherCompanyMaxScore)
                otherCompanyMaxScore=totalWeightageCompanywise.get(companyId);
        }

        int flag=0;
        if(otherCompanyMaxScore<ownCompanyScore){
            flag=1;
            displayAssetStatus="ADVANTAGE";
        }
        else if(ownCompanyScore!=0&&(otherCompanyMaxScore==ownCompanyScore)){
            flag=2;
            displayAssetStatus="EQUAL";
        }
        else if(otherCompanyMaxScore>ownCompanyScore){
            flag=3;
            displayAssetStatus="DISADVANTAGE";
        }


        this.ownCompanyScore=ownCompanyScore;
        this.otherCompanyMaxScore=otherCompanyMaxScore;
        assetView.updateStatus(ownCompanyName,ownCompanyScore,otherCompanyMaxScore,flag);

    }

    @Override
    public boolean saveDisplayAssets(Context context) {


        return displayAssetHelper.saveDisplayAsset(context,getDisplayAssetStatus(),ownCompanyScore,otherCompanyMaxScore);

    }

    public String getDisplayAssetStatus() {
        return displayAssetStatus;
    }

    public double getOwnCompanyScore() {
        return ownCompanyScore;
    }

    public double getOtherCompanyMaxScore() {
        return otherCompanyMaxScore;
    }

}
