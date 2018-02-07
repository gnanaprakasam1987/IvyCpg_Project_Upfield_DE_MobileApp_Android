package com.ivy.sd.png.model;

import android.app.Activity;
import android.os.Handler;

import com.ivy.cpg.primarysale.view.PrimarySaleOrderSummaryActivity;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.sd.png.view.BixolonIIPrint;
import com.ivy.sd.png.view.BixolonIPrint;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.InvoicePrintZebraNew;
import com.ivy.sd.png.view.ReAllocationActivity;
import com.ivy.sd.png.view.UserSettingsActivity;
import com.ivy.sd.print.GhanaPrintPreviewActivity;
import com.ivy.sd.print.PrintPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenDiageo;
import com.ivy.sd.print.PrintPreviewScreenTitan;

import java.util.Locale;

public class MyThread extends Thread {

    private Activity ctx;
    private int opt;
    Handler handler;

    public MyThread(Activity ctx, int opt) {
        this.ctx = ctx;
        this.opt = opt;
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
        //FragmentManager fm = ((FragmentActivity)ctx).getSupportFragmentManager();
        //HomeScreenFragment fragment = (HomeScreenFragment)fm.findFragmentById(R.id.synchronization_fragment);

        if (opt == DataMembers.LOCAL_LOGIN) {
            LoginScreen frm = (LoginScreen) ctx;
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

                bmodel.collectionHelper.updateCreditNoteACtualAmt();
                bmodel.collectionHelper.loadCreditNote();
                bmodel.productHelper.downloadOrdeType();
                bmodel.configurationMasterHelper.downloadPasswordPolicy();

                if (bmodel.configurationMasterHelper.IS_ENABLE_GCM_REGISTRATION && bmodel.isOnline())
                    LoginHelper.getInstance(ctx).onGCMRegistration(ctx.getApplicationContext());
                if (bmodel.configurationMasterHelper.IS_CHAT_ENABLED)
                    bmodel.downloadChatCredentials();


                frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_USEREXIST);
            } else {
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_NOT_USEREXIST);
            }
        } else if (opt == DataMembers.AMAZONIMAGE_UPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.uploadImageToAmazonCloud(fragment.getHandler());

        } else if (opt == DataMembers.SYNCUPLOAD_IMAGE) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            if (bmodel.synchronizationHelper.checkForImageToUpload()) {
                bmodel.prepareUploadImageAtSOAP(fragment.getHandler());
                // imageUploadStatus = bmodel.uploadImageAtSOAP(data);
            }
        } else if (opt == DataMembers.SYNCUPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {


                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNCUPLOAD);
                // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 0);

                if (bool == 1) {
                    if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                        if (!bmodel.synchronizationHelper.backUpDB()) {
                            fragment.getHandler().sendEmptyMessage(
                                    DataMembers.NOTIFY_DATABASE_NOT_SAVED);

                        }
                    }
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNC_REALLOC_UPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            ReAllocationActivity frm = (ReAllocationActivity) ctx;

            if (bmodel.isOnline()) {

                int bool = bmodel.synchronizationHelper.uploadUsingHttp(frm.getHandler(), DataMembers.SYNC_REALLOC_UPLOAD);
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
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 1);
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNCUPLOADRETAILERWISE);

                if (bool == 1) {
                    if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                        if (!bmodel.synchronizationHelper.backUpDB()) {
                            fragment.getHandler().sendEmptyMessage(
                                    DataMembers.NOTIFY_DATABASE_NOT_SAVED);

                        }
                    }
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOADED);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNC_EXPORT) // download other masters
        {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            // int bool = bmodel.uploadAtSOAP(frm.getHandler(), 2);
            int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNC_EXPORT);

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

            // Save Order
            orderHelper.saveOrder(ctx);
            bmodel.updateSbdDistStockinRetailerMaster();

            // Save Discount
