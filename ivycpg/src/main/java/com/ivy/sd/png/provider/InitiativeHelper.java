package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.InitiativeDetailBO;
import com.ivy.sd.png.bo.InitiativeHeaderBO;
import com.ivy.sd.png.bo.InitiativeHolder;
import com.ivy.sd.png.bo.InitiativeReportBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.InitiativeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class InitiativeHelper {

    public static final String QUANTITY_TYPE = "QTY";
    public static final String VALUE_TYPE = "SV";
    private static InitiativeHelper instance = null;

    private Context mContext;
    private Vector<InitiativeHeaderBO> initiativeHeaderBOVector;
    private ArrayList<InitiativeReportBO> initiativeReportBO;
    private BusinessModel bmodel;
    private List<InitiativeHolder> initativeList;
    private StringBuilder sb;
    private Vector<InitiativeReportBO> initlist;

    protected InitiativeHelper(Context context) {
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            }
            c.close();
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            }
            c.close();
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

    public InitiativeHeaderBO checkInitiative(InitiativeHeaderBO initHeaderBO,
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

                            float val = (float) (productMasterBO
                                    .getOrderedPcsQty() * productMasterBO
                                    .getSrp())
                                    + (productMasterBO.getOrderedCaseQty() * productMasterBO
                                    .getCsrp())
                                    + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                    .getOsrp());
                            float init_val = (float) (productMasterBO
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

        if (keyword.equals("AND")) {
            initHeaderBO.setDone(checkForAnd(initiativeDetailVector));
        } else if (keyword.equals("ANY")) {
            initHeaderBO.setDone(checkForAny(initiativeDetailVector));
        } else if (keyword.equals("ONLY")) {
            initHeaderBO.setDone(checkForOnly(initiativeDetailVector));
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

            float target = 0;
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

        if (acheived >= target)
            return true;
        else
            return false;

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
        if (initiativeDetailVector.size() > 0) {
            if (initiativeDetailVector.get(0).isDone())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    public Vector<InitiativeDetailBO> downloadInitiativeDetail(
            InitiativeHeaderBO initId, String retailerId, int subChannelId) {
        Vector<InitiativeDetailBO> initiativeDetailBO = new Vector<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String target = "";
            String achievement = "";
            if (initId.getType().equals("QTY")) {
                target = "A.DropQtyValue";
                achievement = "ifnull(B.Value,0)";
            } else {
                target = "A.TargetValue";
                achievement = "ifnull(B.Qty,0)";
            }

            Cursor c = null;
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
        Vector<InitiativeDetailBO> initiativeDetailBO = new Vector<InitiativeDetailBO>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
                            + bmodel.getRetailerMasterBO().getRetailerID()
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

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = null;

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
            product = (ProductMasterBO) bmodel.productHelper.getProductMaster()
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
            downloadInitiativeHeader(bmodel.getRetailerMasterBO()
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
        bmodel.getRetailerMasterBO().setInitiative_achieved(acheived);
        bmodel.getRetailerMasterBO().setInitiativePercent(
                SDUtil.roundIt(precent, 2) + "");

    }

    /**
     * Store the Initiative acheived precentage vale in RetailerMaster's
     * initiativepercent field.
     *
     * @param percent
     */
    public void storeInitiativePrecentageInDB(String percent, float achieved) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update " + DataMembers.tbl_retailerMaster
                    + " set initiativepercent=" + QT(percent)
                    + " ,initiative_achieved=" + QT(achieved + "")
                    + " where retailerid="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    /**
     * This method will load the list of Initiative with number of retailers it
     * mapped.
     * <p>
     * Reports used in VisitScreen
     */
    public void loadInitiativeReportTarget() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select initid,initDesc,InitKeyWord from InitiativeHeaderMaster where isParent=1");

            setInitiativeReportBO(new ArrayList<InitiativeReportBO>());

            if (c != null) {
                while (c.moveToNext()) {
                    InitiativeReportBO init = new InitiativeReportBO();
                    init.setInitId(c.getInt(0));
                    init.setInitDesc(c.getString(1));
                    init.setInitKeyWord(c.getString(2));
                    initiativeReportBO.add(init);
                }
                c.close();
            }

            for (InitiativeReportBO initReportBO : initiativeReportBO) {

                String sql = "SELECT count( distinct A.retailerid) FROM InitiativeCoverageReport A "
                        + " inner join Retailermasterinfo RMI on RMI.retailerid= B.retailerid "
                        + "inner join RetailerMaster B on A.retailerId=B.retailerid and RMI.isToday=1 where A.initid="
                        + initReportBO.getInitId();
                c = db.selectSQL(sql);
                if (c != null) {
                    if (c.moveToNext())
                        initReportBO.setTarget(c.getInt(0));
                }

                sql = "SELECT count( distinct A.retailerid) FROM InitiativeCoverageReport A "
                        + " inner join Retailermasterinfo RMI on RMI.retailerid= B.retailerid "
                        + "inner join RetailerMaster B on A.retailerId=B.retailerid and RMI.isToday=1 where A.initid="
                        + initReportBO.getInitId() + " and A.isDone=1";
                c = db.selectSQL(sql);
                if (c != null) {
                    if (c.moveToNext())
                        initReportBO.setAcheived(c.getInt(0));
                    c.close();
                }

            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateInitiativeCoverageReport(String retailerId,
                                               boolean calculateBalance) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
                            .append("SELECT ").append(colValues.toString());

                    if (calculateBalance)
                        queryString2
                                .append("INSERT INTO ")
                                .append("InitiativeBalance")
                                .append(" ( InitId,RetailerId,InitDesc,balance,balanceEdit ) ")
                                .append("SELECT ")
                                .append(colValues2.toString());

                } else {
                    queryString.append(" UNION ALL SELECT ").append(
                            colValues.toString());
                    if (calculateBalance)
                        queryString2.append(" UNION ALL SELECT ").append(
                                colValues2.toString());
                }

            }

            if (queryString.length() > 0)
                db.multiInsert(queryString.toString());

            if (queryString2.length() > 0 && calculateBalance)
                db.multiInsert(queryString2.toString());

            db.closeDB();
        } catch (Exception e) {

        }
    }

    /**
     * this method will load Initiative mapped for all today's retailers and
     * update the covered status in InitiativeCoverageReport table.
     */
    public void generateInitiativeCoverageReport() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

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

    public InitiativeHeaderBO checkInitiativeForCoverage(
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

        if (keyword.equals("AND")) {
            initHeaderBO.setDone(checkForAnd(initiativeDetailVector));
        } else if (keyword.equals("ANY")) {
            initHeaderBO.setDone(checkForAny(initiativeDetailVector));
        } else if (keyword.equals("ONLY")) {
            initHeaderBO.setDone(checkForOnly(initiativeDetailVector));
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

    public ArrayList<InitiativeReportBO> getInitiativeReportBO() {
        return initiativeReportBO;
    }

    public void setInitiativeReportBO(
            ArrayList<InitiativeReportBO> initiativeReportBO) {
        this.initiativeReportBO = initiativeReportBO;
    }

    public Vector<InitiativeHeaderBO> getInitiativeHeaderBOVector() {
        return initiativeHeaderBOVector;
    }

	/*public void downloadInitiativeandInsertinRetailerInfoMaster() {
        DBUtil db = null;
		try {
			db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			db.deleteSQL("RetailerMasterInfo",null,true);

			StringBuffer sb = new StringBuffer();

			sb.append("select count(ChannelId) from SbdMerchandisingMaster");
			sb.append(" WHERE TypeListId=(select ListId from StandardListMaster where ListCode='MERCH_INIT')");

			Cursor c = db.selectSQL(sb.toString());
			if (c != null) {
				if (c.moveToNext()) {
					sb = new StringBuffer();
					if(c.getInt(0)>0){
						sb.append("INSERT INTO RetailerMasterInfo (RetailerId,IS_SBDMerchTarget)");
						sb.append(" SELECT DISTINCT R.RetailerID, IFNULL(A.sbdtgt,0) as tgt FROM RetailerMaster R");
						sb.append(" LEFT JOIN ( select distinct ChannelId, count (sbdid) as sbdtgt from SbdMerchandisingMaster");
						sb.append(" WHERE TypeListId=(select ListId from StandardListMaster where ListCode='MERCH_INIT')");
						sb.append(" group by ChannelId) A ON A.ChannelId = R.ChannelId");
					} else {
						sb.append("INSERT INTO RetailerMasterInfo(RetailerId,IS_SBDMerchTarget)");
						sb.append(" SELECT DISTINCT R.RetailerID, 0 as tgt FROM RetailerMaster R");
					}
				}
				c.close();
			}
			db.executeQ(sb.toString());
			db.closeDB();
		} catch (Exception e) {

			Commons.printException(e);
		}
	}*/
    // Initative Report Fragment

    /**
     * Load the data needed for title view.
     */
    public void generateIntiativeView() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            initativeList = new ArrayList<InitiativeHolder>();
            Cursor c = db
                    .selectSQL("SELECT InitId, InitDesc, IsParent FROM InitiativeHeaderMaster  where isParent=1  ORDER BY InitDesc ASC");
            if (c != null) {
                while (c.moveToNext()) {
                    InitiativeHolder holder = new InitiativeHolder();
                    holder.setInitiativeId(c.getInt(0));
                    holder.setInitiativeDesc(c.getString(1));
                    holder.setIsParent(c.getInt(2));
                    initativeList.add(holder);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    /**
     * Calculates the total Initiative achieved today
     */
    public void downloadInitTotalValue() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;
            if (bmodel.configurationMasterHelper.IS_INVOICE) {
                c = db.selectSQL("select CASE WHEN IHM.IsCombination = 0 AND IHM.IsParent = 0 THEN (SELECT DISTINCT(InitId) FROM InitiativeDetailMaster WHERE ProductInitId = IDM.InitId) "
                        + "ELSE IDM.InitId END,sum(OH.qty*OH.rate) from InvoiceDetails OH inner join InitiativeDetailMaster IDM "
                        + "on IDM.ProductInitId=OH.ProductID inner join InitiativeHeaderMaster  IHM on IHM.InitId = IDM.InitId "
                        + "where  CASE WHEN IHM.IsCombination = 0 AND IHM.IsParent = 0 THEN "
                        + "(SELECT DISTINCT(InitId) FROM InitiativeDetailMaster WHERE ProductInitId = IDM.InitID) ELSE IDM.InitId END "
                        + "in (select InitId from InitiativeCoverageReport) group by IDM.InitId order by IDM.rowid");
            } else {
                c = db.selectSQL("select CASE WHEN IHM.IsCombination = 0 AND IHM.IsParent = 0 THEN (SELECT DISTINCT(InitId) FROM InitiativeDetailMaster WHERE ProductInitId = IDM.InitId) "
                        + "ELSE IDM.InitId END,sum(OH.qty*OH.rate) from orderdetail OH inner join InitiativeDetailMaster IDM "
                        + "on IDM.ProductInitId=OH.ProductID inner join InitiativeHeaderMaster  IHM on IHM.InitId = IDM.InitId "
                        + "where  CASE WHEN IHM.IsCombination = 0 AND IHM.IsParent = 0 THEN "
                        + "(SELECT DISTINCT(InitId) FROM InitiativeDetailMaster WHERE ProductInitId = IDM.InitID) ELSE IDM.InitId END "
                        + "in (select InitId from InitiativeCoverageReport) group by IDM.InitId order by IDM.rowid");
            }
            if (c != null) {
                while (c.moveToNext()) {

                    for (int j = 0; j < bmodel.initiativeHelper
                            .getInitativeList().size(); j++) {
                        InitiativeHolder bo = bmodel.initiativeHelper
                                .getInitativeList().get(j);
                        if (c.getInt(0) == bo.getInitiativeId()) {
                            bo.setTotalInitiative(c.getDouble(1));
                            break;
                        }
                    }

                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {

            Commons.printException(e);
        }
    }

    /**
     * Get the retailer, initiative and initiative hit
     *
     * @return Vector<InitiativeReportBO>
     */
    public Vector<InitiativeReportBO> downloadInitReport() {
        initlist = new Vector<InitiativeReportBO>();
        try {
            InitiativeReportBO initbo;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int seq = 1;
            Cursor c1 = null;
            sb = new StringBuilder();
            Cursor c = db
                    .selectSQL("select distinct(B.RetailerId),B.RetailerName,RBM.isDeviated," +
                            "when RC.weekno not null  then RC.WalkingSeq when RC.Date='2015/10/05' then RC.WalkingSeq else 0 end as seq,"
                            + " from Retailermaster  B "
                            + "  inner join Retailermasterinfo RMI on Rmi.retailerid= B.retailerid"
                            + " left join RetailerClientMappingMaster RC on RC.rid=B.RetailerID"
                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = B.RetailerID"
                            + " where  (RMI.isToday=1 or RBM.isdeviated='Y')  order by seq");
            if (c != null) {
                while (c.moveToNext()) {
                    initbo = new InitiativeReportBO();
                    int tempInitHit[] = null;
                    if (initativeList.size() > 0)
                        tempInitHit = new int[initativeList.size()];
                    initbo.setRetailerid(c.getInt(0));
                    initbo.setRetailername(c.getString(1));
                    sb.append(c.getString(0));
                    sb.append(",");
                    if (!c.getString(2).equalsIgnoreCase("Y")) {
                        initbo.setWalkingSequence(seq);
                        seq++;
                    } else {
                        initbo.setWalkingSequence(1000000);
                    }
                    initbo.setIsdeviated(c.getString(2));
                    c1 = db.selectSQL("SELECT DISTINCT(IHM.InitId), IFNULL(isDone,0) FROM InitiativeHeaderMaster IHM LEFT JOIN  InitiativeCoverageReport "
                            + "ICR  ON ICR.InitId = IHM.InitId  AND RetailerId="
                            + initbo.getRetailerid()
                            + " where IHM.IsParent= 1 order by IHM.InitDesc ASC");
                    if (c1 != null) {
                        int itr = 0;
                        while (c1.moveToNext()) {

                            if (tempInitHit != null) {

                                tempInitHit[itr] = c1.getInt(1);
                                itr++;

                            }
                        }
                        if (tempInitHit != null)
                            initbo.setInitiativeHit(tempInitHit);
                        c1.close();
                    }
                    initlist.add(initbo);

                }
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                Cursor c2 = db
                        .selectSQL("select initid, sum(isDone) from InitiativeCoverageReport where RetailerId in ("
                                + sb.toString() + ")  group by initid");
                if (c2 != null) {
                    while (c2.moveToNext()) {
                        for (int z = 0; z < initativeList.size(); z++) {
                            InitiativeHolder bo = initativeList.get(z);
                            if (c2.getInt(0) == bo.getInitiativeId()) {
                                bo.setHitCount(c2.getInt(1));
                                break;
                            }
                        }
                    }
                    c2.close();
                }
            }


            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return initlist;

    }

    /**
     * Calculates the total of Initiative achieved today and past.
     */
    public void downloadInitMTDValue() {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String tableHeader = null;
            String tableDetail = null;
            String idOne = null;
            String idTwo = null;
            if (bmodel.configurationMasterHelper.IS_INVOICE) {
                tableHeader = "InvoiceMaster";
                tableDetail = "InvoiceDetails";
                idOne = "InvoiceNo";
                idTwo = "invoiceID";
            } else {
                tableHeader = "OrderHeader";
                tableDetail = "OrderDetail";
                idOne = "OrderID";
                idTwo = "OrderID";
            }
            Cursor c = db
                    .selectSQL("SELECT DISTINCT(RM.RetailerID),CASE WHEN IHM.IsCombination = 0 AND IHM.IsParent = 0 THEN"
                            + " (SELECT DISTINCT(InitId) FROM InitiativeDetailMaster WHERE ProductInitId = IDM.InitId) ELSE IDM.InitId END,"
                            + " IFNULL(OD.qty,0) AS OrdQty,IFNULL(IAM.qty,0) AS InitQty,"
                            + " CASE WHEN SLM .listcode='CASH' THEN ifnull(p1.srp1,p2.srp1) ELSE IFNULL(p1.srp2,p2.srp2) END,OD.PRoductId FROM RetailerMaster RM"
                            + " LEFT JOIN StandardListMaster SLM ON   RM.RPTypeId =SLM.ListId"
                            + " INNER JOIN InitiativeDetailMaster IDM on RM.SubChannelId = IDM.LocalChannelId"
                            + " LEFT JOIN " + tableHeader + " OH ON " + "RM.RetailerID = OH.RetailerId"
                            + " INNER JOIN InitiativeHeaderMaster IHM ON IDM.InitId = IHM.InitId"
                            + " LEFT JOIN " + tableDetail + " OD ON OH." + idOne + " = OD." + idTwo + " and OD.PRoductId = IDM.ProductInitId"
                            + " LEFT JOIN InitiativeAchievementMaster IAM ON IAM.Initid = IDM.InitId AND IAM.ProductId = IDM.ProductInitId AND IAM.RetailerId= RM.RetailerId"
                            + " LEFT JOIN RetailerPriceGroup RPG ON RPG.RetailerID = RM.RetailerID"
                            + " LEFT JOIN PriceMaster P1 ON  RPG.GroupId =P1.scid AND P1.pid = IDM.ProductInitId"
                            + " LEFT JOIN PriceMaster P2 ON  P2.scid = 0 AND  CASE WHEN IHM.IsCombination = 1 THEN P2.pid IN (SELECT A.ProductInitId FROM InitiativeDetailMaster A  "
                            + "INNER JOIN InitiativeDetailMaster B ON A.InitId = B.ProductInitId AND B.InitId = IDM.InitID AND B.LocalChannelId = RM.SubChannelId ) ELSE P2.pid =IDM.ProductInitId END WHERE (OrdQty>0 OR InitQty>0)");

            double total = 0;
            if (c != null) {
                while (c.moveToNext()) {
                    for (int j = 0; j < initativeList.size(); j++) {
                        InitiativeHolder bo = initativeList.get(j);
                        if (c.getInt(1) == bo.getInitiativeId()
                                && ((c.getDouble(2) > 0 || c.getDouble(3) > 0) && c
                                .getDouble(4) > 0)) {
                            total = 0;
                            total = ((c.getDouble(2) + c.getDouble(3)) * c
                                    .getDouble(4)) + bo.getTotalMTD();

                            bo.setTotalMTD(total);
                            break;
                        }
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {

            Commons.printException(e);
        }
    }

    public Vector<InitiativeReportBO> getInitlist() {
        return initlist;
    }

    public void setInitlist(Vector<InitiativeReportBO> initlist) {
        this.initlist = initlist;
    }

    public List<InitiativeHolder> getInitativeList() {
        return initativeList;
    }

}
