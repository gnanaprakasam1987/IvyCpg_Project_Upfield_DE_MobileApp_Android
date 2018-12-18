package com.ivy.cpg.view.nearexpiry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class NearExpiryTrackingFragment extends IvyBaseFragment implements
        BrandDialogInterface, FiveLevelFilterCallBack {


    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private final String strBarCodeSearch = "ALL";
    private BusinessModel mBModel;
    private DrawerLayout mDrawerLayout;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private Vector<ProductMasterBO> myList = new Vector<>();
    private EditText QUANTITY;
    private String append = "";
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private String generalFilterText = "General";
    private TextView tvSelectedFilter;
    private ListView lvwplist;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private boolean isAlertShowed;
    private boolean isFromChild;
    private ArrayList<ProductMasterBO> clearList = null;

    NearExpiryTrackingHelper mNearExpiryHelper;
    private ActionBar actionBar;

    private final static int NEAR_EXPIRY_RESULT_CODE = 119;
    private MyAdapter mSchedule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isAlertShowed = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_nearexpiry_tracking,
                container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            setScreenTitle(mNearExpiryHelper.mSelectedActivityName);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                if (actionBar != null) {
                    setScreenTitle(mNearExpiryHelper.mSelectedActivityName);
                }

                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {

                if (actionBar != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().invalidateOptionsMenu();
            }
        };

        clearList = new ArrayList<>();

        if (getView() != null) {
            lvwplist = view.findViewById(R.id.list);
            lvwplist.setCacheColorHint(0);
        }
        tvSelectedFilter = view.findViewById(R.id.sku);
        TextView tvcalendar = view.findViewById(R.id.opencalendar);
        TextView tvaudit = view.findViewById(R.id.audit);


        if (mBModel.configurationMasterHelper.IS_TEAMLEAD) {
            tvaudit.setVisibility(View.VISIBLE);

        }
        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        lvwplist.setLongClickable(true);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        Button btn_save = getView().findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClick();
            }
        });

        mSchedule = new MyAdapter(myList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());

        for (StandardListBO temp : mBModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mNearExpiryHelper.mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
            mNearExpiryHelper.mSelectedLocationName = " -"
                    + mBModel.productHelper.getInStoreLocation()
                    .get(mBModel.productHelper.getmSelectedGLobalLocationIndex()).getListName();
        }

        updateGeneralText(GENERAL);
        updateBrandText(BRAND, -1);
        FiveFilterFragment();

        mDrawerLayout.closeDrawer(GravityCompat.END);

    }

    @Override
    public void onResume() {
        lvwplist.invalidateViews();
        super.onResume();

        // if statement to make sure the alert is displayed
        // only for the first time
        if (mBModel.productHelper.getInStoreLocation().size() > 1 && !isAlertShowed) {
            if (!mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
                showLocationFilterAlert();
                isAlertShowed = true;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        try {
            boolean drawerOpen = false;

            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                if (mBModel.productHelper.getInStoreLocation().size() > 1)
                    menu.findItem(R.id.menu_location_filter).setVisible(false);
            }
            menu.findItem(R.id.menu_spl_filter).setVisible(false);

            menu.findItem(R.id.menu_remarks).setVisible(false);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_next).setVisible(false);

            if (mBModel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);


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
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_location_filter && mBModel.productHelper.getInStoreLocation().size() > 1) {
            showLocationFilterAlert();
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog dialog = new RemarksDialog("MENU_NEAREXPIRY");
            dialog.setCancelable(false);
            dialog.show(ft, "MENU_NEAREXPIRY");
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextButtonClick() {
        try {

            if (mNearExpiryHelper.checkDataToSave())
                new SaveAsyncTask().execute();
            else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void showLocationFilterAlert() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter,
                mNearExpiryHelper.mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mNearExpiryHelper.mSelectedLocationIndex = item;
                        mNearExpiryHelper.mSelectedLocationName = " -"
                                + mBModel.productHelper.getInStoreLocation()
                                .get(item).getListName();
                        dialog.dismiss();
                        lvwplist.invalidateViews();
                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }


    class ViewHolder {
        ProductMasterBO mSKUBO;
        TextView mBarCode;
        TextView mSKU;
        LinearLayout rlCalendar;
        ImageButton mCalendar;
        ImageButton audit;
        TextView productCodeTV;
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;

        // mSelectedLocation
        public MyAdapter(Vector<ProductMasterBO> items) {
            super(getActivity(), R.layout.nearexpiry_tracking_listview, items);
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

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());
                row = inflater.inflate(
                        R.layout.nearexpiry_tracking_listview, parent, false);

                holder.mBarCode = row
                        .findViewById(R.id.barcode);

                holder.mSKU = row.findViewById(R.id.sku);

                holder.rlCalendar = row
                        .findViewById(R.id.rl_calendar);

                holder.mCalendar = row
                        .findViewById(R.id.calendar);

                holder.audit = row
                        .findViewById(R.id.btn_audit);
                holder.productCodeTV = row
                        .findViewById(R.id.product_code);

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.mSKUBO.getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 2) {

                            holder.mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex).setAudit(1);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSKUBO.getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 1) {

                            holder.mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex).setAudit(0);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSKUBO.getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 0) {

                            holder.mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex).setAudit(2);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                if (mBModel.configurationMasterHelper.IS_TEAMLEAD && mBModel.configurationMasterHelper.IS_AUDIT_USER) {
                    holder.audit.setVisibility(View.VISIBLE);

                }
                holder.mCalendar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String id = holder.mSKUBO.getProductID();

                        Bundle args = new Bundle();
                        args.putString("PID", id);

                        Intent intent = new Intent(getActivity(),NearExpiryDateInputActivity.class);
                        intent.putExtras(args);

                        startActivityForResult( intent, NEAR_EXPIRY_RESULT_CODE);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

                    }
                });

                holder.mCalendar.setVisibility(View.VISIBLE);

                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSKUBO = items.get(position);
            holder.productCodeTV.setText(holder.mSKUBO.getProductCode());


            if (holder.mSKUBO.getLocations()
                    .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 2)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSKUBO.getLocations()
                    .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 1)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSKUBO.getLocations()
                    .get(mNearExpiryHelper.mSelectedLocationIndex).getAudit() == 0)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            if (holder.mSKUBO.getBarCode() == null
                    || "null".equals(holder.mSKUBO.getBarCode()))
                holder.mBarCode.setText("");
            else {
                holder.mBarCode.setText(holder.mSKUBO.getBarCode());
            }

            if (!mBModel.configurationMasterHelper.SHOW_BARCODE) {
                holder.mBarCode.setVisibility(View.GONE);
            }
            if (!mBModel.configurationMasterHelper.SHOW_PRODUCT_CODE) {
                holder.productCodeTV.setVisibility(View.GONE);
            }
            holder.mSKU.setText(holder.mSKUBO.getProductName());

            checkDataForColor(holder.mSKUBO.getProductID());

            if (holder.mSKUBO
                    .getLocations()
                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                    .isHasData())
                holder.mCalendar.setImageResource(R.drawable.ic_date_picker_blue);
            else {
                holder.mCalendar.setImageResource(R.drawable.ic_date_picker);
            }

            return row;
        }
    }

    String lastPid = "";

    private void checkDataForColor(String pid) {

        for (ProductMasterBO skubo : mBModel.productHelper.getProductMaster()) {

            if (skubo.getProductID().equals(pid)) {

                for (int k = 0; k < (skubo
                        .getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().size()); k++) {
                    if ((!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpPC()))
                            || (!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpOU()))
                            || (!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpCA()))) {
                        skubo.getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .setHasData(true);

                        if (!lastPid.equals(skubo.getProductID())) {
                            clearList.add(skubo);
                            lastPid = skubo.getProductID();
                        }
                        break;
                    } else {
                        skubo.getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .setHasData(false);
                    }

                }
            }
        }

    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                mNearExpiryHelper.saveSKUTracking(getActivity().getApplicationContext());
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_NEAREXPIRY);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            if (result == Boolean.TRUE) {
                clearObjects();
                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                        "", getResources().getString(R.string.saved_successfully),
                        false, getActivity().getResources().getString(R.string.ok),
                        null, new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                        }

                        startActivity(intent);
                        getActivity().finish();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();


            }

        }

    }

    //clear the list after save data into db to avoid duplicate records
    private void clearObjects() {
        if (clearList != null) {
            for (ProductMasterBO skubo : clearList) {

                for (int j = 0; j < skubo.getLocations().size(); j++) {

                    for (int k = 0; k < (skubo.getLocations()
                            .get(j).getNearexpiryDate().size()); k++) {

                        if (!"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpPC()) || !"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpCA()) || !"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpOU())) {

                            skubo.getLocations().get(j)
                                    .getNearexpiryDate()
                                    .get(k).setDate("");
                            skubo.getLocations().get(j)
                                    .getNearexpiryDate()
                                    .get(k).setNearexpPC("0");
                            skubo.getLocations().get(j)
                                    .getNearexpiryDate()
                                    .get(k).setNearexpCA("0");
                            skubo.getLocations().get(j)
                                    .getNearexpiryDate()
                                    .get(k).setNearexpOU("0");
                        }

                    }
                }
            }
        }
    }

    public void updateBrandText(String mFilterText, int mBid) {
        try {

            // Close the drawer
            mDrawerLayout.closeDrawers();
            Vector<ProductMasterBO> items = mBModel.productHelper
                    .getProductMaster();
            if (items == null) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalFilterText;

            myList.clear();
            // Add the products into list
            for (ProductMasterBO ret : items) {

                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || "ALL".equals(strBarCodeSearch)
                        && (mBid == ret.getParentid() || mBid == -1)
                        && ret.getIsSaleable() == 1
                        && generaltxt.equals(GENERAL)) {
                    myList.add(ret);
                }
            }

            String strFilterTxt;
            if (generaltxt.equals(GENERAL) && mFilterText.equals(BRAND)) {
                strFilterTxt = getResources().getString(
                        R.string.product_name)
                        + "(" + myList.size() + ")";
                tvSelectedFilter.setText(strFilterTxt);
            } else {
                strFilterTxt = mFilterText + "(" + myList.size() + ")";
                tvSelectedFilter
                        .setText(strFilterTxt);
            }

            // set the new list to listview
            mSchedule = new MyAdapter(myList);
            lvwplist.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void updatebrandtext(int productId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts) {
        try {
            Vector<ProductMasterBO> items = mBModel.productHelper
                    .getProductMaster();
            if (items == null) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            myList.clear();
            // Add the products into list
            if (mAttributeProducts != null) {
                if (productId != 0) {
                    for (ProductMasterBO productBO : items) {
                        if (productBO.getIsSaleable() == 1
                                && productBO.getParentHierarchy().contains("/" + productId + "/")
                                && mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            myList.add(productBO);
                        }
                    }
                } else {
                    for (int pid : mAttributeProducts) {
                        for (ProductMasterBO productBO : items) {
                            if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                                myList.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (ProductMasterBO ret : items) {

                    if (ret.getBarCode().equals(strBarCodeSearch)
                            || ret.getCasebarcode().equals(strBarCodeSearch)
                            || ret.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)
                            && (ret.getParentHierarchy().contains("/" + productId + "/"))
                            && ret.getIsSaleable() == 1) {
                        myList.add(ret);
                    }
                }
            }
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            // set the new list to listview
            mSchedule = new MyAdapter(myList);
            lvwplist.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public void updateGeneralText(String mFilterText) {
        generalFilterText = mFilterText;
        updateBrandText(BRAND, -1);
    }

    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s)) {
            s = QUANTITY.getText() + append;
            QUANTITY.setText(s);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                String strQty = Integer.toString(s);
                QUANTITY.setText(strQty);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                    eff();
                }
            }
        }
    }

    @Override
    public void updateFromFiveLevelFilter(int productId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mDrawerLayout.closeDrawers();
        updatebrandtext(productId, mSelectedIdByLevelId, mAttributeProducts);
    }


    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    mBModel.configurationMasterHelper.getGenFilter());
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
    public void onDestroy() {
        super.onDestroy();
        if (clearList != null)
            clearList = null;
        mNearExpiryHelper.clear();
        System.gc();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEAR_EXPIRY_RESULT_CODE && resultCode == 1){
            mSchedule.notifyDataSetChanged();
        }
    }
}
