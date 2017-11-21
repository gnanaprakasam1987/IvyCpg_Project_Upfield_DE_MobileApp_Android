package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SalesReturnReasonBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SalesReturnWithActionBar extends ToolBarwithFilter implements
        BrandDialogInterface, OnEditorActionListener {

    private static final int SALES_RET_SUMMARY = 1;
    private static final int SALES_ENTRY = 2;
    private double totalvalue = 0;
    private ProductMasterBO productMasterBO;
    private SalesReturnHelper salesReturnHelper;
    private String BRAND_STRING = "Brand";
    private TextView pnametitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // include List Header
        LinearLayout ll = (LinearLayout) findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        ll.addView(layoutInflater.inflate(
                R.layout.include_salesreturn_list_header, nullParent));

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        salesReturnHelper = SalesReturnHelper.getInstance(this);


        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totalTitle).getTag()) != null)
                ((TextView) findViewById(R.id.totalTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.totalTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        pnametitle = (TextView) findViewById(R.id.tvProductNameTitle);
        ((TextView) findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.totalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        mDrawerToggle = new ActionBarDrawerToggle(
                SalesReturnWithActionBar.this, mDrawerLayout,
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getSupportActionBar() != null) {
                    setScreenTitle(bmodel.configurationMasterHelper
                            .getHomescreentwomenutitle(StandardListMasterConstants.MENU_SALES_RET));
                }

                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getSupportActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        setActionBarTitle(bmodel.configurationMasterHelper
                .getHomescreentwomenutitle(StandardListMasterConstants.MENU_SALES_RET));


        hideSpecialFilter();
        hideLocationButton();

        updateBrandText(BRAND_STRING, -1);

        if (bmodel.productHelper.getChildLevelBo() != null) {
            // Check weather Object are still exist or not.
            int siz = 0;
            try {
                Vector<ChildLevelBo> items = bmodel.productHelper.getChildLevelBo();
                siz = items.size();
            } catch (Exception nulle) {
                Toast.makeText(this, "Session out. Login again.",
                        Toast.LENGTH_SHORT).show();
                Commons.printException(nulle);
                finish();
            }
            if (siz == 0)
                return;
        }

        onKeyInvisibleSub();

        mBtn_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonClick();
            }
        });
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Commons.print("OnResume Called");
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        salesReturnHelper = SalesReturnHelper.getInstance(this);
    }

    public void refreshList() {
        String strPname = getResources().getString(
                R.string.product_name)
                + " (" + mylist.size() + ")";
        pnametitle.setText(strPname);
        lvwplist.setAdapter(new MyAdapter(mylist));
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        salesReturnHelper = SalesReturnHelper.getInstance(this);
    }


    class MyAdapter extends ArrayAdapter<ProductMasterBO> {

        private final ArrayList<ProductMasterBO> items;
        private CustomKeyBoard dialogCustomKeyBoard;

        MyAdapter(ArrayList<ProductMasterBO> items) {

            super(SalesReturnWithActionBar.this, R.layout.row_salesreturn,
                    items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            productMasterBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();

                row = inflater.inflate(R.layout.row_salesreturn, parent, false);

                holder = new ViewHolder();

                holder.psname = (TextView) row.findViewById(R.id.productName);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.total = (TextView) row.findViewById(R.id.total);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.total.setPaintFlags(holder.total.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                        productName.setText(holder.pname);


                        if (viewFlipper.getDisplayedChild() != 0) {
                           /* viewFlipper.setInAnimation(
                                    SalesReturnWithActionBar.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(
                                    SalesReturnWithActionBar.this,
                                    R.anim.out_to_left);*/
                            viewFlipper.showPrevious();
                        }
                    }
                });

                holder.total.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        View vChild = lvwplist.getChildAt(0);
                        int holderPosition = lvwplist.getFirstVisiblePosition();
                        int holderTop = (vChild == null) ? 0 : (vChild.getTop() - lvwplist.getPaddingTop());

                        productName.setText(holder.pname);
                        showSalesReturnDialog(holder.productBO.getProductID(), v, holderPosition, holderTop);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = productMasterBO;
            if (holder.productBO.getSalesReturnReasonList() != null && holder.productBO.getSalesReturnReasonList().size() != 0)
                holder.reasonBO = holder.productBO.getSalesReturnReasonList().get(holder.productBO.getSelectedSalesReturnPosition());

            holder.pname = productMasterBO.getProductName();
            holder.psname.setText(productMasterBO.getProductShortName());


            int total = 0;
            for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
                total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
            String strTotal = Integer.toString(total);
            holder.total.setText(strTotal);
            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(SalesReturnWithActionBar.this, R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(SalesReturnWithActionBar.this, R.color.list_odd_item_bg));
            }
            return row;
        }
    }

    class ViewHolder {
        private SalesReturnReasonBO reasonBO;
        private ProductMasterBO productBO;
        private String pname;
        private TextView psname;
        private TextView total;
    }

    private void updateValue() {
        totalvalue = 0;
        int lpccount = 0;
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
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
                /*} else {
                    if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                            || bo.getOuterQty() > 0) {
                        lineflag = true;
                        temp = (bo.getCaseQty() * ret.getCsrp())
                                + (bo.getPieceQty() * ret.getSrp())
                                + (bo.getOuterQty() * ret.getOsrp());
                        totalvalue = totalvalue + temp;
                    }
                }*/
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
//                CommonDialog dialog=
                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        salesReturnHelper
                                .clearSalesReturnTable();
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(SDUtil
                                        .now(SDUtil.TIME));
                        finish();
                        BusinessModel.loadActivity(
                                SalesReturnWithActionBar.this,
                                DataMembers.actHomeScreenTwo);
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

    @Override
    public void onNextButtonClick() {
        if (salesReturnHelper.hasSalesReturn()) {
            if (!isValidData()) {
                Toast.makeText(this, "Replace quantity should not exceed Return quantity", Toast.LENGTH_LONG).show();
                return;
            }

            if (bmodel.configurationMasterHelper.CHECK_MRP_VALUE) {
                if (!isValidMRP()) {
                    Toast.makeText(this, "Please enter MRP Value", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            salesReturnHelper.setLpcValue((String) lpcText.getText());
            salesReturnHelper.setReturnValue(totalvalue);

            Intent init = new Intent(SalesReturnWithActionBar.this,
                    SalesReturnSummery.class);
            startActivityForResult(init, SALES_RET_SUMMARY);

        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_items_added), 0);
        }
    }

    private boolean isValidData() {
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
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
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return true;

        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper.getProductMaster().get(i);
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

    private void enableDot() {
        SalesReturnWithActionBar.super.onDotBtnEnable();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        if (!bmodel.configurationMasterHelper.SHOW_REMARKS_SAL_RET) {
            hideRemarksButton();
            menu.findItem(R.id.menu_remarks).setVisible(false);
        } else
            menu.findItem(R.id.menu_remarks).setVisible(true);
        hideShemeButton();
        menu.findItem(R.id.menu_apply_so).setVisible(false);
        menu.findItem(R.id.menu_apply_std_qty).setVisible(false);
        menu.findItem(R.id.menu_next).setIcon(
                R.drawable.ic_action_navigation_next_item);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                    mDrawerLayout.closeDrawers();
                else
                    showCustomDialog();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onNoteButtonClick() {
        super.onNoteButtonClick();
        FragmentTransaction ft = (this)
                .getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog(StandardListMasterConstants.MENU_SALES_RET);
        dialog.setCancelable(false);
        dialog.show(ft, "sl_ret_remark");
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = mFilterText;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            productName.setText("");

            items = getProducts();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<>();
            mylist.clear();
            // Add the products into list
            int orderCount = -1;
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (bid == -1) {
                    if (BRAND_STRING.equals(mFilterText) || (bid == ret.getParentid())) {
                        int count = 0;
                        for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {

                            if (bo.getPieceQty() > 0 || bo.getCaseQty() > 0 || bo.getOuterQty() > 0) {
                                count = count + 1;
                                orderCount = orderCount + 1;
                                mylist.add(orderCount, ret);
                                break;
                            }
                        }
                        if (count > 0)
                            continue;
                    }
                } else if (bid == ret.getParentid()) {
                    int count = 0;
                    for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {
                        if (bo.getPieceQty() > 0 || bo.getCaseQty() > 0 || bo.getOuterQty() > 0) {
                            count = count + 1;
                            orderCount = orderCount + 1;
                            mylist.add(orderCount, ret);
                            break;
                        }
                    }
                    if (count > 0)
                        continue;
                }

                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || ("ALL").equals(strBarCodeSearch)) {

                    if (isSpecialFilter_enabled) {
                        if ((bid == ret.getParentid() || bid == -1) && ret.getIsSaleable() == 1) {
                            if (generaltxt.equals(mSbd) && ret.isRPS()) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mOrdered)
                                    && (ret.getOrderedPcsQty() > 0
                                    || ret.getOrderedCaseQty() > 0 || ret
                                    .getOrderedOuterQty() > 0)) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mPurchased)
                                    && ret.getIsPurchased() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mInitiative)
                                    && ret.getIsInitiativeProduct() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mCommon)
                                    && applyCommonFilterConfig(ret)) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mSbdGaps)
                                    && (ret.isRPS() && !ret.isSBDAcheived())) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(GENERAL)) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mInStock)
                                    && ret.getWSIH() > 0) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mOnAllocation)
                                    && ret.getSIH() > 0
                                    && ret.isAllocation() == 1
                                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mPromo)
                                    && ret.isPromo()) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mMustSell)
                                    && ret.getIsMustSell() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mFocusBrand)
                                    && ret.getIsFocusBrand() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mFocusBrand2)
                                    && ret.getIsFocusBrand2() == 1) {
                                mylist.add(ret);

                            } else if (generaltxt.equals(msih)
                                    && ret.getSIH() > 0) {
                                mylist.add(ret);

                            } else if (generaltxt.equals(mOOS)
                                    && ret.getOos() == 0) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mDiscount) && ret.getIsDiscountable() == 1) {
                                mylist.add(ret);
                            }
                        }
                    } else {
                        if (bid == -1 && ret.getIsSaleable() == 1) {
                            if (mFilterText.equals(BRAND_STRING)) {
                                mylist.add(ret);
                            }
                        } else if (bid == ret.getParentid() && ret.getIsSaleable() == 1) {
                            mylist.add(ret);
                        }

                    }
                }
            }
            refreshList();
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void showCustomDialog() {

        new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {
                salesReturnHelper.clearSalesReturnTable();
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                BusinessModel.loadActivity(SalesReturnWithActionBar.this,
                        DataMembers.actHomeScreenTwo);
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        }).show();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mylist = new ArrayList<>();
        fiveFilter_productIDs = new ArrayList<>();
        brandbutton = mFilterText;
        if (mAttributeProducts != null) {
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    for (ProductMasterBO productBO : items) {
                        if (levelBO.getProductID() == productBO.getParentid() && productBO.getIsSaleable() == 1) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(Integer.parseInt(productBO.getProductID()))) {
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {
                        if (pid == Integer.parseInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            }
        } else {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO productBO : items) {
                    if (levelBO.getProductID() == productBO.getParentid() && productBO.getIsSaleable() == 1) {
                        mylist.add(productBO);
                        fiveFilter_productIDs.add(productBO.getProductID());
                    }

                }
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();
        refreshList();
        updateValue();
    }

    private void showSalesReturnDialog(String productId, View v, int holderPostion, int holderTop) {
        Intent intent = new Intent(SalesReturnWithActionBar.this,
                SalesReturnEntryActivity.class);
        intent.putExtra("pid", productId);
        intent.putExtra("position", holderPostion);
        intent.putExtra("top", holderTop);

        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
        ActivityCompat.startActivityForResult(this, intent, SALES_ENTRY, opts.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SALES_RET_SUMMARY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                /*finish();
                BusinessModel.loadActivity(this,
                        DataMembers.actHomeScreenTwo);*/

                Intent intent = new Intent(this, HomeScreenTwo.class);

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                    intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                }

                startActivity(intent);
                finish();
            }
        }

        if (requestCode == SALES_ENTRY) {
            if (resultCode == RESULT_OK) {
                overridePendingTransition(0, R.anim.zoom_exit);
                updateValue();
                refreshList();
                Bundle extras = data.getExtras();
                int holderPosition = extras.getInt("position", 0);
                int holderTop = extras.getInt("top", 0);
                if (mylist.size() > 0)
                    lvwplist.setSelectionFromTop(holderPosition, holderTop);
            }
        }
    }
}