//            bmodel.saveInvoiceDiscountDetails();

            bmodel.setOrderHeaderBO(null);

            // Upadte isVisited Flag
            if (bmodel.mSelectedModule != 1) {
                bmodel.updateIsVisitedFlag();
            }
            // Update review plan in DB
            bmodel.setReviewPlanInDB();

            // If Stock and order is enabled , then save stock too.
            if (bmodel.configurationMasterHelper.IS_ORDER_STOCK
                    && bmodel.hasStockInOrder()) {
                bmodel.saveClosingStock(true);

                if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                    // save price check
                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(ctx);
                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                        priceTrackingHelper.savePriceTransaction(ctx.getApplicationContext(), bmodel.productHelper.getProductMaster());

                    // save near expiry
                    bmodel.saveNearExpiry();
                }
            }

            // Calculate and set Distribution percent
            if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                String percent = bmodel.getSBDDistributionPrecentNewPhilip();
                bmodel.sbdMerchandisingHelper
                        .setSBDDistributionPercent(percent);
                bmodel.setDistributionPercent(percent);
                bmodel.getRetailerMasterBO().setSbdDistpercent(percent);
            }

            // Set Order Flag
            bmodel.setIsOrdered("Y");
            bmodel.setOrderedInDB("Y");
            bmodel.getRetailerMasterBO().setOrdered("Y");

            // Set Order Taken/ executed flag
            bmodel.setIsOrderMerch();
            bmodel.setOrderMerchInDB();
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

            // Backup the database
            if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                if (!bmodel.synchronizationHelper.backUpDB()) {
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_DATABASE_NOT_SAVED);
                }
            }

            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);
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
                    bmodel.mEmptyReconciliationhelper.saveSKUWiseTransaction();
            }
            bmodel.setOrderHeaderBO(null);
            OrderSummary frm = (OrderSummary) ctx;
            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_SAVED);

        } else if (opt == DataMembers.DELETE_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            orderHelper.deleteOrder(ctx, bmodel.getRetailerMasterBO().getRetailerID());

            // Calculate and set Distribution percent
            if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                String percent = bmodel.getSBDDistributionPrecentNewPhilip();
                bmodel.sbdMerchandisingHelper
                        .setSBDDistributionPercent(percent);
                bmodel.setDistributionPercent(percent);
                bmodel.getRetailerMasterBO().setSbdDistpercent(percent);
            }

            if (!bmodel.isOrderTaken()) {
                bmodel.setIsOrdered("N");
                bmodel.setOrderedInDB("N");
                bmodel.getRetailerMasterBO().setOrdered("N");
            }

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
                bmodel.mEmptyReconciliationhelper
                        .deleteEmptyReconciliationOrder();
            }
            bmodel.setOrderHeaderBO(null);
            if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                if (!bmodel.synchronizationHelper.backUpDB()) {
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_DATABASE_NOT_SAVED);

                }
            }

            frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_ORDER_DELETED);
        } else if (opt == DataMembers.DELETE_STOCK_AND_ORDER) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            orderHelper.deleteStockAndOrder(ctx);

            orderHelper.deleteOrder(ctx, bmodel.getRetailerMasterBO().getRetailerID());

            // Calculate and set Distribution percent
            if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                String percent = bmodel.getSBDDistributionPrecentNewPhilip();
                bmodel.sbdMerchandisingHelper
                        .setSBDDistributionPercent(percent);
                bmodel.setDistributionPercent(percent);
                bmodel.getRetailerMasterBO().setSbdDistpercent(percent);
            }

            if (!bmodel.isOrderTaken()) {
                bmodel.setIsOrdered("N");
                bmodel.setOrderedInDB("N");
                bmodel.getRetailerMasterBO().setOrdered("N");
            }

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
                bmodel.mEmptyReconciliationhelper
                        .deleteEmptyReconciliationOrder();
            }
            bmodel.setOrderHeaderBO(null);
            if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                if (!bmodel.synchronizationHelper.backUpDB()) {
                    frm.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_DATABASE_NOT_SAVED);

                }
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

            orderHelper.saveOrder(ctx);

            bmodel.setOrderHeaderNote("");

            // Upadte isVisited Flag
            bmodel.updateIsVisitedFlag();

            // Update review plan in DB
            bmodel.setReviewPlanInDB();

            if (bmodel.configurationMasterHelper.IS_ORDER_STOCK
                    && bmodel.hasStockInOrder()) {
                bmodel.saveClosingStock(true);

                if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                    // save price check
                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(ctx);
                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                        priceTrackingHelper.savePriceTransaction(ctx.getApplicationContext(), bmodel.productHelper.getProductMaster());

                    // save near expiry
                    bmodel.saveNearExpiry();
                }
            }

            if (bmodel.configurationMasterHelper.IS_INVOICE) {
                // update stockinhandmaster all record upload=N
                //bmodel.updateStockinHandMaster();


                orderHelper.saveInvoice(ctx);
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

            bmodel.setOrderHeaderBO(null);

            // Calculate and set Distribution percent
            String percent = bmodel.getSBDDistributionPrecentNewPhilip();
            bmodel.sbdMerchandisingHelper.setSBDDistributionPercent(percent);
            bmodel.setDistributionPercent(percent);
            bmodel.getRetailerMasterBO().setSbdDistpercent(percent);

            bmodel.setIsOrdered("Y");
            bmodel.setOrderedInDB("Y");
            bmodel.getRetailerMasterBO().setOrdered("Y");

            // Set Order Taken/ executed flag
            bmodel.setIsOrderMerch();
            bmodel.setOrderMerchInDB();
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
                bmodel.collectionHelper.collectionBeforeInvoice();

            Commons.print("Class Simple Name :"
                    + ctx.getClass().getSimpleName());
            if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("BixolonIIPrint")) {
                BixolonIIPrint frm = (BixolonIIPrint) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("BixolonIPrint")) {
                BixolonIPrint frm = (BixolonIPrint) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("InvoicePrintZebraNew")) {
                InvoicePrintZebraNew frm = (InvoicePrintZebraNew) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName().equals("BatchAllocation")) {
                BatchAllocation frm = (BatchAllocation) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);

            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("PrintPreviewScreen")) {
                PrintPreviewScreen frm = (PrintPreviewScreen) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("BtPrint4Ivy")) {
                BtPrint4Ivy frm = (BtPrint4Ivy) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("PrintPreviewScreenDiageo")) {
                PrintPreviewScreenDiageo frm = (PrintPreviewScreenDiageo) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("PrintPreviewScreenTitan")) {
                PrintPreviewScreenTitan frm = (PrintPreviewScreenTitan) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else if (ctx.getClass().getSimpleName()
                    .equalsIgnoreCase("GhanaPrintPreviewActivity")) {
                GhanaPrintPreviewActivity frm = (GhanaPrintPreviewActivity) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            } else {
                OrderSummary frm = (OrderSummary) ctx;
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_INVOICE_SAVED);
            }
            // Data loaded into BO will be cleared only after print preview
            // bmodel.clearOrderTable();
            // OrderSummary frm = (OrderSummary) ctx;
            // frm.getHandler().sendEmptyMessage(DataMembers.NOTIFY_INVOICE_SAVED);
        } else if (opt == DataMembers.SYNCSIHUPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNCSIHUPLOAD);

                if (bool == 2) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_SIH_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_SIH_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.SYNCSTKAPPLYUPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNCSTKAPPLYUPLOAD);

                if (bool == 2) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_STOCKAPLY_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_STOCKAPLY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (opt == DataMembers.COUNTER_SIH_UPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.COUNTER_SIH_UPLOAD);

                if (bool == 2) {
                        fragment.getHandler().sendEmptyMessage(
                                DataMembers.NOTIFY_COUNTER_SIH_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }
        } else if (opt == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.COUNTER_STOCK_APPLY_UPLOAD);

                if (bool == 2) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_COUNTER_STOCK_APPLY_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }
        } else if (opt == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.CS_REJECTED_VARIANCE_UPLOAD);

                if (bool == 2) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_CS_REJECTED_VARIANCE_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }
        } else if (opt == DataMembers.SYNCLYTYPTUPLOAD) {
            HomeScreenActivity fragment = (HomeScreenActivity) ctx;
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            if (bmodel.isOnline()) {
                int bool = bmodel.synchronizationHelper.uploadUsingHttp(fragment.getHandler(), DataMembers.SYNCLYTYPTUPLOAD);

                if (bool == 2) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_LP_UPLOADED);
                } else if (bool == -1) {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                } else {
                    fragment.getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_LP_UPLOAD_ERROR);
                }
            } else {
                fragment.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        }
    }// RUN

}
