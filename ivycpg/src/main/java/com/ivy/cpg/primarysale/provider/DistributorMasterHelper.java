package com.ivy.cpg.primarysale.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 21-09-2015.
 */
public class DistributorMasterHelper {
    private static DistributorMasterHelper instance = null;
    private ArrayList<DistributorMasterBO> distributors;
    private Context context;
    private DistributorMasterBO distributor;
    private BusinessModel bmodel;
    private boolean isEditDistributorStockCheck;
    private boolean isEditDistributorOrder;

    /**
     * download the distributors list
     *
     * @return
     */

    public DistributorMasterHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static DistributorMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DistributorMasterHelper(context);
        }
        return instance;
    }

    /**
     * @deprecated
     * @see {@link com.ivy.core.data.distributor.DistributorDataManagerImpl#fetchDistributorList()}
     */
    @Deprecated
    public void downloadDistributorsList() {
        distributors = new ArrayList<>();

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select DISTINCT DId,DName, ParentID, IFNULL(GroupId,'') as GroupId from "
                    + DataMembers.tbl_DistributorMaster + " LEFT JOIN DistributorPriceMapping ON Did = DistId";

            Cursor c = db.selectSQL(sql);

            DistributorMasterBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new DistributorMasterBO();//DId,DName,CNumber,Address1,Address2,Address3,Type,TinNo
                    con.setDId(c.getString(c.getColumnIndex("DId")));
                    con.setDName(c.getString(c.getColumnIndex("DName")));
                    con.setParentID(c.getString(c.getColumnIndex("ParentID")));
                    /*con.setCNumber(c.getString(2));
                    con.setAddress1(c.getString(3));
                    con.setAddress2(c.getString(4));
                    con.setAddress3(c.getString(5));
                    con.setType(c.getString(6));
                    con.setTinNo(c.getString(7));*/
                    con.setGroupId(c.getString(c.getColumnIndex("GroupId")));
                    distributors.add(con);

                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }


    }

    public boolean hasAlreadyDistributorStockChecked(String distributorId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor orderHeaderCursor = db.selectSQL("select UId from DistStockCheckHeader where DistId='"
                    + distributorId + "' and upload='N'");
            if (orderHeaderCursor.getCount() > 0) {
                orderHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                orderHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {
            Commons.printException("hasAlreadyDistributorStockChecked", e);
            return false;
        }
    }

    public boolean hasAlreadyDistributorOrdered(String distributorId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor orderHeaderCursor = db.selectSQL("select UId from DistOrderHeader where DistId='"
                    + distributorId + "' AND Upload='N'");
            if (orderHeaderCursor.getCount() > 0) {
                orderHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                orderHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {
            Commons.printException("hasAlreadyDistributorOrdered", e);
            return false;
        }
    }

    public void saveDistributorClosingStock() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String id = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
            if (isEditDistributorStockCheck()) {
                Cursor closingStockCursor = db
                        .selectSQL("select UId from DistStockCheckHeader where DistId='"
                                + getDistributor().getDId() + "' and upload='N'");
                if (closingStockCursor.getCount() > 0) {
                    closingStockCursor.moveToNext();
                    id = bmodel.QT(closingStockCursor.getString(0));
                    db.deleteSQL(DataMembers.tbl_distributor_closingstock_header, "UId=" + id, false);
                    db.deleteSQL(DataMembers.tbl_distributor_closingstock_detail, "UId=" + id, false);
                }
                closingStockCursor.close();
            }

            // ClosingStock Header entry

            String columns = "UId,DistId,Date,DownloadedDate,Upload";

            String values = (id) + ", " + getDistributor().getDId() + ", " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + ", " + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()) + ", "
                    + bmodel.QT("N");

            db.insertSQL(DataMembers.tbl_distributor_closingstock_header, columns, values);

            ProductMasterBO product;

            // ClosingStock Detail entry
            columns = "UId,PId,BatchId,WarehouseId,Upload,UomId,UomCount,Qty";

            int siz = bmodel.productHelper.getProductMaster().size();
            for (int i = 0; i < siz; ++i) {
                product = bmodel.productHelper.getProductMaster().elementAt(i);

                if (product.getLocations().get(0).getShelfPiece() >-1
                        || product.getLocations().get(0).getShelfCase() > -1
                        || product.getLocations().get(0).getShelfOuter() > -1) {
                    values = (id) + "," + bmodel.QT(product.getProductID()) + ","
                            + 0
                            + ","
                            + 0
                            + ","
                            + bmodel.QT("N");
                    if (product.getLocations().get(0).getShelfPiece() > -1) {
                        db.insertSQL(DataMembers.tbl_distributor_closingstock_detail,
                                columns, values + "," + product.getPcUomid() + "," + 0 + "," + product.getLocations().get(0).getShelfPiece());
                    }
                    if (product.getLocations().get(0).getShelfCase() > -1) {
                        db.insertSQL(DataMembers.tbl_distributor_closingstock_detail,
                                columns, values + "," + product.getCaseUomId() + "," + product.getCaseSize() + "," + product.getLocations().get(0).getShelfCase());
                    }
                    if (product.getLocations().get(0).getShelfOuter() > -1) {
                        db.insertSQL(DataMembers.tbl_distributor_closingstock_detail,
                                columns, values + "," + product.getOuUomid() + "," + product.getOutersize() + "," + product.getLocations().get(0).getShelfOuter());
                    }
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    public void saveDistributorOrder() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String id = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
            if (isEditDistributorOrder()) {
                Cursor orderCursor = db
                        .selectSQL("select UId from " + DataMembers.tbl_distributor_order_header + " where DistId='"
                                + getDistributor().getDId() + "'");
                if (orderCursor.getCount() > 0) {
                    orderCursor.moveToNext();
                    id = bmodel.QT(orderCursor.getString(0));
                    db.deleteSQL(DataMembers.tbl_distributor_order_header, "UId=" + id, false);
                    db.deleteSQL(DataMembers.tbl_distributor_order_detail, "UId=" + id, false);
                }
                orderCursor.close();
            }
            // ClosingStock Header entry

            String columns = "UId,DistId,Date,DownloadedDate,Upload,TotalValue,LPC,DeliveryDate";

            String values = (id) + ", " + getDistributor().getDId() + ", " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + ", " + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()) + ", "
                    + bmodel.QT("N") + ", " + bmodel.QT(SDUtil.format(bmodel.getOrderHeaderBO().getOrderValue(), 2, 0) + "") + ", " + bmodel.getOrderHeaderBO().getLinesPerCall()
                    + ", " + bmodel.QT(bmodel.getOrderHeaderBO().getDeliveryDate());

            db.insertSQL(DataMembers.tbl_distributor_order_header, columns, values);

            ProductMasterBO product;

            // ClosingStock Detail entry
            columns = "UId,PId,BatchId,Upload,UomId,UomCount,Qty,Price,LineValue";
            OrderHelper.getInstance(context).setOrderId(id);
            int siz = bmodel.productHelper.getProductMaster().size();
            for (int i = 0; i < siz; ++i) {
                product = bmodel.productHelper.getProductMaster().elementAt(i);

                if (product.getOrderedCaseQty() > 0
                        || product.getOrderedPcsQty() > 0
                        || product.getOrderedOuterQty() > 0) {
                    values = (id) + "," + bmodel.QT(product.getProductID()) + ","
                            + 0
                            + ","
                            + bmodel.QT("N");
                    if (product.getOrderedPcsQty() > 0) {
                        db.insertSQL(DataMembers.tbl_distributor_order_detail,
                                columns, values + "," + product.getPcUomid() + "," + 0 + "," + product.getOrderedPcsQty() + "," + product
                                        .getSrp() + "," + bmodel.QT(SDUtil.format((product.getOrderedPcsQty() * product.getSrp()), 2, 0) + ""));
                    }
                    if (product.getOrderedCaseQty() > 0) {
                        db.insertSQL(DataMembers.tbl_distributor_order_detail,
                                columns, values + "," + product.getCaseUomId() + "," + product.getCaseSize() + "," + product.getOrderedCaseQty() + "," + product
                                        .getCsrp() + "," + bmodel.QT(SDUtil.format((product.getOrderedCaseQty() * product.getCsrp()), 2, 0) + ""));
                    }
                    if (product.getOrderedOuterQty() > 0) {
                        db.insertSQL(DataMembers.tbl_distributor_order_detail,
                                columns, values + "," + product.getOuUomid() + "," + product.getOutersize() + "," + product.getOrderedOuterQty() + "," + product
                                        .getOsrp() + "," + bmodel.QT(SDUtil.format((product.getOrderedOuterQty() * product.getOsrp()), 2, 0) + ""));
                    }
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    public Boolean hasDistributorOrder(String distid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            // Order Header

           String sb="Select UId from DistOrderHeader where DistId="+bmodel.QT(distid)+" AND Upload='N'";

            Cursor orderHeaderCursor = db.selectSQL(sb);

            if (orderHeaderCursor.getCount() > 0) {
                orderHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                orderHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {
            Commons.print(""+e);
            return false;
        }
    }


    public void loadDistributedStockCheckedProducts(String distributorId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String stockID = new String();
            // Order Header
            Cursor stockCheckedHeaderCursor = db.selectSQL("select UId from DistStockCheckHeader where DistId='"
                    + distributorId + "' and upload='N'");
            if (stockCheckedHeaderCursor != null) {
                if (stockCheckedHeaderCursor.moveToNext()) {
                    stockID = stockCheckedHeaderCursor.getString(0);
                }
            }
            stockCheckedHeaderCursor.close();

            String sql1 = "select PId,Qty,UomId,UomCount,BatchId,WarehouseId from "
                    + DataMembers.tbl_distributor_closingstock_detail
                    + " where UId="
                    + bmodel.QT(stockID);
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int Qty = orderDetailCursor.getInt(1);
                    int uomId = orderDetailCursor.getInt(2);
                    int UomCount = orderDetailCursor.getInt(3);
                    int locationId = orderDetailCursor.getInt(5);
                    setStockCheckQtyDetails(productId, Qty, uomId,
                            UomCount, locationId);
                }
            }
            orderDetailCursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    private void setStockCheckQtyDetails(String productid, int Qty,
                                         int uomId, int UomCount, int locationId) {
        ProductMasterBO product;
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            product = (ProductMasterBO) bmodel.productHelper.getProductMaster().get(i);
            if (product.getProductID().equals(productid)) {

                // for (int j = 0; j < product.getInStoreLocations().size(); j++) {
                //if (product.getInStoreLocations().get(j).getLocationId() == locationId) {
                        if (product.getPcUomid() == uomId) {
                            product.getLocations().get(0).setShelfPiece(Qty);
                        }
                        if (product.getCaseUomId() == uomId) {
                            product.getLocations().get(0).setShelfCase(Qty);
                        }
                        if (product.getOuUomid() == uomId) {
                            product.getLocations().get(0).setShelfOuter(Qty);
                        }
                        return;
                // }
                // }
            }
        }
        return;
    }

    public void loadDistributedOrderedProducts(String distributorId, String uID) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String orderID = new String();
            Cursor orderHeaderCursor = null;
            if (bmodel.getOrderHeaderBO() == null)
                bmodel.setOrderHeaderBO(new OrderHeader());
            // Order Header
            if (uID != null) {
                orderHeaderCursor = db.selectSQL("select UId,TotalValue,LPC,DeliveryDate from DistOrderHeader where UId='"
                        + uID + "'");
                if (orderHeaderCursor != null) {
                    if (orderHeaderCursor.moveToNext()) {
                        orderID = orderHeaderCursor.getString(0);
                        bmodel.getOrderHeaderBO().setOrderValue(orderHeaderCursor.getDouble(1));
                        bmodel.getOrderHeaderBO().setLinesPerCall(orderHeaderCursor.getInt(2));
                        bmodel.getOrderHeaderBO().setDeliveryDate(
                                orderHeaderCursor.getString(3));
                    }
                }
            } else {
                orderHeaderCursor = db.selectSQL("select UId,TotalValue,LPC from DistOrderHeader where DistId='"
                        + distributorId + "'");
                if (orderHeaderCursor != null) {
                    if (orderHeaderCursor.moveToNext()) {
                        orderID = orderHeaderCursor.getString(0);
                        bmodel.getOrderHeaderBO().setOrderValue(orderHeaderCursor.getDouble(1));
                        bmodel.getOrderHeaderBO().setLinesPerCall(orderHeaderCursor.getInt(2));
                    }
                }
            }
            orderHeaderCursor.close();

            String sql1 = "select PId,UomId,UomCount,Qty,Price,LineValue from "
                    + DataMembers.tbl_distributor_order_detail
                    + " where UId="
                    + bmodel.QT(orderID);
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int uomId = orderDetailCursor.getInt(1);
                    int UomCount = orderDetailCursor.getInt(2);
                    int Qty = orderDetailCursor.getInt(3);
                    setOrderDetails(productId, Qty, uomId,
                            UomCount);
                }
            }
            orderDetailCursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    private void setOrderDetails(String productid, int Qty,
                                 int uomId, int UomCount) {
        ProductMasterBO product;
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            product = (ProductMasterBO) bmodel.productHelper.getProductMaster().get(i);
            if (product.getProductID().equals(productid)) {
                if (product.getPcUomid() == uomId) {
                    product.setOrderedPcsQty(Qty);
                }
                if (product.getCaseUomId() == uomId) {
                    product.setOrderedCaseQty(Qty);
                }
                if (product.getOuUomid() == uomId) {
                    product.setOrderedOuterQty(Qty);
                }
                return;
            }
        }
        return;
    }

    public String getDeliveryDate() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        String date = "";
        // Order Header
        String sql = null;
        sql = "select deliveryDate from DistOrderHeader  where DistId =" + bmodel.QT(getDistributor().getDId()); // Its Order Id not
        Cursor orderHeaderCursor = db.selectSQL(sql);
        if (orderHeaderCursor != null) {
            if (orderHeaderCursor.moveToNext()) {
                date = orderHeaderCursor.getString(0);

            }
        }
        orderHeaderCursor.close();
        db.closeDB();
        return date;
    }


    public boolean hasDistributorStockCheck() {
        // TODO Auto-generated method stub
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = (ProductMasterBO) bmodel.productHelper
                    .getProductMaster().get(i);

            int siz1 = product.getLocations().size();
            if (product.getLocations().get(0).getShelfPiece() > 0
                    || product.getLocations().get(0).getShelfCase() > 0
                    || product.getLocations().get(0).getShelfOuter() > 0)
                return true;

        }
        return false;
    }

    public ArrayList<DistributorMasterBO> getDistributors() {
        return distributors;
    }

    public DistributorMasterBO getDistributor() {
        return distributor;
    }

    public void setDistributor(DistributorMasterBO distributor) {
        this.distributor = distributor;
    }

    public boolean isEditDistributorStockCheck() {
        return isEditDistributorStockCheck;
    }

    public void setIsEditDistributorStockCheck(boolean isEditDistributorStockCheck) {
        this.isEditDistributorStockCheck = isEditDistributorStockCheck;
    }

    public boolean isEditDistributorOrder() {
        return isEditDistributorOrder;
    }

    public void setIsEditDistributorOrder(boolean isEditDistributorOrder) {
        this.isEditDistributorOrder = isEditDistributorOrder;
    }


    /*Method to download Distributor Data to show in profile screen tab
        Rfield 0 - distributor id from User master
        Rfield 1 - distributor id from Retailer master
        Rfield 2 - distributor id from User master and Retailer master */
    public Vector<DistributorMasterBO> getDistributorProfileList(){
        Vector<DistributorMasterBO> distributorMasterBOs  = new Vector<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            int distributorId = bmodel.userMasterHelper.getUserMasterBO().getDistributorid();
            String retailerId = bmodel.retailerMasterBO.getRetailerID();

            Cursor cursor = null;
            if(bmodel.configurationMasterHelper.SHOW_DISTRIBUTOR_PROFILE_FROM == 0) {
                cursor = db.selectSQL("select DId,DName,CNumber,Address1,Address2,Address3," +
                        "Type,email from DistributorMaster where DId = " + distributorId);
            }
            else if(bmodel.configurationMasterHelper.SHOW_DISTRIBUTOR_PROFILE_FROM == 1) {
                cursor = db.selectSQL("select DId,DName,CNumber,Address1,Address2,Address3," +
                        "Type,email from DistributorMaster DM left join SupplierMaster SM on SM.sid=DM.DId where SM.rid= " +bmodel.QT(retailerId));
            }else if(bmodel.configurationMasterHelper.SHOW_DISTRIBUTOR_PROFILE_FROM == 2)
                cursor = db.selectSQL("select DId,DName,CNumber,Address1,Address2,Address3," +
                        "Type,email from DistributorMaster DM left join SupplierMaster SM on SM.sid=DM.DId " +
                        "where SM.rid="+bmodel.QT(retailerId)+" or DM.DId = "+distributorId+ " group by DM.DId");

            if(cursor!=null && cursor.getCount() > 0 ){
                while( cursor.moveToNext()) {
                    DistributorMasterBO distributorMasterBO = new DistributorMasterBO();
                    distributorMasterBO.setDId(cursor.getString(0));
                    distributorMasterBO.setDName(cursor.getString(1));
                    distributorMasterBO.setCNumber(cursor.getString(2));
                    distributorMasterBO.setAddress1(cursor.getString(3));
                    distributorMasterBO.setAddress2(cursor.getString(4));
                    distributorMasterBO.setAddress3(cursor.getString(5));
                    distributorMasterBO.setType(cursor.getString(6));
                    distributorMasterBO.setEmail(cursor.getString(7));

                    distributorMasterBOs.add(distributorMasterBO);

                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }

        return distributorMasterBOs;
    }
}
