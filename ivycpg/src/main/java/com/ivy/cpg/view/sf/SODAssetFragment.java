package com.ivy.cpg.view.sf;

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
import android.support.annotation.NonNull;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class SODAssetFragment extends IvyBaseFragment implements
        BrandDialogInterface,FiveLevelFilterCallBack {

    private BusinessModel mBModel;
    private DrawerLayout mDrawerLayout;
    private Dialog dialog = null;
    private ViewHolder mSelectedHolder;
    private EditText mSelectedET;
    private EditText mParentTotal;
    private TextView tvSelectedName;
    private ListView mListView;

    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private ArrayAdapter<ReasonMaster> assetReasonAdapter;
    private ArrayAdapter<ReasonMaster> assetLocationAdapter;
    private final ArrayList<AssetTrackingBO> mAssetsForDialog = new ArrayList<>();
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    private static final String BRAND = "Brand";
    private static final String TAG="SOD Tracking";
    private String brandFilterText = "BRAND";
    private String mImageName;
    private int mSelectedFilterId = -1;
    private int mSelectedLocationIndex;
    private boolean isFromChild;
    AssetTrackingHelper assetTrackingHelper;
    SODAssetHelper mSODAssetHelper;
    SalesFundamentalHelper mSFHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sos, container, false);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());
        mSODAssetHelper = SODAssetHelper.getInstance(getActivity());
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());

        initializeViews(view);

        return view;
    }

    /**
     * Initialize views
     *
     * @param view Parent view
     */
    private void initializeViews(View view) {
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        if (getView() != null) {
            mListView = (ListView) view.findViewById(R.id.list);
            mListView.setCacheColorHint(0);
        }

        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        //setting Header Title Fonts
        ((TextView) view.findViewById(R.id.levelName)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hTotal)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hlength)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hlengthacttar)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.hpercent)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hpercentacttar)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.hGap)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvSelectedName = (TextView) view.findViewById(R.id.levelName);
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
            setScreenTitle(mSODAssetHelper.mSelectedActivityName);
        }


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mSODAssetHelper.mSelectedActivityName);
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

        Button btn_save = (Button) view.findViewById(R.id.btn_save);
        btn_save.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSOS();
            }
        });

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mBModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
        }


        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());
        mSODAssetHelper = SODAssetHelper.getInstance(getActivity());
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());

        if (mBModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        updateBrandText(BRAND, mSelectedFilterId);

        if (mSODAssetHelper.getSODList() != null)
            calculateTotalValues();
    }

    /**
     * Add sum of values and show in bottom of the Screen
     */
    private void calculateTotalValues() {
        try {
            ArrayList<Integer> parentIds = new ArrayList<>();
            float mActual = 0;
            float mTotal = 0;
            float mTarget = 0;
            float mGap = 0;
            float mPercentageTotal = 0;
            float mNameTotal = 0;
            for (SODBO temp : mSODAssetHelper.getSODList()) {
                if (temp.getIsOwn() == 1) {
                    if (!parentIds.contains(temp.getParentID())) {
                        mTotal = mTotal + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getParentTotal());
                        parentIds.add(temp.getParentID());
                    }
                    mTarget = mTarget + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getTarget());
                    mActual = mActual + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getActual());
                    mPercentageTotal = mPercentageTotal
                            + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getPercentage());
                    mGap = mGap + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getGap());
                    mNameTotal = mNameTotal + temp.getNorm();
                }

            }
            parentIds.clear();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Initialize Adapter and add reason for SOD module Reason Category : SOD
     */
    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
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

        ReasonMaster reasonBO;
        reasonBO = new ReasonMaster();
        reasonBO.setReasonID("0");
        reasonBO.setReasonDesc(getActivity().getResources().getString(R.string.select_reason));
        reasonBO.setReasonCategory("NONE");

        ReasonMaster reasonBO1;
        reasonBO1 = new ReasonMaster();
        reasonBO1.setReasonID("0");
        reasonBO1.setReasonDesc(getActivity().getResources().getString(R.string.select_location));
        reasonBO1.setReasonCategory("NONE");

        assetReasonAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        assetReasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        assetLocationAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        assetLocationAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        assetReasonAdapter.add(reasonBO);
        assetLocationAdapter.add(reasonBO1);

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if ("SOD_ASSET_REASON".equalsIgnoreCase(temp.getReasonCategory()))
                assetReasonAdapter.add(temp);
        }

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if ("SOD_ASSET_LOCATION".equalsIgnoreCase(temp.getReasonCategory()))
                assetLocationAdapter.add(temp);
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

    /**
     * Adapter for list view
     */
    private class MyAdapter extends ArrayAdapter<SODBO> {
        private final ArrayList<SODBO> items;

        public MyAdapter(ArrayList<SODBO> mList) {
            super(getActivity(), R.layout.row_sos, mList);
            this.items = mList;
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

        @NonNull
        public View getView(int position, View convertView,@NonNull ViewGroup parent) {
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

                holder.etTotal.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (dialog != null) {
                            if (!dialog.isShowing()) {
                                dialog.cancel();
                                dialog = null;

                            }
                        }
                        // Open dialog
                        if (dialog == null) {
                            mSelectedHolder = (ViewHolder) v.getTag();
                            getTotalValue(mSelectedHolder.mSOD.getProductID());
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
                        if (mBModel.isExternalStorageAvailable()) {
                            mImageName = "SOD_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            mSODAssetHelper.mSelectedBrandID = holder.mSOD
                                    .getProductID();
                            String mFileNameStarts = "SOD_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE);

                            boolean mIsFileAvailable = mBModel
                                    .checkForNFilesInFolder(
                                            FileUtils.photoFolderPath,
                                            1, mFileNameStarts);
                            if (mIsFileAvailable) {

                                showFileDeleteAlert(mFileNameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                String _path = FileUtils.photoFolderPath + "/"
                                        + mImageName;
                                intent.putExtra(CameraActivity.PATH, _path);
                                startActivityForResult(intent,
                                        mBModel.CAMERA_REQUEST_CODE);
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
                if (mBModel.configurationMasterHelper.IS_TEAMLEAD) {
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
            holder.tvBrandName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.etTotal.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvActual.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvTarget.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvPercentage.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvNorm.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvGap.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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

            if (SDUtil.convertToFloat(holder.mSOD.getLocations().get(mSelectedLocationIndex).getGap()) < 0)
                holder.tvGap.setTextColor(Color.RED);
            else if (SDUtil.convertToFloat(holder.mSOD.getGap()) > 0)
                holder.tvGap.setTextColor(Color.rgb(34, 139, 34));
            else
                holder.tvGap.setTextColor(Color.BLACK);
            holder.spnReason.setSelection(getReasonIndex(holder.mSOD
                    .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
            holder.spnReason.setSelected(true);

            if ((holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName() != null)
                    && (!"".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))
                    && (!"null".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))) {
                Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera);
                Glide.with(getActivity()).load(FileUtils.photoFolderPath + "/" + holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()).asBitmap().centerCrop().placeholder(new BitmapDrawable(getResources(), defaultIcon)).into(new BitmapImageViewTarget(holder.btnPhoto) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        holder.btnPhoto.setImageDrawable(new BitmapDrawable(getResources(), getCircularBitmapFrom(resource)));
                    }
                });

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera));
            }
            TypedArray mTypedArray = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(mTypedArray.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(mTypedArray.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return row;
        }

        /**
         * Get the selected reason id, iterate and get position and set in the
         * spinner item
         *
         * @param reasonId reason Id
         * @return return Index
         */
        private int getReasonIndex(String reasonId) {
            if (spinnerAdapter.getCount() == 0)
                return 0;
            int len = spinnerAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster mReasonBO = spinnerAdapter.getItem(i);
                if(mReasonBO!=null) {
                    if (mReasonBO.getReasonID().equals(reasonId))
                        return i;
                }
            }
            return -1;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == mBModel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(mSODAssetHelper.mSelectedActivityName
                        + "Camera Activity : Successfully Captured.");
                if (mSODAssetHelper.mSelectedBrandID != 0) {
                    mSODAssetHelper.onSaveImageName(
                            mSODAssetHelper.mSelectedBrandID,
                            mImageName, HomeScreenTwo.MENU_SOD_ASSET, mSelectedLocationIndex);
                }
            } else {
                Commons.print(mSODAssetHelper.mSelectedActivityName
                        + "Camera Activity : Canceled");
            }
        }
    }

    /**
     * Five level filter
     */
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
                    mBModel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "SF");
            bundle.putBoolean("isAttributeFilter", false);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);
            ft.replace(R.id.right_drawer, mFragment, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mDrawerLayout.closeDrawers();

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        updateFiveFilterSelection(mFilteredPid, mSelectedIdByLevelId);
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
            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            if (mBModel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            if (mBModel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOD_ASSET))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (drawerOpen)
                menu.clear();
            // return super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.menu_next).setVisible(false);

            if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (mBModel.productHelper.getInStoreLocation().size() < 2)
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
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
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
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(
                    HomeScreenTwo.MENU_SOD_ASSET);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_SOD_ASSET);
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
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
            mSelectedFilterId = id;
            tvSelectedName.setText(mFilterText);
            ArrayList<SODBO> items = mSODAssetHelper
                    .getSODList();
            if (items == null) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            ArrayList<SODBO> myList = new ArrayList<>();
            for (SODBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentID() == id || id == -1 && temp.getIsOwn() == 1) {
                    myList.add(temp);
                }
            }

            // set the new list to list view
            MyAdapter mSchedule = new MyAdapter(myList);
            mListView.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * update list based on filter selection
     *
     * @param mFilteredPid        FilteredPid Product Id
     * @param mSelectedIdByLevelId Selected product Id's by level ID
     */
    private void updateFiveFilterSelection(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId) {
        ArrayList<SODBO> items = mSODAssetHelper.getSODList();
        if (items == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        ArrayList<SODBO> myList = new ArrayList<>();
            for (SODBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentHierarchy().contains("/"+mFilteredPid+"/")) {
                    if (temp.getIsOwn() == 1)
                        myList.add(temp);
                }
            }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        MyAdapter mSchedule = new MyAdapter(myList);
        mListView.setAdapter(mSchedule);
    }

    /**
     * Save transaction
     */
    private void saveSOS() {
        try {
            if (mSODAssetHelper
                    .hasData(HomeScreenTwo.MENU_SOD_ASSET)) {
                new SaveAsyncTask().execute();
            } else {
                mBModel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Save transaction in background
     */
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mSODAssetHelper
                        .saveSalesFundamentalDetails(HomeScreenTwo.MENU_SOD_ASSET, mAssetsForDialog);
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SOD_ASSET, true);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
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
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {

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

    /**
     * Showing alert dialog to denote image availability..
     * @param imageNameStarts
     */
    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + 1
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                mBModel.CAMERA_REQUEST_CODE);

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
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
        if(dialog.getWindow()!=null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        mParentTotal = (EditText) dialog.findViewById(R.id.et_total);

        // setting no of characters from configuration
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sodDigits);
        mParentTotal.setFilters(FilterArray);

        mParentTotal.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) dialog.findViewById(R.id.tvTotal)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((Button) dialog.findViewById(R.id.btn_cancel)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) dialog.findViewById(R.id.btn_done)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mAssetsForDialog.clear();
        // All Brands in Total PopUp
        if (assetTrackingHelper.getAssetTrackingList() != null) {
            for (AssetTrackingBO assetTrackingBO : assetTrackingHelper.getAssetTrackingList()) {
                if (assetTrackingBO.getProductId() == categoryId) {
                    mAssetsForDialog.add(assetTrackingBO);
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
                        if (!mAssetsForDialog.isEmpty()) {

                            for (SODBO sodbo : mSODAssetHelper.getSODList()) {
                                if (sodbo.getProductID() == categoryId) {
                                    sodbo.getLocations().get(mSelectedLocationIndex).setParentTotal(SDUtil
                                            .convertToFloat(mParentTotal.getText()
                                                    .toString()) + "");

                                    sodbo.setGap(Integer.toString(0));
                                }
                            }
                        }
                        calculateTotalValues();
                        dialog.dismiss();
                        mListView.invalidateViews();
                        dialog = null;
                    }
                });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListView.invalidateViews();
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
            return mAssetsForDialog.size();
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
                        R.layout.row_sod_asset_list, parent, false);

                holder.assertName = (TextView) row.findViewById(R.id.assetName);
                holder.target = (TextView) row.findViewById(R.id.tgt);
                holder.date = (TextView) row.findViewById(R.id.doa);
                holder.actual = (EditText) row.findViewById(R.id.et);
                holder.reason = (Spinner) row.findViewById(R.id.spnreason);
                holder.location = (Spinner) row.findViewById(R.id.spnlocation);
                holder.promo = (CheckBox) row.findViewById(R.id.cbPromo);
                holder.full = (CheckBox) row.findViewById(R.id.cbDisplay);
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                // setting no of characters from configuration
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sodDigits);
                holder.actual.setFilters(FilterArray);

                holder.actual.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = holder.actual;
                        int inType = holder.actual.getInputType();
                        holder.actual.setInputType(InputType.TYPE_NULL);
                        holder.actual.onTouchEvent(event);
                        holder.actual.setInputType(inType);
                        if (holder.actual.getText().length() > 0)
                            holder.actual.setSelection(holder.actual.getText().length());
                        return true;
                    }


                });
                holder.actual.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                holder.actual.setSelection(s.toString().length());
                            try {
                                holder.assetTrackingBO.setActual(SDUtil.convertToInt(s
                                        .toString()));
                            } catch (Exception e) {
                                holder.assetTrackingBO.setActual(0);
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.assetTrackingBO.setActual(0);
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

                holder.promo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            holder.assetTrackingBO.setIsPromo("Y");
                        else
                            holder.assetTrackingBO.setIsPromo("N");

                    }
                });

                holder.full.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            holder.assetTrackingBO.setIsDisplay("Y");
                        else
                            holder.assetTrackingBO.setIsDisplay("N");

                    }
                });
                holder.reason.setAdapter(assetReasonAdapter);
                holder.reason
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.reason
                                        .getSelectedItem();

                                holder.assetTrackingBO.setReasonID(SDUtil.convertToInt(reString.getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.location.setAdapter(assetLocationAdapter);
                holder.location
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.location
                                        .getSelectedItem();

                                holder.assetTrackingBO.setLocationID(SDUtil.convertToInt(reString.getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                row.setTag(holder);
            } else {
                holder = (CompetitorHolder) row.getTag();
            }

            if (position == 0 && mSelectedET == null) {
                holder.actual.requestFocus();
                mSelectedET = holder.actual;
            }

            AssetTrackingBO brand = mAssetsForDialog.get(position);

            holder.assetTrackingBO = brand;
            holder.assertName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.target.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.date.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.actual.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) row.findViewById(R.id.tvactual)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) row.findViewById(R.id.tvreason)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) row.findViewById(R.id.tvpromo)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) row.findViewById(R.id.tvdisplay)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) row.findViewById(R.id.tvlocation)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.assertName.setText(brand.getAssetName());

            if (brand.getIsPromo().equalsIgnoreCase("Y"))
                holder.promo.setChecked(true);
            else
                holder.promo.setChecked(false);

            if (brand.getIsDisplay().equalsIgnoreCase("Y"))
                holder.full.setChecked(true);
            else
                holder.full.setChecked(false);

            String strActual = brand.getActual() + "";
            holder.actual.setText(strActual);

            holder.reason.setSelection(getReasonIndex(holder.assetTrackingBO.getReasonID() + ""));
            holder.reason.setSelected(true);

            holder.location.setSelection(getLocationIndex(holder.assetTrackingBO.getLocationID() + ""));
            holder.location.setSelected(true);

            holder.target.setText("Tgt : " + holder.assetTrackingBO.getTarget());
            holder.date.setText("DOA : " + holder.assetTrackingBO.getInstallDate() == null ? "" : holder.assetTrackingBO.getInstallDate());


            return row;
        }

        /**
         * Get index of given reason ID
         * @param reasonId Reason Id
         * @return Index
         */
        private int getReasonIndex(String reasonId) {
            if (assetReasonAdapter.getCount() == 0)
                return 0;
            int len = assetReasonAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster s = assetReasonAdapter.getItem(i);
                if(s!=null) {
                    if (s.getReasonID().equals(reasonId))
                        return i;
                }
            }
            return -1;
        }

        /**
         * Get index of given location ID
         *
         * @param mLocationId Location Id
         * @return Index
         */
        private int getLocationIndex(String mLocationId) {
            if (assetLocationAdapter.getCount() == 0)
                return 0;
            int len = assetLocationAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster s = assetLocationAdapter.getItem(i);
                if (s != null) {
                    if (s.getReasonID().equals(mLocationId))
                        return i;
                }
            }
            return -1;
        }

    }

    class CompetitorHolder {
        AssetTrackingBO assetTrackingBO;
        TextView assertName;
        TextView target;
        TextView date;
        EditText actual;
        Spinner reason;
        Spinner location;
        CheckBox promo;
        CheckBox full;
    }

    /**
     * Add Actual Values and Update the Total
     */
    private void updateTotal() {
        int tot = 0;
        if (!mAssetsForDialog.isEmpty()) {
            for (int i = 0; i < mAssetsForDialog.size(); i++) {

                AssetTrackingBO mAssetTrackingBO = mAssetsForDialog.get(i);
                tot = tot + (mAssetTrackingBO.getActual());
            }
            String strTotal = tot + "";
            mParentTotal.setText(strTotal);

        }
    }


    /**
     * NumberPad click listener
     */
    private class MyClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero) {
                updateValue(0);

            } else if (i == R.id.calcone) {
                updateValue(1);

            } else if (i == R.id.calctwo) {
                updateValue(2);

            } else if (i == R.id.calcthree) {
                updateValue(3);

            } else if (i == R.id.calcfour) {
                updateValue(4);

            } else if (i == R.id.calcfive) {
                updateValue(5);

            } else if (i == R.id.calcsix) {
                updateValue(6);

            } else if (i == R.id.calcseven) {
                updateValue(7);

            } else if (i == R.id.calceight) {
                updateValue(8);

            } else if (i == R.id.calcnine) {
                updateValue(9);

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

    /**
     * update values in view
     *
     * @param val value
     */
    private void updateValue(int val) {
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

    /**
     * Show location dialog
     */
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
                        updateBrandText(BRAND, mSelectedFilterId);
                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }

    /**
     * Draw circular image
     * @param source Source image
     * @return
     */
    private Bitmap getCircularBitmapFrom(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return null;
        }
        float radius = source.getWidth() > source.getHeight() ? ((float) source
                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
                source.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
                radius, paint);

        return bitmap;
    }

}
