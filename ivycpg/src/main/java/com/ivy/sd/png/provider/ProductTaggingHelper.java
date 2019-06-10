package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ProductTaggingBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ProductTaggingHelper {

    private static ProductTaggingHelper instance;
    private ArrayList<ProductTaggingBO> productTaggingList;

    private ArrayList<Integer> taggedLocations;
    private Vector<ProductMasterBO> mTaggedProducts;
    private Map<String, ProductMasterBO> mTaggedProductById;
    private BusinessModel businessModel;
    private ProductHelper productHelper;

    private static int TAGGING_TYPE_MODULE_WITH_STORELOCATION=1;
    private static int TAGGING_TYPE_MODULE=2;
    private static int TAGGING_TYPE_COMMON_WITH_STORELOCATION=3;
    private static int TAGGING_TYPE_COMMON=4;



    public ProductTaggingHelper(Context context){
        mTaggedProducts = new Vector<>();
        businessModel =(BusinessModel)context.getApplicationContext();
        productHelper= ProductHelper.getInstance(context);
    }

    public static ProductTaggingHelper getInstance(Context context) {
        if (instance == null)
            instance = new ProductTaggingHelper(context);

        return instance;
    }


    public Vector<ProductMasterBO> getTaggedProducts() {
        if (mTaggedProducts == null)
            return new Vector<>();
        return mTaggedProducts;
    }
    public ArrayList<ProductTaggingBO> getProductTaggingList() {
        return productTaggingList;
    }

    /**
     * get tagged products and update the productBO.
     *
     * @param mMenuCode menu code
     */
    public void downloadTaggedProducts(Context mContext,String mMenuCode) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            ProductHelper productHelper=ProductHelper.getInstance(mContext);
            int mContentLevel = productHelper.getContentLevel(db, mMenuCode);
            String productIds = getTaggedProductIds(mContext,mMenuCode, mContentLevel);
            List<String> mSKUId = new ArrayList<>();

            mSKUId = Arrays.asList(productIds.split(","));

            mTaggedProducts = new Vector<>();
            mTaggedProductById = new HashMap<>();

            if (productIds != null && !productIds.trim().equals("")) {
                for (ProductMasterBO sku : productHelper.getProductMaster()) {
                    if (mSKUId.contains(sku.getProductID())) {

                        updateTaggedLocations(sku);

                        mTaggedProducts.add(sku);
                        mTaggedProductById.put(sku.getProductID(), sku);
                    }
                }
            } else {
                for (ProductMasterBO sku : productHelper.getProductMaster()) {
                    mTaggedProducts.add(sku);
                    mTaggedProductById.put(sku.getProductID(), sku);
                }
            }
            if(!db.isDbNullOrClosed())
                db.closeDB();
        } catch (Exception e) {
            Commons.printException("downloadTaggedProducts", e);
        }

    }

    /**
     * Updating only tagged locations
     * @param productMasterBO
     */
    private void updateTaggedLocations(ProductMasterBO productMasterBO){
        if(getTaggedLocations().size()>0) {
            ArrayList<LocationBO> taggedLocations = new ArrayList<>();
            for (LocationBO locationBO : productMasterBO.getLocations()) {
                if (getTaggedLocations().contains(locationBO.getLocationId())) {
                    taggedLocations.add(locationBO);
                }
            }
            productMasterBO.setLocations(productHelper.cloneInStoreLocationList(taggedLocations));
        }

    }

    public ArrayList<Integer> getTaggedLocations() {
        if(taggedLocations==null)
            taggedLocations=new ArrayList<>();
        return taggedLocations;
    }


    /**
     * Method will return tagged products list as a string with comma separator.
     *
     * @param moduleCode tagging type
     * @return productId with comma separated string.
     */
    public String getTaggedProductIds(Context mContext, String moduleCode, int mContentLevelId) {
        try {
            ProductTaggingBO taggingBO;
            productTaggingList = new ArrayList<>();

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            int typeOfTagging=getTypeOfTagging(db,moduleCode);

            boolean isLocationMappingIncluded=false;
            String criteriaTable="ProductTaggingCriteriaMapping";
            if(typeOfTagging==TAGGING_TYPE_MODULE_WITH_STORELOCATION
                    ||typeOfTagging==TAGGING_TYPE_COMMON_WITH_STORELOCATION) {
                isLocationMappingIncluded = true;
                criteriaTable="ProductTaggingCriteriaLocationMapping";
            }

            String groupIds = getMappedGroupId(mContext,db, moduleCode,typeOfTagging);
            StringBuilder productIds = new StringBuilder();

            Cursor c = db.selectSQL("SELECT PMM.pid,PCM.GroupID,FromNorm,ToNorm,Weightage"+
                    (isLocationMappingIncluded?",PCM.InStoreLocationId":"")+
                    " FROM "+criteriaTable+" PCM " +
                    "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid " +
                    "INNER JOIN ProductTaggingGroupMapping PGM ON PGM.groupid=PM.groupid and PGM.isOwn = 1 " +
                    "INNER JOIN ProductMaster PMM ON PMM.Plid = "+ mContentLevelId +" and PMM.ParentHierarchy like '%/'||PGM.PID||'/%' " +
                    "WHERE PCM.groupid IN(" + groupIds + ")");

            if (c != null) {
                while (c.moveToNext()) {
                    if (!productIds.toString().equals(""))
                        productIds.append(",");
                    productIds.append(c.getInt(0));
                    if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED || businessModel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
                        taggingBO = new ProductTaggingBO();
                        taggingBO.setHeaderID(c.getString(1));
                        taggingBO.setPid(c.getString(0));
                        taggingBO.setFromNorm(c.getInt(2));
                        taggingBO.setToNorm(c.getInt(3));
                        taggingBO.setWeightage(c.getInt(4));
                        productTaggingList.add(taggingBO);
                    }

                    if(isLocationMappingIncluded){
                        if(!taggedLocations.contains(c.getInt(5)))
                            taggedLocations.add(c.getInt(5));
                        productHelper.getProductMasterBOById(c.getString(0)).getTaggedLocations().add(c.getInt(5));
                    }

                    if(moduleCode.equalsIgnoreCase("PC")){
                        // overriding price for price module
                        float price=c.getFloat(3);
                        if(price>0) {
                            productHelper.getProductMasterBOById(c.getString(0)).setSrp(price);
                            productHelper.getProductMasterBOById(c.getString(0)).setPriceMOP(String.valueOf(price));
                        }
                    }
                }
                c.close();
            }
            db.closeDB();
            setProductTaggingList(productTaggingList);
            return productIds.toString();
        } catch (Exception e) {
//            e.printStackTrace();
            return "";
        }
    }

    /**
     * Returns the type of tagging based on the priority
     * Priority 1 - Module wise with instore location
     * Priority 2 - Module wise
     * Priority 3 - Common mapping with instore location
     * Priority 4 - Common mapping
     * @param db
     * @param taggingType
     * @return
     */
    private int getTypeOfTagging(DBUtil db,String taggingType){
        try {
            StringBuilder priorityQuery = new StringBuilder();
            Cursor priorityCursor;

            priorityQuery.append("SELECT DISTINCT PCM.GroupID FROM ProductTaggingCriteriaLocationMapping PCM " +
                    " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
                    " WHERE PM.TaggingTypelovID = " +
                    " (SELECT ListId FROM StandardListMaster WHERE ListCode = '" + taggingType + "' AND ListType = 'PRODUCT_TAGGING')");
            priorityCursor = db.selectSQL(priorityQuery.toString());
            if (priorityCursor.getCount() > 0) {
                return TAGGING_TYPE_MODULE_WITH_STORELOCATION;
            } else {

                priorityQuery = new StringBuilder();
                priorityQuery.append("SELECT DISTINCT PCM.GroupID FROM ProductTaggingCriteriaMapping PCM " +
                        " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
                        " WHERE PM.TaggingTypelovID = " +
                        " (SELECT ListId FROM StandardListMaster WHERE ListCode = '" + taggingType + "' AND ListType = 'PRODUCT_TAGGING')");
                priorityCursor = db.selectSQL(priorityQuery.toString());
                if (priorityCursor.getCount() > 0) {
                   return TAGGING_TYPE_MODULE;

                } else {
                    priorityQuery = new StringBuilder();
                    priorityQuery.append("SELECT DISTINCT PCM.GroupID FROM ProductTaggingCriteriaLocationMapping PCM " +
                            " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
                            " WHERE PM.TaggingTypelovID = " +
                            " (SELECT ListId FROM StandardListMaster WHERE ListCode = 'ACCOUNT_ASSORTMENT' AND ListType = 'PRODUCT_TAGGING')");
                    priorityCursor = db.selectSQL(priorityQuery.toString());
                    if (priorityCursor.getCount() > 0) {
                        return TAGGING_TYPE_COMMON_WITH_STORELOCATION;
                    }
                }
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return TAGGING_TYPE_COMMON;
    }


    //new producttagging mapping
    //Mansoor
    private String getMappedGroupId(Context mContext,DBUtil db, String moduleCode,int typeOfTagging) {
        StringBuilder groupIds = new StringBuilder();
        ArrayList<String> attrmappingsetId = new ArrayList<>();
        //taggedLocationIds=new ArrayList<>();
        String mappedGrpIds = "";

        String attrQuery = "Select distinct PTAM.Groupid from ProductTaggingAttributesMapping PTAM" +
                " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PTAM.groupid" +
                " inner join RetailerAttribute RA on RA.AttributeId = PTAM.RetailerAttibuteId and RA.RetailerId =" + StringUtils.QT(businessModel.getRetailerMasterBO().getRetailerID()) +
                " WHERE PM.TaggingTypelovID = " + "(SELECT ListId FROM StandardListMaster WHERE ListCode = '" + moduleCode + "' AND ListType = 'PRODUCT_TAGGING')";

        Cursor c1 = db.selectSQL(attrQuery);

        if (c1.getCount() > 0) {
            while (c1.moveToNext()) {
                attrmappingsetId.add("/" + c1.getInt(0) + "/");
            }
        }

        StringBuilder accountGroupIds=new StringBuilder();
        String accountQuery="Select groupId from AccountGroupDetail where retailerId="+ businessModel.getRetailerMasterBO().getRetailerID();
        Cursor accountCursor=db.selectSQL(accountQuery);
        if(accountCursor.getCount()>0){
            while (accountCursor.moveToNext()){
                accountGroupIds.append(accountCursor.getString(0));

                if(accountGroupIds.toString().length()>0)
                    accountGroupIds.append(",");
            }
        }



        String criteriaTableName;
        boolean isInStoreLocation=false;
        if(typeOfTagging==TAGGING_TYPE_MODULE_WITH_STORELOCATION){
            criteriaTableName="ProductTaggingCriteriaLocationMapping";
            isInStoreLocation=true;
        }
        else if(typeOfTagging==TAGGING_TYPE_MODULE){
            criteriaTableName="ProductTaggingCriteriaMapping";
        }
        else if(typeOfTagging==TAGGING_TYPE_COMMON_WITH_STORELOCATION){
            criteriaTableName="ProductTaggingCriteriaLocationMapping";
            moduleCode="ACCOUNT_ASSORTMENT";
            isInStoreLocation=true;
        }
        else {
            criteriaTableName="ProductTaggingCriteriaMapping";
            moduleCode="ACCOUNT_ASSORTMENT";
        }


        StringBuilder query=new StringBuilder();
        query.append("SELECT DISTINCT PCM.GroupID,PCM.RetailerId,PCM.LocationId,PCM.ChannelId,PCM.AccountId," +
                " PCM.DistributorId,PCM.UserId,PCM.ClassId,IFNULL(PTAM.groupid,0)"+
                 " FROM "+criteriaTableName+" PCM " +
                " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
                " INNER JOIN ProductTaggingGroupMapping PGM ON PGM.groupid=PM.groupid" +
                " Left Join ProductTaggingAttributesMapping PTAM on PTAM.groupid = PCM.GroupID " +
                " WHERE PM.TaggingTypelovID = " +
                " (SELECT ListId FROM StandardListMaster WHERE ListCode = '" + moduleCode + "' AND ListType = 'PRODUCT_TAGGING')");


        if(accountGroupIds.toString().length()>0){
            query.append(" AND PCM.accountGroupId in("+accountGroupIds.toString()+")");

        } else {
            query.append(" AND PCM.RetailerId IN(0," + businessModel.getRetailerMasterBO().getRetailerID() + ") " +
                    " AND PCM.LocationId IN(0," + businessModel.channelMasterHelper.getLocationHierarchy(mContext) + "," + businessModel.getRetailerMasterBO().getLocationId() + ") " +
                    " AND PCM.ChannelId IN(0," + businessModel.channelMasterHelper.getChannelHierarchy(businessModel.getRetailerMasterBO().getSubchannelid(), mContext) + "," + businessModel.getRetailerMasterBO().getSubchannelid() + ") " +
                    " AND PCM.AccountId IN(0," + businessModel.getRetailerMasterBO().getAccountid() + ") " +
                    " AND PCM.DistributorId IN(0," + businessModel.getRetailerMasterBO().getDistributorId() + ") " +
                    " AND PCM.UserId IN(0," + businessModel.userMasterHelper.getUserMasterBO().getUserid() + ") " +
                    " AND PCM.ClassId IN(0," + businessModel.getRetailerMasterBO().getClassid() + ") ");
        }

        Cursor c = db
                .selectSQL(query.toString());

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                // only mapped through attribute
                if (c.getInt(1) == 0 && c.getInt(2) == 0 && c.getInt(3) == 0
                        && c.getInt(4) == 0 && c.getInt(5) == 0 && c.getInt(6) == 0 && c.getInt(7) == 0) {
                    if (attrmappingsetId.contains("/" + c.getInt(0) + "/"))
                        groupIds.append(c.getString(0));
                } // only criteria mapped
                else if (c.getInt(8) == 0) {
                    groupIds.append(c.getString(0));
                } // both criteria and attr mapped AND condition
                else if (c.getInt(8) > 0 && attrmappingsetId.contains("/" + c.getInt(0) + "/")) {
                    groupIds.append(c.getString(0));
                }
                if (groupIds.length() > 0)
                    groupIds.append(",");

              /*  if(!taggedLocationIds.contains(c.getInt(9)))
                    taggedLocationIds.add(c.getInt(9));*/
            }
            if (groupIds.toString().endsWith(","))
                mappedGrpIds = groupIds.toString().substring(0, groupIds.length() - 1);
            mappedGrpIds = mappedGrpIds.trim();
        }

        c.close();
        return mappedGrpIds;

    }


    private void setProductTaggingList(ArrayList<ProductTaggingBO> productTaggingList) {
        this.productTaggingList = productTaggingList;
    }


    ///////////////// Competitor tagging ////////////////////

    /**
     * get competitor tagged products and update the productBO.
     *
     * @param mMenuCode menu code
     */
    public void downloadCompetitorTaggedProducts(Context context,String mMenuCode) {
        try {

            String productIds = getCompetitorTaggingDetails(context,mMenuCode);


            if (mTaggedProducts == null) {
                mTaggedProducts = new Vector<>();
            }
            if (mTaggedProductById == null) {
                mTaggedProductById = new HashMap<>();
            }

            if (productIds != null && !productIds.trim().equals("")) {
                for (ProductMasterBO sku : productHelper.getCompetitorProductMaster()) {
                    //if (mSKUId.contains(sku.getProductID())) {
                    mTaggedProducts.add(sku);
                    mTaggedProductById.put(sku.getProductID(), sku);

                }
            } else {
                for (ProductMasterBO sku : productHelper.getCompetitorProductMaster()) {
                    mTaggedProducts.add(sku);
                    mTaggedProductById.put(sku.getProductID(), sku);

                }
            }


            Vector<ProductMasterBO> tagItems = getTaggedProducts();
            if (tagItems != null)
                for (ProductMasterBO tagBo : tagItems) {
                    if (tagBo.getOwn() == 0 && productHelper.getFilterColor("Filt23") != 0) {
                        tagBo.setTextColor(productHelper.getFilterColor("Filt23"));
                    } else {
                        if (tagBo.getOwn() == 0)
                            tagBo.setTextColor(ContextCompat.getColor(context, R.color.list_item_primary_text_color));
                    }
                }
        } catch (Exception e) {
            Commons.printException("downloadTaggedProducts", e);
        }

    }

    /* get All competitor tagged products irrespective of own product mapping */
    public void getAlCompetitorTaggedProducts(Context context,int loopEnd) {
        DBUtil db;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            String sql;
            sql = "SELECT distinct A1.CPID, A1.CPName," +
                    "(SELECT ListId from StandardListMaster where ListCode = " + businessModel.QT(businessModel.synchronizationHelper.CASE_TYPE) + " and ListType = 'PRODUCT_UOM')as duomid," +
                    "(SELECT ListId from StandardListMaster where ListCode = " + businessModel.QT(businessModel.synchronizationHelper.OUTER_TYPE) + " and ListType = 'PRODUCT_UOM') as dOuomid," +
                    "(SELECT ListId from StandardListMaster where ListCode = " + businessModel.QT(businessModel.synchronizationHelper.PIECE_TYPE) + " and ListType = 'PRODUCT_UOM') as piece_uomid," +
                    "A1.CPCode,A" + loopEnd + ".CPID as parentId,ifnull(A1.Barcode,'') from CompetitorProductMaster A1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN CompetitorProductMaster A" + i + " ON A" + i
                        + ".CPID = A" + (i - 1) + ".CPTid";
            Cursor cur = db
                    .selectSQL(sql
                            + " INNER JOIN ProductTaggingGroupMapping PTGM ON PTGM.isOwn = 0 AND PTGM.pid = A1.CPID");
            if (cur != null) {

                while (cur.moveToNext()) {
                    ProductMasterBO product = new ProductMasterBO();
                    product.setProductID(cur.getString(0));
                    product.setProductName(cur.getString(1));
                    product.setProductShortName(cur.getString(1));
                    product.setParentid(0);
                    product.setIsSaleable(1);
                    product.setBarCode(cur.getString(7));
                    product.setCasebarcode(cur.getString(7));
                    product.setOuterbarcode(cur.getString(7));
                    product.setOwn(0);
                    product.setCaseUomId(cur.getInt(2));
                    product.setOuUomid(cur.getInt(3));
                    product.setPcUomid(cur.getInt(4));
                    product.setProductCode(cur.getString(5));
                    product.setOwnPID("0");
                    product.setCompParentId(cur.getInt(cur.getColumnIndex("parentId")));

                    product.setLocations(productHelper.cloneInStoreLocationList(productHelper.getStoreLocations()));
                    for (int i = 0; i < productHelper.getStoreLocations().size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(productHelper.cloneDateList(productHelper.getNearExpiryDateList()));
                    }

                    productHelper.getCompetitorProductMaster().add(product);

                }
                cur.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method will return competitor tagged products list as a string with comma separator.
     *
     * @param moduleCode tagging type
     * @return productId with comma separated string.
     */
    public String getCompetitorTaggingDetails(Context mContext,String moduleCode) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);

            db.openDataBase();
            int typeOfTagging=getTypeOfTagging(db,moduleCode);
            String groupIds = getMappedGroupId(mContext,db, moduleCode,typeOfTagging);
            StringBuilder productIds = new StringBuilder();
            Cursor c = db
                    .selectSQL("SELECT distinct pid FROM ProductTaggingCriteriaMapping PCM " +
                            " INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid " +
                            " INNER JOIN ProductTaggingGroupMapping PGM ON PGM.groupid=PM.groupid and PGM.isOwn = 0" +
                            " WHERE PCM.groupid IN(" + groupIds + ")");

            if (c != null) {
                while (c.moveToNext()) {
                    if (!productIds.toString().equals(""))
                        productIds.append(",");
                    productIds.append(c.getInt(0));
                }
                c.close();
            }
            db.closeDB();

            return productIds.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public ProductMasterBO getTaggedProductBOById(String productId) {
        if (mTaggedProductById == null)
            return null;
        return mTaggedProductById.get(productId);
    }


}
