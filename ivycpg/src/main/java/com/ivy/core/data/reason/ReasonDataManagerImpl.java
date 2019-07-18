package com.ivy.core.data.reason;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class ReasonDataManagerImpl implements ReasonDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public ReasonDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = mDbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    /**
     * Download sales return reason from reason master.
     * Saleable reason (SR) and Non-salable reason (SRS) will be downlaoded.
     */
    @Override
    public Observable<ArrayList<SalesReturnReasonBO>> fetchSalesReturnReasons() {
        return Observable.fromCallable(new Callable<ArrayList<SalesReturnReasonBO>>() {
            @Override
            public ArrayList<SalesReturnReasonBO> call() throws Exception {
                ArrayList<SalesReturnReasonBO> reasonSalesReturnList = new ArrayList<>();
                SalesReturnReasonBO reason;
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT A.ListId, A.ListName, B.ListCode FROM StandardListMaster A"
                            + " INNER JOIN StandardListMaster B ON A.ParentId = B.ListId AND"
                            + " ( B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                            + "' OR B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "')"
                            + " WHERE A.ListType = 'REASON'";
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        reasonSalesReturnList = null;
                        reasonSalesReturnList = new ArrayList<>();
                        while (c.moveToNext()) {
                            reason = new SalesReturnReasonBO();
                            reason.setReasonID(c.getString(0));
                            reason.setReasonDesc(c.getString(1));
                            reason.setReasonCategory(c.getString(2));
                            reasonSalesReturnList.add(reason);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonSalesReturnList;

                } catch (Exception ignored) {
                    shutDownDb();
                }

                return reasonSalesReturnList;
            }
        });
    }

    /**
     * This method used to fetch Non Planned Reasons from SLM table
     *
     * @return
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchNonPlannedReasons() {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                ArrayList<ReasonMaster> nonPlannedReasonList = new ArrayList<>();
                ReasonMaster reasonBo;
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT A.ListId, A.ListName, A.ListCode FROM StandardListMaster A"
                            + " INNER JOIN StandardListMaster B ON A.ParentId = B.ListId AND "
                            + " ( B.ListCode = '" + StandardListMasterConstants.NON_PLANNED_REASON_TYPE
                            + "') WHERE A.ListType ='REASON'";
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        nonPlannedReasonList = null;
                        nonPlannedReasonList = new ArrayList<>();
                        while (c.moveToNext()) {
                            reasonBo = new ReasonMaster();
                            reasonBo.setReasonID(c.getString(0));
                            reasonBo.setReasonDesc(c.getString(1));
                            reasonBo.setReasonCategory(c.getString(2));
                            nonPlannedReasonList.add(reasonBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return nonPlannedReasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return nonPlannedReasonList;
            }
        });
    }


    /*
    This method used to fetch non Productive Reasons based on retailer id and module Name
     */
    @Override
    public Single<NonproductivereasonBO> fetchNonProductiveReasons(String retailerID, String moduleName) {
        return Single.fromCallable(new Callable<NonproductivereasonBO>() {
            NonproductivereasonBO reasonsWithPhotoBo = new NonproductivereasonBO();

            @Override
            public NonproductivereasonBO call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT RetailerID,ModuleCode,ReasonID,ImagePath,ImageName FROM NonProductiveModules "
                            + " WHERE RetailerID = '" + retailerID + "' and ModuleCode = '" + moduleName + "' and upload = 'N'";

                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            reasonsWithPhotoBo.setRetailerid(c.getString(0));
                            reasonsWithPhotoBo.setModuleCode(c.getString(1));
                            reasonsWithPhotoBo.setReasonid(c.getString(2));
                            reasonsWithPhotoBo.setImagePath(c.getString(3));
                            reasonsWithPhotoBo.setImageName(c.getString(4));
                            break;
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonsWithPhotoBo;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return reasonsWithPhotoBo;
            }
        });
    }

    /*
     This method used to return  reason with photo capture is available
      or not based on retailer id and Module Name
     */
    @Override
    public Single<Boolean> isNpReasonPhotoAvailable(String retailerID, String moduleName) {
        return Single.fromCallable(new Callable<Boolean>() {
            boolean isAvaiable = false;

            @Override
            public Boolean call() throws Exception {

                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT ReasonID  FROM NonProductiveModules "
                            + " WHERE RetailerID = '" + retailerID + "' and ModuleCode = '" + moduleName + "' and upload = 'N'";

                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        if (c.getCount() > 0) {
                            isAvaiable = true;
                        }
                        c.close();
                    }
                } catch (Exception ignore) {
                    shutDownDb();
                }
                shutDownDb();
                return isAvaiable;
            }
        });
    }

    /*
    This method used to fetch plane deviation reason based on ListType
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetPlaneDeviatedReasons(String listType) {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> planedDeviateReasonList = new ArrayList<>();
            ReasonMaster reasonMasterBo;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT ListId, ListName,CASE WHEN ifnull(P.id,0) >0 THEN 1 ELSE 0 END as planned FROM StandardListMaster S" +
                            " Left join PlannedNonFieldActivity P on P.id=S.ListId WHERE ListType =" + StringUtils.getStringQueryParam(listType);
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            reasonMasterBo = new ReasonMaster();
                            reasonMasterBo.setReasonID(c.getString(0));
                            reasonMasterBo.setReasonDesc(c.getString(1));
                            reasonMasterBo.setIsPlanned(c.getInt(2));
                            planedDeviateReasonList.add(reasonMasterBo);
                        }
                        c.close();
                        reasonMasterBo = new ReasonMaster();
                        reasonMasterBo.setReasonID("0");
                        reasonMasterBo.setReasonDesc("Others");
                        reasonMasterBo.setIsPlanned(1);
                        planedDeviateReasonList.add(reasonMasterBo);
                    }
                    shutDownDb();
                    return planedDeviateReasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return planedDeviateReasonList;
            }
        });
    }

    /*
    This method used to fetch planned activities reasons based on ListType
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchPlanedActivitiesReason(String listType) {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> plannedActivitiesReasonList = new ArrayList<>();
            ReasonMaster reasonMasterBO;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();
                    String s = "SELECT ListId, ListName FROM StandardListMaster S INNER JOIN PlannedNonFieldActivity P on P.id=S.ListId WHERE ListType =" + StringUtils.getStringQueryParam(listType);
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            reasonMasterBO = new ReasonMaster();
                            reasonMasterBO.setReasonID(c.getString(0));
                            reasonMasterBO.setReasonDesc(c.getString(1));
                            plannedActivitiesReasonList.add(reasonMasterBO);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return plannedActivitiesReasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return plannedActivitiesReasonList;
            }
        });
    }

    /*
    This method used to fetch reasons from SLM Table
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchReasons() {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> reasonMasterArrayList = new ArrayList<>();
            ReasonMaster reasonMasterBo;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();
                    StringBuilder sb = new StringBuilder();
                    sb.append("SELECT A.ListId, A.ListName, B.ListCode FROM StandardListMaster A");
                    sb.append(" INNER JOIN StandardListMaster B ON A.ParentId = B.ListId");
                    sb.append(" WHERE A.ListType = 'REASON'");

                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    if (c.getCount() > 0) {
                       /* reasonMasterBo = new ReasonMaster();
                        reasonMasterBo.setReasonID("0");
                        reasonMasterBo.setReasonDesc(context.getResources().getString(R.string.plain_select));
                        reasonMasterBo.setReasonCategory("NONE");
                        reasonMasterArrayList.add(reasonMasterBo);*/
                        while (c.moveToNext()) {
                            reasonMasterBo = new ReasonMaster();
                            reasonMasterBo.setReasonID(c.getString(0));
                            reasonMasterBo.setReasonDesc(c.getString(1));
                            reasonMasterBo.setReasonCategory(c.getString(2));
                            reasonMasterArrayList.add(reasonMasterBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonMasterArrayList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return reasonMasterArrayList;
            }
        });
    }

    /*
    This method used to fetch Sales Return Category reasons based on two type : SR,SRS
    SR  - SALES RETURN NON-SALABLE REASON TYPE
    SRS - SALES RETURN SALABLE REASON TYPE
     */
    @Override
    public Observable<ArrayList<StandardListBO>> fetchSRCategoryReasons() {
        return Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            ArrayList<StandardListBO> srcCategoryReasonList = new ArrayList<>();
            StandardListBO categoryBo;

            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT A.ListId, A.ListName, A.ListCode FROM StandardListMaster A"
                            + "  Where (A.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                            + "' OR A.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "') and A.ListType = 'REASON_TYPE' ";
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            categoryBo = new StandardListBO();
                            categoryBo.setListID(c.getString(0));
                            categoryBo.setListName(c.getString(1));
                            categoryBo.setListCode(c.getString(2));
                            srcCategoryReasonList.add(categoryBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return srcCategoryReasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return srcCategoryReasonList;
            }
        });
    }

    /*@Override
    public Observable<ArrayList<ReasonMaster>> fetchCloseCallReasons() {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> ccReasonList = new ArrayList<>();
            ReasonMaster closeCallBo;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {

                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();
                    Cursor c = mDbUtil.selectSQL(getReasonFromStdListMaster("CLCR"));

                    if (c != null) {
                        while (c.moveToNext()) {
                            closeCallBo = new ReasonMaster();
                            closeCallBo.setReasonID(c.getString(0));
                            closeCallBo.setReasonDesc(c.getString(1));
                            ccReasonList.add(closeCallBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return ccReasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return ccReasonList;
            }
        });
    }*/

    /*
    This method used to fetch reason's from SLM table based on ListCode
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchReasonFromStdListMasterByListCode(String mReasonListCode) {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> reasonList = new ArrayList<>();
            ReasonMaster reasonMasterBo;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String query = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REASON'"
                            + " AND ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = '" + mReasonListCode + "')";

                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            reasonMasterBo = new ReasonMaster();
                            reasonMasterBo.setReasonID(c.getString(0));
                            reasonMasterBo.setReasonDesc(c.getString(1));
                            reasonList.add(reasonMasterBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return reasonList;
            }
        });
    }

    /*
    This method used to fetch reason's from SLM table based on ListType
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchReasonsFromStdListMasterByListType(String listType) {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> reasonMasterArrayList = new ArrayList<>();
            ReasonMaster reasonMasterBo;

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType =" + StringUtils.getStringQueryParam(listType);
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            reasonMasterBo = new ReasonMaster();
                            reasonMasterBo.setReasonID(c.getString(0));
                            reasonMasterBo.setReasonDesc(c.getString(1));
                            reasonMasterArrayList.add(reasonMasterBo);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonMasterArrayList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return reasonMasterArrayList;
            }
        });
    }

    /*
    This method used to fetch Retailer address from SLM table based on retailer id and addressCode(ListCode)
    and List Type should be ADDRESS_TYPE
     */
    @Override
    public Observable<ArrayList<ReasonMaster>> fetchRetailerAddressFromSLM(String retailerID, String addressCode) {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            ArrayList<ReasonMaster> retailerAddressList = new ArrayList<>();
            ReasonMaster addressBO;
            StringBuilder addressSB = new StringBuilder();

            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    if (addressCode.contains(",")) {
                        String[] addressArray = addressCode.split(",");
                        int i = 0;
                        for (String addCodes : addressArray) {
                            if (addressSB.length() > 0)
                                addressSB.append(",");

                            addressSB.append(StringUtils.getStringQueryParam(addCodes));
                            i = i + 1;
                        }
                    } else
                        addressSB.append(addressCode);

                    String s = "SELECT RA.AddressId,RA.Address1,RA.Address2,RA.Address3  FROM StandardListMaster SLM"
                            + " LEFT JOIN RetailerAddress RA ON SLM.ListId = RA.AddressTypeID"
                            + " WHERE RA.RetailerId =" + retailerID
                            + " AND SLM.ListCode in(" + addressSB.toString() + ")"
                            + " AND ListType = 'ADDRESS_TYPE'";
                    Cursor c = mDbUtil.selectSQL(s);
                    if (c != null) {
                        while (c.moveToNext()) {
                            addressBO = new ReasonMaster();
                            StringBuilder address = new StringBuilder();
                            addressBO.setReasonID(c.getString(0));

                            address.append(c.getString(1));
                            address.append(", ");

                            if (c.getString(2).length() > 0) {
                                address.append(c.getString(2));
                                address.append(", ");
                            }
                            if (c.getString(3).length() > 0) {
                                address.append(c.getString(3));
                                address.append(",");
                            }

                            addressBO.setReasonDesc(address.toString());
                            retailerAddressList.add(addressBO);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return retailerAddressList;
                } catch (Exception ignore) {
                    shutDownDb();
                }
                return retailerAddressList;
            }
        });
    }

    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }

    /*private String getReasonFromStdListMaster(String mReasonTypeCode) {
        return ("SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REASON'"
                + " AND ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = '" + mReasonTypeCode + "')");
    }*/
}
