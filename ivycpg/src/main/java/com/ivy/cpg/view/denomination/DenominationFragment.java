package com.ivy.cpg.view.denomination;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by murugan on 30/8/18.
 */

public class DenominationFragment extends IvyBaseFragment {


    @BindView(R.id.denomination_scrollview)
    ScrollView mScrollView;

    @BindView(R.id.today_collection_amount_textview)
    TextView mTodayCollectionTextview;

    @BindView(R.id.total_collection_amount_textview)
    TextView mTotalCollectionTextview;

    @BindView(R.id.currency_textview)
    TextView currencyTextview;

    @BindView(R.id.qty_textview)
    TextView quentyTextview;

    @BindView(R.id.amount_textview)
    TextView amountTextview;

    @BindView(R.id.denomination_save)
    Button mDenominationSave;


    private Unbinder unbinder;
    private LinearLayout mRootLinearLayout = null;

    private String initialTotalAmount = "0";


    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, EditText> editTextHashMap = new HashMap<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_denomination, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        downloadDenomintionData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.denomination_save)
    public void saveOnclick() {
        saveCollectionReference();
    }

    ArrayList<DenominationBO> denominationInputValues = new ArrayList<>();

    private void downloadDenomintionData() {
        DBUtil dbUtil = new DBUtil(getActivity(), DataMembers.DB_NAME, DataMembers.DB_PATH);
        dbUtil.openDataBase();
        Cursor cursor = dbUtil.selectSQL("Select * from DenominationMaster");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DenominationBO denominationBO = new DenominationBO();
                denominationBO.setDenomintionId(cursor.getString(0));
                denominationBO.setDenominationDisplayName(cursor.getString(1));
                denominationBO.setDenominationDisplayNameValues(cursor.getString(2));
                denominationInputValues.add(denominationBO);
            }
            cursor.close();
        }
        dbUtil.closeDB();
        for (int i = 0; i < denominationInputValues.size(); i++) {
            createDynamicRowForDenominationValues(i, denominationInputValues.get(i));
        }
    }

    private void init() {
        Bundle extras = getArguments();
        try {
            if (extras != null) {
                setScreenTitle(extras.getString("screentitle"));
            }
        } catch (Exception e) {
            setScreenTitle("Denomination");
            Commons.printException(e);
        }

        currencyTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        quentyTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        amountTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

        mScrollView.addView(getRootLinearLayout());
        downLoadTotalCashInHand();


    }




    private void downLoadTotalCashInHand() {

        DBUtil dbUtil = new DBUtil(getActivity(), DataMembers.DB_NAME, DataMembers.DB_PATH);
        dbUtil.openDataBase();
        Cursor cursor = dbUtil.selectSQL("SELECT ifnull(sum(amount),0) FROM Payment pt inner join StandardListMaster sd on sd.ListId = pt.CashMode where sd.ListCode = 'CA'");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                initialTotalAmount = cursor.getString(0);
            }
            cursor.close();
        }
        dbUtil.closeDB();
        mTodayCollectionTextview.setText(initialTotalAmount);
    }


    private LinearLayout getRootLinearLayout() {
        if (mRootLinearLayout == null) {
            mRootLinearLayout = new LinearLayout(getActivity());
            mRootLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mRootLinearLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.background_noise));
            return mRootLinearLayout;
        }
        return mRootLinearLayout;
    }

     TextView dinominationAmount;

    private void createDynamicRowForDenominationValues(final int mNumber, DenominationBO denominationBO) {

        View view;
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(R.layout.fragment_denomination_item_view, null);

        TextView dinominationTextview = (TextView) view.findViewById(R.id.dinomination_textview);
        EditText dinominationValues = (EditText) view.findViewById(R.id.dinomination_values_edittext);
        dinominationAmount = (TextView) view.findViewById(R.id.dinomination_amount_edittext);
        dinominationTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        dinominationValues.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        dinominationAmount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

        dinominationTextview.setText(denominationBO.getDenominationDisplayName() + " *");
        dinominationAmount.setTag(mNumber);
        getRootLinearLayout().addView(view);

        editTextHashMap.put(mNumber, dinominationValues);

        editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable et) {

                String s = et.toString();

                int totalValues = 0;

                for (Integer key : editTextHashMap.keySet()) {

                    String values = getDynamicEditTextValues(key);

                    if (!isEmptyString(values)) {

                        int temp = Integer.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());

                        totalValues = totalValues + (Integer.valueOf(values) * temp);

                        if (totalValues <= Integer.valueOf(initialTotalAmount))
                            if (key == dinominationAmount.getTag()) {
                                int amount = Integer.valueOf(values) * temp;
                                dinominationAmount.setText(String.valueOf(amount));
                            }
                    } else if (key == dinominationAmount.getTag()){
                        dinominationAmount.setText("0");
                    }
                }
                if (totalValues <= Integer.valueOf(initialTotalAmount))
                    mTotalCollectionTextview.setText("Total :" + String.valueOf(totalValues));
                 else
                    Toast.makeText(getActivity(), "Total amount not match with you denomination count amount", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }


    private String getDynamicEditTextValues(int mNumber) {
        EditText value = editTextHashMap.get(mNumber);
        if (value != null) {
            return value.getText().toString().trim();
        } else {
            if (editTextHashMap.containsKey(mNumber)) {
                // Okay, there's a key but the value is null
            } else {
                // Definitely no such key
            }
            return "";
        }

    }


    private void saveCollectionReference() {

        int totalValues = 0;

        for (Integer key : editTextHashMap.keySet()) {

            String count = getDynamicEditTextValues(key);

            if (!isEmptyString(count)) {

                int temp = Integer.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());

                totalValues = totalValues + (Integer.valueOf(count) * temp);
            }
        }

        if (totalValues != Integer.valueOf(initialTotalAmount)){
            Toast.makeText(getActivity(), "Total amount not match with you denomination count amount", Toast.LENGTH_SHORT).show();
        }else {
            insertData();
        }

    }

    private void insertData(){
         try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.createDataBase();

            db.openDataBase();

            for (Integer key : editTextHashMap.keySet()) {

                String count = getDynamicEditTextValues(key);

                if (!isEmptyString(count)) {

                    String id=denominationInputValues.get(key).getDenomintionId();

                    String columns = "Uid,Total";

                    db.deleteSQL("DenominationDetails", "Uid=" + AppUtils.QT(id), false);

                    String values = id + "," + AppUtils.QT(count);

                    db.insertSQL("DenominationDetails", columns, values);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("DenominationDetails insert Error" + e);
        }
    }
}
