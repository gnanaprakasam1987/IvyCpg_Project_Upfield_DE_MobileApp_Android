package com.ivyretail.views;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.PromotionBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.ivy.sd.png.asean.view.R.id.tvannouncer;

public class PromotionTrackingFragment extends IvyBaseFragment implements
        BrandDialogInterface {

    private static final String MENU_CODE = "MENU_PROMO";
    private BusinessModel bmodel;
    // Drawer Implementation
    private DrawerLayout mDrawerLayout;
    private static final String BRAND = "Brand";
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private int mselectedfilterid = -1;
    private ArrayList<PromotionBO> promoList;
    private ListView lvwplist;
    private String brandFilterText = "BRAND";
    private final int mImageCount = 1;
    private String mImageName;
    private String promoName;
    private String mImagePath;
    private TextView tvdesc;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private ArrayAdapter<StandardListBO> mRatingAdapter;
    private MyAdapter mSchedule;

    // PROMO ADD DIALOG
    private Dialog dialog = null;
    private Spinner mPromo;
    private Spinner mProduct;
    private final ArrayList<Integer> vExcludedPromo = new ArrayList<>();
    private int promo;
    private int productID;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private CardView card_keyboard;


    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;

    private StandardListBO mSelectedStandardListBO;

    private static final String SELECT = "-Select-";
    private InputMethodManager inputManager;
    private EditText QUANTITY;
    private String append = "";
    private boolean isFromChild;
    private Vector<LevelBO> parentidList;
    private ArrayList<Integer> mAttributeProducts;
    private String filtertext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotion, container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Option to enable action bar in fragment
        setHasOptionsMenu(true);
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
        // Initialize the UI components
        viewInitialization();

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);
        getActionBar().setElevation(0);
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
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

        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        loadReason();
        bmodel.promotionHelper.downloadPromotionRating();
        ArrayList<StandardListBO> ratingList = bmodel.promotionHelper.getmRatingList();
        if (ratingList != null) {
            mRatingAdapter = new ArrayAdapter<StandardListBO>(getActivity(),
                    R.layout.spinner_bluetext_layout);
            mRatingAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        }

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }
        if (mLocationAdapter.getCount() > 0) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }

        if (parentidList != null || mSelectedIdByLevelId != null || mAttributeProducts != null) {
            updatefromFiveLevelFilter(parentidList, mSelectedIdByLevelId, mAttributeProducts, filtertext);
        } else {
            updatebrandtext(BRAND, mselectedfilterid);
        }
        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            FiveFilterFragment();
        else
            productFilterClickedFragment();


        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void viewInitialization() {
        if (getView() != null) {
            lvwplist = (ListView) getView().findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);
        }

        tvdesc = (TextView) getView().findViewById(R.id.tvdesc);
        tvdesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvExecuted = (TextView) getView().findViewById(R.id.tvexecuted);
        tvExecuted.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvpromoqty = (TextView) getView().findViewById(R.id.tvpromoqty);
        tvpromoqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tvreason = (TextView) getView().findViewById(R.id.tvreason);
        tvreason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView tv_executing_rating = (TextView) getView().findViewById(R.id.tv_executing_rating);
        tv_executing_rating.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        card_keyboard = (CardView) getView().findViewById(R.id.card_keyboard);

        if (!bmodel.promotionHelper.SHOW_PROMO_PHOTO) {
            getView().findViewById(R.id.tvphoto).setVisibility(View.GONE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                        R.id.tvphoto).getTag()) != null) {
                    ((TextView) getView().findViewById(R.id.tvphoto))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.tvphoto).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!bmodel.promotionHelper.SHOW_PROMO_REASON) {
            getView().findViewById(R.id.tvreason).setVisibility(View.GONE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                        R.id.tvreason).getTag()) != null) {
                    ((TextView) getView().findViewById(R.id.tvreason))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.tvreason).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!bmodel.promotionHelper.SHOW_PROMO_RATING) {
            getView().findViewById(R.id.tv_executing_rating).setVisibility(View.GONE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                        R.id.tv_executing_rating).getTag()) != null) {
                    ((TextView) getView().findViewById(R.id.tv_executing_rating))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.tv_executing_rating).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
     /*   if (!bmodel.promotionHelper.SHOW_PROMO_TYPE) {
            getView().findViewById(R.id.tv_group_name).setVisibility(View.GONE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                        R.id.tv_group_name).getTag()) != null) {
                    ((TextView) getView().findViewById(R.id.tv_group_name))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.tv_group_name).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }*/
        if (!bmodel.promotionHelper.SHOW_PROMO_QTY) {
            getView().findViewById(R.id.tvpromoqty).setVisibility(View.GONE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                        R.id.tvpromoqty).getTag()) != null) {
                    ((TextView) getView().findViewById(R.id.tvpromoqty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(getView().findViewById(
                                            R.id.tvpromoqty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if(bmodel.promotionHelper.SHOW_PROMO_ANNOUNCER){
            getView().findViewById(tvannouncer).setVisibility(View.VISIBLE);
        }else{
            getView().findViewById(tvannouncer).setVisibility(View.GONE);
        }
        Button btn_save = (Button) getView().findViewById(R.id.btn_save);
        btn_save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btn_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                savePromotionData();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == bmodel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Sucessfully Captured.");
                if (bmodel.promotionHelper.mSelectedPromoID != 0) {
                    bmodel.promotionHelper
                            .onsaveImageName(mSelectedStandardListBO.getListID(),
                                    bmodel.promotionHelper.mSelectedPromoID,
                                    mImageName, mImagePath);
                }
            } else {
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Canceled");
            }
        }
    }

    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId
     * @return position of the reason id
     */
    private int getReasonIndex(String reasonId) {

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

    private int getRatingIndex(String id) {
        if (mRatingAdapter != null) {
            int len = mRatingAdapter.getCount();
            if (len == 0) {
                return 0;
            }
            for (int i = 0; i < len; i++) {
                StandardListBO standardListBO = mRatingAdapter.getItem(i);
                if (standardListBO.getListID().equals(id)) {
                    return i;
                }
            }
        }
        return 0;

    }

    class ViewHolder {
        PromotionBO mPromotionMasterBO;
        TextView tvpromoName;
        EditText etDisplay;
        EditText etPromoQty;
        CheckBox rbExecuted;
        CheckBox rbAnnounced;
        ImageView btnPhoto;
        Spinner reasonSpin;
        Spinner ratingSpin;
        TextView tvgroupName;
    }

    private class MyAdapter extends ArrayAdapter<PromotionBO> {
        private final ArrayList<PromotionBO> items;

        public MyAdapter(ArrayList<PromotionBO> mylist) {
            super(getActivity(), R.layout.row_promo, mylist);
            this.items = mylist;
        }

        public PromotionBO getItem(int position) {
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
                row = inflater.inflate(R.layout.row_promo, parent, false);

                holder.tvpromoName = (TextView) row
                        .findViewById(R.id.tvPromoName);
                holder.tvpromoName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.rbExecuted = (CheckBox) row
                        .findViewById(R.id.executed_CB);
                holder.rbAnnounced = (CheckBox) row
                        .findViewById(R.id.announced_CB);

                holder.etPromoQty = (EditText) row.findViewById(R.id.et_promoqty);
                holder.etPromoQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.btnPhoto = (ImageView) row
                        .findViewById(R.id.btn_photo);
                holder.tvgroupName = (TextView) row.findViewById(R.id.tv_group_name);

                holder.reasonSpin = (Spinner) row
                        .findViewById(R.id.spin_reason);

                holder.reasonSpin.setAdapter(spinnerAdapter);
                holder.ratingSpin = (Spinner) row.findViewById(R.id.spin_rating);
                if (mRatingAdapter != null)
                    holder.ratingSpin.setAdapter(mRatingAdapter);

                if (!bmodel.promotionHelper.SHOW_PROMO_TYPE) {
                    row.findViewById(R.id.tv_group_name_header).setVisibility(View.GONE);

                } else {
                    try {
                        ((TextView) row.findViewById(R.id.tv_group_name_header)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_group_name_header).getTag()) != null) {
                            ((TextView) row.findViewById(R.id.tv_group_name_header))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_group_name_header).getTag()));

                        }
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                }
                if (bmodel.promotionHelper.SHOW_PROMO_PHOTO) {
                    holder.btnPhoto.setVisibility(View.VISIBLE);

                } else {
                    holder.btnPhoto.setVisibility(View.GONE);

                }
                if (bmodel.promotionHelper.SHOW_PROMO_REASON) {
                    holder.reasonSpin.setVisibility(View.VISIBLE);
                } else {
                    holder.reasonSpin.setVisibility(View.GONE);
                }
                if (bmodel.promotionHelper.SHOW_PROMO_TYPE) {
                    holder.tvgroupName.setVisibility(View.VISIBLE);
                } else {
                    holder.tvgroupName.setVisibility(View.GONE);
                }
                if (bmodel.promotionHelper.SHOW_PROMO_RATING) {
                    holder.ratingSpin.setVisibility(View.VISIBLE);
                } else {
                    holder.ratingSpin.setVisibility(View.GONE);
                }
                if (bmodel.promotionHelper.SHOW_PROMO_QTY) {
                    holder.etPromoQty.setVisibility(View.VISIBLE);
                    card_keyboard.setVisibility(View.VISIBLE);
                } else {
                    holder.etPromoQty.setVisibility(View.GONE);
                    card_keyboard.setVisibility(View.GONE);
                }
                if (bmodel.promotionHelper.SHOW_PROMO_ANNOUNCER) {
                    holder.rbAnnounced.setVisibility(View.VISIBLE);
                } else {
                    holder.rbAnnounced.setVisibility(View.GONE);
                }

                holder.rbExecuted.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                            holder.rbExecuted.setChecked(false);
                            holder.mPromotionMasterBO.setIsExecuted(0);
                            holder.btnPhoto.setEnabled(false);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_grey_24dp,null));
                            holder.reasonSpin.setEnabled(true);
                            holder.etPromoQty.setEnabled(false);
                            holder.etPromoQty.setText("0");
                        } else {
                            holder.rbExecuted.setChecked(true);
                            holder.mPromotionMasterBO.setIsExecuted(1);
                            holder.btnPhoto.setEnabled(true);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_blue_24dp,null));
                            holder.reasonSpin.setEnabled(false);
                            holder.reasonSpin.setSelection(0);
                            holder.mPromotionMasterBO.setReasonID("0");
                            holder.etPromoQty.setEnabled(true);
                        }
                    }
                });
                holder.rbAnnounced.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mPromotionMasterBO.getHasAnnouncer() == 1) {
                            holder.rbAnnounced.setChecked(false);
                            holder.mPromotionMasterBO.setHasAnnouncer(0);
                        } else {
                            holder.rbAnnounced.setChecked(true);
                            holder.mPromotionMasterBO.setHasAnnouncer(1);
                        }
                    }
                });

                holder.reasonSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.reasonSpin
                                        .getSelectedItem();

                                holder.mPromotionMasterBO.setReasonID(reString
                                        .getReasonID());
                                holder.mPromotionMasterBO
                                        .setReasonDesc(reString.getReasonDesc());

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.ratingSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        StandardListBO standardListBO = (StandardListBO) holder.ratingSpin.getSelectedItem();
                        holder.mPromotionMasterBO.setRatingId(standardListBO.getListID());
                        holder.mPromotionMasterBO.setRatingDec(standardListBO.getListName());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                holder.etPromoQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!"".equals(s.toString())) {
                            int scqty = SDUtil.convertToInt(holder.etPromoQty
                                    .getText().toString());
                            holder.mPromotionMasterBO.setPromoQty(scqty);
                        }


                    }
                });

                holder.etPromoQty.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.etPromoQty;
                        QUANTITY.setTag(holder.mPromotionMasterBO);
                        int inType = holder.etPromoQty.getInputType();
                        holder.etPromoQty.setInputType(InputType.TYPE_NULL);
                        holder.etPromoQty.onTouchEvent(event);
                        holder.etPromoQty.setInputType(inType);
                        holder.etPromoQty.selectAll();
                        holder.etPromoQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(null, 0);
                        return true;
                    }
                });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bmodel.isExternalStorageAvailable()) {
                            mImageName = "PT_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    + "_" + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";
                            mImagePath = "Promotion/"
                                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                                    .replace("/", "") + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;

                            bmodel.promotionHelper.mSelectedPromoID = holder.mPromotionMasterBO
                                    .getPromoId();
                            String fnameStarts = "PT_"
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    + "_" + Commons.now(Commons.DATE);

                            boolean nFilesThere = bmodel
                                    .checkForNFilesInFolder(
                                            HomeScreenFragment.photoPath, mImageCount,
                                            fnameStarts);
                            if (nFilesThere) {
                                showFileDeleteAlert(
                                        holder.mPromotionMasterBO.getPromoId()
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

            holder.mPromotionMasterBO = items.get(position);

            holder.tvpromoName
                    .setText(holder.mPromotionMasterBO.getPromoName());
            if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                holder.rbExecuted.setChecked(true);
                holder.btnPhoto.setEnabled(true);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_blue_24dp,null));
                holder.reasonSpin.setEnabled(false);
                holder.reasonSpin.setSelection(0);
                holder.mPromotionMasterBO.setReasonID("0");
            } else {
                holder.rbExecuted.setChecked(false);
                holder.btnPhoto.setEnabled(false);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_grey_24dp,null));
                holder.reasonSpin.setEnabled(true);
            }
            if (holder.mPromotionMasterBO.getHasAnnouncer() == 1) {
                holder.rbAnnounced.setChecked(true);
            } else {
                holder.rbAnnounced.setChecked(false);
            }

            if (holder.mPromotionMasterBO.getIsExecuted() == 0) {
                holder.btnPhoto.setImageBitmap(null);
              //  holder.btnPhoto.setImageResource(R.drawable.ic_photo_camera);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_grey_24dp,null));

            } else if ((holder.mPromotionMasterBO.getImageName() != null)
                    && (!"".equals(holder.mPromotionMasterBO.getImageName()))
                    && (!"null"
                    .equals(holder.mPromotionMasterBO.getImageName()))) {
                File imgFile = new File(HomeScreenFragment.photoPath + "/" + holder.mPromotionMasterBO.getImageName());
                Glide.with(getActivity())
                        .load(imgFile.getAbsoluteFile())
                        .asBitmap()
                        .centerCrop()
                        .transform(bmodel.circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageBitmap(null);
               // holder.btnPhoto.setImageResource(R.drawable.ic_photo_camera);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_photo_camera_grey_24dp,null));
            }
            if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                holder.etPromoQty.setEnabled(true);
            } else {
                holder.etPromoQty.setEnabled(false);
            }

            TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);

            if ("I".equals(holder.mPromotionMasterBO.getFlag()))
                holder.tvpromoName.setTextColor(color);
            else
                holder.tvpromoName.setTextColor(ContextCompat.getColor(getActivity(),
                        R.color.list_item_text_primary_color));
            holder.reasonSpin
                    .setSelection(getReasonIndex(holder.mPromotionMasterBO
                            .getReasonID()));
            holder.ratingSpin.setSelection(getRatingIndex(holder.mPromotionMasterBO.getRatingId()));
            String promo_groupName = " : " + holder.mPromotionMasterBO.getGroupName();
            holder.tvgroupName.setText(promo_groupName);
            holder.tvgroupName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            String promoQty = holder.mPromotionMasterBO.getPromoQty() + "";
            holder.etPromoQty.setText(promoQty);

            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return row;
        }
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        spinnerAdapter = new ArrayAdapter<ReasonMaster>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if ("PROR".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_promo, menu);
    }

    private void loadPromoData(int productID) {

        bmodel.promotionHelper.downloadAllPromotionMaster();

        PromotionBO promotionMaster = new PromotionBO();
        promotionMaster.setPromoId(0);
        promotionMaster.setProductId(0);
        promotionMaster.setPromoName(SELECT);

        ArrayAdapter<PromotionBO> mPromoSpinAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        mPromoSpinAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPromoSpinAdapter.add(promotionMaster);

        for (PromotionBO temp : bmodel.promotionHelper.getmPromotionList()) {

            Commons.print("PromoID>>>>," + "" + temp.getProductId() + " Incoming id "
                    + productID);

            if (temp.getProductId() == productID) {

                vExcludedPromo.add(temp.getPromoId());
            }
        }

        Commons.print("All promo list size>>," + ""
                + bmodel.promotionHelper.getmAllPromotionList().size());

        for (PromotionBO temp : bmodel.promotionHelper.getmAllPromotionList()) {
            if (!vExcludedPromo.contains(temp.getPromoId()))
                mPromoSpinAdapter.add(temp);

        }

        mPromo.setAdapter(mPromoSpinAdapter);
        mPromo.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                PromotionBO reString = (PromotionBO) arg0.getSelectedItem();

                promo = reString.getPromoId();
                promoName = reString.getPromoName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * Method to save Asset Details in sql table
     */
    private void savePromoAddandDeletedetails() {
        PromotionBO promotionMaster = new PromotionBO();
        promotionMaster.setPromoId(promo);
        promotionMaster.setIsExecuted(1);
        promotionMaster.setProductId(productID);
        promotionMaster.setPromoName(promoName);
        promotionMaster.setFlag("I");

        bmodel.promotionHelper.getmPromotionList().add(promotionMaster);

    }

    @Override
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Promotion Tracking");
    }

    private void getAddPromoScreen() {
        dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_promo_dailog);

        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setCancelable(false);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mPromo = (Spinner) dialog.findViewById(R.id.spinner_promo);

        mProduct = (Spinner) dialog.findViewById(R.id.spinner_brand);
        loadBrandData();

        dialog.findViewById(R.id.msave)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!mPromo.getSelectedItem().toString()
                                .equals(SELECT)
                                && !mProduct.getSelectedItem().toString()
                                .equals(SELECT)) {

                            savePromoAddandDeletedetails();
                            updatebrandtext("Brand", -1);
                            dialog.dismiss();
                            dialog = null;

                        } else {
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.nopromo),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        dialog.findViewById(R.id.mback)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        dialog = null;
                    }

                });
        this.dialog.show();

    }

    private void loadBrandData() {

        ArrayAdapter<ChildLevelBo> mpromoProductadapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        mpromoProductadapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ChildLevelBo mChildLevelBo = new ChildLevelBo();
        mChildLevelBo.setParentid(0);
        mChildLevelBo.setProductid(0);
        mChildLevelBo.setPlevelName(SELECT);
        mChildLevelBo.setProductLevel("");

        mpromoProductadapter.add(mChildLevelBo);

        for (ChildLevelBo temp : bmodel.promotionHelper.getmChildLevelBo()) {
            mpromoProductadapter.add(temp);
        }

        mProduct.setAdapter(mpromoProductadapter);

        mProduct.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                ChildLevelBo reString = (ChildLevelBo) mProduct
                        .getSelectedItem();

                productID = reString.getProductid();

                Commons.print(">>>>>>>>>>>>>>>>>>>>," + "" + productID);

                loadPromoData(productID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

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

            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            boolean navDrawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (bmodel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.productHelper.isFilterAvaiable(MENU_CODE)) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
            }/* else {
            if (isFilter) {
                menu.findItem(R.id.menu_product_filter).setVisible(true);
            }
        }*/

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            menu.findItem(R.id.menu_add).setVisible(false);
            menu.findItem(R.id.menu_next).setVisible(false);
            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
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
        } else if (i == R.id.menu_next) {
            savePromotionData();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_add) {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    return false;
                } else {
                    dialog.cancel();
                    dialog = null;

                }
            }
            // Open dialog
            getAddPromoScreen();

            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(MENU_CODE);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, MENU_CODE);
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            // Initialize the Filter Fragment class
            FilterFragment<?> frag = (FilterFragment<?>) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            // Child level filter Name
            bundle.putString("filterHeader", bmodel.promotionHelper
                    .getmChildLevelBo().get(0).getProductLevel());
            // Child level content to filter fragment class
            bundle.putSerializable("serilizeContent",
                    bmodel.promotionHelper.getmChildLevelBo());
            bundle.putString("isFrom", "Promo");
            // Parent level filter
            if (bmodel.promotionHelper.getmParentLevelBo() != null
                    && bmodel.promotionHelper.getmParentLevelBo().size() > 0) {
                bundle.putBoolean("isFormBrand", true);
                // Parent level filter header name
                bundle.putString("pfilterHeader", bmodel.promotionHelper
                        .getmParentLevelBo().get(0).getPl_productLevel());
                // Parent Level Filter Content
                bmodel.productHelper.setPlevelMaster(bmodel.promotionHelper
                        .getmParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);

            }
            // set Fragment class Arguments
            FilterFragment<?> fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
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
            bundle.putString("isFrom", "Promo");
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
    public void updatebrandtext(String filtertext, int mPid) {
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();
            // Change the Brand button Name
            brandFilterText = filtertext;
            mselectedfilterid = mPid;
            if (mPid == -1)
                tvdesc.setText(getResources()
                        .getString(R.string.promotion_name));
            else
                tvdesc.setText(brandFilterText); // set Selected Brand Name

            ArrayList<PromotionBO> items = mSelectedStandardListBO.getPromotionTrackingList();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            promoList = new ArrayList<>();
            // Iterate the List and the items to the Listholder
            for (PromotionBO temp : items) {
                if (temp.getProductId() == mPid || mPid == -1)
                    promoList.add(temp);
            }
            // set the list values to the adapter
            mSchedule = new MyAdapter(promoList);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Save the Promotion Data in DB And validation is data entered or not
     */
    private void savePromotionData() {
        try {
            // Check Promotion data is or not
            if (bmodel.promotionHelper.hasPromoData())
                new SavePromotionData().execute();
            else
                bmodel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    class SavePromotionData extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.promotionHelper.savePromotionDetails();
                bmodel.promotionHelper.deleteUnusedImages();
                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_PROMO);
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
                + mImageCount
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<PromotionBO> items = bmodel.promotionHelper
                                .getmPromotionList();
                        for (int i = 0; i < items.size(); i++) {
                            PromotionBO promoBO = items.get(i);
                            if (promoBO.getPromoId() == Integer.parseInt(bbid)) {
                                promoBO.setImageName("");
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

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        try {
            ArrayList<PromotionBO> items = mSelectedStandardListBO.getPromotionTrackingList();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            promoList = new ArrayList<>();

            for (LevelBO levelBO : parentidList) {
                for (PromotionBO promoBO : items) {
                    if (levelBO.getProductID() == promoBO.getProductId()) {
                        promoList.add(promoBO);
                    }

                }
            }
            // set the list values to the adapter
            mSchedule = new MyAdapter(promoList);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        try {
            this.parentidList = parentidList;
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            this.mAttributeProducts = mAttributeProducts;
            this.filtertext = filtertext;
            ArrayList<PromotionBO> items = mSelectedStandardListBO.getPromotionTrackingList();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            promoList = new ArrayList<>();

            if (mAttributeProducts != null) {
                if (!parentidList.isEmpty()) {
                    for (LevelBO levelBO : parentidList) {
                        for (PromotionBO productBO : items) {
                            if (levelBO.getProductID() == productBO.getProductId()
                                    && mAttributeProducts.contains(productBO.getProductId())) {
                                // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                promoList.add(productBO);
                            }
                        }
                    }
                } else {
                    for (int pid : mAttributeProducts) {
                        for (PromotionBO promoBO : items) {

                            if (pid == promoBO.getProductId()) {
                                promoList.add(promoBO);
                            }
                        }
                    }
                }
            } else {
                if (mSelectedIdByLevelId.size() == 0 || bmodel.isMapEmpty(mSelectedIdByLevelId)) {
                    promoList.addAll(items);
                } else {
                    for (LevelBO levelBO : parentidList) {
                        for (PromotionBO promoBO : items) {
                            if (levelBO.getProductID() == promoBO.getProductId()) {
                                promoList.add(promoBO);
                            }

                        }
                    }
                }
            }
            // set the list values to the adapter
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            mSchedule = new MyAdapter(promoList);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
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
                        mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
                        updatebrandtext("", mselectedfilterid);
                        dialog.dismiss();

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
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
