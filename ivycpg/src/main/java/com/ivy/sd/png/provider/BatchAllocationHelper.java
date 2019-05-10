package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SchemeProductBatchQty;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * @author rajesh.k
 */

public class BatchAllocationHelper {

    private static final String TAG = "BatchAllocationHelper";
    private Context context;
    private BusinessModel bmodel;
    private static BatchAllocationHelper instance = null;
    private String signImageName;

    public String getSignImageName() {
        return signImageName;
    }

    public void setSignImageName(String signImageName) {
        this.signImageName = signImageName;
    }

    /**
     * this HashMap return ArrayList for given product id key - product id
     */
    private HashMap<String, ArrayList<ProductMasterBO>> mBatchListByproductID = new HashMap<String, ArrayList<ProductMasterBO>>();
    /**
     * this Hashmap return free product ArrayList for given productid
     */
    private HashMap<String, ArrayList<ProductMasterBO>> mFreeProductListByProductID = new HashMap<String, ArrayList<ProductMasterBO>>();

    protected BatchAllocationHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static BatchAllocationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BatchAllocationHelper(context);
        }
        return instance;
    }

    /**
     * Method to use download batch wise product and set into objects
     *
     * @param channelid
     */

    public void downloadBatchDetails(int channelid) {
        mBatchListByproductID = new HashMap<String, ArrayList<ProductMasterBO>>();
        ProductMasterBO productBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        try {
            String str = "Price.srp1,Price1.srp1";
            String csrp = "Price.csrp1,Price1.csrp1";
            String osrp = "Price.osrp1,Price1.osrp1";
            try {
                if (bmodel.getRetailerMasterBO().getRpTypeCode()
                        .equals("CASH")) {
                    str = "Price.srp1,Price1.srp1";
                    csrp = "Price.csrp1,Price1.csrp1";
                    osrp = "Price.osrp1,Price1.osrp1";
                } else {
                    str = "Price.srp2,Price1.srp2";
                    csrp = "Price.csrp2,Price1.csrp2";
                    osrp = "Price.osrp2,Price1.osrp2";
                }
            } catch (Exception e) {
                Commons.printException(e);
            }

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select  PM.pid, PM.pcode,PM.pname,0,0,PM.sih, PM.psname,PM.barcode,PM.vat,PM.isfocus,ifnull(" + str + ") as srp , ifnull(" + csrp + ") as csrp ,ifnull(" + osrp + ") as ");
            sb.append("osrp,BM.batchid,BM.batchNum,BM.MfgDate,BM.ExpDate,SHM.qty,Price.scid,Price.priceoffvalue,Price.PriceOffId from BatchMAster BM");
            sb.append(" inner join  ProductMaster PM on (BM.Pid= PM.pid) inner join stockinhandmaster SHM on BM.batchid =SHM.batchid and SHM.pid=BM.pid ");
            sb.append("left join PriceMaster Price1 on PM.Pid = Price1.pid AND Price1.scid=0 ");
            sb.append("left join PriceMaster Price on PM.Pid = Price.pid  ");
            sb.append("AND Price.scid = " + bmodel.getAppDataProvider().getRetailMaster().getGroupId() + " where ");
            if (!bmodel.configurationMasterHelper.IS_APPLY_BATCH_PRICE_FROM_PRODUCT)
                sb.append("BM.batchid=Price.batchid AND ");


            sb.append(" BM.pid = PM.pid group by BM.batchid");

            if (bmodel.configurationMasterHelper.IS_ORD_BY_BATCH_EXPIRY_DATE_WISE)
                sb.append(" Order By PM.pid,BM.ExpDate asc");
            else
                sb.append(" Order By PM.pid,BM.MfgDate asc");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String productid = "";

                ArrayList<ProductMasterBO> batchList = new ArrayList<>();
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductCode(c.getString(1));
                    productBO.setProductName(c.getString(2));

                    productBO.setProductShortName(c.getString(6));

                    productBO.setSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(10), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setCsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(11), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setOsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(12), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

                    productBO.setBatchid(c.getString(13));
                    productBO.setBatchNo(c.getString(14));
                    productBO.setMfgDate(c.getString(15));
                    productBO.setExpDate(c.getString(16));
                    productBO.setSIH(c.getInt(17));
                    productBO.setPriceoffvalue(c.getDouble(19));
                    productBO.setPriceOffId(c.getInt(20));


                    if (!productid.equals(productBO.getProductID())) {
                        if (!productid.equals("")) {
                            mBatchListByproductID.put(productid, batchList);
                            batchList = new ArrayList<ProductMasterBO>();
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        } else {
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        }
                    } else {
                        batchList.add(productBO);
                    }

                }
                if (batchList.size() > 0) {
                    mBatchListByproductID.put(productid, batchList);
                }
            }
            c.close();

            if (bmodel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
                updateBatchWiseFreeProducts(db);

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
        }
    }

    private void updateBatchWiseFreeProducts(DBUtil db) {
        mFreeProductListByProductID = new HashMap<String, ArrayList<ProductMasterBO>>();
        ProductMasterBO productBO;
        try {
            String str = "Price.srp1,Price1.srp1";
            String csrp = "Price.csrp1,Price1.csrp1";
            String osrp = "Price.osrp1,Price1.osrp1";
            try {
                if (bmodel.getRetailerMasterBO().getRpTypeCode()
                        .equals("CASH")) {
                    str = "Price.srp1,Price1.srp1";
                    csrp = "Price.csrp1,Price1.csrp1";
                    osrp = "Price.osrp1,Price1.osrp1";
                } else {
                    str = "Price.srp2,Price1.srp2";
                    csrp = "Price.csrp2,Price1.csrp2";
                    osrp = "Price.osrp2,Price1.osrp2";
                }
            } catch (Exception e) {
                Commons.printException(e);
            }

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select  PM.pid, PM.pcode,PM.pname,0,0,PM.sih, PM.psname,PM.barcode,PM.vat,PM.isfocus,ifnull(" + str + ") as srp , ifnull(" + csrp + ") as csrp ,ifnull(" + osrp + ") as ");
            sb.append("osrp,BM.batchid,BM.batchNum,BM.MfgDate,BM.ExpDate,FSM.qty,Price.scid,Price.priceoffvalue,Price.PriceOffId from BatchMAster BM");
            sb.append(" inner join  ProductMaster PM on (BM.Pid= PM.pid) inner join FreeStockInHandMaster FSM on BM.batchid =FSM.batchid and FSM.pid=BM.pid ");
            sb.append("left join PriceMaster Price1 on PM.Pid = Price1.pid AND Price1.scid=0 ");
            sb.append("left join PriceMaster Price on PM.Pid = Price.pid  ");
            sb.append("AND Price.scid = " + bmodel.getAppDataProvider().getRetailMaster().getGroupId() + " where ");
            if (!bmodel.configurationMasterHelper.IS_APPLY_BATCH_PRICE_FROM_PRODUCT)
                sb.append("BM.batchid=Price.batchid AND ");


            sb.append(" BM.pid = PM.pid group by BM.batchid");

            if (bmodel.configurationMasterHelper.IS_ORD_BY_BATCH_EXPIRY_DATE_WISE)
                sb.append(" Order By PM.pid,BM.ExpDate asc");
            else
                sb.append(" Order By PM.pid,BM.MfgDate asc");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String productid = "";

                ArrayList<ProductMasterBO> freeProductList = new ArrayList<>();
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductCode(c.getString(1));
                    productBO.setProductName(c.getString(2));

                    productBO.setProductShortName(c.getString(6));

                    productBO.setSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(10), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setCsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(11), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setOsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(12), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

                    productBO.setBatchid(c.getString(13));
                    productBO.setBatchNo(c.getString(14));
                    productBO.setMfgDate(c.getString(15));
                    productBO.setExpDate(c.getString(16));
                    productBO.setSIH(c.getInt(17));
                    productBO.setPriceoffvalue(c.getDouble(19));
                    productBO.setPriceOffId(c.getInt(20));


                    if (!productid.equals(productBO.getProductID())) {
                        if (!productid.equals("")) {
                            mFreeProductListByProductID.put(productid,
                                    freeProductList);
                            freeProductList = new ArrayList<ProductMasterBO>();
                            freeProductList.add(productBO);
                            productid = productBO.getProductID();
                        } else {
                            freeProductList.add(productBO);
                            productid = productBO.getProductID();
                        }
                    } else {
                        freeProductList.add(productBO);

                    }

                }
                if (freeProductList.size() > 0) {
                    mFreeProductListByProductID.put(productid, freeProductList);
                }

            }
            c.close();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
    }


    public void loadOrderedBatchProducts(String invoiceId) {
        mBatchListByproductID = new HashMap<String, ArrayList<ProductMasterBO>>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        ProductMasterBO productBO;
        try {
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select ID.productid,ID.batchid,pcsqty,caseqty,outerqty,d1,d2,d3,da,totalamount,P.priceoffValue,");
            sb.append("ifnull(P.srp1,0) as srp ,ifnull(P.csrp1,0) as csrp ,ifnull(P.osrp1,0) as osrp,bm.batchnum from invoicedetails ID ");
            sb.append("left join pricemaster P on P.pid=ID.productid and P.batchid=ID.batchid ");
            sb.append("left join batchmaster bm on bm.batchid=ID.batchid and bm.pid=ID.productid ");
            sb.append(" where invoiceid=" + bmodel.QT(invoiceId) + " and ID.batchid!=0 order by ID.productid");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String productid = "";

                ArrayList<ProductMasterBO> batchList = new ArrayList<ProductMasterBO>();
                ArrayList<ProductMasterBO> freeProductList = new ArrayList<ProductMasterBO>();
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setOrderedPcsQty(c.getInt(2));
                    productBO.setOrderedCaseQty(c.getInt(3));
                    productBO.setOrderedOuterQty(c.getInt(4));
                    productBO.setD1(c.getDouble(5));
                    productBO.setD2(c.getDouble(6));
                    productBO.setD3(c.getDouble(7));
                    productBO.setDA(c.getDouble(8));
                    productBO.setNetValue(c.getDouble(9));
                    productBO.setPriceoffvalue(c.getDouble(10));

                    productBO.setSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(11), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setCsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(12), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    productBO.setOsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(13), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

                    productBO.setBatchNo(c.getString(14));

                    if (!productid.equals(productBO.getProductID())) {
                        if (productid != "") {
                            mBatchListByproductID.put(productid, batchList);

                            batchList = new ArrayList<ProductMasterBO>();

                            batchList.add(productBO);

                            productid = productBO.getProductID();
                        } else {
                            batchList.add(productBO);

                            productid = productBO.getProductID();
                        }
                    } else {
                        batchList.add(productBO);
                    }

                }

                if (batchList.size() > 0) {

                    mBatchListByproductID.put(productid, batchList);
                }
            }
            c.close();
            db.closeDB();


        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }
    }


    /**
     * Method to return hashmap key -- productid , value -- product master
     * arraylist
     *
     * @return
     */
    public HashMap<String, ArrayList<ProductMasterBO>> getBatchlistByProductID() {

        return mBatchListByproductID;
    }

    /**
     * Method to return HashMap,which is stored in Free product Arraylist for
     * given keyvalue of product id
     *
     * @return
     */

    public HashMap<String, ArrayList<ProductMasterBO>> getFreeProductListByProductID() {
        return mFreeProductListByProductID;
    }

    /**
     * Method to allocate product details batch wise
     *
     * @param mylist - Ordered product list
     */
    public void loadOrderedBatchList(Vector<ProductMasterBO> mylist) {

        for (ProductMasterBO productBo : mylist) {
            if (productBo.getOrderedPcsQty() > 0
                    || productBo.getOrderedCaseQty() > 0
                    || productBo.getOrderedOuterQty() > 0) {
                updateBatchlist(productBo);
            }

        }

    }

    /**
     * Method to allocate free product batch wise
     */
    public void loadFreeProductBatchList() {
        List<SchemeProductBO> schemeList;
        ArrayList<SchemeBO> appliedSchemeList = SchemeDetailsMasterHelper.getInstance(context).getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBo : appliedSchemeList) {
                schemeList = schemeBo.getFreeProducts();

                if (schemeList != null) {
                    if (schemeList.size() > 0) {
                        for (SchemeProductBO freeProductBo : schemeList) {
                            if (freeProductBo.getQuantitySelected() > 0) {
                                ProductMasterBO schemeProduct = bmodel.productHelper
                                        .getProductMasterBOById(freeProductBo
                                                .getProductId());
                                if (schemeProduct != null) {
                                    updateFreeProductBatchlist(schemeProduct,
                                            freeProductBo);
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Method to use single productBO allocated batchwise depends on entered Qty
     *
     * @param productBo - this productBO used to allocate batchwise
     */
    public void updateBatchlist(ProductMasterBO productBo) {

        ArrayList<ProductMasterBO> batchList;
        int pieceQty = 0;

        pieceQty = productBo.getOrderedPcsQty()
                + (productBo.getOrderedCaseQty() * productBo.getCaseSize())
                + (productBo.getOrderedOuterQty() * productBo.getOutersize());

        int caseQty = 0;
        int outerQty = 0;
        int balanceQTY = 0;
        double batchwiseTotalvalue = 0;
        double batchwiseDiscountvalue = 0;

        batchList = mBatchListByproductID.get(productBo.getProductID());
        // Arrange list in reverse order
        // Collections.sort(batchList, ProductMasterBO.DateWiseAscending);

        if (batchList != null) {

            double totalValue = 0.0;
            // batch wise product
            for (ProductMasterBO product : batchList) {
                product.setOrderedPcsQty(0);
                product.setOrderedCaseQty(0);
                product.setOrderedOuterQty(0);
                product.setNetValue(0);

                if (product.getSIH() < pieceQty) {

                    if (productBo.getCaseSize() > 0 && bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                        caseQty = product.getSIH() / productBo.getCaseSize();

                        balanceQTY = product.getSIH() % productBo.getCaseSize();
                        product.setOrderedCaseQty(caseQty);

                        if (balanceQTY > 0) {
                            if (productBo.getOutersize() > 0 && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                                outerQty = balanceQTY
                                        / productBo.getOutersize();
                                balanceQTY = balanceQTY
                                        % productBo.getOutersize();
                                product.setOrderedOuterQty(outerQty);
                                if (balanceQTY > 0) {

                                    product.setOrderedPcsQty(balanceQTY);
                                }

                            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                                product.setOrderedPcsQty(balanceQTY);
                            }
                        }
                    } else if (productBo.getOutersize() > 0 && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {

                        outerQty = product.getSIH() / productBo.getOutersize();

                        balanceQTY = pieceQty % productBo.getOutersize();
                        product.setOrderedOuterQty(outerQty);
                        if (balanceQTY > 0) {

                            product.setOrderedPcsQty(balanceQTY);
                        }
                    } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                        product.setOrderedPcsQty(product.getSIH());
                    }

                    pieceQty = pieceQty - product.getSIH();

                } else {

                    if (productBo.getCaseSize() > 0 && bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                        caseQty = pieceQty / productBo.getCaseSize();

                        balanceQTY = pieceQty % productBo.getCaseSize();
                        product.setOrderedCaseQty(caseQty);

                        if (balanceQTY > 0) {
                            if (productBo.getOutersize() > 0 && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                                outerQty = balanceQTY
                                        / productBo.getOutersize();
                                balanceQTY = balanceQTY
                                        % productBo.getOutersize();
                                product.setOrderedOuterQty(outerQty);

                                if (balanceQTY > 0) {

                                    product.setOrderedPcsQty(balanceQTY);
                                }
                            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                                product.setOrderedPcsQty(balanceQTY);
                            }
                        }
                    } else if (productBo.getOutersize() > 0 && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                        outerQty = pieceQty / productBo.getOutersize();
                        balanceQTY = pieceQty % productBo.getOutersize();
                        product.setOrderedOuterQty(outerQty);
                        if (balanceQTY > 0) {

                            product.setOrderedPcsQty(balanceQTY);
                        }
                    } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                        product.setOrderedPcsQty(pieceQty);
                    }

                    pieceQty = 0;

                }
                if (product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0
                        || product.getOrderedOuterQty() > 0) {

                    batchwiseTotalvalue = (product.getOrderedPcsQty() * product
                            .getSrp())
                            + (product.getOrderedCaseQty() * product.getCsrp())
                            + (product.getOrderedOuterQty() * product.getOsrp());

                    batchwiseTotalvalue = SDUtil.formatAsPerCalculationConfig(batchwiseTotalvalue);


                    product.setNetValue(batchwiseTotalvalue
                            - batchwiseDiscountvalue);
                    totalValue = totalValue + product.getNetValue();
                }

            }
            productBo.setBatchwiseTotal(totalValue);
            productBo.setTotalamount(totalValue);
        }
    }

    /**
     * update free product in batchwise
     *
     * @param productBo
     * @param schemeBO
     */

    public void updateFreeProductBatchlist(ProductMasterBO productBo,
                                           SchemeProductBO schemeBO) {
        SchemeProductBO schemeProductBO = schemeBO;
        ArrayList<SchemeProductBatchQty> schemeProductBatchList = new ArrayList<SchemeProductBatchQty>();
        SchemeProductBatchQty schemeProductBatchBO;

        ArrayList<ProductMasterBO> batchList;
        int pieceQty = 0;

        if (!bmodel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
            batchList = mBatchListByproductID.get(productBo.getProductID());
        else
            batchList = mFreeProductListByProductID.get(productBo.getProductID());

        // Arrange list in reverse order
        // Collections.sort(batchList, ProductMasterBO.DateWiseAscending);

        // Free product update uom wise
        if (productBo.getCaseUomId() == schemeProductBO.getUomID()
                && productBo.getCaseUomId() != 0) {
            pieceQty = schemeProductBO.getQuantitySelected()
                    * productBo.getCaseSize();

        } else if (productBo.getOuUomid() == schemeProductBO.getUomID()
                && productBo.getOuUomid() != 0) {
            pieceQty = schemeProductBO.getQuantitySelected()
                    * productBo.getOutersize();
        } else {
            pieceQty = schemeProductBO.getQuantitySelected();
        }

        if (batchList != null) {

            // batch wise product
            for (ProductMasterBO product : batchList) {
                schemeProductBatchBO = new SchemeProductBatchQty();
                int currentStockinhand = 0;
                int totalQty = product.getOrderedPcsQty()
                        + (product.getOrderedCaseQty() * productBo
                        .getCaseSize())
                        + (product.getOrderedOuterQty() * productBo
                        .getOutersize());
                if (product.getSIH() > totalQty) {
                    currentStockinhand = product.getSIH() - totalQty;
                }

                schemeProductBatchBO.setBatchid(SDUtil.convertToInt(product
                        .getBatchid()));
                //If stock not enough to give free then remaining quantity will be given from next batch
                if (currentStockinhand < pieceQty) {
                    // stock not enough
                    schemeProductBatchBO.setQty(currentStockinhand);
                    pieceQty = pieceQty - currentStockinhand;
                    schemeProductBatchList.add(schemeProductBatchBO);
                } else {
                    // stock available
                    if (productBo.getCaseUomId() == schemeProductBO.getUomID()) {

                        if (productBo.getCaseSize() > 0) {
                            schemeProductBatchBO.setCaseQty(pieceQty / productBo.getCaseSize());
                        }
                    } else if (productBo.getOuUomid() == schemeProductBO.getUomID()) {
                        if (productBo.getOutersize() > 0) {
                            schemeProductBatchBO.setOuterQty(pieceQty / productBo.getOutersize());
                        }
                    } else {
                        schemeProductBatchBO.setQty(pieceQty);
                        pieceQty = 0;
                    }
                    schemeProductBatchList.add(schemeProductBatchBO);
                    //Breaking loop as all free qty delivered
                    break;
                }


            }
            schemeProductBO.setBatchWiseQty(schemeProductBatchList);

        }
    }

    /**
     * Method to check and validateData,while enter value in BatchAllocation
     * Dialogue screen
     *
     * @param product            this is parent product BO
     * @param batchWiseproductBO this child Batchwise selected productBO
     * @param pcsorcseorout      this is enter qty is piece or case or outer
     * @param qty                - this is entered value
     * @return
     */

    public boolean checkAndValidateBatchEnteredValue(ProductMasterBO product,
                                                     ProductMasterBO batchWiseproductBO, int pcsorcseorout, int qty) {
        ArrayList<ProductMasterBO> batchList = mBatchListByproductID
                .get(product.getProductID());

        int total = 0;
        // totalEnteredQty calculated while taking order
        int totalEnteredQty = product.getOrderedPcsQty()
                + (product.getOrderedCaseQty() * product.getCaseSize())
                + (product.getOrderedOuterQty() * product.getOutersize());
        // total value calculated while batch wise order by single product

        // if pcsorcseorout is 0 piece,1 case,2 outer

        if (pcsorcseorout == 0) {
            total = qty
                    + (batchWiseproductBO.getOrderedCaseQty() * product
                    .getCaseSize())
                    + (batchWiseproductBO.getOrderedOuterQty() * product
                    .getOutersize());
        } else if (pcsorcseorout == 1) {
            total = batchWiseproductBO.getOrderedPcsQty()
                    + (qty * product.getCaseSize())
                    + (batchWiseproductBO.getOrderedOuterQty() * product
                    .getOutersize());
        } else if (pcsorcseorout == 2) {
            total = batchWiseproductBO.getOrderedPcsQty()
                    + (batchWiseproductBO.getOrderedCaseQty() * product
                    .getCaseSize()) + (qty * product.getOutersize());
        }

        if (batchList != null) {
            for (ProductMasterBO productBO : batchList) {
                if (!productBO.getBatchid().equals(
                        batchWiseproductBO.getBatchid())) {
                    total = total
                            + (productBO.getOrderedPcsQty()
                            + (productBO.getOrderedCaseQty() * product
                            .getCaseSize()) + (productBO
                            .getOrderedOuterQty() * product
                            .getOutersize()));
                }

            }

        }

        if (total <= totalEnteredQty) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * download batch count for all product
     */
    public void downloadProductBatchCount() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select count(B.pid),P.pid from BatchMaster B inner join Productmaster P on B.pid=P.pid ");
            sb.append("group by P.pid");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    int productID = c.getInt(1);
                    setproductBatchCount(count, productID);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
        }
    }

    /**
     * set how many batch count in single product
     *
     * @param count     how many batch count
     * @param prodcutid for given product
     */
    public void setproductBatchCount(int count, int prodcutid) {
        ProductMasterBO productBO = bmodel.productHelper
                .getProductMasterBOById(prodcutid + "");
        if (productBO != null) {
            productBO.setBatchwiseProductCount(count);

        }

    }

    /**
     * check batch wise record available or not
     *
     * @param productid for given product
     * @return True - available, false - not available
     */

    public boolean checkBatchwiseRecordAvailable(String productid) {
        ArrayList<ProductMasterBO> batchWiseList = mBatchListByproductID
                .get(productid);
        if (batchWiseList != null) {
            int size = batchWiseList.size();
            if (size > 0) {
                return true;
            }

        }
        return false;

    }

    /**
     * Batchwise stock in hand update for product
     *
     * @param productBO in which product sih update
     * @param batchid   in which product's batch sih update
     * @param qty       reduce this value from current batchwise sih
     * @param flag      if flag=true,subtract qty from sih, else add qty in sih
     */
    public void setBatchwiseSIH(ProductMasterBO productBO, String batchid,
                                int qty, boolean flag) {
        ProductMasterBO product = productBO;
        ArrayList<ProductMasterBO> batchList = mBatchListByproductID
                .get(product.getProductID());
        if (batchList != null) {
            for (ProductMasterBO batchwiseProductBO : batchList) {
                if (batchwiseProductBO.getBatchid().equals(batchid)) {
                    if (flag) {
                        batchwiseProductBO.setSIH(batchwiseProductBO.getSIH()
                                - qty);
                    } else {
                        batchwiseProductBO.setSIH(batchwiseProductBO.getSIH()
                                + qty);
                    }
                    break;
                }
            }
        }
    }

    /**
     * update discount as batchwise
     *
     * @param productBO
     * @param discountValue
     * @return
     */
    public double updateDiscontBatchwise(ProductMasterBO productBO,
                                         double discountValue) {
        double totalVaue = 0.0;

        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        if (batchList != null) {
            for (ProductMasterBO batchProductBO : batchList) {
                double lineValue = batchProductBO.getNetValue();
                if (lineValue > 0) {
                    batchProductBO.setApplyValue(SDUtil.formatAsPerCalculationConfig(lineValue * discountValue / 100));
                    totalVaue = totalVaue + batchProductBO.getNetValue() - batchProductBO.getApplyValue();
                }

            }
        }
        return totalVaue;

    }

    /**
     * update discount as batchwise
     *
     * @param productBO
     * @param discountValue
     * @return
     */
    public double updateDiscontBatchwiseAmt(ProductMasterBO productBO,
                                            double discountValue) {
        double totalVaue = 0.0;

        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        if (batchList != null) {
            for (ProductMasterBO batchProductBO : batchList) {
                int totalQty = batchProductBO.getOrderedPcsQty()
                        + batchProductBO.getOrderedCaseQty()
                        * productBO.getCaseSize()
                        + batchProductBO.getOrderedOuterQty()
                        * productBO.getOutersize();

                if (totalQty > 0) {
                    batchProductBO.setApplyValue(SDUtil.formatAsPerCalculationConfig(totalQty * discountValue));
                    totalVaue = totalVaue + batchProductBO.getNetValue() - (batchProductBO.getApplyValue());
                }
            }
        }
        return totalVaue;

    }

    /**
     * set batchwise ordered products
     *
     * @param productid
     * @param caseqty
     * @param pieceqty
     * @param outerQty
     * @param pricePerPiece
     * @param OrderDetails
     * @param caseSize
     * @param outerSize
     * @param batchid
     */
    public void setBatchwiseProducts(String productid, int caseqty,
                                     int pieceqty, int outerQty, float srp, double pricePerPiece,
                                     Cursor OrderDetails, int caseSize, int outerSize, String batchid, int skuResonId, String remarks) {
        ProductMasterBO produBo = bmodel.getProductbyId(productid);
        if (produBo != null) {
            if (produBo.getBatchwiseProductCount() > 0) {
                ArrayList<ProductMasterBO> batchList = getBatchlistByProductID()
                        .get(productid);
                if (batchList != null) {

                    for (ProductMasterBO batchProductBO : batchList) {
                        if (batchid.equals(batchProductBO.getBatchid())) {
                            batchProductBO.setNetValue(0);

                            batchProductBO.setOrderedPcsQty(pieceqty);
                            batchProductBO.setOrderedCaseQty(caseqty);
                            batchProductBO.setOrderedOuterQty(outerQty);
                            batchProductBO.setSrp(srp);
                            batchProductBO.setSoreasonId(skuResonId);
                            double totalValue = pieceqty * batchProductBO.getSrp()
                                    + caseqty * batchProductBO.getCsrp() + outerQty
                                    * batchProductBO.getOsrp();
                            if (!bmodel.configurationMasterHelper.SHOW_FOC)
                                batchProductBO.setRemarks(remarks);

                            if (OrderDetails != null) {

                                produBo.setD1(OrderDetails.getDouble(OrderDetails
                                        .getColumnIndex("d1")));
                                produBo.setD2(OrderDetails.getDouble(OrderDetails
                                        .getColumnIndex("d2")));
                                produBo.setD3(OrderDetails.getDouble(OrderDetails
                                        .getColumnIndex("d3")));
                                produBo.setDA(OrderDetails.getDouble(OrderDetails
                                        .getColumnIndex("DA")));
                                produBo.setTotalamount(OrderDetails
                                        .getDouble(OrderDetails
                                                .getColumnIndex("totalamount")));
                                batchProductBO.setNetValue(totalValue);


                            }

                            if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                                if (produBo.isAllocation() == 1) {
                                    int newsih = (caseSize * caseqty)
                                            + pieceqty + (outerQty * outerSize);
                                    produBo.setSIH(produBo.getSIH() + newsih);

                                }
                            }
                            int totalQty = pieceqty + caseqty
                                    * produBo.getCaseSize() + outerQty
                                    * produBo.getOutersize();
                            produBo.setOrderedPcsQty(produBo.getOrderedPcsQty()
                                    + pieceqty);
                            produBo.setOrderedCaseQty(produBo.getOrderedCaseQty()
                                    + caseqty);
                            produBo.setOrderedOuterQty(produBo.getOrderedOuterQty()
                                    + outerQty);

                        }
                    }

                    produBo.setOrderPricePiece(pricePerPiece);

                }
            }

        }

    }

    /**
     * Method to use update batch ordered count for corresponding product
     */
    public void updateOrderedeBatchCount(ArrayList<ProductMasterBO> orderList) {
        for (ProductMasterBO productBO : orderList) {
            ArrayList<ProductMasterBO> batchList = mBatchListByproductID.get(productBO.getProductID());
            if (batchList != null && batchList.size() > 0) {
                productBO.setOrderedBatchCount(0);
                for (ProductMasterBO batchProduct : batchList) {
                    final int totalQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * productBO.getCaseSize())
                            + (batchProduct.getOrderedOuterQty() * productBO.getOutersize());
                    if (totalQty > 0) {
                        productBO.setOrderedBatchCount(productBO.getOrderedBatchCount() + 1);
                    }
                }
            } else {
                productBO.setOrderedBatchCount(0);
            }
        }


    }

}
