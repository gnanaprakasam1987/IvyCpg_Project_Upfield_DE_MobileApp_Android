package com.ivy.cpg.view.sf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.Logs;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.bo.ShelfShareBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ShelfShareCallBackListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.BrandsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShelfShareDialogFragment extends DialogFragment {

    public static final HashMap<String, ShelfShareBO> mBrandsDetailsHashMap = new HashMap<>();
    private HashMap<String, HashMap<String, Object>> mBrandNameColorCount;
    private ArrayList<String> mBrandNameList;

    private static final String TAG = "ShelfShareDialogFragment";
    private static final String COUNT = "count";
    private static final String DO_NOT_SHOW_CLEAR_DIALOG = "do_not_show_clear_data_dialog";
    private static final String EXT_SHELF = "Ext.Shelf";
    final private String mEmpty = "empty";
    private final int mMaxBlockCount = 10;
    private final int mMaxShelfCount = 15;
    private boolean isCreateClicked;
    private int mBlockCount = 0;
    private int mShelfCount = 0;
    private String mShelfLength = "0";
    private int mInitialBlockCount = 0;
    private int mInitialShelfCount = 0;
    private String mInitialShelfLength = "0";
    private int mShare = 40;
    private int mExtraShelfCount = 0;
    private int mSelectedLocationIndex;
    private int mSelectedBrandColor = 0;
    private int mExtraCount = 0;
    private int mParentID;
    private String mSelectedBrandName = null;
    private String mKey;
    private int mModuleFlag;
    private boolean mTextWatcherEnabled = true;

    private BrandsAdapter mBrandsAdapter;
    private SalesFundamentalHelper mSFHelper;
    private RecyclerView mListBrands;
    private EditText mEdtTxtBlock;
    private EditText mEdtTxtShelf;
    private EditText mEdtTxtShelfLength;
    private EditText mSelectedET;

    private HorizontalScrollView mHScrollShelfWrapper;
    private ShelfShareHelper mShelfShareHelper;
    private BusinessModel mBModel;


    private final OnClickListener mNumberPadListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero || i == R.id.calcone || i == R.id.calctwo || i == R.id.calcthree
                    || i == R.id.calcfour || i == R.id.calcfive || i == R.id.calcsix
                    || i == R.id.calcseven || i == R.id.calceight || i == R.id.calcnine) {
                eff(((Button) v).getText().toString());
            } else if (i == R.id.calcdel) {
                String s = mSelectedET.getText().toString();

                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0)
                        s = "0";
                }
                mSelectedET.setText(s);

            } else if (i == R.id.calcdot) {
                if (mSelectedET == mEdtTxtShelfLength) {
                    String s1 = mSelectedET.getText().toString();
                    if (!s1.contains(".")) {
                        String strS1 = s1 + ".";
                        mSelectedET.setText(strS1);
                    }
                }

            }

        }
    };

    private final OnClickListener mShelfCreateListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mBlockCount == 0 || mShelfCount == 0
                    || SDUtil.convertToFloat(mShelfLength) == 0)
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.shelf_empty_alert),
                        Toast.LENGTH_SHORT).show();
            else if (mBlockCount <= mMaxBlockCount
                    && mShelfCount <= mMaxShelfCount
                    && SDUtil.convertToFloat(mShelfLength) != 0)
                showDataClearDialog();
        }
    };
    private ArrayList<String> mBrandNameListForDB;
    private List<SOSBO> mCategoryForDialogSOSBO = null;
    private ShelfShareCallBackListener callBackListener;
    private final OnClickListener mCancelListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            callBackListener.handleDialogClose();
        }
    };
    private final OnClickListener mEditDoneListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mModuleFlag == ShelfShareHelper.SOS) {
                if (!isCreateClicked) {
                    mBlockCount = mInitialBlockCount;
                    mShelfCount = mInitialShelfCount;
                    mShelfLength = mInitialShelfLength;
                }
                if (!mBrandsDetailsHashMap.isEmpty()) {
                    buildResultForSOS();
                    mShelfShareHelper.getLocations().get(mSelectedLocationIndex).setShelfDetailForSOS(
                            String.valueOf(mKey), getEntireShelfDetail());
                    mShelfShareHelper.getLocations().get(mSelectedLocationIndex).setShelfBlockDetailForSOS(
                            String.valueOf(mKey), mBrandsDetailsHashMap);
                }
                callBackListener.SOSBOCallBackListener(mCategoryForDialogSOSBO);
            }

        }

        /*
    * Returns currently edited shelf detail
    */
        private HashMap<String, Object> getEntireShelfDetail() {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(ShelfShareHelper.BLOCK_COUNT, mBlockCount);
            hashMap.put(ShelfShareHelper.SHELF_COUNT, mShelfCount);
            hashMap.put(ShelfShareHelper.SHELF_LENGTH, mShelfLength);
            hashMap.put(ShelfShareHelper.EXTRA_LENGTH, mExtraShelfCount);
            hashMap.put(ShelfShareHelper.SHARE, mShare);
            hashMap.put(ShelfShareHelper.LOADING_FROM_DB, false);
            hashMap.put(ShelfShareHelper.BRAND_DETAIL_HASH_MAP,
                    mBrandsDetailsHashMap);
            return hashMap;
        }

        private void buildResultForSOS() {

            if (!mCategoryForDialogSOSBO.isEmpty()) {

                //get extra shelf and reduce that value from total
                float mExtActual = (Integer) mBrandNameColorCount.get(EXT_SHELF).get(COUNT);

                // IF All Brands in Total PopUp
                for (int i = 0; i < mCategoryForDialogSOSBO.size(); i++) {
                    SOSBO sosbo = mCategoryForDialogSOSBO.get(i);

                    float actual = (Integer) mBrandNameColorCount.get(
                            sosbo.getProductName()).get(COUNT);

                    float tot = (mShelfCount * mBlockCount * 4) - mExtActual;

                    actual = (float) (actual * (SDUtil.convertToFloat(mShelfLength) / 4.0));
                    tot = (float) (tot * (SDUtil.convertToFloat(mShelfLength) / 4.0));

                    sosbo.getLocations().get(mSelectedLocationIndex).setActual(Float.toString(actual));

                    sosbo.getLocations().get(mSelectedLocationIndex).setParentTotal(Float.toString(tot));

                    // Gap Calculation

                    sosbo.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(0, 1));

                    if (SDUtil.convertToFloat(sosbo.getLocations().get(mSelectedLocationIndex).getParentTotal()) > 0) {

                        float mParentTotal = SDUtil.convertToFloat(sosbo.getLocations().get(mSelectedLocationIndex)
                                .getParentTotal());
                        float mNorm = sosbo.getNorm();
                        float actual1 = SDUtil.convertToFloat(sosbo.getLocations().get(mSelectedLocationIndex).getActual());

                        float target = (mParentTotal * mNorm) / 100;
                        float gap = target - actual1;
                        float percentage = 0;
                        if (mParentTotal > 0)
                            percentage = (actual1 / mParentTotal) * 100;

                        sosbo.getLocations().get(mSelectedLocationIndex).setTarget(SDUtil.roundIt(target, 2));
                        sosbo.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel.formatPercent(percentage));
                        sosbo.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(-gap, 2));
                    } else {
                        sosbo.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                        sosbo.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                        sosbo.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
                    }
                }

            }

        }
    };
    private HashMap<String, HashMap<String, Object>> mShelfDetailForSOS = null;

    public ShelfShareDialogFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) context.getApplicationContext();
        mBModel.setContext(getActivity());
        mShelfShareHelper = ShelfShareHelper.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mParentID = bundle.getInt("parent_id");
            this.mModuleFlag = bundle.getInt("flag");
            this.mSelectedLocationIndex = bundle.getInt("selectedlocation");
            mKey = Integer.toString(mParentID);
        }
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_shelf_share,
                container, false);

        isCreateClicked = false;

        mEdtTxtBlock = (EditText) view.findViewById(R.id.edtTxtBlock);
        mEdtTxtShelf = (EditText) view.findViewById(R.id.edtTxtShelf);
        mEdtTxtShelfLength = (EditText) view
                .findViewById(R.id.edtTxtShelfLength);
        TextView mTxtBrandName = (TextView) view.findViewById(R.id.txtBrandName);
        mHScrollShelfWrapper = (HorizontalScrollView) view
                .findViewById(R.id.hrScrollShelfWrapper);
        mListBrands = (RecyclerView) view.findViewById(R.id.listBrands);
        Button mBtnDone = (Button) view.findViewById(R.id.btnDoneShelfShare);
        Button mBtnCancel = (Button) view.findViewById(R.id.btnCancelShelfShare);
        Button mBtnCreate = (Button) view.findViewById(R.id.btnCreateShelfShare);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((TextView) view.findViewById(R.id.tv_toolbar_title)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            toolbar.setTitle(R.string.create_shelf);
        }

        mSelectedET = null;

        //setTypefaces
        ((TextView) view.findViewById(R.id.tvEnterTitle)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvNorows)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvNoColumns)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvBlockLength)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvChooseBlocks)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mTxtBrandName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mEdtTxtBlock.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdtTxtShelf.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdtTxtShelfLength.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mBtnCancel.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mBtnCreate.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mBtnDone.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mEdtTxtBlock.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSelectedET = mEdtTxtBlock;
                int inType = mEdtTxtBlock.getInputType();
                mEdtTxtBlock.setInputType(InputType.TYPE_NULL);
                mEdtTxtBlock.onTouchEvent(event);
                mEdtTxtBlock.setInputType(inType);
                return true;
            }
        });

        mEdtTxtShelf.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSelectedET = mEdtTxtShelf;
                int inType = mEdtTxtShelf.getInputType();
                mEdtTxtShelf.setInputType(InputType.TYPE_NULL);
                mEdtTxtShelf.onTouchEvent(event);
                mEdtTxtShelf.setInputType(inType);
                return true;
            }
        });

        mEdtTxtShelfLength.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSelectedET = mEdtTxtShelfLength;
                int inType = mEdtTxtShelfLength.getInputType();
                mEdtTxtShelfLength.setInputType(InputType.TYPE_NULL);
                mEdtTxtShelfLength.onTouchEvent(event);
                mEdtTxtShelfLength.setInputType(inType);
                return true;
            }
        });

        mEdtTxtBlock
                .addTextChangedListener(new CommonTextWatcher(mEdtTxtBlock));
        mEdtTxtShelf
                .addTextChangedListener(new CommonTextWatcher(mEdtTxtShelf));
        mEdtTxtShelfLength.addTextChangedListener(new CommonTextWatcher(
                mEdtTxtShelfLength));

        mBtnDone.setOnClickListener(mEditDoneListener);
        mBtnCancel.setOnClickListener(mCancelListener);
        mBtnCreate.setOnClickListener(mShelfCreateListener);

        setNumberPadListener(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        initBrandNameList();
        createBrandColor();
        preloadShelf();
        mBrandsAdapter = new BrandsAdapter(getActivity(), mBrandNameList, mBrandNameColorCount, mSelectedBrandName, new BrandsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                mSelectedBrandName = item;
                mSelectedBrandColor = (Integer) mBrandNameColorCount.get(
                        item).get("color");
                Commons.print(TAG + "SelectedBrandColor: "
                        + mSelectedBrandColor + " SelectedBrandName: "
                        + mSelectedBrandName);

                mBrandsAdapter.setSelectedItem(item);
                mBrandsAdapter.notifyDataSetChanged();
                mListBrands.invalidate();
            }
        });

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mListBrands.setLayoutManager(layoutManager);
        mListBrands.setAdapter(mBrandsAdapter);


    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logs.debug(TAG, "Cycle: onPause");
    }

    private void setNumberPadListener(View view) {
        view.findViewById(R.id.calczero)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcone)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calctwo)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcthree)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcfour)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcfive)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcsix)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcseven)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calceight)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcnine)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcdel)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcdot)
                .setOnClickListener(mNumberPadListener);
        view.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
    }

    private void eff(String val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {

            String s = mSelectedET.getText().toString();

            if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s))
                mSelectedET.setText(val);
            else
                mSelectedET.setText(mSelectedET.getText().append(val));
        }
    }

    public void setOnShelfShareListener(
            ShelfShareCallBackListener callBackListener) {
        this.callBackListener = callBackListener;
    }

    /**
     * It initialise product names depends on selected module and selected
     * category. At first time it uses data from database to build shelf.
     * Second time it give actual structure which have build previously. Loads
     * depends on modules.
     */
    private void initBrandNameList() {
        int mLocationId;
        mBrandNameList = new ArrayList<>();
        mBrandNameListForDB = new ArrayList<>();

        if (mModuleFlag == ShelfShareHelper.SOS) {
            initBrandNameListFromSOS();
            if (mShelfShareHelper.getLocations().get(mSelectedLocationIndex).getShelfDetailForSOS() != null)
                mShelfDetailForSOS = mShelfShareHelper.getLocations().get(mSelectedLocationIndex).
                        getShelfDetailForSOS();
            if (mShelfDetailForSOS != null
                    && mShelfDetailForSOS.containsKey(String.valueOf(mKey))) {
                mLocationId = mShelfShareHelper.getLocations().get(mSelectedLocationIndex).getLocationId();
                if (!(Boolean) mShelfDetailForSOS.get(String.valueOf(mKey))
                        .get(ShelfShareHelper.LOADING_FROM_DB))
                    loadEntireShelfDetail(mShelfDetailForSOS.get(String
                            .valueOf(mKey)));
                else
                    buildShelfWithDBData(mShelfDetailForSOS.get(String
                            .valueOf(mKey)), mLocationId);
            }
        }
    }

    /**
     * Loads already constructed shelf detail from global variable.
     * BlockCount,ShelfCount,ShelfLength,Share,BrandDetailsHashMap
     *
     * @param hashMap SOS List
     */
    @SuppressWarnings("unchecked")
    private void loadEntireShelfDetail(HashMap<String, Object> hashMap) {
        mBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mInitialBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mInitialShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mInitialShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mExtraShelfCount = (Integer) hashMap.get(ShelfShareHelper.EXTRA_LENGTH);
        mShare = (Integer) hashMap.get(ShelfShareHelper.SHARE);
        mBrandsDetailsHashMap.putAll((HashMap<String, ShelfShareBO>) hashMap
                .get(ShelfShareHelper.BRAND_DETAIL_HASH_MAP));
    }

    /**
     * Constructing shelf using BlockCount,ShelfCount,ShelfLength and Share
     * using these details.
     *
     * @param hashMap SOS list
     */
    private void buildShelfWithDBData(HashMap<String, Object> hashMap, int mLocationId) {
        mShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);
        mExtraShelfCount = (Integer) hashMap.get(ShelfShareHelper.EXTRA_LENGTH);

        mInitialBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mInitialShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mInitialShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        int totalShelf = mShelfCount * mBlockCount;

        if (mShelfCount == 0 && !mBrandNameListForDB.isEmpty())
            return;

        mSFHelper.loadSOSBlockDetails(DataMembers.uidSOS,
                String.valueOf(mKey), totalShelf, mLocationId);
    }

    /**
     * It works when BrandDetails not being empty.
     */
    private void preloadShelf() {
        if (mBrandsDetailsHashMap.size() > 0) {
            mTextWatcherEnabled = false;
            mEdtTxtBlock.setText(String.valueOf(mBlockCount));
            mEdtTxtShelf.setText(String.valueOf(mShelfCount));
            mEdtTxtShelfLength.setText(String.valueOf(mShelfLength));
            populateGridItems(true);
            mTextWatcherEnabled = true;
        }
    }

    /**
     * Creates colour for every products.
     */
    private void createBrandColor() {
        /*
      Contains all colours which are added into colour arrays.xml
     */
        int[] mBrandColorArray = getActivity().getResources().getIntArray(
                R.array.array_color_brands);
        mBrandNameColorCount = new HashMap<>();

        for (int i = 0; i < mBrandNameList.size(); i++) {
            int color;

            if (mBrandColorArray.length > i)
                color = mBrandColorArray[i];
            else {
                color = mBrandColorArray[i % mBrandColorArray.length];
            }

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("color", color);
            hashMap.put(COUNT, 0);
            mBrandNameColorCount.put(mBrandNameList.get(i), hashMap);
        }

        int color = ContextCompat.getColor(getActivity(), R.color.competitor_brand);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("color", color);
        hashMap.put(COUNT, 0);
        mBrandNameColorCount.put(mEmpty, hashMap);

        Commons.print("ShelfShareDialogFragment in createBrandColor," +
                "Brand Name and Color: " + mBrandNameColorCount.toString());
    }

    /**
     * Used for setting colour for entire shelf. To be called when need to empty
     * single shelf or fill entire shelf with single product.
     *
     * @param view view that color has to be changed
     * @param color color for the view
     */
    private void setCellColor(View view, int color) {
        view.findViewById(R.id.relLytFirst)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytSecond)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytThird)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytFourth)
                .setBackgroundColor(color);
    }

    /**
     * Used to select colour while clicking on shelf. Whether selected brand
     * colour or empty.
     *
     * @param brandName Brand Name
     * @return color
     */
    private int getOppositeColor(String brandName) {
        int competitorBrandColor = ContextCompat.getColor(getActivity(),
                R.color.competitor_brand);
        return brandName.equals(mSelectedBrandName) ? competitorBrandColor
                : mSelectedBrandColor;
    }

    /**
     * Used to set colour for each 4 part of shelf.
     *
     * @param view view that color has to be changed
     * @param positionKey Brand Position
     */
    private void setCellColor(View view, String positionKey) {
        int color;

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getFirstCell()).get(
                "color");
        view.findViewById(R.id.relLytFirst)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytFirst)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getFirstCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getSecondCell()).get(
                "color");
        view.findViewById(R.id.relLytSecond)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytSecond)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getSecondCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getThirdCell()).get(
                "color");
        view.findViewById(R.id.relLytThird)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytThird)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getThirdCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getFourthCell()).get(
                "color");
        view.findViewById(R.id.relLytFourth)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytFourth)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getFourthCell());
    }

    /**
     * Shows dialog to split shelf into 4 part.
     *
     * @param gridCellView Grid view
     * @param positionKey Brand Position
     */
    private void showDialog(final View gridCellView, final String positionKey) {

        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar);
        LayoutInflater inflater = getActivity()
                .getLayoutInflater();
        final ViewGroup nullParent = null;
        final View dialogView = inflater.inflate(R.layout.dialog_shelf_share,
                nullParent, false);

        setCellColor(dialogView, positionKey);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        RelativeLayout firstView = (RelativeLayout) dialogView
                .findViewById(R.id.relLytFirst);
        RelativeLayout secondView = (RelativeLayout) dialogView
                .findViewById(R.id.relLytSecond);
        RelativeLayout thirdView = (RelativeLayout) dialogView
                .findViewById(R.id.relLytThird);
        RelativeLayout fourthView = (RelativeLayout) dialogView
                .findViewById(R.id.relLytFourth);

        firstView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String tag = (String) v.getTag();

                ShelfShareBO shelf = mBrandsDetailsHashMap.get(positionKey);

                ShelfShareBO prevShelf = new ShelfShareBO();
                prevShelf.setOthersCount(shelf.getOthersCount());
                prevShelf.setFirstCell(shelf.getFirstCell());
                prevShelf.setSecondCell(shelf.getSecondCell());
                prevShelf.setThirdCell(shelf.getThirdCell());
                prevShelf.setFourthCell(shelf.getFourthCell());

                v.setBackgroundColor(getOppositeColor(tag));

                if (tag.equals(mSelectedBrandName)) {
                    shelf.setFirstCell(mEmpty);
                    v.setTag(mEmpty);
                } else {
                    shelf.setFirstCell(mSelectedBrandName);
                    v.setTag(mSelectedBrandName);
                }
                brandsCounter(prevShelf, shelf);
            }
        });

        secondView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String tag = (String) v.getTag();

                ShelfShareBO shelf = mBrandsDetailsHashMap.get(positionKey);

                ShelfShareBO prevShelf = new ShelfShareBO();
                prevShelf.setOthersCount(shelf.getOthersCount());
                prevShelf.setFirstCell(shelf.getFirstCell());
                prevShelf.setSecondCell(shelf.getSecondCell());
                prevShelf.setThirdCell(shelf.getThirdCell());
                prevShelf.setFourthCell(shelf.getFourthCell());

                v.setBackgroundColor(getOppositeColor(tag));

                if (tag.equals(mSelectedBrandName)) {
                    shelf.setSecondCell(mEmpty);
                    v.setTag(mEmpty);
                } else {
                    shelf.setSecondCell(mSelectedBrandName);
                    v.setTag(mSelectedBrandName);
                }
                brandsCounter(prevShelf, shelf);

            }
        });

        thirdView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String tag = (String) v.getTag();

                ShelfShareBO shelf = mBrandsDetailsHashMap.get(positionKey);

                ShelfShareBO prevShelf = new ShelfShareBO();
                prevShelf.setOthersCount(shelf.getOthersCount());
                prevShelf.setFirstCell(shelf.getFirstCell());
                prevShelf.setSecondCell(shelf.getSecondCell());
                prevShelf.setThirdCell(shelf.getThirdCell());
                prevShelf.setFourthCell(shelf.getFourthCell());

                v.setBackgroundColor(getOppositeColor(tag));

                if (tag.equals(mSelectedBrandName)) {
                    shelf.setThirdCell(mEmpty);
                    v.setTag(mEmpty);
                } else {
                    shelf.setThirdCell(mSelectedBrandName);
                    v.setTag(mSelectedBrandName);
                }
                brandsCounter(prevShelf, shelf);
            }
        });

        fourthView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String tag = (String) v.getTag();

                ShelfShareBO shelf = mBrandsDetailsHashMap.get(positionKey);

                ShelfShareBO prevShelf = new ShelfShareBO();
                prevShelf.setOthersCount(shelf.getOthersCount());
                prevShelf.setFirstCell(shelf.getFirstCell());
                prevShelf.setSecondCell(shelf.getSecondCell());
                prevShelf.setThirdCell(shelf.getThirdCell());
                prevShelf.setFourthCell(shelf.getFourthCell());

                v.setBackgroundColor(getOppositeColor(tag));

                if (tag.equals(mSelectedBrandName)) {
                    shelf.setFourthCell(mEmpty);
                    v.setTag(mEmpty);
                } else {
                    shelf.setFourthCell(mSelectedBrandName);
                    v.setTag(mSelectedBrandName);
                }
                brandsCounter(prevShelf, shelf);
            }
        });

        dialogView.findViewById(R.id.btnDialogCancel)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setCellColor(gridCellView, positionKey);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    /**
     * Dialog to alert user while try to alter shelf count after filling product
     * detail
     */
    private void showDataClearDialog() {

        boolean flag = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getBoolean(DO_NOT_SHOW_CLEAR_DIALOG,
                false);
        int count = (mShelfCount * mBlockCount) * 4;
        if (!flag) {
            if (mBrandsDetailsHashMap.size() > 0
                    && count != (Integer) mBrandNameColorCount.get(mEmpty).get(
                    COUNT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final ViewGroup nullParent = null;
                View view = inflater.inflate(R.layout.check_box_do_not_show,
                        nullParent, false);
                builder.setView(view);
                final CheckBox checkBox = (CheckBox) view
                        .findViewById(R.id.chkBoxDoNotShowAgain);
                builder.setMessage("This may remove existing data");
                builder.setNeutralButton("Ok",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (checkBox.isChecked())
                                    PreferenceManager
                                            .getDefaultSharedPreferences(
                                                    getActivity())
                                            .edit()
                                            .putBoolean(
                                                    DO_NOT_SHOW_CLEAR_DIALOG,
                                                    true).apply();
                                else
                                    PreferenceManager
                                            .getDefaultSharedPreferences(
                                                    getActivity())
                                            .edit()
                                            .putBoolean(
                                                    DO_NOT_SHOW_CLEAR_DIALOG,
                                                    false).apply();
                                populateGridItems(false);
                                isCreateClicked = true;
                                dialog.dismiss();
                            }
                        });
                mBModel.applyAlertDialogTheme(builder);
            } else {
                populateGridItems(false);
                isCreateClicked = true;
            }
        } else {
            populateGridItems(false);
            isCreateClicked = true;
        }
    }

    /**
     * It forms grid depends on user input.
     *
     * @param withExistingData Is Populate with existing data
     */
    private void populateGridItems(boolean withExistingData) {
        Commons.print(TAG + "ShelfCount: " + mShelfCount
                + " BlockCount: " + mBlockCount);
        createBrandColor();
        if (mBrandNameColorCount != null) {
            if (!withExistingData)
                mBrandsDetailsHashMap.clear();

            HashMap<String, Object> hashMap = mBrandNameColorCount.get(mEmpty);
            hashMap.put(COUNT,
                    (mShelfCount * mBlockCount - mExtraShelfCount) * 4);
            mBrandNameColorCount.put(mEmpty, hashMap);
            populateShelf(withExistingData);
        }
    }

    /**
     * Shelf get build depends on user input.
     *
     * @param withExistingData Is with existing data
     */
    private void populateShelf(boolean withExistingData) {
        final ViewGroup nullParent = null;
        int counter = 0;
        mExtraCount = 0;
        final ArrayList<Integer> mExtraPositionList = new ArrayList<>();
        mHScrollShelfWrapper.removeAllViews();
        TableLayout tableLayout = new TableLayout(getActivity());
        LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(tableLayoutParams);
        for (int i = 0; i < mShelfCount; i++) {

            TableRow tableRow = new TableRow(getActivity());
            LayoutParams tableRowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, 60);
            tableRowParams.setMargins(0, 4, 0, 4);
            tableRow.setLayoutParams(tableRowParams);
            tableRow.setOrientation(TableRow.HORIZONTAL);

            for (int j = 0; j < mBlockCount; j++) {
                LayoutInflater inflater = getActivity()
                        .getLayoutInflater();
                View convertView = inflater.inflate(R.layout.grid_item_shelfs,
                        nullParent, false);
                convertView.setTag(counter);

                convertView.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        Commons.print("ShelfShareFragment," + "OnLongClickListener");
                        if (mSelectedBrandColor != 0
                                && mSelectedBrandName != null)
                            showDialog(view,
                                    String.valueOf(view.getTag()));
                        else
                            Toast.makeText(
                                    getActivity(),
                                    getResources()
                                            .getString(R.string.selectany)
                                            + " "
                                            + getResources().getString(
                                            R.string.brand),
                                    Toast.LENGTH_LONG).show();
                        return true;
                    }
                });

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Commons.print("ShelfShareFragment," + "OnClickListener");
                        if (mSelectedBrandColor != 0
                                && mSelectedBrandName != null) {
                            if (EXT_SHELF.equalsIgnoreCase(mSelectedBrandName)) {

                                setShelfColor(view, (Integer) view.getTag());
                                mExtraCount++;
                                mExtraPositionList.add((Integer) view.getTag());

                            } else {

                                if (mExtraPositionList.contains((Integer) view.getTag())) {
                                    setShelfColor(view, (Integer) view.getTag());
                                    mExtraCount = 0;

                                } else
                                    setShelfColor(view, (Integer) view.getTag());

                            }
                            mExtraShelfCount = mExtraCount;
                            Commons.print("extra count" + mExtraShelfCount);
                        } else
                            Toast.makeText(
                                    getActivity(),
                                    getResources()
                                            .getString(R.string.selectany)
                                            + " "
                                            + getResources().getString(
                                            R.string.brand),
                                    Toast.LENGTH_LONG).show();
                    }
                });
                invalidateShelf((Integer) convertView.getTag(), convertView,
                        withExistingData);
                tableRow.addView(convertView);
                Commons.print("ShelfShareFragment," +
                        "View Pos:" + convertView.getTag());
                counter++;
            }
            tableLayout.addView(tableRow);
        }


        mHScrollShelfWrapper.addView(tableLayout);
    }

    /**
     * To remove ExtraShelf
     *
     * @param position Position
     * @param view View
     * @param withExistingData With Existing Data
     */
    private void invalidateShelf(int position, View view,
                                 boolean withExistingData) {

        if (withExistingData) {
            ShelfShareBO prevShelf = new ShelfShareBO();
            prevShelf.setFirstCell(mEmpty);
            prevShelf.setSecondCell(mEmpty);
            prevShelf.setThirdCell(mEmpty);
            prevShelf.setFourthCell(mEmpty);
            prevShelf.setOthersCount(1);
            brandsCounter(prevShelf,
                    mBrandsDetailsHashMap.get(String.valueOf(position)));
            setCellColor(view, String.valueOf(position));
        } else {
            setCellColor(view,
                    ContextCompat.getColor(getActivity(), R.color.competitor_brand));
            addShelfDetails(String.valueOf(position), mEmpty, mEmpty,
                    mEmpty, mEmpty, 1, true);
        }
    }

    /**
     * Setting values in ShelfShareBO and setting color.
     *
     * @param view View
     * @param position Position
     */
    private void setShelfColor(View view, int position) {
        String positionKey = String.valueOf(position);

        if (mBrandsDetailsHashMap.containsKey(positionKey)) {
            ShelfShareBO shelf = mBrandsDetailsHashMap.get(positionKey);
            if (shelf.getOthersCount() == 0
                    && shelf.getFirstCell().equals(mSelectedBrandName)) {
                addShelfDetails(positionKey, mEmpty, mEmpty, mEmpty, mEmpty, 1,
                        false);
                setCellColor(view,
                        ContextCompat.getColor(getActivity(), R.color.competitor_brand));
            } else {
                // Full shelf occupied by Others.
                addShelfDetails(positionKey, mSelectedBrandName,
                        mSelectedBrandName, mSelectedBrandName,
                        mSelectedBrandName, 0, false);
                setCellColor(view, mSelectedBrandColor);
            }
        } else {
            addShelfDetails(positionKey, mSelectedBrandName,
                    mSelectedBrandName, mSelectedBrandName, mSelectedBrandName,
                    0, false);
            setCellColor(view, mSelectedBrandColor);
        }
    }


    /**
     * Need to maintain every alter of shelf in mBrandsDetailsHashMap
     *
     * @param positionKey Position key
     * @param first cell
     * @param second cell
     * @param third cell
     * @param fourth cell
     * @param othersCount Other count
     * @param isInitShelf  Is Initial
     */
    private void addShelfDetails(String positionKey, String first,
                                 String second, String third, String fourth, int othersCount,
                                 boolean isInitShelf) {

        ShelfShareBO shelf = new ShelfShareBO();
        shelf.setFirstCell(first);
        shelf.setSecondCell(second);
        shelf.setThirdCell(third);
        shelf.setFourthCell(fourth);
        shelf.setOthersCount(othersCount);
        if (!isInitShelf)
            brandsCounter(mBrandsDetailsHashMap.get(positionKey), shelf);
        mBrandsDetailsHashMap.put(positionKey, shelf);
    }


    /**
     * to maintain count for every product.
     *
     * @param previousShelf Previous Shelf
     * @param currentShelf Current Shelf
     */
    private void brandsCounter(ShelfShareBO previousShelf,
                               ShelfShareBO currentShelf) {
        if (!previousShelf.getFirstCell().equals(currentShelf.getFirstCell())) {
            int count;
            HashMap<String, Object> hashMap;
            // Minus one on previous shelf
            hashMap = mBrandNameColorCount.get(previousShelf.getFirstCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, --count);
            mBrandNameColorCount.put(previousShelf.getFirstCell(), hashMap);
            // Plus one on current shelf
            Commons.print("SSDialogFragment," + "brandsCounter: firstCell: " + currentShelf.getFirstCell());
            hashMap = mBrandNameColorCount.get(currentShelf.getFirstCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, ++count);
            mBrandNameColorCount.put(currentShelf.getFirstCell(), hashMap);
        }
        if (!previousShelf.getSecondCell().equals(currentShelf.getSecondCell())) {
            int count;
            HashMap<String, Object> hashMap;
            // Minus one on previous shelf
            hashMap = mBrandNameColorCount.get(previousShelf.getSecondCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, --count);
            mBrandNameColorCount.put(previousShelf.getSecondCell(), hashMap);
            // Plus one on current shelf
            hashMap = mBrandNameColorCount.get(currentShelf.getSecondCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, ++count);
            mBrandNameColorCount.put(currentShelf.getSecondCell(), hashMap);
        }
        if (!previousShelf.getThirdCell().equals(currentShelf.getThirdCell())) {
            int count;
            HashMap<String, Object> hashMap;
            // Minus one on previous shelf
            hashMap = mBrandNameColorCount.get(previousShelf.getThirdCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, --count);
            mBrandNameColorCount.put(previousShelf.getThirdCell(), hashMap);
            // Plus one on current shelf
            hashMap = mBrandNameColorCount.get(currentShelf.getThirdCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, ++count);
            mBrandNameColorCount.put(currentShelf.getThirdCell(), hashMap);
        }
        if (!previousShelf.getFourthCell().equals(currentShelf.getFourthCell())) {
            int count;
            HashMap<String, Object> hashMap;
            // Minus one on previous shelf
            hashMap = mBrandNameColorCount.get(previousShelf.getFourthCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, --count);
            mBrandNameColorCount.put(previousShelf.getFourthCell(), hashMap);
            // Plus one on current shelf
            hashMap = mBrandNameColorCount.get(currentShelf.getFourthCell());
            count = (Integer) hashMap.get(COUNT);
            hashMap.put(COUNT, ++count);
            mBrandNameColorCount.put(currentShelf.getFourthCell(), hashMap);
        }
        Commons.print(TAG + "BrandNameColorCount: "
                + mBrandNameColorCount.toString());
    }

    private void initBrandNameListFromSOS() {

        /*
      Contains entire product's details as SOSBO for SOS Module
     */
        ArrayList<SOSBO> mSosList = mSFHelper.getmSOSList();

        mCategoryForDialogSOSBO = new ArrayList<>();

        mCategoryForDialogSOSBO.clear();

        // All Brands in Total PopUp
        if (mSosList != null) {
            SOSBO sos = new SOSBO();
            sos.setProductName(EXT_SHELF);
            sos.setProductID(0);
            sos.setLocations(SalesFundamentalHelper.cloneLocationList(mSFHelper.getLocationList()));
            mCategoryForDialogSOSBO.add(sos);
            mBrandNameList.add(sos.getProductName());
            for (SOSBO sosBO : mSosList) {
                if (sosBO.getParentID() == mParentID) {

                    Commons.print("location size" + sosBO.getLocations().size());
                    mCategoryForDialogSOSBO.add(sosBO);
                    mBrandNameList.add(sosBO.getProductName());
                }
            }
            mBrandNameListForDB.add(EXT_SHELF);
        }
    }

    /**
     * Handle every EditText's edits
     *
     * @author sathishkumar.m
     */
    private class CommonTextWatcher implements TextWatcher {

        private final EditText editText;

        public CommonTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (mTextWatcherEnabled) {
                String data = s.toString();
                if (editText.getId() == R.id.edtTxtBlock) {
                    if (!"".equals(data))
                        mBlockCount = Integer.parseInt(data);
                    else
                        mBlockCount = 0;

                    if (mBlockCount > mMaxBlockCount)
                        Toast.makeText(
                                getActivity(),
                                getResources().getString(
                                        R.string.shelf_max_block_count)
                                        + " " + mMaxBlockCount,
                                Toast.LENGTH_SHORT).show();

                } else if (editText.getId() == R.id.edtTxtShelf) {
                    if (!"".equals(data))
                        mShelfCount = Integer.parseInt(data);
                    else
                        mShelfCount = 0;

                    if (mShelfCount > mMaxShelfCount)
                        Toast.makeText(
                                getActivity(),
                                getResources().getString(
                                        R.string.shelf_max_shelf_count)
                                        + " " + mMaxShelfCount,
                                Toast.LENGTH_SHORT).show();

                } else if (editText.getId() == R.id.edtTxtShelfLength) {
                    if (!"".equals(data))
                        mShelfLength = data;
                    else
                        mShelfLength = "0";
                }
            }

            editText.setSelection(editText.length());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }


}