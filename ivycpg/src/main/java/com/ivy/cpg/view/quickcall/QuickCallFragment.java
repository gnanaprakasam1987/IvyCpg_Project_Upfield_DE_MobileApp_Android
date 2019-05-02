package com.ivy.cpg.view.quickcall;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.order.tax.TaxGstHelper;
import com.ivy.cpg.view.order.tax.TaxHelper;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SBDHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.OrderTransactionListDialog;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;


public class QuickCallFragment extends IvyBaseFragment {


    private String MENU_STK_ORD = "MENU_STK_ORD";
    private String MENU_ORDER = "MENU_ORDER";

    BusinessModel bmodel;
    ListView lvSubDId;
    Context context;
    Vector<RetailerMasterBO> retailer = new Vector<>();
    ArrayList<SupplierMasterBO> mSupplierList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_d, container, false);

        context = getActivity();
        Vector<RetailerMasterBO> retailerList = bmodel.getRetailerMaster();
        for(RetailerMasterBO retailerBO : retailerList){
            if(!retailerBO.getIsNew().equalsIgnoreCase("y")){
                retailer.add(retailerBO);
            }
        }

        lvSubDId = view.findViewById(R.id.lv_subdid);

        setScreenTitle(bmodel.configurationMasterHelper.getSubdtitle());

        lvSubDId.setDivider(null);
        lvSubDId.setDividerHeight(0);
        if (retailer.size() > 0) {
            if (retailer.size() == 1) {
                bmodel.setRetailerMasterBO(retailer.get(0));
                loadOrderScreen();
            } else {
                RetailerSelectionAdapter adapter = new RetailerSelectionAdapter(retailer);
                lvSubDId.setAdapter(adapter);
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (retailer.size() > 0) {
            RetailerSelectionAdapter adapter = new RetailerSelectionAdapter(retailer);
            lvSubDId.setAdapter(adapter);
        }
    }

    private void updateDefaultSupplierSelection() {
        try {
            int mDefaultSupplierSelection = bmodel.getSupplierPosition(mSupplierList);
            if (mSupplierList != null && mSupplierList.size() > 0) {
                bmodel.getRetailerMasterBO().setSupplierBO(
                        mSupplierList.get(mDefaultSupplierSelection));
                bmodel.getRetailerMasterBO().setDistributorId(mSupplierList.get(mDefaultSupplierSelection).getSupplierID());
                bmodel.getRetailerMasterBO().setDistParentId(mSupplierList.get(mDefaultSupplierSelection).getDistParentID());
                bmodel.getRetailerMasterBO().setSupplierTaxLocId(mSupplierList.get(mDefaultSupplierSelection).getSupplierTaxLocId());
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private class RetailerSelectionAdapter extends ArrayAdapter<RetailerMasterBO> {

        private final Vector<RetailerMasterBO> items;
        LayoutInflater inflater;
        private RetailerMasterBO retailerObj;

        private RetailerSelectionAdapter(Vector<RetailerMasterBO> items) {
            super(context, R.layout.visit_list_child_item, items);
            this.items = items;
            inflater = LayoutInflater.from(context);
        }

        public RetailerMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            final RetailerSelectionAdapter.ViewHolder holder;
            retailerObj = items.get(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_subdid_layout, parent, false);
                holder = new RetailerSelectionAdapter.ViewHolder();

                holder.retailertNameTextView = convertView.findViewById(R.id.retailer_name_subdid);
                holder.cardViewItem = convertView.findViewById(R.id.cardview);
                holder.retailertNameTextView.setTypeface(bmodel.configurationMasterHelper
                        .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                convertView.setTag(holder);

                holder.cardViewItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                        String rSalesType = bmodel.getStandardListCode(bmodel.getRetailerMasterBO().getSalesTypeId());
                        if (bmodel.configurationMasterHelper.IS_SHOW_RID_CONCEDER_AS_DSTID && rSalesType.equalsIgnoreCase("INDIRECT")) {
                            bmodel.retailerMasterBO.setDistributorId(SDUtil.convertToInt(bmodel.retailerMasterBO.getRetailerID()));
                            bmodel.retailerMasterBO.setDistParentId(0);
                        } else {
                            if (!bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                                    && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE) {
                                mSupplierList = bmodel.downloadSupplierDetails();
                                if (mSupplierList != null && mSupplierList.size() > 0)
                                    updateDefaultSupplierSelection();
                            }
                        }
                        loadOrderScreen();
                    }
                });

            } else {
                holder = (RetailerSelectionAdapter.ViewHolder) convertView.getTag();
            }

            holder.retailerObjectHolder = retailerObj;

            String tvText = items.get(position).getRetailerName();
            holder.retailertNameTextView.setText(tvText);
            return convertView;
        }

        class ViewHolder {
            private RetailerMasterBO retailerObjectHolder;
            private TextView retailertNameTextView;
            private CardView cardViewItem;

        }
    }

    private void loadOrderScreen() {
        //for SalesType default value
        bmodel.getRetailerMasterBO().setOrderTypeId(0 + "");
        new DownloadProductsAndPrice().execute();

    }

    private class DownloadProductsAndPrice extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (!isCancelled()) {

                    if (bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN)
                        bmodel.productHelper.taxHelper = TaxGstHelper.getInstance(getActivity());
                    else
                        bmodel.productHelper.taxHelper = TaxHelper.getInstance(getActivity());

                    if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        bmodel.getRetailerWiseSellerType();
                        bmodel.configurationMasterHelper.updateConfigurationSelectedSellerType(bmodel.getRetailerMasterBO().getIsVansales() != 1);
                    }


                    GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_STK_ORD);
                    if (genericObjectPair != null) {
                        bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                        bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                    }
                    bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_STK_ORD));
                    bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getFilterProductLevels(),true));


                    bmodel.configurationMasterHelper
                            .loadOrderAndStockConfiguration(bmodel.retailerMasterBO
                                    .getSubchannelid());

                    if (bmodel.productHelper.isSBDFilterAvaiable())
                        SBDHelper.getInstance(getActivity()).loadSBDFocusData(getActivity());

                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        bmodel.batchAllocationHelper.downloadBatchDetails(bmodel
                                .getRetailerMasterBO().getGroupId());
                        bmodel.batchAllocationHelper.downloadProductBatchCount();
                    }

                    bmodel.productHelper.downloadBomMaster();

                    if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        bmodel.productHelper.downlaodReturnableProducts(MENU_STK_ORD);
                        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                            bmodel.productHelper.downloadTypeProducts();
                            bmodel.productHelper.downloadGenericProductID();
                        }
                    }


                    if (!bmodel.configurationMasterHelper.SHEME_NOT_APPLY_DEVIATEDSTORE
                            || (bmodel.getRetailerMasterBO().getIsDeviated() != null && !"Y".equals(bmodel.getRetailerMasterBO().getIsDeviated()))) {

                        SchemeDetailsMasterHelper.getInstance(getActivity()).initializeScheme(getActivity(),
                                bmodel.userMasterHelper.getUserMasterBO().getUserid(), bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION);

                    }

                    if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
                        bmodel.productHelper.downloadProductDiscountDetails();
                        bmodel.productHelper.downloadDiscountIdListByTypeId();
                    }

                    if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
                        bmodel.productHelper.downloadDocketPricing();
                    }

                    //Getting Attributes mapped for the retailer
                    bmodel.getAttributeHierarchyForRetailer();

                    bmodel.reasonHelper.downloadReasons();

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return Boolean.TRUE;
        }

        protected void onPreExecute() {
            if (!isCancelled()) {
                builder = new AlertDialog.Builder(getActivity());
                customProgressDialog(builder, getResources().getString(R.string.loading));
                alertDialog = builder.create();
                alertDialog.show();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {


                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                OrderHelper orderHelper = OrderHelper.getInstance(getActivity());
                if (bmodel.productHelper.getProductMaster().size() > 0) {
                    bmodel.setEditStockCheck(false);
                    if (bmodel.hasAlreadyStockChecked(bmodel
                            .getRetailerMasterBO().getRetailerID())) {

                        bmodel.setEditStockCheck(true);
                        bmodel.loadStockCheckedProducts(bmodel
                                .getRetailerMasterBO().getRetailerID(), MENU_STK_ORD);


                        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                            if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK
                                    && bmodel.configurationMasterHelper.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) {
                                NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(getActivity());
                                mNearExpiryHelper.loadSKUTracking(getActivity(), false);
                            }

                            if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                                PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(getActivity());
                                priceTrackingHelper.loadPriceTransaction(getActivity());
                                if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getActivity())) {
                                    priceTrackingHelper.updateLastVisitPriceAndMRP();
                                }
                            }
                        }

                    }
                    bmodel.productHelper.setProductImageUrl();
                    bmodel.setEdit(false);
                    if (orderHelper.hasAlreadyOrdered(getActivity(), bmodel.getRetailerMasterBO()
                            .getRetailerID())) {
                        bmodel.setEdit(true);
                    }

                    if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                        bmodel.productHelper.getmProductidOrderByEntry().clear();
                        bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                    }

                    bmodel.productHelper.downloadIndicativeOrderList();//moved here to check size of indicative order
                    orderHelper.selectedOrderId = "";
                    if (bmodel.productHelper.getIndicativeList() != null
                            && bmodel.productHelper.getIndicativeList().size() < 1
                            && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {
                        if (bmodel.isEdit()) {
                            orderHelper.selectedOrderId = "";//cleared to avoid reuse of id
                            OrderTransactionListDialog obj = new OrderTransactionListDialog(getActivity().getApplicationContext(), getActivity(), new OrderTransactionListDialog.newOrderOnClickListener() {
                                @Override
                                public void onNewOrderButtonClick() {
                                    //the methods that were called during normal stock and order loading in non edit mode are called here
                                    //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                    bmodel.setOrderHeaderBO(null);
                                    loadRequiredMethodsForStockAndOrder();
                                    loadstockorderscreen();
                                }
                            }, new OrderTransactionListDialog.oldOrderOnClickListener() {
                                @Override
                                public void onOldOrderButtonClick(String id) {
                                    OrderHelper.getInstance(getActivity()).selectedOrderId = id;
                                    //the methods that were called during normal stock and order loading in edit mode are called here
                                    //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                                    //loadSerialNo,enableSchemeModule included as these were called in edit mode
                                    OrderHelper.getInstance(getActivity()).loadOrderedProducts(getActivity(), bmodel.getRetailerMasterBO()
                                            .getRetailerID(), id);
                                    OrderHelper.getInstance(getActivity()).loadSerialNo(getActivity());
                                    enableSchemeModule();
                                    loadRequiredMethodsForStockAndOrder();
                                    loadOrderSummaryScreen();
                                }
                            }, false, new OrderTransactionListDialog.OnDismissListener() {
                                @Override
                                public void onDismiss() {

                                }
                            });
                            obj.show();
                            obj.setCancelable(false);
                        } else {
                            //the methods that were called during normal stock and order loading in non edit mode are called here
                            //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                            bmodel.setOrderHeaderBO(null);
                            loadRequiredMethodsForStockAndOrder();
                            loadstockorderscreen();
                        }
                    } else {
                        if (bmodel.isEdit()) {//doubt
                            orderHelper.loadOrderedProducts(getActivity(), bmodel.getRetailerMasterBO()
                                    .getRetailerID(), null);
                            orderHelper.loadSerialNo(getActivity());
                            enableSchemeModule();
                        } else {
                            bmodel.setOrderHeaderBO(null);
                        }
                        loadRequiredMethodsForStockAndOrder();
                        if (bmodel.isEdit()) {
                            loadOrderSummaryScreen();

                        } else {
                            loadstockorderscreen();
                        }
                    }
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                }
            }
        }
    }

    private void loadRequiredMethodsForStockAndOrder() {
        try {
            CollectionHelper collectionHelper = CollectionHelper.getInstance(getActivity());

            if (bmodel.configurationMasterHelper.IS_GUIDED_SELLING) {
                bmodel.downloadGuidedSelling();
            }

            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                collectionHelper.downloadDiscountSlab();
            }

            //  bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
            bmodel.productHelper.loadRetailerWiseProductWisePurchased();
            bmodel.productHelper
                    .loadRetailerWiseProductWiseP4StockAndOrderQty();

            bmodel.configurationMasterHelper
                    .downloadProductDetailsList();

            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                /** Load Initiative **/
                bmodel.productHelper.loadInitiativeProducts();
                bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                        .getRetailerMasterBO().getSubchannelid());
                /** Load Order History **/
                bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }

            /** Load SO Norm **/
            if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                bmodel.productHelper
                        .loadRetailerWiseInventoryOrderQty();
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                bmodel.productHelper.updateProductColorAndSequance();

            /** Settign color **/
            bmodel.configurationMasterHelper.downloadFilterList();
            bmodel.productHelper.updateProductColor();

            DiscountHelper discountHelper = DiscountHelper.getInstance(getActivity());
            if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

                discountHelper.downloadBillWiseDiscount(getActivity());
                discountHelper.loadExistingBillWiseRangeDiscount(getActivity());
            }
            // apply bill wise pay term discount
            discountHelper.downloadBillWisePayTermDiscount(getActivity());

            bmodel.productHelper.downloadInStoreLocations();
            SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getActivity());
            if (schemeHelper.IS_SCHEME_ON_MASTER) {
                OrderHelper orderHelper = OrderHelper.getInstance(getActivity());
                schemeHelper.downloadSchemeHistoryDetails(getActivity(), bmodel.getRetailerMasterBO().getRetailerID(), bmodel.isEdit(), orderHelper.selectedOrderId);
            }
            schemeHelper.downloadOffInvoiceSchemeDetails(getActivity(), bmodel.getRetailerMasterBO().getRetailerID());


            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                collectionHelper.downloadBankDetails();
                collectionHelper.downloadBranchDetails();
                collectionHelper.downloadRetailerAccountDetails();
                collectionHelper.loadCreditNote();
            }

            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STK_ORD), 1);


            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                bmodel.downloadCurrencyConfig();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void enableSchemeModule() {
        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getActivity());
        if (schemeHelper.IS_SCHEME_ON
                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
            schemeHelper.loadSchemeDetails(getActivity(), bmodel
                    .getRetailerMasterBO().getRetailerID());
        }
    }

    public void loadstockorderscreen() {
        OrderHelper.getInstance(getActivity()).isQuickCall = true;
        Intent intent = new Intent(getActivity(),
                StockAndOrder.class);
        intent.putExtra("OrderFlag", "Nothing");
        intent.putExtra("ScreenCode",
                MENU_ORDER);
        startActivity(intent);


    }

    private void loadOrderSummaryScreen() {
        OrderHelper.getInstance(getActivity()).isQuickCall = true;

        Intent intent = new Intent(getActivity(),
                OrderSummary.class);
        intent.putExtra("ScreenCode", "MENU_ORDER");
        startActivity(intent);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getActivity().getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String searchText) {
                searchRetailer(searchText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    searchRetailer(null);
                }
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_deviate_retailers).setVisible(false);
    }

    private void searchRetailer(String searchText) {
        Vector<RetailerMasterBO> retailernew = new Vector<>();
        for (int i = 0; i < bmodel.retailerMaster.size(); i++) {
            if (searchText != null) {
                if ((bmodel.getRetailerMaster().get(i)
                        .getRetailerCode() != null) && ((bmodel.getRetailerMaster().get(i).getRetailerName()
                        .toLowerCase()).contains(searchText.toLowerCase()) ||
                        (bmodel.getRetailerMaster().get(i)
                                .getRetailerCode().toLowerCase())
                                .contains(searchText.toLowerCase())))

                    retailernew.add(bmodel.getRetailerMaster().get(i));


            } else
                retailernew.add(bmodel.getRetailerMaster().get(i));
        }

        RetailerSelectionAdapter adapter = new RetailerSelectionAdapter(retailernew);
        adapter.notifyDataSetChanged();
        lvSubDId.setAdapter(adapter);
        setHasOptionsMenu(true);
    }

}
