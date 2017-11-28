package com.ivyretail.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.Logs;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.ShelfShareBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ShelfShareCallBackListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.SalesFundamentalHelper;
import com.ivy.sd.png.provider.ShelfShareHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SODDialogFragment extends DialogFragment {

    private Button mBtnDone;
    private Button mBtnCreate;
    private Button mBtnCancel;
    private RecyclerView mListBrands;
    private TextView mTxtBrandName;
    private EditText mEdtTxtBlock;
    private EditText mEdtTxtShelf;
    private EditText mEdtTxtShelfLength;
    private EditText mSelectedET;
    private HorizontalScrollView mHrSrollShelfWrapper;
    private RelativeLayout mRelLytRootLayout;
    private Toolbar toolbar;


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
    private int extracount = 0;

    private int mParentID;
    private int mParentTypeID;
    private String mSelectedBrandName = null;

    // Global Variables
    private BusinessModel bmodel;
    /**
     * mKey = "mParentID-mParentTypeID"; to identify which category's dialog
     * going to show.
     */
    private String mKey;
    private static final String mEmpty = "empty";

    private int mModuleFlag;
    private boolean mTextWatcherEnabled = true;
    /**
     * Contains list of all products in selected category.
     */
    private ArrayList<String> mBrandNameList;
    /**
     * Used when constructing shelfs using database data. Contains list of non
     * zero count products.
     */
    private ArrayList<String> mBrandNameListForDB;

    /**
     * Contains selected categories product's details as SODBO for SOD Module
     */
    private List<SODBO> mCategoryForDialogSODBO = null;

    private ShelfShareCallBackListener callBackListener;
    com.ivy.sd.png.view.BrandsAdapter mBrandsAdapter;
    /**
     * Limit the BlockCount and ShelfCount While Creating the Shelf
     */
    private final int mMaxBlockCount = 10;
    private final int mMaxShelfCount = 15;

    /**
     * Key is product name. Returns HashMap with colour for a product and total
     * selection count of product.
     */
    private HashMap<String, HashMap<String, Object>> mBrandNameColorCount;
    private static final String TAG = "SODDialogFragment";
    /**
     * This variable contains single shelf's detail. Colour of shelf at
     * particular position. HashMap Key is position of that shelf.
     */
    public static final HashMap<String, ShelfShareBO> mBrandsDetailsHashMap = new HashMap<>();

    /**
     * Key is "mParentID-mParentTypeID" to identify which category's dialog
     * going to show. Returns HashMap with
     * BLOCK_COUNT,SHELF_COUNT,SHELF_LENGTH,EXTRA_LENGTH
     * ,SHARE,LOADING_FROM_DB,BRAND_DETAIL_HASHMAP these details. For SOD
     * Module.
     */
    private HashMap<String, HashMap<String, Object>> mShelfDetailForSOD = null;
    private static final String COLOR = "color";
    private static final String COUNT = "count";
    SalesFundamentalHelper mSFHelper;

    public SODDialogFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bmodel = (BusinessModel) activity.getApplicationContext();
        bmodel.setContext(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mParentID = bundle.getInt("parent_id");
            this.mParentTypeID = bundle.getInt("parent_type_id");
            this.mModuleFlag = bundle.getInt("flag");
            this.mSelectedLocationIndex = bundle.getInt("selectedlocation");
            mKey = Integer.toString(mParentID);
        }
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logs.debug("ShelfShareDialogFragment", "Cycle: onCreateView");
        View view = inflater.inflate(R.layout.dialog_fragment_shelf_share,
                container, false);

        isCreateClicked = false;

        mRelLytRootLayout = (RelativeLayout) view
                .findViewById(R.id.fragment_shelf_share);
        mEdtTxtBlock = (EditText) view.findViewById(R.id.edtTxtBlock);
        mEdtTxtShelf = (EditText) view.findViewById(R.id.edtTxtShelf);
        mEdtTxtShelfLength = (EditText) view
                .findViewById(R.id.edtTxtShelfLength);
        mTxtBrandName = (TextView) view.findViewById(R.id.txtBrandName);
        mHrSrollShelfWrapper = (HorizontalScrollView) view
                .findViewById(R.id.hrScrollShelfWrapper);
        mListBrands = (RecyclerView) view.findViewById(R.id.listBrands);
        mBtnDone = (Button) view.findViewById(R.id.btnDoneShelfShare);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancelShelfShare);
        mBtnCreate = (Button) view.findViewById(R.id.btnCreateShelfShare);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((TextView) view.findViewById(R.id.tv_toolbar_title)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            toolbar.setTitle(R.string.create_shelf);
        }

        mSelectedET = null;
        //setTypefaces
        ((TextView) view.findViewById(R.id.tvEnterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvNorows)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvNoColumns)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvBlockLength)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvChooseBlocks)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mTxtBrandName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mEdtTxtBlock.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdtTxtShelf.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdtTxtShelfLength.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mBtnCancel.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mBtnCreate.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mBtnDone.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mEdtTxtBlock.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSelectedET = mEdtTxtBlock;
                int inType = mEdtTxtBlock.getInputType();
                mEdtTxtBlock.setInputType(InputType.TYPE_NULL);
                mEdtTxtBlock.onTouchEvent(event);
                mEdtTxtBlock.setInputType(inType);
                return true;
            }
        });

        mEdtTxtShelf.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSelectedET = mEdtTxtShelf;
                int inType = mEdtTxtShelf.getInputType();
                mEdtTxtShelf.setInputType(InputType.TYPE_NULL);
                mEdtTxtShelf.onTouchEvent(event);
                mEdtTxtShelf.setInputType(inType);
                return true;
            }
        });

        mEdtTxtShelfLength.setOnTouchListener(new View.OnTouchListener() {
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

        setNumberPadlistener(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        Logs.debug("ShelfShareDialogFragment", "Cycle: onActivityCreated");

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initBrandNameList();
        createBrandColor();
        preloadShelfs();
        mBrandsAdapter = new com.ivy.sd.png.view.BrandsAdapter(getActivity(), mBrandNameList, mBrandNameColorCount, mSelectedBrandName, new com.ivy.sd.png.view.BrandsAdapter.OnItemClickListener() {
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
        Logs.debug("ShelfShareDialogFragment", "Cycle: onResume");
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        Logs.debug("ShelfShareDialogFragment", "Cycle: onStart");
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logs.debug("ShelfShareDialogFragment", "Cycle: onPause");
    }

    public void setOnShelfShareListener(
            ShelfShareCallBackListener callBackListener) {
        this.callBackListener = callBackListener;
    }

    private class BrandsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mBrandNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_item_brands,
                        parent, false);
            }
            TextView text = (TextView) row
                    .findViewById(R.id.txtBrandNameListItem);
            text.setText(mBrandNameList.get(position));
            text.setBackgroundColor((Integer) mBrandNameColorCount.get(
                    mBrandNameList.get(position)).get(COLOR));
            return row;
        }
    }

    /**
     * Handle every EditText's edits
     *
     * @author sathishkumar.m
     */
    private class CommonTextWatcher implements TextWatcher {

        private final EditText editText;
        private final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        public CommonTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable s) {

            Commons.print("ShelfShareDialogFragment," + "inputMethodManager.isActive(): "
                    + inputMethodManager.isActive(editText));

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

    private void setNumberPadlistener(View view) {
        view.findViewById(R.id.calczero)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcone)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calctwo)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcthree)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcfour)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcfive)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcsix)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcseven)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calceight)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcnine)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdel)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdot)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdot).setVisibility(View.GONE);
    }

    private final View.OnClickListener mNumperPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero || i == R.id.calcone || i == R.id.calctwo || i == R.id.calcthree || i == R.id.calcfour || i == R.id.calcfive || i == R.id.calcsix || i == R.id.calcseven || i == R.id.calceight || i == R.id.calcnine) {
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
                    if (!".".contains(s1)) {
                        String strS1 = s1 + ".";
                        mSelectedET.setText(strS1);
                    }

                }
            }

        }
    };

    private void eff(String val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {

            String s = mSelectedET.getText().toString();

            if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s))
                mSelectedET.setText(val);
            else
                mSelectedET.setText(mSelectedET.getText().append(val));
        }
    }

    /**
     * It initialise product names depends on selected module and selected
     * category. At first time it uses data from database to build shelfs.
     * Second time it give actual structure which have build previously. Loads
     * depends on modules.
     */
    private void initBrandNameList() {
        int locid;
        Logs.debug("ShelfShareDialogFragment", "initBrandNameList Method");

        mBrandNameList = new ArrayList<>();
        mBrandNameListForDB = new ArrayList<>();

        if (ShelfShareHelper.SOD == mModuleFlag) {
            initBrandNameListFromSOD();
            if (bmodel.mShelfShareHelper.getLocations().get(mSelectedLocationIndex).getShelfDetailForSOD() != null)
                mShelfDetailForSOD = bmodel.mShelfShareHelper.getLocations().get(mSelectedLocationIndex)
                        .getShelfDetailForSOD();
            if (mShelfDetailForSOD != null
                    && mShelfDetailForSOD.containsKey(String.valueOf(mKey))) {
                locid = bmodel.mShelfShareHelper.getLocations().get(mSelectedLocationIndex).getLocationId();
                if (!(Boolean) mShelfDetailForSOD.get(String.valueOf(mKey))
                        .get(ShelfShareHelper.LOADING_FROM_DB))
                    loadEntireShalfDetail(mShelfDetailForSOD.get(String
                            .valueOf(mKey)));
                else
                    buildShelfsWithDBData(mShelfDetailForSOD.get(String
                            .valueOf(mKey)), locid);
            }
        }
    }

    private void initBrandNameListFromSOD() {

        ArrayList<SODBO> mSodList = mSFHelper.getmSODList();

        mCategoryForDialogSODBO = new ArrayList<>();

        mCategoryForDialogSODBO.clear();

        // All Brands in Total PopUp
        if (mSodList != null) {
            SODBO sod = new SODBO();
            sod.setProductName("Ext.Shelf");
            sod.setProductID(0);
            sod.setLocations(ProductHelper.cloneLocationList(bmodel.productHelper.locations));
            mCategoryForDialogSODBO.add(sod);
            mBrandNameList.add(sod.getProductName());
            for (SODBO sodBO : mSodList) {
                if (sodBO.getParentID() == mParentID) {
                    mCategoryForDialogSODBO.add(sodBO);
                    mBrandNameList.add(sodBO.getProductName());
                    if (SDUtil.convertToFloat(sodBO.getActual() + "") > 0) {
                        mBrandNameListForDB.add(sodBO.getProductName());
                    }
                }
            }
            mBrandNameListForDB.add("Ext.Shelf");
        }
    }

    /**
     * Loads already constructed shelfs detail from global variable.
     * BloackCount,ShelfCount,ShelfLength,Share,BrandDetailsHashMap
     *
     * @param hashMap
     */
    @SuppressWarnings("unchecked")
    private void loadEntireShalfDetail(HashMap<String, Object> hashMap) {
        Logs.debug("ShelfShareDialogFragment", "loadEntireShalfDetail Method");
        mBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mInitialBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mInitialShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mInitialShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mExtraShelfCount = (Integer) hashMap.get(ShelfShareHelper.EXTRA_LENGTH);
        mShare = (Integer) hashMap.get(ShelfShareHelper.SHARE);
        mBrandsDetailsHashMap.putAll((HashMap<String, ShelfShareBO>) hashMap
                .get(ShelfShareHelper.BRAND_DETAIL_HASHMAP));
    }


    /**
     * Constructing shelfs using BloackCount,ShelfCount,ShelfLength and Share
     * using these details.
     *
     * @param hashMap
     */
    private void buildShelfsWithDBData(HashMap<String, Object> hashMap, int locid) {
        Logs.debug("ShelfShareDialogFragment", "buildShelfsWithDBData");
        mShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mInitialBlockCount = (Integer) hashMap.get(ShelfShareHelper.BLOCK_COUNT);
        mInitialShelfCount = (Integer) hashMap.get(ShelfShareHelper.SHELF_COUNT);
        mInitialShelfLength = (String) hashMap.get(ShelfShareHelper.SHELF_LENGTH);

        mExtraShelfCount = (Integer) hashMap.get(ShelfShareHelper.EXTRA_LENGTH);
        int totalShelfs = mShelfCount * mBlockCount;

        if (mShelfCount == 0 && !mBrandNameListForDB.isEmpty())
            return;

        mSFHelper.loadSODBlockDetails(DataMembers.uidSOD,
                String.valueOf(mKey), totalShelfs, locid);

    }

    /**
     * Creates colour for every products.
     */
    private void createBrandColor() {

        Logs.debug("ShelfShareDialogFragment", "createBrandColor Method");

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
            hashMap.put(COLOR, color);
            hashMap.put(COUNT, 0);
            mBrandNameColorCount.put(mBrandNameList.get(i), hashMap);
        }

        int color = ContextCompat.getColor(getActivity(), R.color.competitor_brand);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(COLOR, color);
        hashMap.put(COUNT, 0);
        mBrandNameColorCount.put(mEmpty, hashMap);

        Commons.print("ShelfShareDialogFragment in createBrandColor," +
                "Brand Name and Color: " + mBrandNameColorCount.toString());
    }

    /**
     * It works when BrandDetails not being empty.
     */
    private void preloadShelfs() {
        if (!mBrandsDetailsHashMap.isEmpty()) {
            Logs.debug("ShelfShareDialogFragment", "preloadShelfs");
            mTextWatcherEnabled = false;
            mEdtTxtBlock.setText(String.valueOf(mBlockCount));
            mEdtTxtShelf.setText(String.valueOf(mShelfCount));
            mEdtTxtShelfLength.setText(String.valueOf(mShelfLength));
            populateGridItems(true);
            mTextWatcherEnabled = true;
        }
    }

    /**
     * It forms grid depends on user input.
     *
     * @param withExistingData
     */
    private void populateGridItems(boolean withExistingData) {
        Commons.print("ShelfShareDialogFragment," + "ShelfCount: " + mShelfCount
                + " BlockCount: " + mBlockCount);
        createBrandColor();
        if (mBrandNameColorCount != null) {
            if (!withExistingData)
                mBrandsDetailsHashMap.clear();

            HashMap<String, Object> hashMap = mBrandNameColorCount.get(mEmpty);
            hashMap.put(COUNT,
                    (mShelfCount * mBlockCount - mExtraShelfCount) * 4);
            mBrandNameColorCount.put(mEmpty, hashMap);
            populateShelfs(withExistingData);
        }
    }

    /**
     * Shelf get builded depends on user input.
     *
     * @param withExistingData
     */
    private void populateShelfs(boolean withExistingData) {
        int counter = 0;
        extracount = 0;
        final Vector<Integer> extrapos = new Vector<>();
        mHrSrollShelfWrapper.removeAllViews();
        TableLayout tableLayout = new TableLayout(getActivity());
        LinearLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(tableLayoutParams);
        final ViewGroup parent = null;
        for (int i = 0; i < mShelfCount; i++) {

            TableRow tableRow = new TableRow(getActivity());
            LinearLayout.LayoutParams tableRowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, 60);
            tableRowParams.setMargins(0, 4, 0, 4);
            tableRow.setLayoutParams(tableRowParams);
            tableRow.setOrientation(TableRow.HORIZONTAL);

            for (int j = 0; j < mBlockCount; j++) {
                LayoutInflater inflater = getActivity()
                        .getLayoutInflater();
                View convertView = inflater.inflate(R.layout.grid_item_shelfs,
                        parent, false);
                convertView.setTag(counter);

                convertView.setOnLongClickListener(new View.OnLongClickListener() {

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

                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Commons.print("ShelfShareFragment," + "OnClickListener");
                        if (mSelectedBrandColor != 0
                                && mSelectedBrandName != null) {
                            if (mSelectedBrandName.equalsIgnoreCase("Ext.Shelf")) {

                                setShelfColor(view, (Integer) view.getTag());
                                extracount++;
                                extrapos.add((Integer) view.getTag());
                            } else {
                                if (extrapos.contains(Integer.valueOf((Integer) view.getTag()))) {
                                    setShelfColor(view, (Integer) view.getTag());
                                    extracount = 0;

                                } else
                                    setShelfColor(view, (Integer) view.getTag());
                            }
                            mExtraShelfCount = extracount;
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
                invalidateShelfs((Integer) convertView.getTag(), convertView,
                        withExistingData);
                tableRow.addView(convertView);
                Commons.print("ShelfShareFragment," +
                        "View Pos:" + convertView.getTag());
                counter++;
            }
            tableLayout.addView(tableRow);
        }


        mHrSrollShelfWrapper.addView(tableLayout);
    }

    /**
     * To remove ExtraShelfs
     *
     * @param position
     * @param view
     * @param withExistingData
     */
    private void invalidateShelfs(int position, View view,
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
     * Used for setting colour for entire shelf. To be called when need to empty
     * single shelf or fill entire shelf with single product.
     *
     * @param view
     * @param color
     */
    private void setCellColor(View view, int color) {
        Logs.debug("ShelfShareFragment", "setCellColor");
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
     * to maintain count for every product.
     *
     * @param previousShelf
     * @param currentShelf
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
        if (previousShelf.getFourthCell().equals(currentShelf.getFourthCell())) {
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
        Commons.print("ShelfShareDialogFragment," + "BrandNameColorCount: "
                + mBrandNameColorCount.toString());
    }

    /**
     * Need to maintain every alter of shelf in mBrandsDetailsHashMap
     *
     * @param positionKey
     * @param first
     * @param second
     * @param third
     * @param fourth
     * @param othersCount
     * @param isInitShelf
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
     * Setting values in ShelfShareBO and setting color.
     *
     * @param view
     * @param position
     */
    private void setShelfColor(View view, int position) {
        Logs.debug("ShelfShareFragment", "setCellColor");
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
     * Shows dialog to split shelf into 4 part.
     *
     * @param gridCellView
     * @param positionKey
     */
    private void showDialog(final View gridCellView, final String positionKey) {

        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar);
        LayoutInflater inflater = getActivity()
                .getLayoutInflater();
        final ViewGroup parent = null;
        final View dialogView = inflater.inflate(R.layout.dialog_shelf_share,
                parent, false);

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

        firstView.setOnClickListener(new View.OnClickListener() {

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

        secondView.setOnClickListener(new View.OnClickListener() {

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

        thirdView.setOnClickListener(new View.OnClickListener() {

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

        fourthView.setOnClickListener(new View.OnClickListener() {

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
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setCellColor(gridCellView, positionKey);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    /**
     * Used to select colour while clicking on shelf. Whether selected brand
     * colour or empty.
     *
     * @param brandName
     * @return
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
     * @param view
     * @param positionKey
     */
    private void setCellColor(View view, String positionKey) {
        Logs.debug("ShelfShareFragment", "setCellColor");
        int color;

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getFirstCell()).get(
                COLOR);
        view.findViewById(R.id.relLytFirst)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytFirst)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getFirstCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getSecondCell()).get(
                COLOR);
        view.findViewById(R.id.relLytSecond)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytSecond)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getSecondCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getThirdCell()).get(
                COLOR);
        view.findViewById(R.id.relLytThird)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytThird)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getThirdCell());

        color = (Integer) mBrandNameColorCount.get(
                mBrandsDetailsHashMap.get(positionKey).getFourthCell()).get(
                COLOR);
        view.findViewById(R.id.relLytFourth)
                .setBackgroundColor(color);
        view.findViewById(R.id.relLytFourth)
                .setTag(mBrandsDetailsHashMap.get(positionKey).getFourthCell());
    }

    private final View.OnClickListener mCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callBackListener.handleDialogClose();
        }
    };
    private final View.OnClickListener mEditDoneListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (ShelfShareHelper.SOD == mModuleFlag) {
                if (!isCreateClicked) {
                    mBlockCount = mInitialBlockCount;
                    mShelfCount = mInitialShelfCount;
                    mShelfLength = mInitialShelfLength;
                }
                if (!mBrandsDetailsHashMap.isEmpty()) {
                    buildResultForSOD();
                    bmodel.mShelfShareHelper.getLocations().get(mSelectedLocationIndex).setShelfDetailForSOD(
                            String.valueOf(mKey), getEntireShalfDetail());
                    Commons.print("block size" + mBrandsDetailsHashMap.size() + " mKey" + mKey);
                    bmodel.mShelfShareHelper.getLocations().get(mSelectedLocationIndex).setmShelfBlockDetailForSOD(
                            String.valueOf(mKey), mBrandsDetailsHashMap);
                }
                callBackListener.SODDOCallBackListener(mCategoryForDialogSODBO);

            }
        }
    };

    /*
    * Returns currently edited shelfs detail
    */
    private HashMap<String, Object> getEntireShalfDetail() {
        Logs.debug("ShelfShareDialogFragment", "getEntireShalfDetail Method");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(ShelfShareHelper.BLOCK_COUNT, mBlockCount);
        hashMap.put(ShelfShareHelper.SHELF_COUNT, mShelfCount);
        hashMap.put(ShelfShareHelper.SHELF_LENGTH, mShelfLength);
        hashMap.put(ShelfShareHelper.EXTRA_LENGTH, mExtraShelfCount);
        hashMap.put(ShelfShareHelper.SHARE, mShare);
        hashMap.put(ShelfShareHelper.LOADING_FROM_DB, false);
        hashMap.put(ShelfShareHelper.BRAND_DETAIL_HASHMAP,
                mBrandsDetailsHashMap);
        return hashMap;
    }

    private void buildResultForSOD() {

        if (!mCategoryForDialogSODBO.isEmpty()) {

            //get extra shelf and reduce that value from total
            float mExtActual = (Integer) mBrandNameColorCount.get("Ext.Shelf").get(COUNT);

            // IF All Brands in Total PopUp
            for (int i = 0; i < mCategoryForDialogSODBO.size(); i++) {
                SODBO sodbo = mCategoryForDialogSODBO.get(i);

                float actual = (Integer) mBrandNameColorCount.get(
                        sodbo.getProductName()).get(COUNT);
                Commons.print("productname count" + mBrandNameColorCount.get(
                        sodbo.getProductName()).get(COUNT) + " mShelfLength" + mShelfLength);
                float tot = (mShelfCount * mBlockCount * 4) - mExtActual;

                actual = (float) (actual * (SDUtil.convertToFloat(mShelfLength) / 4.0));
                tot = (float) (tot * (SDUtil.convertToFloat(mShelfLength) / 4.0));
                Commons.print("Actual: " + actual + " Tot:" + tot
                        + " Single Shelf: "
                        + SDUtil.convertToFloat(mShelfLength) / 4.0);

                sodbo.getLocations().get(mSelectedLocationIndex).setActual(Float.toString(actual));

                sodbo.getLocations().get(mSelectedLocationIndex).setParentTotal(Float.toString(tot));

                // Gap Calculation

                sodbo.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(0, 1));

                if (SDUtil.convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex).getParentTotal() + "") > 0) {

                    float mParentTotal = SDUtil.convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex)
                            .getParentTotal() + "");
                    float mNorm = sodbo.getNorm();
                    float actual1 = SDUtil.convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex).getActual() + "");

                    float target = (mParentTotal * mNorm) / 100;
                    float gap = target - actual1;
                    float percentage = 0;
                    if (mParentTotal > 0)
                        percentage = (actual1 / mParentTotal) * 100;

                    sodbo.getLocations().get(mSelectedLocationIndex).setTarget(SDUtil.roundIt(target, 2));
                    sodbo.getLocations().get(mSelectedLocationIndex).setPercentage(bmodel.formatPercent(percentage));
                    sodbo.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(-gap, 2));
                } else {
                    sodbo.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                    sodbo.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                    sodbo.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
                }
            }

        }

    }

    private final View.OnClickListener mShelfCreateListener = new View.OnClickListener() {

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

    /**
     * Dialog to alert user while try to alter shelf count after filling product
     * detail
     */
    private void showDataClearDialog() {

        boolean flag = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getBoolean("do_not_show_clear_data_dialog",
                false);
        int count = mShelfCount * mBlockCount * 4;
        if (!flag) {
            final ViewGroup parent = null;
            if (!mBrandsDetailsHashMap.isEmpty()
                    && count != (Integer) mBrandNameColorCount.get(mEmpty).get(
                    COUNT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.check_box_do_not_show,
                        parent, false);
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
                                                    "do_not_show_clear_data_dialog",
                                                    true).commit();
                                else
                                    PreferenceManager
                                            .getDefaultSharedPreferences(
                                                    getActivity())
                                            .edit()
                                            .putBoolean(
                                                    "do_not_show_clear_data_dialog",
                                                    false).apply();
                                populateGridItems(false);
                                isCreateClicked = true;
                                dialog.dismiss();
                            }
                        });
                bmodel.applyAlertDialogTheme(builder);
            } else {
                populateGridItems(false);
                isCreateClicked = true;
            }
        } else {
            populateGridItems(false);
            isCreateClicked = true;
        }
    }

    private final View.OnClickListener mScreenShotListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            View v1 = mRelLytRootLayout.getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            OutputStream fout;
            File rootFile = new File(Environment.getExternalStorageDirectory()
                    + "/IVY");
            boolean isCreated = rootFile.mkdirs();
            if (isCreated) {
                File imageFile = new File(rootFile, "screenshot_" + mParentID + "_"
                        + mParentTypeID + ".jpg");

                boolean isDeleted = true;
                if (imageFile.exists())
                    isDeleted = imageFile.delete();

                if (isDeleted) {
                    Logs.debug("ShelfShareDialogFragment", "Screenshot image path: "
                            + imageFile.getAbsolutePath());
                    try {
                        fout = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                        fout.flush();
                        fout.close();
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.saved_successfully),
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Commons.printException("" + e);
                    }
                }
            }
        }
    };

}
