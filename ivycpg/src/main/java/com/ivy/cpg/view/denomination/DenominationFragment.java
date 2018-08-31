package com.ivy.cpg.view.denomination;


import android.annotation.SuppressLint;
import android.content.Context;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
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
        demo();
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

    ArrayList<Integer> denominationInputValues = new ArrayList<>();

    private void demo() {

        denominationInputValues.add(2000);
        denominationInputValues.add(500);
        denominationInputValues.add(200);
        denominationInputValues.add(100);
        denominationInputValues.add(50);
        denominationInputValues.add(20);
        denominationInputValues.add(10);
        denominationInputValues.add(5);
        denominationInputValues.add(2);
        denominationInputValues.add(1);

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

    private void createDynamicRowForDenominationValues(final int mNumber, final int inputDenomintionText) {

        View view;
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(R.layout.fragment_denomination_item_view, null);

        TextView dinominationTextview = (TextView) view.findViewById(R.id.dinomination_textview);
        EditText dinominationValues = (EditText) view.findViewById(R.id.dinomination_values_edittext);
        final TextView dinominationAmount = (TextView) view.findViewById(R.id.dinomination_amount_edittext);
        dinominationTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        dinominationValues.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        dinominationAmount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

        dinominationTextview.setText(String.valueOf(inputDenomintionText) + " *");
        dinominationAmount.setTag(mNumber);
        getRootLinearLayout().addView(view);

        editTextHashMap.put(mNumber, dinominationValues);

        editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {

                String s = et.toString();

                int totalValues = 0;

                for (Integer key : editTextHashMap.keySet()) {

                    String values = getDynamicEditTextValues(key);

                    if (!isEmptyString(values)) {

                        int temp = denominationInputValues.get(key);
                        totalValues = totalValues + (Integer.valueOf(values) * temp);

                        if (key == dinominationAmount.getTag()) {
                            int amount = Integer.valueOf(values) * temp;
                            dinominationAmount.setText(String.valueOf(amount));
                        }
                    } else
                        if (key == dinominationAmount.getTag()) {
                            dinominationAmount.setText("0");
                        }
                }

                mTotalCollectionTextview.setText("Total :" + String.valueOf(totalValues));
            }
        });
    }

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }

    public String getDynamicEditTextValues(int mNumber) {
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
}
