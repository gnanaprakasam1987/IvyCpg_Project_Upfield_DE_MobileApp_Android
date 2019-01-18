package com.ivy.cpg.view.initiative;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class InitiativeHelper {

    public static final String QUANTITY_TYPE = "QTY";
    public static final String VALUE_TYPE = "SV";
    private static InitiativeHelper instance = null;

    private Context mContext;
    private Vector<InitiativeHeaderBO> initiativeHeaderBOVector;
    private BusinessModel bmodel;

    private InitiativeHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static InitiativeHelper getInstance(Context context) {
        if (instance == null) {
            instance = new InitiativeHelper(context);
        }
        return instance;
    }

    /**
     * Download the list of Initiative mapped for the retailer's local channel.
     * Initiative ID,Desc,Type,KeyWord and IsCombination will be retrived from
     * Initiative Header Table and loaded in
     * bmodel.initiativeHelper.initiativeHeaderBOVector.
     *
     * @param subChannelId
     */
    public void downloadInitiativeHeader(int subChannelId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select B.InitId,B.InitDesc,B.InitType,B.InitKeyWord,B.IsCombination,B.isParent "
                            + "from InitiativeHeaderMaster B where isParent=1 and B.InitID in "
                            + "(select InitId from InitiativeDetailMaster where LocalChannelId="
                            + subChannelId + ")");

            initiativeHeaderBOVector = new Vector<InitiativeHeaderBO>();

            if (c != null) {
                while (c.moveToNext()) {
                    InitiativeHeaderBO initHeader = new InitiativeHeaderBO();
                    initHeader.setInitiativeId(c.getString(0));
                    initHeader.setDescription(c.getString(1));
                    initHeader.setType(c.getString(2));
                    initHeader.setKeyword(c.getString(3));
                    initHeader.setIsCombination(c.getInt(4));
                    initHeader.setIsParent(c.getInt(5));
                    initiativeHeaderBOVector.add(initHeader);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Download Initiative ID,Desc,Type,KeyWord and IsCombination of a
     * particular Initiative. This method is used to load sub-Initiatives used
     * in combination logic
     *
     * @param initId
     * @return InitiativeHeaderBO
     */
    private InitiativeHeaderBO downloadInitiativeHeaderofInitiative(
            String initId) {
        InitiativeHeaderBO initHeader = new InitiativeHeaderBO();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select InitId,InitDesc,InitType,InitKeyWord,isCombination,isParent from "
                            + " InitiativeHeaderMaster where InitId='"
                            + initId
                            + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    initHeader.setInitiativeId(c.getString(0));
                    initHeader.setDescription(c.getString(1));
                    initHeader.setType(c.getString(2));
                    initHeader.setKeyword(c.getString(3));
                    initHeader.setIsCombination(c.getInt(4));
                    initHeader.setIsParent(c.getInt(5));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return initHeader;

    }

    /**
     * This method is used to check the Initiative status like Initiative HIT,
     * Distribution HIT for all the initiative mapped for that particular
     * retailer.
     *
     * @param isUpdateAcheivement - if true the Update the status in InitiativeCoverageReport.
     * @param retailerId
     * @param forCoverageReport   - if true then productmaster will not be consider for
     *                            calculation.
     */
    public void loadInitiativeStatus(boolean isUpdateAcheivement,
                                     String retailerId, boolean forCoverageReport,
                                     boolean calculateBalance, int subChannelId) {

        // isUpdateAcheivement - used to store the the acheivement result in DB
        // for Initiative coverage report

        int siz = initiativeHeaderBOVector.size();

        for (int i = 0; i < siz; i++) {
            if (forCoverageReport) {
                checkInitiativeForCoverage(initiativeHeaderBOVector.get(i),
                        retailerId, subChannelId);
            } else {
                checkInitiative(initiativeHeaderBOVector.get(i), retailerId,
                        subChannelId);
            }
        }

        if (isUpdateAcheivement)
            updateInitiativeCoverageReport(retailerId, calculateBalance);
    }

    private InitiativeHeaderBO checkInitiative(InitiativeHeaderBO initHeaderBO,
                                              String retailerId, int subChannelId) {

        String type = initHeaderBO.getType(); // Value || Qty
        String keyword = initHeaderBO.getKeyword(); // AND || ONLY || ANY
        int isCombination = initHeaderBO.getIsCombination(); // 1 || 0

        Vector<InitiativeDetailBO> initiativeDetailVector = downloadInitiativeDetail(
                initHeaderBO, retailerId, subChannelId);

        int initiativeDetailsiz = initiativeDetailVector.size();
        int productmasterSize = bmodel.productHelper.getProductMaster().size();

        for (int j = 0; j < initiativeDetailsiz; j++) {

            InitiativeDetailBO initDetail = initiativeDetailVector.get(j);

            if (isCombination == 1) {
                // consider it as an initiative and call the Initiative
                // execution method

                InitiativeHeaderBO initHeader = downloadInitiativeHeaderofInitiative(initDetail
                        .getProductId());
                initHeader = checkInitiative(initHeader, retailerId,
                        subChannelId);
                initDetail.setDone(initHeader.isDone());
                initDetail.setDistributed(initHeader.isDistributed());
                initDetail.setAcheivedValue(initHeader.getSum());

            } else {

                for (int i = 0; i < productmasterSize; i++) {

                    ProductMasterBO productMasterBO = bmodel.productHelper
                            .getProductMaster().get(i);

                    if (productMasterBO.getProductID().equals(
                            initDetail.getProductId() + "")) {

                        int count = (productMasterBO.getOrderedPcsQty())
                                + (productMasterBO.getOrderedCaseQty() * productMasterBO
                                .getCaseSize())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOutersize());
                        int init_count = (productMasterBO.getInit_pieceqty())
                                + (productMasterBO.getInit_caseqty() * productMasterBO
                                .getCaseSize())
                                + (productMasterBO.getInit_OuterQty() * productMasterBO
                                .getOutersize());

                        count = count + init_count;

                        if (type.equals(VALUE_TYPE)) {

                            float val = (productMasterBO
                                    .getOrderedPcsQty() * productMasterBO
                                    .getSrp())
                                    + (productMasterBO.getOrderedCaseQty() * productMasterBO
                                    .getCsrp())
                                    + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                    .getOsrp());
                            float init_val = (productMasterBO
                                    .getInit_pieceqty() * productMasterBO
                                    .getSrp())
                                    + (productMasterBO.getInit_caseqty() * productMasterBO
                                    .getCsrp())
                                    + (productMasterBO.getInit_OuterQty() * productMasterBO
                                    .getOsrp());

                            val = val + init_val;

                            if (val >= initDetail.getInitiativeBalanceValue()) {
                                initiativeDetailVector.get(j).setDone(true);

                            } else {
                                initiativeDetailVector.get(j).setDone(false);
                            }

                            // To check Initiative distribution
                            if (initDetail.getInitiativeBalanceValue() < initDetail
                                    .getInitiativeValue() || val > 0) {
                                initiativeDetailVector.get(j).setDistributed(
                                        true);
                            } else {
                                initiativeDetailVector.get(j).setDistributed(
                                        false);
                            }

                            initiativeDetailVector
                                    .get(j)
                                    .setAcheivedValue(
                                            val
                                                    + (initDetail
                                                    .getInitiativeValue() - initDetail
                                                    .getInitiativeBalanceValue()));

                        } else if (type.equals(QUANTITY_TYPE)) {

                            if (count >= initDetail.getInitiativeBalanceValue()) {
                                initiativeDetailVector.get(j).setDone(true);
                            } else {
                                initiativeDetailVector.get(j).setDone(false);
                            }

                            // To check Initiative distribution
                            if (initDetail.getInitiativeBalanceValue() < initDetail
                                    .getInitiativeValue() || count > 0) {
                                initiativeDetailVector.get(j).setDistributed(
                                        true);
                            } else {
                                initiativeDetailVector.get(j).setDistributed(
                                        false);
                            }

                            initiativeDetailVector
                                    .get(j)
                                    .setAcheivedValue(
                                            count
                                                    + (initDetail
                                                    .getInitiativeValue() - initDetail
                                                    .getInitiativeBalanceValue()));
                        }

                        break;
                    }
                }
            }

        }

        // This value would be helpfull to calcualte hit in combination
        // Initiative.
        initHeaderBO.setSum(getSum(initiativeDetailVector));

        if (keyword.equals("AND")) {
            initHeaderBO
                    .setDistributed(checkForAndDist(initiativeDetailVector));
        } else {
            initHeaderBO
                    .setDistributed(checkForAnyOnlyDist(initiativeDetailVector));
        }

        switch (keyword) {
            case "AND":
                initHeaderBO.setDone(checkForAnd(initiativeDetailVector));
                break;
            case "ANY":
                initHeaderBO.setDone(checkForAny(initiativeDetailVector));
                break;
            case "ONLY":
                initHeaderBO.setDone(checkForOnly(initiativeDetailVector));
                break;
        }

        return initHeaderBO;

    }

    private float getSum(Vector<InitiativeDetailBO> initiativeDetailVector) {
        float acheived = 0;
        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            acheived = acheived
                    + initiativeDetailVector.get(i).getAcheivedValue();
        }
        return acheived;
    }

    private boolean checkForAnd(
            Vector<InitiativeDetailBO> initiativeDetailVector) {

        if (bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {

            float target;
            boolean done = false;
            float acheived = 0;
            for (int i = 0; i < initiativeDetailVector.size(); i++) {
                target = initiativeDetailVector.get(i).getInitiativeValue();
                float orderedVal = initiativeDetailVector.get(i).getAcheivedValue();
                acheived = acheived
                        + initiativeDetailVector.get(i).getAcheivedValue();

                if (orderedVal >= target)
                    done = true;
                else {
                    done = false;
                    break;
                }
            }
            return done;
        } else {

            for (int i = 0; i < initiativeDetailVector.size(); i++) {
                if (!initiativeDetailVector.get(i).isDone())
                    return false;
            }
            return true;
        }
    }

    private boolean checkForAndDist(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            if (!initiativeDetailVector.get(i).isDistributed())
                return false;
        }
        return true;

    }

    private boolean checkForAny(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        float target = initiativeDetailVector.get(0).getInitiativeValue();
        float acheived = 0;
        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            acheived = acheived
                    + initiativeDetailVector.get(i).getAcheivedValue();
        }

        Commons.print(target + " Target " + acheived + " Ache");

        return acheived >= target;

    }

    private boolean checkForAnyOnlyDist(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            if (initiativeDetailVector.get(i).isDistributed())
                return true;
        }
        return false;
    }

    private boolean checkForOnly(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        return initiativeDetailVector.size() > 0 && initiativeDetailVector.get(0).isDone();
    }

    public Vector<InitiativeDetailBO> downloadInitiativeDetail(
            InitiativeHeaderBO initId, String retailerId, int subChannelId) {
        Vector<InitiativeDetailBO> initiativeDetailBO = new Vector<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String target;
            String achievement;
            if (initId.getType().equals("QTY")) {
                target = "A.DropQtyValue";
                achievement = "ifnull(B.Value,0)";
            } else {
                target = "A.TargetValue";
                achievement = "ifnull(B.Qty,0)";
            }

            Cursor c;
            if (initId.getIsParent() == 0) {

                c = db.selectSQL("select A.InitId,A.ProductInitId, "
                        + "case when (A.TargetValue-ifnull(B.Qty,0)) >0 then "
                        + "(A.TargetValue-ifnull(B.Qty,0)) else 0 end ,A.TargetValue ,"
                        + target
                        + " , "
                        + achievement
                        + " from InitiativeDetailMaster A left join InitiativeAchievementMaster B "
                        + "on A.ProductInitId=B.ProductId and B.InitId=(select Initid from InitiativeDetailMaster where ProductInitId='"
                        + initId.getInitiativeId() + "') and B.retailerid="
                        + retailerId + " where A.initid='"
                        + initId.getInitiativeId() + "' and A.Localchannelid="
                        + subChannelId);

            } else {
                c = db.selectSQL("select A.InitId,A.ProductInitId, "
                        + "case when (A.TargetValue-ifnull(B.Qty,0)) >0 then "
                        + "(A.TargetValue-ifnull(B.Qty,0)) else 0 end ,A.TargetValue ,"
                        + target
                        + " , "
                        + achievement
                        + " from InitiativeDetailMaster A left join InitiativeAchievementMaster B "
                        + "on A.ProductInitId=B.ProductId and B.InitId='"
                        + initId.getInitiativeId() + "' and B.retailerid="
                        + retailerId + " where A.initid='"
                        + initId.getInitiativeId() + "' and A.Localchannelid="
                        + subChannelId);
            }
            if (c != null) {
                while (c.moveToNext()) {
                    InitiativeDetailBO init = new InitiativeDetailBO();
                    init.setInitId(c.getString(0));
                    init.setProductId(c.getString(1));
                    init.setInitiativeBalanceValue(c.getFloat(2));
                    init.setInitiativeValue(c.getFloat(3));
                    init.setDropQtyValueTarget(c.getFloat(4));
                    init.setDropQtyValueAcheivement(c.getFloat(5));
                    initiativeDetailBO.add(init);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return initiativeDetailBO;

    }

    public Vector<InitiativeDetailBO> downloadCombinationInitiativeDetail(
            String initId) {
        Vector<InitiativeDetailBO> initiativeDetailBO = new Vector<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select A.InitId,A.ProductInitId, "
                            + "case when (A.TargetValue-ifnull(B.Qty,0)) >0 then "
                            + "(A.TargetValue-ifnull(B.Qty,0)) else 0 end ,A.TargetValue  "
                            + "from InitiativeDetailMaster A left join InitiativeAchievementMaster B "
                            + "on A.ProductInitId=B.ProductId and B.InitId ='"
                            + initId
                            + "' and B.retailerid="
                            + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                            + " where A.initid in (SELECT ProductInitId FROM InitiativeDetailMaster where Initid='"
                            + initId + "')");

            if (c != null) {
                while (c.moveToNext()) {
                    InitiativeDetailBO init = new InitiativeDetailBO();
                    init.setInitId(c.getString(0));
                    init.setProductId(c.getString(1));
                    init.setInitiativeBalanceValue(c.getFloat(2));
                    init.setInitiativeValue(c.getFloat(3));
                    initiativeDetailBO.add(init);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return initiativeDetailBO;

    }

    /**
     * Load previously taken Order/Invoice details into ProductmasterBO's
     * setInit_pieceqty & setInit_caseqty, Which is later used to calculate
     * Initiative acheived.
     *
     * @param retailerID
     */
    public void loadLocalOrdersQty(String retailerID) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        Cursor c;

        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            c = db.selectSQL("select productid,sum(pcsQty),sum( caseQty ),sum(outerQty) from invoicedetails where retailerid = "
                    + retailerID + " group by productid");
        } else {
            c = db.selectSQL(" select  productid,sum(caseqty),sum( pieceqty ),sum(outerQty) from orderdetail where retailerid = "
                    + retailerID + " and upload = 'Y' group by productid");
        }

        if (c != null) {
            while (c.moveToNext()) {

                String productid = c.getString(0);
                int caseqty = c.getInt(1);
                int pieceqty = c.getInt(2);
                int outerQty = c.getInt(3);
                setLocalOrdersProductDetail(productid, caseqty, pieceqty,
                        outerQty);
            }

            c.close();
        }
        db.closeDB();

    }

    private void setLocalOrdersProductDetail(String productid, int caseqty,
                                             int pieceqty, int outerQty) {
        ProductMasterBO product;
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            product = bmodel.productHelper.getProductMaster()
                    .get(i);
            if (product.getProductID().equals(productid)) {
                product.setLocalOrderPieceqty(pieceqty);
                product.setLocalOrderCaseqty(caseqty);
                product.setLocalOrderOuterQty(outerQty);
                bmodel.productHelper.getProductMaster()
                        .setElementAt(product, i);
                return;
            }
        }

    }

    public void updateInitAchievedPercentInRetailerMaster() {
        if (initiativeHeaderBOVector == null) {
            downloadInitiativeHeader(bmodel.getAppDataProvider().getRetailMaster()
                    .getSubchannelid());
        }

        int siz = initiativeHeaderBOVector.size();
        int acheived = 0;
        float precent = 0;
        try {
            for (int j = 0; j < siz; j++) {
                InitiativeHeaderBO sbd = initiativeHeaderBOVector.get(j);
                if (sbd.isDistributed())
                    acheived = acheived + 1;
            }

            if (acheived > 0) {
                precent = ((float) acheived / (float) siz) * 100;
            } else {
                precent = 0;
            }

        } catch (Exception e) {
            Commons.print(InitiativeActivity.class.getName() +
                    ",Error calculating the Initiative precent. \n"
                    + e.toString());
        }

        storeInitiativePrecentageInDB(SDUtil.roundIt(precent, 2) + "", acheived);
        bmodel.getAppDataProvider().getRetailMaster().setInitiative_achieved(acheived);
        bmodel.getAppDataProvider().getRetailMaster().setInitiativePercent(
                SDUtil.roundIt(precent, 2) + "");

    }

    /**
     * Store the Initiative acheived precentage vale in RetailerMaster's
     * initiativepercent field.
     *
     * @param percent
     */
    private void storeInitiativePrecentageInDB(String percent, float achieved) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update " + DataMembers.tbl_retailerMaster
                    + " set initiativepercent=" + QT(percent)
                    + " ,initiative_achieved=" + QT(achieved + "")
                    + " where retailerid="
                    + QT(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    private void updateInitiativeCoverageReport(String retailerId,
                                               boolean calculateBalance) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.executeQ("delete from InitiativeCoverageReport where retailerid="
                    + retailerId);

            StringBuilder queryString = new StringBuilder();
            StringBuilder queryString2 = new StringBuilder();

            for (InitiativeHeaderBO initiativeHeaderBOTemp : initiativeHeaderBOVector) {

                String initId = initiativeHeaderBOTemp.getInitiativeId() + "";
                String status = initiativeHeaderBOTemp.isDistributed() ? "1"
                        : "0";
                String colValues = initId + "," + retailerId + "," + status;

                String colValues2 = null;
                if (calculateBalance) {
                    float bal = initiativeHeaderBOTemp.getValueBalance() < 0 ? 0
                            : initiativeHeaderBOTemp.getValueBalance();
                    colValues2 = initId
                            + ","
                            + retailerId
                            + ","
                            + DatabaseUtils
                            .sqlEscapeString(initiativeHeaderBOTemp
                                    .getDescription()) + "," + bal
                            + "," + bal;
                }

                if (queryString.length() == 0) {
                    queryString.append("INSERT INTO ")
                            .append("InitiativeCoverageReport")
                            .append(" ( InitId,RetailerId,isDone ) ")
                            .append("SELECT ").append(colValues);

                    if (calculateBalance)
                        queryString2
                                .append("INSERT INTO ")
                                .append("InitiativeBalance")
                                .append(" ( InitId,RetailerId,InitDesc,balance,balanceEdit ) ")
                                .append("SELECT ")
                                .append(colValues2);

                } else {
                    queryString.append(" UNION ALL SELECT ").append(
                            colValues);
                    if (calculateBalance)
                        queryString2.append(" UNION ALL SELECT ").append(
                                colValues2);
                }

            }

            if (queryString.length() > 0)
                db.multiInsert(queryString.toString());

            if (queryString2.length() > 0 && calculateBalance)
                db.multiInsert(queryString2.toString());

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * this method will load Initiative mapped for all today's retailers and
     * update the covered status in InitiativeCoverageReport table.
     */
    public void generateInitiativeCoverageReport() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

            db.openDataBase();
            Cursor c = db
                    .selectSQL("select RM.RetailerId,RM.subChannelId from Retailermaster RM inner join RetailerMasterInfo RMI"
                            + " on RM.RetailerId=RMI.RetailerId  where RMI.isToday=1");
            if (c != null) {
                while (c.moveToNext()) {
                    downloadInitiativeHeader(c.getInt(1));
                    loadInitiativeStatus(true, c.getString(0), true, true,
                            c.getInt(1));
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private InitiativeHeaderBO checkInitiativeForCoverage(
            InitiativeHeaderBO initHeaderBO, String retailerId, int subChannelId) {

        String type = initHeaderBO.getType(); // Value || Qty
        String keyword = initHeaderBO.getKeyword(); // AND || ONLY || ANY
        int isCombination = initHeaderBO.getIsCombination(); // 1 || 0

        Vector<InitiativeDetailBO> initiativeDetailVector = downloadInitiativeDetail(
                initHeaderBO, retailerId, subChannelId);

        int initiativeDetailsiz = initiativeDetailVector.size();

        for (int j = 0; j < initiativeDetailsiz; j++) {

            InitiativeDetailBO initDetail = initiativeDetailVector.get(j);

            if (isCombination == 1) {
                // consider it as an initiative and call the Initiative
                // execution method

                InitiativeHeaderBO initHeader = downloadInitiativeHeaderofInitiative(initDetail
                        .getProductId());
                initHeader = checkInitiativeForCoverage(initHeader, retailerId,
                        subChannelId);
                initDetail.setDone(initHeader.isDone());
                initDetail.setDistributed(initHeader.isDistributed());
                initDetail.setAcheivedValue(initHeader.getSum());
                initDetail.setDropQtyValueAcheivement(initHeader
                        .getDropValueAchieved());

                Commons.print(initHeader.isDone() + " Is Done");
                Commons.print(initHeader.isDistributed() + " Is Distributed");
                Commons.print(initHeader.getSum() + " Sum");

            } else {
                if (type.equals(VALUE_TYPE)) {

                    if (initDetail.getInitiativeBalanceValue() <= 0) {
                        initiativeDetailVector.get(j).setDone(true);

                    } else {
                        initiativeDetailVector.get(j).setDone(false);
                    }

                    // To check Initiative distribution
                    if (initDetail.getInitiativeBalanceValue() < initDetail
                            .getInitiativeValue()) {
                        initiativeDetailVector.get(j).setDistributed(true);
                    } else {
                        initiativeDetailVector.get(j).setDistributed(false);
                    }

                    initiativeDetailVector.get(j).setAcheivedValue(
                            (initDetail.getInitiativeValue() - initDetail
                                    .getInitiativeBalanceValue()));

                } else if (type.equals(QUANTITY_TYPE)) {
                    if (initDetail.getInitiativeBalanceValue() <= 0) {
                        initiativeDetailVector.get(j).setDone(true);

                    } else {
                        initiativeDetailVector.get(j).setDone(false);
                    }

                    // To check Initiative distribution
                    if (initDetail.getInitiativeBalanceValue() < initDetail
                            .getInitiativeValue()) {
                        initiativeDetailVector.get(j).setDistributed(true);
                    } else {
                        initiativeDetailVector.get(j).setDistributed(false);
                    }

                    initiativeDetailVector.get(j).setAcheivedValue(
                            (initDetail.getInitiativeValue() - initDetail
                                    .getInitiativeBalanceValue()));
                }

            }

        }

        initHeaderBO
                .setDropValueAchieved(getSumDropAchd(initiativeDetailVector));

        // This value would be helpfull to calcualte hit in combination
        // Initiative.
        initHeaderBO.setSum(getSum(initiativeDetailVector));

        float dropQtyValueBal = getSumDropTgt(initiativeDetailVector)
                - getSumDropAchd(initiativeDetailVector);
        initHeaderBO.setValueBalance(dropQtyValueBal < 0 ? 0 : dropQtyValueBal);

        if (keyword.equals("AND")) {
            initHeaderBO
                    .setDistributed(checkForAndDist(initiativeDetailVector));
        } else {
            initHeaderBO
                    .setDistributed(checkForAnyOnlyDist(initiativeDetailVector));
        }

        switch (keyword) {
            case "AND":
                initHeaderBO.setDone(checkForAnd(initiativeDetailVector));
                break;
            case "ANY":
                initHeaderBO.setDone(checkForAny(initiativeDetailVector));
                break;
            case "ONLY":
                initHeaderBO.setDone(checkForOnly(initiativeDetailVector));
                break;
        }

        return initHeaderBO;

    }

    private float getSumDropAchd(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        float acheived = 0;
        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            acheived = acheived
                    + initiativeDetailVector.get(i)
                    .getDropQtyValueAcheivement();
        }
        return acheived;
    }

    private float getSumDropTgt(
            Vector<InitiativeDetailBO> initiativeDetailVector) {
        float target = 0;

        for (int i = 0; i < initiativeDetailVector.size(); i++) {
            target = target
                    + initiativeDetailVector.get(i).getDropQtyValueTarget();
        }
        if (bmodel.configurationMasterHelper.IS_CUMULATIVE_AND)
            return initiativeDetailVector.get(0).getDropQtyValueTarget();
        else
            return target;
    }

    public Vector<InitiativeHeaderBO> getInitiativeHeaderBOVector() {
        return initiativeHeaderBOVector;
    }

}
