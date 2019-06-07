package com.ivy.sd.png.model;

import android.app.Activity;
import android.os.Handler;

import com.ivy.cpg.primarysale.view.PrimarySaleOrderSummaryActivity;
import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.login.LoginBaseActivity;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.cpg.view.callanalysis.CallAnalysisActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.ReAllocationActivity;
import com.ivy.cpg.view.subd.SubDStockOrderActivity;
import com.ivy.cpg.view.settings.UserSettingsActivity;
import com.ivy.sd.print.PrintPreviewScreenTitan;
import com.ivy.utils.FileUtils;

import java.util.Locale;
import java.util.Vector;

public class MyThread extends Thread {

    private Activity ctx;
    private int opt;
    Handler handler;
    private boolean isFromCallAnalysis;

    public MyThread(Activity ctx, int opt) {
        this.ctx = ctx;
        this.opt = opt;
    }

    public MyThread(Activity ctx, int opt, boolean isFromCallAnalysis) {
        this.ctx = ctx;
        this.opt = opt;
        this.isFromCallAnalysis = isFromCallAnalysis;
    }

    public MyThread(Activity ctx, int opt, Handler handler) {
        this.ctx = ctx;
        this.handler = handler;
        this.opt = opt;

    }

    public void run() {
        BusinessModel bmodel = (BusinessModel) ctx.getApplicationContext();
        bmodel.setContext(ctx);
        OrderHelper orderHelper = OrderHelper.getInstance(ctx);
        StockCheckHelper stockCheckHelper = StockCheckHelper.getInstance(ctx);
        //FragmentManager fm = ((FragmentActivity)ctx).getSupportFragmentManager();
        //HomeScreenFragment fragment = (HomeScreenFragment)fm.findFragmentById(R.id.synchronization_fragment);

        if (opt == DataMembers.LOCAL_LOGIN) {
            LoginBaseActivity frm = (LoginScreen) ctx;
            int count = frm.loginPresenter.mPasswordLockCountPref.getInt("passwordlock", 0);
            if (bmodel.synchronizationHelper.validateUser(
                    bmodel.userNameTemp.toLowerCase(Locale.US),
                    bmodel.passwordTemp) && ((count + 1) != LoginHelper.getInstance(ctx).MAXIMUM_ATTEMPT_COUNT)) {
                // If usermaster get updated
                bmodel.userMasterHelper.downloadUserDetails();
                bmodel.userMasterHelper.downloadDistributionDetails();

                /** Load all the configurations**/
                bmodel.configurationMasterHelper.downloadConfig();
                bmodel.configurationMasterHelper.downloadIndicativeOrderConfig();
                bmodel.mRetailerHelper.setVisitPlanning(bmodel.configurationMasterHelper
                        .downloadVisitFragDatas(StandardListMasterConstants.VISITCONFIG_PLANNING));
                bmodel.mRetailerHelper.setVisitCoverage(bmodel.configurationMasterHelper
                        .downloadVisitFragDatas(StandardListMasterConstants.VISITCONFIG_COVERAGE));
                bmodel.configurationMasterHelper.getPrinterConfig();
                bmodel.configurationMasterHelper.downloadRetailerProperty();
                bmodel.configurationMasterHelper.downloadQDVP3ScoreConfig(StandardListMasterConstants.VISITCONFIG_COVERAGE);
                if (bmodel.configurationMasterHelper.CALC_QDVP3)
                    bmodel.updateSurveyScoreHistoryRetailerWise();

                if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                    bmodel.downloadRetailerwiseMerchandiser();
                }

                // Code moved from DOWNLOAD
                bmodel.beatMasterHealper.downloadBeats();
                bmodel.channelMasterHelper.downloadChannel();
                bmodel.subChannelMasterHelper.downloadsubChannel();
                bmodel.downloadRetailerMaster();


                bmodel.reasonHelper.downloadReasons();
                bmodel.reasonHelper.downloadDeviatedReason();
                bmodel.reasonHelper.downloadNonVisitReasonMaster();
                bmodel.reasonHelper.downloadNonProductiveReasonMaster();

                bmodel.productHelper
                        .setBuffer(((float) bmodel.configurationMasterHelper
                                .downloadSOBuffer() / (float) 100));
                bmodel.downloadVisit_Actual_Achieved();
                bmodel.labelsMasterHelper.downloadLabelsMaster();
                bmodel.productHelper.loadOldBatchIDMap();
                CollectionHelper collectionHelper = CollectionHelper.getInstance(ctx);
                collectionHelper.updateCreditNoteACtualAmt();
                collectionHelper.loadCreditNote();
                bmodel.productHelper.downloadOrdeType();
                bmodel.configurationMasterHelper.downloadPasswordPolicy();

                if (bmodel.configurationMasterHelper.IS_ENABLE_GCM_REGISTRATION && bmodel.isOnline())
                    LoginHelper.getInstance(ctx).onFCMRegistration(ctx.getApplicationContext());
                if (bmodel.configurationMasterHelper.IS_CHAT_ENABLED)
                    bmodel.downloadChatCredentials();


                frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_USEREXIST);
            } else {
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_NOT_USEREXIST);
            }
        } else if (opt == DataMembers.AMAZONIMAGE_UPLOAD) {

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.uploadImageToAmazonCloud(handler);

        }
        else if (opt == DataMembers.AZURE_IMAGE_UPLOAD) {

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.uploadImageToAzureCloud(handler);

        }else if (opt == DataMembers.SYNCUPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {

                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCUPLOAD, ctx.getApplicationContext());
                // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 0);

                if (BuildConfig.FLAVOR.equalsIgnoreCase("aws")) {
                    if (bool == 1) {

                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_UPLOADED);
                    } else if (bool == -1) {
                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                    } else {
                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_UPLOAD_ERROR);
                    }
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNC_REALLOC_UPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            ReAllocationActivity frm = (ReAllocationActivity) ctx;

            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(frm.getHandler(), DataMembers.SYNC_REALLOC_UPLOAD, ctx.getApplicationContext());
                if (bool == 1) {
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOADED);
                } else {
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }
        } else if (opt == DataMembers.SYNCUPLOADRETAILERWISE) // download other
        // masters
        {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {
                // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 1);
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCUPLOADRETAILERWISE, ctx.getApplicationContext());

                if (BuildConfig.FLAVOR.equalsIgnoreCase("aws")) {
                    if (bool == 1) {

                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_UPLOADED);
                    } else if (bool == -1) {
                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                    } else {
                        handler.sendEmptyMessage(
                                DataMembers.NOTIFY_UPLOAD_ERROR);
                    }
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNC_EXPORT) // download other masters
        {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
            // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 2);
            int bool = mUploadHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNC_EXPORT, ctx.getApplicationContext());

            if (bool == 1) {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_EXPORT_SUCCESS);
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_EXPORT_FAILURE);
            }

        } else if (opt == DataMembers.UPLOAD_FILE_IN_AMAZON) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            UserSettingsActivity frm = (UserSettingsActivity) ctx;

            bmodel.uploadFileInAmazon(frm.getHandler());
        } else if (opt == DataMembers.SAVEORDERANDSTOCK) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.configurationMasterHelper.IS_ORDER_SPLIT) {
                Vector<ProductMasterBO> productList = bmodel.productHelper.getProductMaster();
                ProductMasterBO product;
                Vector<ProductMasterBO> bill1Products = new Vector<>();
                Vector<ProductMasterBO> bill2Products = new Vector<>();
                for (int i = 0; i < productList.size(); ++i) {
                    product = productList.elementAt(i);

                    if (product.getOrderedPcsQty() > 0
                            || product.getOrderedCaseQty() > 0
                            || product.getOrderedOuterQty() > 0) {

                        if (product.isSeparateBill()) {
                            bill2Products.add(product);
                        } else {
                            bill1Products.add(product);
                        }
                    }

                }
                orderHelper.setOrderId(null);
                if ( orderHelper.saveSplitOrder(ctx, bill1Products, false) && orderHelper.saveSplitOrder(ctx, bill2Products,false)) {


                    // Update review plan in DB
                    stockCheckHelper.setReviewPlanInDB(ctx.getApplicationContext());

                    // If Stock and order is enabled , then save stock too.
                    if (bmodel.configurationMasterHelper.IS_ORDER_STOCK
                            && bmodel.hasStockInOrder()) {
                        stockCheckHelper.saveClosingStock(ctx.getApplicationContext(), true);

                        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                            // save price check
                            PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(ctx);
                            if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                                priceTrackingHelper.savePriceTransaction(ctx.getApplicationContext(), bmodel.productHelper.getProductMaster());

                            // save near expiry
                            stockCheckHelper.saveNearExpiry(ctx.getApplicationContext());
                        }
                    }

                    // Set Order Flag
                    bmodel.setIsOrdered("Y");
                    bmodel.setOrderedInDB("Y");
                    bmodel.getRetailerMasterBO().setOrdered("Y");

                    // Set Order Taken/ executed flag
                    bmodel.setIsOrderMerch();
                    bmodel.setOrderMerchInDB("Y");
                    bmodel.getRetailerMasterBO().setIsOrderMerch("Y");

                    // If initiative is enabled then , claculate and update the values
                    // in DB
                    if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                        bmodel.initiativeHelper.loadInitiativeStatus(true,
                                bmodel.retailerMasterBO.getRetailerID(), false, false,
                                bmodel.getRetailerMasterBO().getSubchannelid());
                        bmodel.initiativeHelper
                                .updateInitAchievedPercentInRetailerMaster();
                    }

                    // Clear all the temp values
                    OrderSummary frm = (OrderSummary) ctx;
                    // bmodel.productHelper.clearOrderTable();
                    frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);
                } else {
                    OrderSummary frm = (OrderSummary) ctx;
                    frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_NOT_SAVED);
                }
            } else {

                // Save Order
                if (orderHelper.saveOrder(ctx, false)) {
                    // Save Discount
//            bmodel.saveInvoiceDiscountDetails();


                    // Update review plan in DB
                    stockCheckHelper.setReviewPlanInDB(ctx.getApplicationContext());

                    // If Stock and order is enabled , then save stock too.
                    if (bmodel.configurationMasterHelper.IS_ORDER_STOCK
                            && bmodel.hasStockInOrder()) {
                        stockCheckHelper.saveClosingStock(ctx.getApplicationContext(), true);

                        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                            // save price check
                            PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(ctx);
                            if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                                priceTrackingHelper.savePriceTransaction(ctx.getApplicationContext(), bmodel.productHelper.getProductMaster());

                            // save near expiry
                            stockCheckHelper.saveNearExpiry(ctx.getApplicationContext());
                        }
                    }

                    // Set Order Flag
                    bmodel.setIsOrdered("Y");
                    bmodel.setOrderedInDB("Y");
                    bmodel.getRetailerMasterBO().setOrdered("Y");

                    // Set Order Taken/ executed flag
                    bmodel.setIsOrderMerch();
                    bmodel.setOrderMerchInDB("Y");
                    bmodel.getRetailerMasterBO().setIsOrderMerch("Y");

                    // If initiative is enabled then , claculate and update the values
                    // in DB
                    if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                        bmodel.initiativeHelper.loadInitiativeStatus(true,
                                bmodel.retailerMasterBO.getRetailerID(), false, false,
                                bmodel.getRetailerMasterBO().getSubchannelid());
                        bmodel.initiativeHelper
                                .updateInitAchievedPercentInRetailerMaster();
                    }

                    // Clear all the temp values
                    OrderSummary frm = (OrderSummary) ctx;
                    // bmodel.productHelper.clearOrderTable();


                    frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);
                } else {
                    OrderSummary frm = (OrderSummary) ctx;
                    frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_NOT_SAVED);
                }
            }
        } else if (opt == DataMembers.SAVESUBDORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            // Save Order
            if (orderHelper.saveOrder(ctx, false)) {


                // Update review plan in DB
                stockCheckHelper.setReviewPlanInDB(ctx.getApplicationContext());

                // Set Order Flag
                bmodel.setIsOrdered("Y");
                bmodel.setOrderedInDB("Y");
                bmodel.getRetailerMasterBO().setOrdered("Y");

                // Set Order Taken/ executed flag
                bmodel.setIsOrderMerch();
                bmodel.setOrderMerchInDB("Y");
                bmodel.getRetailerMasterBO().setIsOrderMerch("Y");

                // Clear all the temp values
                SubDStockOrderActivity frm = (SubDStockOrderActivity) ctx;


                frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);
            } else {
                SubDStockOrderActivity frm = (SubDStockOrderActivity) ctx;
                frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_NOT_SAVED);
            }
        } else if (opt == DataMembers.SAVEORDERPARTIALLY) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.updatePoRemarks();
            // Clear Object other wise Data Retain in Dialog
            // Update the Return product details if it user comes in Edit Mode
            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                bmodel.productHelper.updateReturnProductValue();
                // Insert Product Details to Empty Reconciliation tables if Type
                // wise Group products Edited or updated
                if (!bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                    EmptyReconciliationHelper.getInstance(ctx).saveSKUWiseTransaction();
            }
            OrderSummary frm = (OrderSummary) ctx;
            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);

        } else if (opt == DataMembers.DELETE_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            //delete captured image form folder
            if (bmodel.getOrderHeaderBO().getOrderImageName().length() > 0)
                bmodel.deleteFiles(FileUtils.photoFolderPath,
                        bmodel.getOrderHeaderBO().getOrderImageName());

            orderHelper.deleteOrder(ctx, bmodel.getRetailerMasterBO().getRetailerID());


            if (!bmodel.isOrderTaken()) {
                bmodel.setIsOrdered("N");
                bmodel.setOrderedInDB("N");
                bmodel.getRetailerMasterBO().setOrdered("N");
            }

            bmodel.setOrderMerchInDB("N");
            bmodel.getRetailerMasterBO().setIsOrderMerch("N");


            if (bmodel.getRemarkType().length() > 0)
                bmodel.setRemarkType("");

            if (bmodel.getOrderHeaderNote().length() > 0)
                bmodel.setOrderHeaderNote("");

            if (bmodel.getRField1().length() > 0)

                bmodel.setRField1("");
            if (bmodel.getRField2().length() > 0)
                bmodel.setRField2("");

            if (bmodel.getRField2().length() > 0)
                bmodel.setRField2("");

            // bmodel.initiativeHelper.storeInitiativePrecentageInDB("0",0);
            // bmodel.initiativeHelper.setInitiativePrecentInBO("0");
            // bmodel.getRetailerMasterBO().setInitiativePercent("0");
            bmodel.getRetailerMasterBO().setVisit_Actual(0);

            // bmodel.productHelper.clearOrderTableAndUpdateSIH();
            bmodel.productHelper.clearOrderTableForInitiative();
            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                bmodel.initiativeHelper.loadInitiativeStatus(true,
                        bmodel.retailerMasterBO.getRetailerID(), false, false,
                        bmodel.getRetailerMasterBO().getSubchannelid());
                bmodel.initiativeHelper
                        .updateInitAchievedPercentInRetailerMaster();
            }

            OrderSummary frm = (OrderSummary) ctx;
            bmodel.productHelper.clearOrderTable();
            // If Empties Management(Bottle Return Module) delete the objects
            // value while deleting the Order
            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                bmodel.productHelper.clearBomReturnProductsTable();
                EmptyReconciliationHelper.getInstance(ctx)
                        .deleteEmptyReconciliationOrder();
            }


            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_DELETED);
        } else if (opt == DataMembers.DELETE_STOCK_AND_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            //delete captured image form folder
            if (bmodel.getOrderHeaderBO().getOrderImageName().length() > 0)
                bmodel.deleteFiles(FileUtils.photoFolderPath,
                        bmodel.getOrderHeaderBO().getOrderImageName());

            orderHelper.deleteStockAndOrder(ctx);

            orderHelper.deleteOrder(ctx, bmodel.getRetailerMasterBO().getRetailerID());
            bmodel.deleteModuleCompletion("MENU_STK_ORD");

            if (!bmodel.isOrderTaken()) {
                bmodel.setIsOrdered("N");
                bmodel.setOrderedInDB("N");
                bmodel.getRetailerMasterBO().setOrdered("N");
            }


            bmodel.setOrderMerchInDB("N");
            bmodel.getRetailerMasterBO().setIsOrderMerch("N");

            if (bmodel.getRemarkType().length() > 0)
                bmodel.setRemarkType("");

            if (bmodel.getOrderHeaderNote().length() > 0)
                bmodel.setOrderHeaderNote("");

            if (bmodel.getRField1().length() > 0)

                bmodel.setRField1("");
            if (bmodel.getRField2().length() > 0)
                bmodel.setRField2("");

            if (bmodel.getRField2().length() > 0)
                bmodel.setRField2("");

            // bmodel.initiativeHelper.storeInitiativePrecentageInDB("0",0);
            // bmodel.initiativeHelper.setInitiativePrecentInBO("0");
            // bmodel.getRetailerMasterBO().setInitiativePercent("0");
            bmodel.getRetailerMasterBO().setVisit_Actual(0);

            // bmodel.productHelper.clearOrderTableAndUpdateSIH();
            bmodel.productHelper.clearOrderTableForInitiative();
            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                bmodel.initiativeHelper.loadInitiativeStatus(true,
                        bmodel.retailerMasterBO.getRetailerID(), false, false,
                        bmodel.getRetailerMasterBO().getSubchannelid());
                bmodel.initiativeHelper
                        .updateInitAchievedPercentInRetailerMaster();
            }

            OrderSummary frm = (OrderSummary) ctx;
            bmodel.productHelper.clearOrderTable();
            // If Empties Management(Bottle Return Module) delete the objects
            // value while deleting the Order
            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                bmodel.productHelper.clearBomReturnProductsTable();
                EmptyReconciliationHelper.getInstance(ctx)
                        .deleteEmptyReconciliationOrder();
            }


            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_DELETED);
        } else if (opt == DataMembers.DIST_DELETE_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.deleteDistributorOrder(bmodel.distributorMasterHelper.getDistributor().getDId());
            PrimarySaleOrderSummaryActivity frm = (PrimarySaleOrderSummaryActivity) ctx;
            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_DELETED);
        } else if (opt == DataMembers.DIST_DELETE_STOCK_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.deleteDistributorOrder(bmodel.distributorMasterHelper.getDistributor().getDId());
            bmodel.deleteDistributorStock(bmodel.distributorMasterHelper.getDistributor().getDId());
            PrimarySaleOrderSummaryActivity frm = (PrimarySaleOrderSummaryActivity) ctx;
            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_DELETED);
        } else if (opt == DataMembers.DISTSAVEORDERANDSTOCK) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            // Save Order
            bmodel.distributorMasterHelper.saveDistributorOrder();
            bmodel.setOrderHeaderBO(null);

            // If Stock and order is enabled , then save stock too.
            if (bmodel.configurationMasterHelper.SHOW_DIST_STOCK && bmodel.distributorMasterHelper.hasDistributorStockCheck())
                bmodel.distributorMasterHelper.saveDistributorClosingStock();

            // Clear all the temp values
            PrimarySaleOrderSummaryActivity frm = (PrimarySaleOrderSummaryActivity) ctx;
            // bmodel.productHelper.clearOrderTable();
            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);
        } else if (opt == DataMembers.SAVEINVOICE) {

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            if (orderHelper.saveOrder(ctx, true)) {

                bmodel.setOrderHeaderNote("");


                // Update review plan in DB
                stockCheckHelper.setReviewPlanInDB(ctx.getApplicationContext());

                if (bmodel.configurationMasterHelper.IS_ORDER_STOCK
                        && bmodel.hasStockInOrder()) {
                    stockCheckHelper.saveClosingStock(ctx.getApplicationContext(), true);

                    if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                        // save price check
                        PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(ctx);
                        if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                            priceTrackingHelper.savePriceTransaction(ctx.getApplicationContext(), bmodel.productHelper.getProductMaster());

                        // save near expiry
                        stockCheckHelper.saveNearExpiry(ctx.getApplicationContext());
                    }
                }

                boolean isInvoiceSaved=false;
                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    isInvoiceSaved=orderHelper.saveInvoice(ctx);
                }

                bmodel.setRField1("");
                bmodel.setRField2("");
                bmodel.setRField3("");

                // If Bottle Return Credit Limit Enabled , then substract the bottle
                // return value in Bottle Return CreditLimit in Retailer Master
                if (bmodel.configurationMasterHelper.SHOW_BOTTLE_CREDITLIMIT)
                    bmodel.updateBottleCreditLimitAmount();

                // Save Discount
