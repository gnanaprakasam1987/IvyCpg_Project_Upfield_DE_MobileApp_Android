package com.ivyretail.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.lib.Logs;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ShelfShareCallBackListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ShelfShareHelper;
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

public class SODFragment extends IvyBaseFragment implements
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
    private final List<SODBO> mCategoryForDialog = new ArrayList<>();
    private TextView tvSelectedName;
    //private TextView tvTotalNorm;
    // private TextView tvTotal;
    // private TextView tvTarget;
    // private TextView tvTotalActual;
    //  private TextView tvTotalGap;
    //  private TextView tvTotalPer;
    private SODDialogFragment dialogFragment = null;
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private boolean isFromChild;

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
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("SOD Tracking");
    }

    @Override
    public void onStart() {
        super.onStart();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        if (getView() != null) {
            lvwplist = (ListView) getView().findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);
        }

        FrameLayout drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        //setting Header Title Fonts
        ((TextView) getView().findViewById(R.id.levelName)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hlength)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hlengthacttar)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) getView().findViewById(R.id.hpercent)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.hpercentacttar)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) getView().findViewById(R.id.hGap)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvSelectedName = (TextView) getView().findViewById(R.id.levelName);
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
                final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(bmodel.mSelectedActivityName);
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(getResources().getString(R.string.filter));
                }
                getActivity().supportInvalidateOptionsMenu();
            }
        };

        //  tvSelectedName = (TextView) getView().findViewById(R.id.levelName);
        // tvTotalNorm = (TextView) getView().findViewById(R.id.tv_totalnorm);
        //  tvTotal = (TextView) getView().findViewById(R.id.tv_totalvalue);
        //  tvTarget = (TextView) getView().findViewById(R.id.tv_target);
        //  tvTotalPer = (TextView) getView()
        //  .findViewById(R.id.tv_actualpercentage);
        //  tvTotalActual = (TextView) getView().findViewById(R.id.tv_actual);
        // tvTotalGap = (TextView) getView().findViewById(R.id.tv_gap);
        // TextView audit = (TextView) getView().findViewById(R.id.audit);
        // TextView dummy = (TextView) getView().findViewById(R.id.dummy);
        Button btn_save = (Button) getView().findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSOS();
            }
        });

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
            // audit.setVisibility(View.VISIBLE);
            // dummy.setVisibility(View.VISIBLE);
        }
        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }


        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);

        updatebrandtext(BRAND, selectedfilterid);

        loadReasons();

        if (bmodel.salesFundamentalHelper.getmSODList() != null)
            calculateTotalValues();
    }

    /**
     * Add sum of values and show in bottom of the Screen
     */
    private void calculateTotalValues() {
        try {
            ArrayList<Integer> parentIds = new ArrayList<>();
            float mactual = 0;
            float mtotal = 0;
            float mtarget = 0;
            float mGap = 0;
            float mparcentagetot = 0;
            float mNamtot = 0;
            for (SODBO temp : bmodel.salesFundamentalHelper.getmSODList()) {
                if (temp.getIsOwn() == 1) {
                    if (!parentIds.contains(temp.getParentID())) {
                        mtotal = mtotal + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getParentTotal());
                        parentIds.add(temp.getParentID());
                    }
                    mtarget = mtarget + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getTarget());
                    mactual = mactual + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getActual());
                    mparcentagetot = mparcentagetot
                            + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getPercentage());
                    mGap = mGap + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getGap());
                    mNamtot = mNamtot + temp.getNorm();
                }

            }
            parentIds.clear();
            String strmNamot = mNamtot + "";
            //  tvTotalNorm.setText(strmNamot);
            String strmTotal = mtotal + "";
            //   tvTotal.setText(strmTotal);
            String strmTarget = mtarget + "";
            //   tvTarget.setText(strmTarget);
            String strmActual = mactual + "";
            //  tvTotalActual.setText(strmActual);
            String strmPercentagetot = mparcentagetot + "";
            //    tvTotalPer.setText(strmPercentagetot);
            String strmGap = mGap + "";
            //  tvTotalGap.setText(strmGap);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

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
            if ("SOD".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }

        if (!(spinnerAdapter.getCount() > 0)) {
            ReasonMaster reasonMasterBo = new ReasonMaster();
            reasonMasterBo.setReasonDesc(getActivity().getResources().getString(R.string.select_reason));
            reasonMasterBo.setReasonID("0");
            spinnerAdapter.add(reasonMasterBo);
        }
    }

    class ViewHolder {
        SODBO mSOD;
        TextView tvBrandName;
        TextView tvNorm;
        TextView tvTarget;
        TextView tvActual;
        TextView tvPercentage;
        TextView tvGap;
        EditText etTotal;
        Spinner spnReason;
        ImageView btnPhoto;
        ImageButton audit;
    }

    private class MyAdapter extends ArrayAdapter<SODBO> {
        private final ArrayList<SODBO> items;

        public MyAdapter(ArrayList<SODBO> mylist) {
            super(getActivity(), R.layout.row_sos, mylist);
            this.items = mylist;
        }

        public SODBO getItem(int position) {
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
                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 2) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex).setAudit(1);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 1) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex).setAudit(0);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 0) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex).setAudit(2);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                holder.etTotal.setFocusable(false);

                holder.etTotal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.salesFundamentalHelper.mSOSTotalPopUpType == 0) {
                            if (dialog != null) {
                                if (!dialog.isShowing()) {
                                    dialog.cancel();
                                    dialog = null;

                                }
                            }
                            // Open dialog
                            if (dialog == null) {
                                mSelectedHolder = (ViewHolder) v.getTag();
                                getTotalValue(mSelectedHolder.mSOD.getParentID());
                            }
                        } else if (dialogFragment == null) {
                            mSelectedHolder = (ViewHolder) v.getTag();
                            Bundle bundle = new Bundle();

                            bundle.putInt("parent_id",
                                    mSelectedHolder.mSOD.getParentID());

                            bundle.putInt("parent_type_id",
                                    0);
                            bundle.putInt("product_id",
                                    mSelectedHolder.mSOD.getProductID());
                            bundle.putInt("flag", ShelfShareHelper.SOD);
                            bundle.putInt("selectedlocation", mSelectedLocationIndex);
                            dialogFragment = new SODDialogFragment();
                            dialogFragment.setArguments(bundle);
                            dialogFragment
                                    .setStyle(
                                            DialogFragment.STYLE_NO_TITLE,
                                            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                            dialogFragment.setCancelable(false);
                            dialogFragment
                                    .setOnShelfShareListener(new ShelfShareCallBackListener() {
                                                                 @Override
                                                                 public void SOSBOCallBackListener(List<SOSBO> sosBOList) {

                                                                 }

                                                                 @Override
                                                                 public void SODDOCallBackListener(List<SODBO> sosBOList) {
                                                                     Logs.debug("SOSFragment",
                                                                             "SOSBOCallBackListener");
                                                                     mCategoryForDialog.clear();
                                                                     mCategoryForDialog
                                                                             .addAll(sosBOList);
                                                                     dialogFragment.dismiss();
                                                                     dialogFragment = null;
                                                                     calculateTotalValues();
                                                                     lvwplist.invalidateViews();
                                                                 }

                                                                 @Override
                                                                 public void handleDialogClose() {
                                                                     dialogFragment.dismiss();
                                                                     dialogFragment = null;
                                                                 }
                                                             }
                                    );
                            dialogFragment.show(getChildFragmentManager(),
                                    "Shelf Share");
                        }

                    }

                });

                holder.spnReason.setAdapter(spinnerAdapter);
                holder.spnReason
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.spnReason
                                        .getSelectedItem();

                                holder.mSOD.getLocations().get(mSelectedLocationIndex).setReasonId(SDUtil
                                        .convertToInt(reString.getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bmodel.isExternalStorageAvailable()) {
                            mImageName = "SOD_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            bmodel.salesFundamentalHelper.mSelectedBrandID = holder.mSOD
                                    .getProductID();
                            String fnameStarts = "SOD_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE);

                            boolean nfiles_there = bmodel
                                    .checkForNFilesInFolder(
                                            HomeScreenFragment.photoPath,
                                            bmodel.mImageCount, fnameStarts);
                            if (nfiles_there) {

                                showFileDeleteAlert(fnameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra("quality", 40);
                                String _path = HomeScreenFragment.photoPath + "/"
                                        + mImageName;
                                intent.putExtra("path", _path);
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
                if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                    holder.audit.setVisibility(View.VISIBLE);

                    holder.spnReason.setEnabled(false);
                    holder.spnReason.setClickable(false);
                    holder.btnPhoto.setEnabled(false);
                    holder.btnPhoto.setClickable(false);
                    holder.etTotal.setEnabled(false);
                    holder.etTotal.setEnabled(false);
                }


                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSOD = items.get(position);
            //typeface
            holder.tvBrandName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.etTotal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.tvActual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.tvTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvPercentage.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.tvNorm.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvGap.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 2)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 1)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit() == 0)
                holder.audit.setImageResource(R.drawable.ic_audit_no);
            holder.tvBrandName.setText(holder.mSOD.getProductName());
            String strNorm = holder.mSOD.getNorm() + "";
            holder.tvNorm.setText(strNorm);
            holder.etTotal.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getParentTotal());
            holder.tvActual.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getActual());
            holder.tvTarget.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getTarget());
            holder.tvPercentage.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getPercentage());
            holder.tvGap.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getGap());

            if (Float.parseFloat(holder.mSOD.getLocations().get(mSelectedLocationIndex).getGap()) < 0)
                holder.tvGap.setTextColor(Color.RED);
            else if (Float.parseFloat(holder.mSOD.getGap()) > 0)
                holder.tvGap.setTextColor(Color.rgb(34, 139, 34));
            else
                holder.tvGap.setTextColor(Color.BLACK);
            holder.spnReason.setSelection(getReasonIndex(holder.mSOD
                    .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
            holder.spnReason.setSelected(true);

            if ((holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName() != null)
                    && (!"".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))
                    && (!"null".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))) {
                Glide.with(getActivity())
                        .load(HomeScreenFragment.photoPath + "/" + holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName())
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
                            mImageName, HomeScreenTwo.MENU_SOD, mSelectedLocationIndex);
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
            bundle.putString("isFrom", "SOD");
            bundle.putString("filterHeader", bmodel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
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

            // set Fragmentclass Arguments
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
            Collections.addAll(new Vector<String>(), getResources().getStringArray(
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
            bundle.putBoolean("isAttributeFilter", false);
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
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        mDrawerLayout.closeDrawers();
        loadData(parentidList, mSelectedIdByLevelId);
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

            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER&&bmodel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOD))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            boolean navDrawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (drawerOpen || navDrawerOpen)
                menu.clear();
            // return super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.menu_next).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
            }
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
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                getActivity().finish();
            }
            return true;
        } else if (i == R.id.menu_next) {
            saveSOS();
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
                    HomeScreenTwo.MENU_SOD);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_SOD);
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandFilterText = filtertext;
            selectedfilterid = id;
            tvSelectedName.setText(filtertext);
            ArrayList<SODBO> items = bmodel.salesFundamentalHelper
                    .getmSODList();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            ArrayList<SODBO> myList = new ArrayList<>();
            for (SODBO temp : items) {
                if (temp.getParentID() == id || id == -1 && temp.getIsOwn() == 1) {
                    myList.add(temp);
                }
            }

            // set the new list to list view
            MyAdapter mSchedule = new MyAdapter(myList);
            lvwplist.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void loadData(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId) {
        ArrayList<SODBO> items = bmodel.salesFundamentalHelper.getmSODList();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        ArrayList<SODBO> myList = new ArrayList<>();
        for (LevelBO levelBO : parentidList) {
            for (SODBO temp : items) {
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

    private void saveSOS() {
        try {
            if (bmodel.salesFundamentalHelper
                    .hasData(HomeScreenTwo.MENU_SOD)) {
                new SaveAsyncTask().execute();
            } else {
                bmodel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);
            }

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
                        .saveSalesFundamentalDetails(HomeScreenTwo.MENU_SOD);
                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_SOD);
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

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
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

    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + bmodel.mImageCount
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
    public void updategeneraltext(String filtertext) {

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
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_salesfundamental_total);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentTotal = (EditText) dialog.findViewById(R.id.et_total);

        // setting no of charcters from congifuration
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(bmodel.configurationMasterHelper.sodDigits);
        mParentTotal.setFilters(FilterArray);

        mParentTotal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) dialog.findViewById(R.id.tvTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((Button) dialog.findViewById(R.id.btn_cancel)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) dialog.findViewById(R.id.btn_done)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mCategoryForDialog.clear();
        // All Brands in Total PopUp
        if (bmodel.salesFundamentalHelper.getmSODList() != null) {
            for (SODBO sodBO : bmodel.salesFundamentalHelper.getmSODList()) {
                if (sodBO.getParentID() == categoryId) {
                    mCategoryForDialog.add(sodBO);
                }
            }
        }

        ListView listView = (ListView) dialog.findViewById(R.id.lv);
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

                                SODBO sodbo = mCategoryForDialog.get(i);
                                sodbo.getLocations().get(mSelectedLocationIndex).setParentTotal(SDUtil
                                        .convertToFloat(mParentTotal.getText()
                                                .toString()) + "");

                                sodbo.setGap(Integer.toString(0));
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
                // setting no of charcters from congifuration
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(bmodel.configurationMasterHelper.sodDigits);
                holder.et.setFilters(FilterArray);

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
                                holder.sodBO.setActual(SDUtil.convertToInt(s
                                        .toString()));
                            } catch (Exception e) {
                                holder.sodBO.setActual(0);
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.sodBO.setActual(0);
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

            SODBO brand = mCategoryForDialog.get(position);

            holder.sodBO = brand;
            holder.tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.et.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            holder.tv.setText(brand.getProductName());

            String strActual = brand.getActual() + "";
            holder.et.setText(strActual);

            return row;
        }

    }

    class CompetitorHolder {
        SODBO sodBO;
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

                SODBO sodbo = mCategoryForDialog.get(i);
                tot = tot + (sodbo.getActual());
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
                String strVal = mSelectedET.getText() + String.valueOf(val);
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
                    mSelectedET.setText(s);
                }

            } else if (i == R.id.calcdot) {
                String s1 = mSelectedET.getText().toString();
                if (!s1.contains(".")) {
                    String strS1 = s1 + ".";
                    mSelectedET.setText(strS1);
                }

            }

        }
    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        // TODO Auto-generated method stub

    }

    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        dialog.dismiss();
                        updatebrandtext(BRAND, selectedfilterid);
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
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
