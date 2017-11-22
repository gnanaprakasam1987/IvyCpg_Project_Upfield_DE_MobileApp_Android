package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class NearExpiryTrackingFragment extends IvyBaseFragment implements
        BrandDialogInterface {
    // By Default Select All
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private final String strBarCodeSearch = "ALL";
    private BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private Vector<ProductMasterBO> myList;
    private EditText QUANTITY;
    private String append = "";
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private String generalFilterText = "General";
    private TextView tvSelectedFilter;
    private ListView lvwplist;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private boolean isAlertShowed;
    private NearExpiryDialogueFragment dialog;
    private boolean isFromChild;

    public NearExpiryDialogueFragment getDialog() {
        return dialog;
    }

    public void setDialog(NearExpiryDialogueFragment dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isAlertShowed = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearexpiry_tracking,
                container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onStart() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);

        setScreenTitle(bmodel.mSelectedActivityName);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                if (actionBar != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {

                if (actionBar != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        if (getView() != null) {
            lvwplist = (ListView) getView().findViewById(R.id.list);
            lvwplist.setCacheColorHint(0);
        }
        tvSelectedFilter = (TextView) getView().findViewById(R.id.sku);
        tvSelectedFilter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvcalendar = (TextView) getView().findViewById(R.id.opencalendar);
        tvcalendar.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvaudit = (TextView) getView().findViewById(R.id.audit);
        tvaudit.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvpiece = (TextView) getView().findViewById(R.id.tvpiece);
        TextView tvouter = (TextView) getView().findViewById(R.id.tvouter);
        TextView tvcase = (TextView) getView().findViewById(R.id.tvcase);

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
            tvaudit.setVisibility(View.VISIBLE);

        }

       /* if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
            getView().findViewById(R.id.productBarcodetitle).setVisibility(
                    View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView()
                        .findViewById(R.id.productBarcodetitle).getTag()) != null)
                    ((TextView) getView()
                            .findViewById(R.id.productBarcodetitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.productBarcodetitle).getTag()));
            } catch (Exception e) {
                Commons.printException(""+e);
            }
        }*/

        /*if (!bmodel.configurationMasterHelper.SHOW_PRODUCT_CODE) {
            getView().findViewById(R.id.productcodtitle).setVisibility(
                    View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView()
                        .findViewById(R.id.productcodtitle).getTag()) != null)
                    ((TextView) getView().findViewById(R.id.productcodtitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.productcodtitle).getTag()));
            } catch (Exception e) {
                Commons.printException(""+e);
            }
        }*/

        //LinearLayout layout_keypad = (LinearLayout) getView().findViewById(R.id.footer);

        tvcalendar.setVisibility(View.VISIBLE);
        //layout_keypad.setVisibility(View.GONE);
        tvpiece.setVisibility(View.GONE);
        tvouter.setVisibility(View.GONE);
        tvcase.setVisibility(View.GONE);

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);


        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
            bmodel.mNearExpiryTrackingHelper.mSelectedLocationName = " -"
                    + bmodel.productHelper.getInStoreLocation()
                    .get(bmodel.productHelper.getmSelectedGLobalLocationIndex()).getListName();
        }

        lvwplist.setLongClickable(true);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mSelectedFilterMap.put("Brand",
                String.valueOf(bmodel.mSFSelectedFilter));
        updateGeneralText(GENERAL);
        updateBrandText(BRAND, bmodel.mSFSelectedFilter);
        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            FiveFilterFragment();
        else
            productFilterClickedFragment();
        mDrawerLayout.closeDrawer(GravityCompat.END);

        Button btn_save = (Button) getView().findViewById(R.id.btn_save);
        btn_save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClick();
            }
        });

        super.onStart();
    }

    @Override
    public void onResume() {
        lvwplist.invalidateViews();
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Near Expiry Tracking");
        // if statement to make sure the alert is displayed
        // only for the first time
        if (bmodel.productHelper.getInStoreLocation().size() > 1 && !isAlertShowed) {
            if (!bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
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
            boolean navDrawerOpen = false;

            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_location_filter).setVisible(false);
            }
            menu.findItem(R.id.menu_spl_filter).setVisible(false);

            menu.findItem(R.id.menu_remarks).setVisible(false);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_next).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
          /*else
                menu.findItem(R.id.menu_product_filter).setVisible(true);*/

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }
            if (drawerOpen || navDrawerOpen)
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
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
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
        } else if (i == R.id.menu_location_filter && bmodel.productHelper.getInStoreLocation().size() > 1) {
            showLocationFilterAlert();
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
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

            if (bmodel.mNearExpiryTrackingHelper.checkDataToSave())
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
                bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex = item;
                        bmodel.mNearExpiryTrackingHelper.mSelectedLocationName = " -"
                                + bmodel.productHelper.getInStoreLocation()
                                .get(item).getListName();
                        dialog.dismiss();
                        lvwplist.invalidateViews();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }


    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();

            if (frag != null)
                ft.detach(frag);

            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            bundle.putString("isFrom", "NearExpiry");

            if (bmodel.productHelper.getRetailerModuleChildLevelBO().size() > 0)
                bundle.putString("filterHeader", bmodel.productHelper
                        .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0).getPl_productLevel());

            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getRetailerModuleChildLevelBO());

            if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null
                    && bmodel.productHelper.getRetailerModuleChildLevelBO().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
                bundle.putString("isFrom", "STK");
            }

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());
                row = inflater.inflate(
                        R.layout.nearexpiry_tracking_listview, parent, false);

                holder.mBarCode = (TextView) row
                        .findViewById(R.id.barcode);
                holder.mBarCode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.mSKU = (TextView) row.findViewById(R.id.sku);
                holder.mSKU.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());

                holder.rlCalendar = (LinearLayout) row
                        .findViewById(R.id.rl_calendar);

                holder.mCalendar = (ImageButton) row
                        .findViewById(R.id.calendar);

                //holder.mCalendarDone = (ImageButton) row.findViewById(R.id.calendar_done);

                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);
                holder.productCodeTV = (TextView) row
                        .findViewById(R.id.product_code);
                holder.productCodeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.mSKUBO.getLocations()
                                .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 2) {

                            holder.mSKUBO.getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).setAudit(1);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSKUBO.getLocations()
                                .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 1) {

                            holder.mSKUBO.getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).setAudit(0);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSKUBO.getLocations()
                                .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 0) {

                            holder.mSKUBO.getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).setAudit(2);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                if (bmodel.configurationMasterHelper.IS_TEAMLEAD && bmodel.configurationMasterHelper.IS_AUDIT_USER) {
                    holder.audit.setVisibility(View.VISIBLE);

                }
                holder.mCalendar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String id = holder.mSKUBO.getProductID();
                        dialog = new NearExpiryDialogueFragment();
                        setDialog(dialog);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                notifyDataSetChanged();
                            }
                        });
                        Bundle args = new Bundle();
                        args.putString("PID", id);
                        dialog.setArguments(args);
                        dialog.show(getFragmentManager(), "Near Expiry");
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
                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 2)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSKUBO.getLocations()
                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 1)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSKUBO.getLocations()
                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit() == 0)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            if (holder.mSKUBO.getBarCode() == null
                    || "null".equals(holder.mSKUBO.getBarCode()))
                holder.mBarCode.setText("");
            else {
                holder.mBarCode.setText(holder.mSKUBO.getBarCode());
            }

            if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
                holder.mBarCode.setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.SHOW_PRODUCT_CODE) {
                holder.productCodeTV.setVisibility(View.GONE);
            }
            holder.mSKU.setText(holder.mSKUBO.getProductName());

            checkDataForColor(holder.mSKUBO.getProductID());

            if (holder.mSKUBO
                    .getLocations()
                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                    .isHasData())
                holder.mCalendar.setImageResource(R.drawable.ic_date_picker_blue);
            else {
                holder.mCalendar.setImageResource(R.drawable.ic_date_picker);
            }

            TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return row;
        }
    }

    private void checkDataForColor(String pid) {

        for (ProductMasterBO skubo : bmodel.productHelper.getProductMaster()) {

            if (skubo.getProductID().equals(pid)) {

                for (int k = 0; k < (skubo
                        .getLocations()
                        .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().size()); k++) {
                    if ((!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpPC()))
                            || (!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpOU()))
                            || (!"0"
                            .equals(skubo
                                    .getLocations()
                                    .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(k).getNearexpCA()))) {
                        skubo.getLocations()
                                .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
                                .setHasData(true);
                        break;
                    } else {
                        skubo.getLocations()
                                .get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex)
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
                bmodel.mNearExpiryTrackingHelper.saveSKUTracking();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_NEAREXPIRY);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                bmodel.updateIsVisitedFlag();

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

                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                        "", getResources().getString(R.string.saved_successfully),
                        false, getActivity().getResources().getString(R.string.ok),
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
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

    public void updateBrandText(String mFilterText, int mBid) {
        try {

            // Close the drawer
            mDrawerLayout.closeDrawers();
            bmodel.mSFSelectedFilter = mBid;
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalFilterText;

            myList = new Vector<>();
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
            MyAdapter mSchedule = new MyAdapter(myList);
            lvwplist.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void updatebrandtext(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts) {
        try {
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            myList = new Vector<>();
            // Add the products into list
            if (mAttributeProducts != null) {
                if (!parentidList.isEmpty()) {
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO productBO : items) {
                            if (productBO.getIsSaleable() == 1
                                    && levelBO.getProductID() == productBO.getParentid()
                                    && mAttributeProducts.contains(Integer.parseInt(productBO.getProductID()))) {
                                // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                myList.add(productBO);
                            }
                        }
                    }
                } else {
                    for (int pid : mAttributeProducts) {
                        for (ProductMasterBO productBO : items) {
                            if (pid == Integer.parseInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                                myList.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO ret : items) {

                        if (ret.getBarCode().equals(strBarCodeSearch)
                                || ret.getCasebarcode().equals(strBarCodeSearch)
                                || ret.getOuterbarcode().equals(strBarCodeSearch)
                                || "ALL".equals(strBarCodeSearch)
                                && (levelBO.getProductID() == ret.getParentid())
                                && ret.getIsSaleable() == 1) {
                            myList.add(ret);
                        }
                    }
                }
            }
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            // set the new list to listview
            MyAdapter mSchedule = new MyAdapter(myList);
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
            bmodel.showAlert(
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
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mDrawerLayout.closeDrawers();
        updatebrandtext(mParentIdList, mSelectedIdByLevelId, mAttributeProducts);
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
                    bmodel.configurationMasterHelper.getGenFilter());
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
}