//            bmodel.saveInvoiceDiscountDetails();


                bmodel.setIsOrdered("Y");
                bmodel.setOrderedInDB("Y");
                bmodel.getRetailerMasterBO().setOrdered("Y");

                // Set Order Taken/ executed flag
                bmodel.setIsOrderMerch();
                bmodel.setOrderMerchInDB("Y");
                bmodel.getRetailerMasterBO().setIsOrderMerch("Y");

                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    bmodel.setIsInvoiceDone();
                    bmodel.setInvoiceDoneInDB();
                    bmodel.getRetailerMasterBO().setInvoiceDone("Y");
                }

                if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                    bmodel.initiativeHelper.loadInitiativeStatus(true,
                            bmodel.retailerMasterBO.getRetailerID(), false, false,
                            bmodel.getRetailerMasterBO().getSubchannelid());
                    bmodel.initiativeHelper
                            .updateInitAchievedPercentInRetailerMaster();
                }
                // When Configuration Enabled the data inserted in to the Payment
                if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE)
                    CollectionHelper.getInstance(ctx).collectionBeforeInvoice();

                Commons.print("Class Simple Name :"
                        + ctx.getClass().getSimpleName());
                if (ctx.getClass().getSimpleName().equals("BatchAllocation")) {
                    BatchAllocation frm = (BatchAllocation) ctx;
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_INVOICE_SAVED);

                } else if (ctx.getClass().getSimpleName()
                        .equalsIgnoreCase("PrintPreviewScreenTitan")) {
                    PrintPreviewScreenTitan frm = (PrintPreviewScreenTitan) ctx;
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_INVOICE_SAVED);
                }
                else {
                    OrderSummary frm = (OrderSummary) ctx;

                    if(isInvoiceSaved) {
                        frm.getHandler().sendEmptyMessage(
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    }
                    else {
                        frm.getHandler().sendEmptyMessage(
                                DataMembers.NOTIFY_INVOICE_NOT_SAVED);
                    }
                }
            } else {
                OrderSummary frm = (OrderSummary) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_ORDER_NOT_SAVED);
            }

        } else if (opt == DataMembers.SYNCSIHUPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCSIHUPLOAD, ctx.getApplicationContext());

                if (bool == 2) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_SIH_UPLOADED);
                } else if (bool == -1) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_SIH_UPLOAD_ERROR);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNCSTKAPPLYUPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);


            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCSTKAPPLYUPLOAD, ctx.getApplicationContext());

                if (bool == 2) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_STOCKAPLY_UPLOADED);
                } else if (bool == -1) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                } else {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_STOCKAPLY_UPLOAD_ERROR);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNCLYTYPTUPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCLYTYPTUPLOAD, ctx.getApplicationContext());

                if (bool == 2) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_LP_UPLOADED);
                } else if (bool == -1) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_LP_UPLOAD_ERROR);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNCPICKLISTUPLOAD) {
            bmodel.synchronizationHelper.computePicklistData();
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }
            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNCPICKLISTUPLOAD, ctx.getApplicationContext());

                if (bool == 2) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_PICKLIST_UPLOADED);
                } else if (bool == -1) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);

                } else {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_PICKLIST_UPLOAD_ERROR);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        }
        else if (opt == DataMembers.SYNC_TRIP) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }
            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                int bool = mUploadHelper.uploadUsingHttp(handler, DataMembers.SYNC_TRIP, ctx.getApplicationContext());

                if (bool == 2) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TRIP_UPLOADED);
                } else if (bool == -1) {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);

                } else {
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_TRIP_UPLOAD_ERROR);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        }
    }// RUN

}
