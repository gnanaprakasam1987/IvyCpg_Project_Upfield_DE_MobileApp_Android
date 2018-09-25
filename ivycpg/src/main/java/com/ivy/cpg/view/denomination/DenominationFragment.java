package com.ivy.cpg.view.denomination;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.reports.damageReturn.DamageReturenReportHelper;
import com.ivy.cpg.view.reports.damageReturn.PendingDeliveryBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
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
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

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

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, TextView> textViewHashMap = new HashMap<>();

    CompositeDisposable compositeDisposable;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed())
            compositeDisposable.clear();

    }

    @OnClick(R.id.denomination_save)
    public void saveOnclick() {
        saveCollectionReference();
    }

    ArrayList<DenominationBO> denominationInputValues = new ArrayList<>();

    private void downloadDenomintionData() {

        compositeDisposable.add((Disposable) DenominationHelper.getInstance().downloadDenomintionData(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<DenominationBO>>() {
                    @Override
                    public void onNext(ArrayList<DenominationBO> denominationBOS) {

                        denominationInputValues.addAll(denominationBOS);
                        for (int i = 0; i < denominationInputValues.size(); i++) {
                            createDynamicRowForDenominationValues(i, denominationInputValues.get(i));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));

    }

    private void init() {
        Bundle extras = getArguments();
        try {
            setScreenTitle(extras.getString("screentitle"));
        } catch (NullPointerException e) {
            setScreenTitle("Denomination");
            Commons.printException(e);
        }
        compositeDisposable = new CompositeDisposable();
        currencyTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        quentyTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        amountTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

        mScrollView.addView(getRootLinearLayout());
        downLoadTotalCashInHand();


    }

    private void downLoadTotalCashInHand() {

        compositeDisposable.add((Disposable) DenominationHelper.getInstance().downLoadTotalCashInHand(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        initialTotalAmount = s;
                        mTodayCollectionTextview.setText(initialTotalAmount);
                    }
                }));

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


    private void createDynamicRowForDenominationValues(final int mNumber, DenominationBO denominationBO) {

        View view;
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(R.layout.fragment_denomination_item_view, null);

        TextView dinominationTextview = (TextView) view.findViewById(R.id.dinomination_textview);
        EditText dinominationValues = (EditText) view.findViewById(R.id.dinomination_values_edittext);
        TextView dinominationAmount = (TextView) view.findViewById(R.id.dinomination_amount_textview);

        dinominationTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        dinominationValues.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));
        dinominationAmount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

        dinominationTextview.setText(denominationBO.getDenominationDisplayName() + " *");
        getRootLinearLayout().addView(view);

        editTextHashMap.put(mNumber, dinominationValues);
        textViewHashMap.put(mNumber, dinominationAmount);

        editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {

                double totalValues = 0;
                for (Integer key : editTextHashMap.keySet()) {
                    String values = getDynamicEditTextValues(key);
                    if (!AppUtils.isEmptyString(values)) {
                        double temp = Double.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());
                        totalValues = totalValues + (Double.valueOf(values) * temp);
                        if (totalValues <= Double.valueOf(initialTotalAmount)) {
                            double amount = Double.valueOf(values) * temp;
                            textViewHashMap.get(key).setText(String.valueOf(amount));
                        }
                    } else
                        textViewHashMap.get(key).setText("0");
                }

                if (totalValues <= Integer.valueOf(initialTotalAmount))
                    mTotalCollectionTextview.setText(getActivity().getResources().getString(R.string.total) + ":" + String.valueOf(totalValues));
                else
                    Toast.makeText(getActivity(), "" + getActivity().getResources().getString(R.string.denomination_error), Toast.LENGTH_SHORT).show();

            }
        });
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

        double totalValues = 0;

        for (Integer key : editTextHashMap.keySet()) {

            String count = getDynamicEditTextValues(key);

            if (!AppUtils.isEmptyString(count)) {

                double temp = Double.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());

                totalValues = totalValues + (Double.valueOf(count) * temp);
            }
        }

        if (totalValues != Double.valueOf(initialTotalAmount)) {
            Toast.makeText(getActivity(), ""+getActivity().getResources().getString(R.string.denomination_error), Toast.LENGTH_SHORT).show();
        } else {
            insertData();
        }

    }

    private void insertData() {
        ArrayList<DenominationBO> denomination = new ArrayList<>();
        for (Integer key : editTextHashMap.keySet()) {
            String count = getDynamicEditTextValues(key);
            if (!AppUtils.isEmptyString(count)) {
                DenominationBO denominationBO=new DenominationBO();
                denominationBO.setDenomintionId(denominationInputValues.get(key).getDenomintionId());
                denominationBO.setDenominationDisplayName(denominationInputValues.get(key).getDenominationDisplayName());
                denominationBO.setDenominationDisplayNameValues(denominationInputValues.get(key).getDenominationDisplayNameValues());
                denominationBO.setCount(count);
                denomination.add(denominationBO);
            }
        }
        compositeDisposable.add((Disposable) DenominationHelper.getInstance().insertDenomination(getActivity(),denomination,initialTotalAmount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean s) throws Exception {

                        if (s) {
                            for (Integer key : editTextHashMap.keySet()) {
                                editTextHashMap.get(key).setText("");
                                textViewHashMap.get(key).setText("0");
                            }
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.successfully_exported), Toast.LENGTH_SHORT).show();
                        }

                    }
                }));
    }

}
