package com.ivy.cpg.view.salesreturn;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.ProductSearch;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.model.ProductSearchCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.PauseOnFling;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.view.OnSingleClickListener;
import com.squareup.haha.trove.THash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;

public class SalesReturnFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, FiveLevelFilterCallBack, SalesReturnAdapter.SalesReturnInterface, ProductSearchCallBack {

    private static final int SALES_RET_SUMMARY = 1;
    private static final int SALES_ENTRY = 2;
    private double totalvalue = 0;

    private SalesReturnHelper salesReturnHelper;
    private TextView pnametitle;
    private View view;
    private DrawerLayout mDrawerLayout;
    private BusinessModel bmodel;
    FrameLayout drawer;
    private ViewFlipper viewFlipper;
    private Vector<ProductMasterBO> mylist;
    private static final String GENERAL = "General";
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    public RecyclerView lvwplist;
    public String strBarCodeSearch = "ALL";
    public TextView totalValueText, lpcText, productName;
    private Button btn_next;
    public int mSelectedFilter;
    private int mSelectedProductId;
    private ArrayList<Integer> mSelectedAttributeProductIds;
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private String screenTitle = "";
    private boolean loadBothSalable;
    private LinearLayout ListHeader;
    private RequestManager glide;
    private ProductSearch productSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_salesreturn_header, container,
                false);
        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);

        this.glide = Glide.with(getActivity());

        //setting drawer width equal to scren width
        drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        screenTitle = getActivity().getIntent().getStringExtra("screentitle");


        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());

        if(salesReturnHelper.IS_SHOW_SR_CATALOG){
            ListHeader=view.findViewById(R.id.ListHeader);
            ListHeader.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        setHasOptionsMenu(true);
        PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(getContext());
        priceTrackingHelper.mSelectedFilter = -1;
    }


    @Override
    public void onStart() {

        super.onStart();

        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());

        viewFlipper = view.findViewById(R.id.view_flipper);
        productName = view.findViewById(R.id.productName);


        btn_next = view.findViewById(R.id.btn_save);
        productSearch = new ProductSearch(getActivity(), salesReturnHelper.getSalesReturnProducts(), ProductSearch.SCREEN_CODE_ORDER);


        lvwplist = view.findViewById(R.id.list);
        lvwplist.setHasFixedSize(true);
        lvwplist.setItemViewCacheSize(10);
        lvwplist.setDrawingCacheEnabled(true);
        lvwplist.setItemAnimator(new DefaultItemAnimator());
        lvwplist.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        lvwplist.setNestedScrollingEnabled(false);
        lvwplist.addOnScrollListener(new PauseOnFling(glide));

        if(salesReturnHelper.IS_SHOW_SR_CATALOG) {
            GridLayoutManager gridLayoutManager;
            if (DeviceUtils.isCatalogDevice(getActivity())) {
                gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            } else {
                gridLayoutManager = new GridLayoutManager(getActivity(), 1);
            }
            lvwplist.setLayoutManager(gridLayoutManager);
        }else {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            lvwplist.setLayoutManager(linearLayoutManager);
        }

        totalValueText = view.findViewById(R.id.totalValue);
        lpcText = view.findViewById(R.id.lcp);

        btn_next.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onNextButtonClick();
            }
        });

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.totalTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.totalTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.totalTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        setScreenTitle(screenTitle);

        pnametitle = view.findViewById(R.id.tvProductNameTitle);
        loadBothSalable = salesReturnHelper.SHOW_SALABLE_AND_NON_SALABLE_SKU;

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout,
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View v) {
                if (getActionBar() != null) {
                    if (mSelectedFilter == 1) {
                        view.findViewById(R.id.view_loading).setVisibility(View.VISIBLE);
                        productSearch.startSearch(salesReturnHelper.getSalesReturnProducts(), mSelectedProductId, mSelectedAttributeProductIds);
                    }
                    else {
                        setScreenTitle(screenTitle);
                    }
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        productSearch.startSpecialFilterSearch(salesReturnHelper.getSalesReturnProducts(), GENERAL);


        mDrawerLayout.closeDrawer(GravityCompat.END);

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));


    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }


    public void refreshList() {
        String strPname = getResources().getString(
                R.string.product_name)
                + " (" + mylist.size() + ")";
        pnametitle.setText(strPname);




    }

    @Override
    public void onClick(View v) {

    }



    private void updateFooter() {
        totalvalue = 0;
        int lpccount = 0;
        Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        if (siz == 0)
            return;

        boolean lineflag;
        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);

            lineflag = false;
            for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {
                double temp;
                //if (bo.getSrpedit() > 0) {
                if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                        || bo.getOuterQty() > 0) {
                    lineflag = true;
                    temp = ((bo.getCaseQty() * bo.getCaseSize())
                            + (bo.getOuterQty() * bo.getOuterSize()) + bo
                            .getPieceQty()) * bo.getSrpedit();
                    totalvalue = totalvalue + temp;
                }

            }
            if (lineflag) {
                lpccount = lpccount + 1;
            }
        }
        String strLpcCount = Integer.toString(lpccount);
        lpcText.setText(strLpcCount);
        String strTotalValue = bmodel.formatValue(totalvalue) + "";
        totalValueText.setText(strTotalValue);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                new CommonDialog(getActivity().getApplicationContext(), getActivity(), "", getResources().getString(
                        R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        salesReturnHelper
                                .clearSalesReturnTable(false);
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(DateTimeUtils
                                        .now(DateTimeUtils.TIME));
                        getActivity().finish();

                        Intent myIntent = new Intent(getActivity(), HomeScreenTwo.class);
                        startActivityForResult(myIntent, 0);

                        /* User clicked OK so do some stuff */
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
                break;
            default:
                break;
        }
        return null;
    }

    public void onNextButtonClick() {
        if (salesReturnHelper.hasSalesReturn()) {
            if (!isValidData()) {
                Toast.makeText(getActivity(), "Replace quantity should not exceed Return quantity", Toast.LENGTH_LONG).show();
                return;
            }

            if (salesReturnHelper.CHECK_MRP_VALUE) {
                if (!isValidMRP()) {
                    Toast.makeText(getActivity(), "Please enter MRP Value", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            double totalOrderValue = bmodel.getOrderValue();
            if (bmodel.configurationMasterHelper.IS_ORD_SR_VALUE_VALIDATE &&
                    !bmodel.configurationMasterHelper.IS_INVOICE &&
                    totalvalue >= totalOrderValue) {
                Toast.makeText(getActivity(),
                        getResources().getString(
                                R.string.sales_return_value_should_not_exceed_order_value,
                                String.valueOf(totalOrderValue)),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (bmodel.configurationMasterHelper.IS_SR_VALIDATE_BY_RETAILER_TYPE) {
                if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CASH")) {
                    if (OrderHelper.getInstance(getActivity()).returnReplacementAmountValidation(true, false, getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.return_products_not_matching_replacing_product_price), Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CREDIT")) {
                    if (!OrderHelper.getInstance(getActivity()).returnReplacementAmountValidation(false, false, getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.return_products_price_less_than_replacing_product_price), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            salesReturnHelper.setLpcValue((String) lpcText.getText());
            salesReturnHelper.setReturnValue(totalvalue);

            Intent init = new Intent(getActivity(),
                    SalesReturnSummery.class);
            startActivityForResult(init, SALES_RET_SUMMARY);

        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_items_added), 0);
        }
    }

    private boolean isValidData() {
        Vector<ProductMasterBO> items = salesReturnHelper.getSalesReturnProducts();
        int totalRetQty = 0;
        int totalRepQty;
        int siz = items.size();

        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getRepPieceQty() > 0 || ret.getRepCaseQty() > 0 || ret.getRepOuterQty() > 0) {

                List<SalesReturnReasonBO> reasonList = ret.getSalesReturnReasonList();
                for (SalesReturnReasonBO salesReturnReasonBO : reasonList) {
                    totalRetQty = totalRetQty + (salesReturnReasonBO.getCaseQty() * ret.getCaseSize())
                            + (salesReturnReasonBO.getPieceQty())
                            + (salesReturnReasonBO.getOuterQty() * ret.getOutersize());
                }

                totalRepQty = (ret.getRepCaseQty() * ret.getCaseSize())
                        + (ret.getRepPieceQty())
                        + (ret.getRepOuterQty() * ret.getOutersize());
                if (totalRetQty < totalRepQty)
                    return false;
            }
        }
        return true;
    }

    private boolean isValidMRP() {
        int siz = salesReturnHelper.getSalesReturnProducts().size();
        if (siz == 0)
            return true;

        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = salesReturnHelper.getSalesReturnProducts().get(i);
            if (product.getSalesReturnReasonList() == null || product.getSalesReturnReasonList().size() == 0)
                return true;
            for (SalesReturnReasonBO bo : product
                    .getSalesReturnReasonList()) {
                if (bo.getCaseQty() > 0 || bo.getPieceQty() > 0 || bo.getOuterQty() > 0) {
                    if (bo.getOldMrp() == 0)
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);


        menu.findItem(R.id.menu_fivefilter).setVisible(true);
        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_scheme).setVisible(false);
        menu.findItem(R.id.menu_apply_so).setVisible(false);
        menu.findItem(R.id.menu_apply_std_qty).setVisible(false);
        menu.findItem(R.id.menu_sih_apply).setVisible(false);
        menu.findItem(R.id.menu_next).setIcon(
                R.drawable.ic_action_navigation_next_item);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);

        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        if (drawerOpen)
            menu.clear();


        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //clear sign details once sales return is exited to start fresh on next visit
                if (salesReturnHelper.getSignatureName() != null && !salesReturnHelper.getSignatureName().isEmpty()) {
                    String PHOTO_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;
                    bmodel.synchronizationHelper.deleteFiles(
                            PHOTO_PATH, salesReturnHelper.getSignatureName());
                }
                salesReturnHelper.setIsSignCaptured(false);
                salesReturnHelper.setSignatureName("");
                salesReturnHelper.setSignaturePath("");
                salesReturnHelper.setInvoiceId("");
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                    mDrawerLayout.closeDrawers();

                else {
                    if (salesReturnHelper.hasSalesReturn())
                        showCustomDialog();
                    else {
                        salesReturnHelper.clearSalesReturnTable(false);
                        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                                .now(DateTimeUtils.TIME));
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                        startActivity(intent);
                        getActivity().finish();
                    }

                }

                return true;

            case R.id.menu_fivefilter:
                FiveFilterFragment();
                return true;

            case R.id.menu_barcode:
                ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
                int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                        @Override
                        protected void startActivityForResult(Intent intent, int code) {
                            SalesReturnFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                        }
                    };
                    integrator.setBeepEnabled(false).initiateScan();
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.permission_enable_msg)
                                    + " " + getResources().getString(R.string.permission_camera)
                            , Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            if (salesReturnHelper.getFilterProductLevels() != null && salesReturnHelper.getFilterProductsByLevelId() != null)
                bundle.putString("isFrom", "SR");
            else
                bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public void updateBrandText(String mFilterText, int bid) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mSelectedFilter = 1;
        mSelectedProductId = mProductId;
        mSelectedAttributeProductIds = mAttributeProducts;

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();

    }


    private void showCustomDialog() {

        new CommonDialog(getActivity().getApplicationContext(), getActivity(), "", getResources().getString(
                R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                salesReturnHelper.clearSalesReturnTable(false);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                startActivity(intent);
                getActivity().finish();
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        }).show();
    }



    private void showSalesReturnDialog(String productId, int holderPostion, int holderTop) {
        Intent intent = new Intent(getActivity(),
                SalesReturnEntryActivity.class);
        intent.putExtra("pid", productId);
        intent.putExtra("position", holderPostion);
        intent.putExtra("top", holderTop);
        intent.putExtra("from", "SALESRETURN");

        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.zoom_enter, R.anim.hold);
        ActivityCompat.startActivityForResult(getActivity(), intent, SALES_ENTRY, opts.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == SALES_RET_SUMMARY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                if (getActivity() != null)
                    getActivity().finish();
            }
        } else if (requestCode == SALES_ENTRY) {
            if (resultCode == RESULT_OK) {
                getActivity().overridePendingTransition(0, R.anim.zoom_exit);
                productSearchResult(mylist);
                Bundle extras = data.getExtras();
                int holderPosition = extras.getInt("position", 0);
                int holderTop = extras.getInt("top", 0);
                if (mylist.size() > 0)
                    lvwplist.getLayoutManager().scrollToPosition(holderPosition);
            }
        } else {
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    strBarCodeSearch = result.getContents();
                    if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                        productSearch.startSearch(salesReturnHelper.getSalesReturnProducts(), strBarCodeSearch);

                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onListItemSelected(String pid) {
        productName.setText(bmodel.productHelper.getProductMasterBOById(pid).getProductName());
        if (viewFlipper.getDisplayedChild() != 0) {
            viewFlipper.showPrevious();
        }
    }

    @Override
    public void showSalesReturnDialog(String pid) {
        View vChild = lvwplist.getChildAt(0);
                        int holderPosition = ((LinearLayoutManager)lvwplist.getLayoutManager()).findFirstVisibleItemPosition();
                        int holderTop = (vChild == null) ? 0 : (vChild.getTop() - lvwplist.getPaddingTop());

                        productName.setText(bmodel.productHelper.getProductMasterBOById(pid).getProductName());
                        showSalesReturnDialog(pid, holderPosition, holderTop);
    }

    @Override
    public void productSearchResult(Vector<ProductMasterBO> searchedList) {

        mSelectedFilter = -1;// clearing filter flag

        if(mylist==null)mylist=new Vector<>();
        mylist.clear();
        mylist.addAll(searchedList);

        lvwplist.setAdapter(new SalesReturnAdapter(mylist,getActivity(),bmodel,glide));

        updateFooter();
        setScreenTitle(screenTitle);

        view.findViewById(R.id.view_loading).setVisibility(View.GONE);
        if(mylist.size()>0)
        view.findViewById(R.id.view_empty).setVisibility(View.GONE);
        else view.findViewById(R.id.view_empty).setVisibility(View.VISIBLE);


        getActivity().supportInvalidateOptionsMenu();
    }
}