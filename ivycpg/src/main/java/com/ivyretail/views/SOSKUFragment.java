package com.ivyretail.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SOSKUBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SOSKUFragment extends IvyBaseFragment implements
        BrandDialogInterface {
    // Constants
    private static final String BRAND = "Brand";
    // Global Variables
    private BusinessModel bmodel;
    // Drawer Implementation
    private DrawerLayout mDrawerLayout;
    // Hash map to get selected Category and its Id
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private int selectedfilterid = -1;
    private List<SOSKUBO> myList;
    private ListView lvwplist;
    private String brandFilterText = "BRAND";
    // Reason Adapter
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    // Photo Image Name
    private String mImageName;
    // Dialog to enter brand total values
    private Dialog dialog = null;
    // Holder to pass BrandId to dialog
    private ViewHolder mSelectedHolder;
    // Get the typed number and set in Edit Text
    private EditText mSelectedET;
    private EditText mParentTotal;
    // List for SOSBo
    private final List<SOSKUBO> mCategoryForDialog = new ArrayList<>();
    // Calculate sum for norm,total,target, actual;
    private TextView tvSelectedName;
    //private TextView tvTotalNorm;
    // private TextView tvTotal;
    //  private TextView tvTarget;
    //  private TextView tvTotalActual;
    //  private TextView tvTotalGap;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private boolean isFromChild;

    private Vector<LevelBO> parentidList;
    private ArrayList<Integer> mAttributeProducts;
    private String filtertext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sos, container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (getView() != null) {
            lvwplist = (ListView) getView().findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);
        }

        FrameLayout drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);

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
            setScreenTitle(
                    bmodel.mSelectedActivityName);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (actionBar != null) {
                    actionBar.setTitle(bmodel.mSelectedActivityName);
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null) {
                    actionBar.setTitle(getResources().getString(R.string.filter));
                }
                getActivity().supportInvalidateOptionsMenu();
            }
        };
        //setting Header Title Fonts
        ((TextView) getView().findViewById(R.id.levelName)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hlength)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hlengthacttar)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) getView().findViewById(R.id.hpercent)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hpercentacttar)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) getView().findViewById(R.id.hGap)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvSelectedName = (TextView) getView().findViewById(R.id.levelName);
        // tvTotalNorm = (TextView) getView().findViewById(R.id.tv_totalnorm);
        //   tvTotal = (TextView) getView().findViewById(R.id.tv_totalvalue);
        //    tvTarget = (TextView) getView().findViewById(R.id.tv_target);
        //  tvTotalActual = (TextView) getView().findViewById(R.id.tv_actual);
        //   tvTotalGap = (TextView) getView().findViewById(R.id.tv_gap);
        Button btn_save = (Button) getView().findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSOSKU();
            }
        });


        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);
        if (parentidList != null || mSelectedIdByLevelId != null || mAttributeProducts != null) {
            updateFromFiveLevelFilter(parentidList, mSelectedIdByLevelId, mAttributeProducts, filtertext);
        } else {
            updateBrandText(BRAND, selectedfilterid);
        }

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            FiveFilterFragment();
        else
            productFilterClickedFragment();

        loadReasons();

        calculateTotalValues();
    }

    /**
     * Add sum of values and show in bottom of the Screen
     */
    private void calculateTotalValues() {
        ArrayList<Integer> parentIds = new ArrayList<>();
        int mactual = 0;
        int mtotal = 0;
        float mtarget = 0;
        float mGap = 0;
        float mNamtot = 0;
        ArrayList<SOSKUBO> soskuList = bmodel.salesFundamentalHelper.getmSOSKUList();
        if (soskuList != null) {
            for (SOSKUBO temp : bmodel.salesFundamentalHelper.getmSOSKUList()) {
                if (temp.getIsOwn() == 1) {
                    if (!parentIds.contains(temp.getParentID())) {
                        mtotal = mtotal + temp.getParentTotal();
                        parentIds.add(temp.getParentID());
                    }
                    mtarget = mtarget + SDUtil.convertToFloat(temp.getTarget());
                    mactual = mactual + temp.getActual();
                    mGap = mGap + SDUtil.convertToFloat(temp.getGap());
                    mNamtot = mNamtot + temp.getNorm();
                }

            }
        }
        String strNamtot = mNamtot + "";
        //tvTotalNorm.setText(strNamtot);
        String strTotal = mtotal + "";
        //tvTotal.setText(strTotal);
        String strTarget = mtarget + "";
        //tvTarget.setText(strTarget);
        String strActual = mactual + "";
        //tvTotalActual.setText(strActual);
        String strGap = mGap + "";
        //tvTotalGap.setText(strGap);
    }

    /**
     * Initialize Adapter and add reason for SOD module Reason Category : SOD
     */
    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<ReasonMaster>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if ("SOSKU".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }
    }

    class ViewHolder {
        SOSKUBO mSOSKU;
        TextView tvBrandName;
        TextView tvNorm;
        TextView tvTarget;
        TextView tvActual;
        TextView tvPercentage;
        TextView tvGap;
        EditText etTotal;
        Spinner spnReason;
        ImageView btnPhoto;
    }

    private class MyAdapter extends ArrayAdapter<SOSKUBO> {
        private final List<SOSKUBO> items;

        public MyAdapter(List<SOSKUBO> mylist) {
            super(getActivity(), R.layout.row_sos, mylist);
            this.items = mylist;
        }

        public SOSKUBO getItem(int position) {
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
                row = inflater.inflate(R.layout.row_sos, parent, false);
                holder.tvBrandName = (TextView) row
                        .findViewById(R.id.tvBrandName);
                holder.tvNorm = (TextView) row
                        .findViewById(R.id.tvNorm);

                holder.tvTarget = (TextView) row
                        .findViewById(R.id.tvTarget);
                holder.tvActual = (TextView) row
                        .findViewById(R.id.tvActual);
                holder.tvPercentage = (TextView) row
                        .findViewById(R.id.tvPercentage);
                holder.tvGap = (TextView) row.findViewById(R.id.tvGap);
                holder.btnPhoto = (ImageView) row
                        .findViewById(R.id.btn_photo);
                holder.spnReason = (Spinner) row
                        .findViewById(R.id.spnReason);

                holder.etTotal = (EditText) row
                        .findViewById(R.id.etTotal);
                holder.etTotal.setTag(holder);
                holder.etTotal.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        int inType = holder.etTotal.getInputType();
                        holder.etTotal.setInputType(InputType.TYPE_NULL);
                        holder.etTotal.onTouchEvent(event);
                        holder.etTotal.setInputType(inType);
                        holder.etTotal.selectAll();
                        holder.etTotal.requestFocus();

                        if (dialog != null) {
                            if (dialog.isShowing()) {
                                return false;
                            } else {
                                dialog.cancel();
                                dialog = null;

                            }
                        }
                        mSelectedHolder = (ViewHolder) v.getTag();
                        getTotalValue(mSelectedHolder.mSOSKU
                                .getParentID());

                        return true;
                    }
                });
                holder.spnReason.setAdapter(spinnerAdapter);
                holder.spnReason
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.spnReason
                                        .getSelectedItem();

                                holder.mSOSKU.setReasonId(SDUtil
                                        .convertToInt(reString.getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bmodel.isExternalStorageAvailable()) {
                            mImageName = "SOSKU_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOSKU.getProductID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            bmodel.salesFundamentalHelper.mSelectedBrandID = holder.mSOSKU
                                    .getProductID();
                            String fnameStarts = "SOSKU_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOSKU.getProductID() + "_"
                                    + Commons.now(Commons.DATE);

                            boolean nFilesthere = bmodel
                                    .checkForNFilesInFolder(
                                            HomeScreenFragment.photoPath,
                                            bmodel.mImageCount, fnameStarts);
                            if (nFilesthere) {

                                showFileDeleteAlert(holder.mSOSKU.getProductID()
                                        + "", fnameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra("quality", 40);
                                String path = HomeScreenFragment.photoPath + "/"
                                        + mImageName;
                                intent.putExtra("path", path);
                                startActivityForResult(intent,
                                        bmodel.CAMERA_REQUEST_CODE);
                                holder.btnPhoto.requestFocus();
                            }

                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    R.string.sdcard_is_not_ready_to_capture_img,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSOSKU = items.get(position);

            //typeface
            holder.tvBrandName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.etTotal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvActual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvPercentage.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvNorm.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvGap.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.tvBrandName.setText(holder.mSOSKU.getProductName());
            String strNorm = holder.mSOSKU.getNorm() + "";
            holder.tvNorm.setText(strNorm);
            String strParentTotal = holder.mSOSKU.getParentTotal() + "";
            holder.etTotal.setText(strParentTotal);
            String strActual = holder.mSOSKU.getActual() + "";
            holder.tvActual.setText(strActual);
            holder.tvTarget.setText(holder.mSOSKU.getTarget());
            holder.tvPercentage.setText(holder.mSOSKU.getPercentage());
            holder.tvGap.setText(holder.mSOSKU.getGap());

            if (Float.parseFloat(holder.mSOSKU.getGap()) < 0)
                holder.tvGap.setTextColor(Color.RED);
            else if (Float.parseFloat(holder.mSOSKU.getGap()) > 0)
                holder.tvGap.setTextColor(Color.rgb(34, 139, 34));
            else
                holder.tvGap.setTextColor(Color.BLACK);
            holder.spnReason.setSelection(getReasonIndex(holder.mSOSKU
                    .getReasonId() + ""));
            holder.spnReason.setSelected(true);


            if ((holder.mSOSKU.getImageName() != null)
                    && (!"".equals(holder.mSOSKU.getImageName()))
                    && (!"null".equals(holder.mSOSKU.getImageName()))) {
                Glide.with(getActivity())
                        .load(HomeScreenFragment.photoPath + "/" + holder.mSOSKU.getImgName())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_camera)
                        .transform(bmodel.circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera));
            }

            TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return row;
        }

        /**
         * Get the selected reason id, iterate and get position and set in the
         * spinner item
         *
         * @param reasonId
         * @return
         */
        private int getReasonIndex(String reasonId) {
            if (spinnerAdapter.getCount() == 0)
                return 0;
            int len = spinnerAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster s = spinnerAdapter.getItem(i);
                if (s.getReasonID().equals(reasonId))
                    return i;
            }
            return -1;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == bmodel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Sucessfully Captured.");
                if (bmodel.salesFundamentalHelper.mSelectedBrandID != 0) {
                    bmodel.salesFundamentalHelper.onsaveImageName(
                            bmodel.salesFundamentalHelper.mSelectedBrandID,
                            mImageName, HomeScreenTwo.MENU_SOSKU, 0);
                }
            } else {
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Canceled");
            }
        }
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
            bundle.putString("isFrom", "SOSKU");
            bundle.putString("filterHeader", bmodel.productHelper
                    .getChildLevelBo().get(0).getProductLevel());
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getRetailerModuleChildLevelBO());

            if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null
                    && bmodel.productHelper.getRetailerModuleParentLeveBO().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set Fragment class Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void FiveFilterFragment() {
        try {
            Collections.addAll(new Vector(), getResources().getStringArray(
                    R.array.productFilterArray));

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "SF");
            bundle.putBoolean("isAttributeFilter", true);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mDrawerLayout.closeDrawers();

        this.parentidList = mParentIdList;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mAttributeProducts = mAttributeProducts;
        this.filtertext = mFilterText;

        loadData(mParentIdList, mSelectedIdByLevelId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sales_fundamental, menu);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        // Change color if Filter is selected
        try {
            if (!brandFilterText.equals(BRAND))
                menu.findItem(R.id.menu_product_filter).setIcon(
                        R.drawable.ic_action_filter_select);

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            if (bmodel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }
            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            boolean navDrawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (drawerOpen || navDrawerOpen)
                menu.clear();

            menu.findItem(R.id.menu_next).setVisible(false);

            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOSKU))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            // return super.onPrepareOptionsMenu(menu);
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
        } else if (i == R.id.menu_next) {
            saveSOSKU();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(
                    HomeScreenTwo.MENU_SOSKU);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_SOSKU);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandFilterText = mFilterText;
            selectedfilterid = id;
            tvSelectedName.setText(mFilterText);
            ArrayList<SOSKUBO> items = bmodel.salesFundamentalHelper
                    .getmSOSKUList();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            myList = new ArrayList<>();
            for (SOSKUBO temp : items) {
                if (temp.getParentID() == id || id == -1 && temp.getIsOwn() == 1) {
                    myList.add(temp);
                }
            }

            // set the new list to listview
            MyAdapter mSchedule = new MyAdapter(myList);
            lvwplist.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void loadData(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId) {
        ArrayList<SOSKUBO> items = bmodel.salesFundamentalHelper.getmSOSKUList();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        myList = new ArrayList<>();
        for (LevelBO levelBO : parentidList) {
            for (SOSKUBO temp : items) {
                if (temp.getParentID() == levelBO.getProductID()) {
                    if (temp.getIsOwn() == 1)
                        myList.add(temp);
                }
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        MyAdapter mSchedule = new MyAdapter(myList);
        lvwplist.setAdapter(mSchedule);
    }

    private void saveSOSKU() {
        try {
            if (bmodel.salesFundamentalHelper
                    .hasData(HomeScreenTwo.MENU_SOSKU))
                new SaveAsyncTask().execute();
            else
                bmodel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.salesFundamentalHelper
                        .saveSalesFundamentalDetails(HomeScreenTwo.MENU_SOSKU);
                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_SOSKU);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder,  getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
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

    private void showFileDeleteAlert(final String bbid,
                                     final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + bmodel.mImageCount
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<SOSKUBO> items = bmodel.salesFundamentalHelper
                                .getmSOSKUList();
                        for (int i = 0; i < items.size(); i++) {
                            SOSKUBO sosku = items.get(i);
                            if (sosku.getProductID() == Integer.parseInt(bbid)) {
                                sosku.setImageName("");
                                sosku.setImgName("");
                            }
                        }
                        bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String path = HomeScreenFragment.photoPath + "/" + mImageName;
                        intent.putExtra("path", path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {

    }

    /**
     * Open Dialog with Competitor to Get Actual Values and Calculate Total
     * Value
     */
    private void getTotalValue(final int categoryId) {
        mSelectedET = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_salesfundamental_total);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentTotal = (EditText) dialog.findViewById(R.id.et_total);

        mParentTotal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) dialog.findViewById(R.id.tvTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((Button) dialog.findViewById(R.id.btn_cancel)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) dialog.findViewById(R.id.btn_done)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mCategoryForDialog.clear();
        // All Brands in Total PopUp
        if (bmodel.salesFundamentalHelper.getmSOSKUList() != null) {
            for (SOSKUBO soskuBO : bmodel.salesFundamentalHelper
                    .getmSOSKUList()) {
                if (soskuBO.getParentID() == categoryId) {
                    mCategoryForDialog.add(soskuBO);
                }
            }
        }

        ListView listView = (ListView) dialog.findViewById(R.id.lv);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = displayMetrics.heightPixels / 3;
        listView.setLayoutParams(params);
        listView.setAdapter(new TotalDialogAdapter());
        dialog.findViewById(R.id.calczero)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcone)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calctwo)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcthree)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcfour)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcfive)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcsix)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcseven)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calceight)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcnine)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdel)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdot)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdot).setVisibility(View.GONE);

        dialog.findViewById(R.id.btn_done)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mCategoryForDialog.isEmpty()) {
                            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                                SOSKUBO soskuBO = mCategoryForDialog.get(i);
                                soskuBO.setParentTotal(Integer
                                        .parseInt(mParentTotal.getText()
                                                .toString()));

                                if (soskuBO.getParentTotal() > 0) {

                                    long parentTotal = soskuBO.getParentTotal();
                                    float mNorm = soskuBO.getNorm();
                                    float actual = soskuBO.getActual();

                                    float target = (parentTotal * mNorm) / 100;
                                    float gap = target - actual;
                                    float percentage = 0;
                                    if (parentTotal > 0)
                                        percentage = (actual / parentTotal) * 100;


                                    soskuBO.setTarget(SDUtil.roundIt(target, 2));
                                    soskuBO.setPercentage(bmodel.formatPercent(percentage));
                                    soskuBO.setGap(SDUtil.roundIt(-gap, 2));
                                } else {
                                    soskuBO.setTarget(Integer.toString(0));
                                    soskuBO.setPercentage(Integer.toString(0));
                                    soskuBO.setGap(Integer.toString(0));
                                }

                            }
                        }
                        calculateTotalValues();
                        dialog.dismiss();
                        lvwplist.invalidateViews();
                        dialog = null;
                    }
                });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                lvwplist.invalidateViews();
                dialog = null;
            }
        });


        dialog.show();
    }

    /**
     * List of Products with Actual Edit Text
     */
    private class TotalDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCategoryForDialog.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CompetitorHolder holder;
            View row = convertView;
            if (row == null) {
                holder = new CompetitorHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());

                row = inflater.inflate(
                        R.layout.row_salesfundamental_total_list, parent, false);

                holder.tv = (TextView) row.findViewById(R.id.tv);
                holder.et = (EditText) row.findViewById(R.id.et);

                holder.et.setOnTouchListener(new OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = holder.et;
                        int inType = holder.et.getInputType();
                        holder.et.setInputType(InputType.TYPE_NULL);
                        holder.et.onTouchEvent(event);
                        holder.et.setInputType(inType);
                        return true;
                    }
                });
                holder.et.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (!"".equals(s)) {

                            try {
                                holder.soskuBO.setActual(SDUtil.convertToInt(s
                                        .toString()));
                            } catch (Exception e) {
                                holder.soskuBO.setActual(0);
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.soskuBO.setActual(0);
                        }
                        updateTotal();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                row.setTag(holder);
            } else {
                holder = (CompetitorHolder) row.getTag();
            }

            if (position == 0 && mSelectedET == null) {
                holder.et.requestFocus();
                mSelectedET = holder.et;
            }

            SOSKUBO brand = mCategoryForDialog.get(position);

            holder.soskuBO = brand;
            holder.tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.et.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tv.setText(brand.getProductName());
            String strActual = brand.getActual() + "";
            holder.et.setText(strActual);

            return row;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("SOSKU Tracking");
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    class CompetitorHolder {
        SOSKUBO soskuBO;
        TextView tv;
        EditText et;
    }

    /**
     * Add Actual Values and Update the Total
     */
    private void updateTotal() {
        int tot = 0;
        if (!mCategoryForDialog.isEmpty()) {
            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                SOSKUBO soskuBO = mCategoryForDialog.get(i);
                tot = tot + (soskuBO.getActual());
            }
            String strTotal = tot + "";
            mParentTotal.setText(strTotal);

        }
    }

    private void eff(int val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {
            String s = mSelectedET.getText().toString();

            if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s)) {

                mSelectedET.setText(String.valueOf(val));
            } else {
                String strVal = mSelectedET.getText()
                        + String.valueOf(val);
                mSelectedET.setText(strVal);
            }
        }
    }

    private class MyClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero) {
                eff(0);

            } else if (i == R.id.calcone) {
                eff(1);

            } else if (i == R.id.calctwo) {
                eff(2);

            } else if (i == R.id.calcthree) {
                eff(3);

            } else if (i == R.id.calcfour) {
                eff(4);

            } else if (i == R.id.calcfive) {
                eff(5);

            } else if (i == R.id.calcsix) {
                eff(6);

            } else if (i == R.id.calcseven) {
                eff(7);

            } else if (i == R.id.calceight) {
                eff(8);

            } else if (i == R.id.calcnine) {
                eff(9);

            } else if (i == R.id.calcdel) {
                String s = mSelectedET.getText().toString();

                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);

                    if (s.length() == 0) {
                        s = "0";
                    }
                }

                mSelectedET.setText(s);

            } else if (i == R.id.calcdot) {
                String s1 = mSelectedET.getText().toString();
                if (!".".contains(s1)) {
                    String strS1 = s1 + ".";
                    mSelectedET.setText(strS1);
                }

            }

        }
    }


    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        // TODO Auto-generated method stub

    }

//    private Bitmap getCircularBitmapFrom(Bitmap source) {
//        if (source == null || source.isRecycled()) {
//            return null;
//        }
//        float radius = source.getWidth() > source.getHeight() ? ((float) source
//                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
//        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
//                source.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
//                Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
//                radius, paint);
//
//        return bitmap;
//    }

}
