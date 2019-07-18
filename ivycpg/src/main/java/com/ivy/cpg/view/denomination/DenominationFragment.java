package com.ivy.cpg.view.denomination;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
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
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
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

    private CompositeDisposable compositeDisposable;
    private AppSchedulerProvider appSchedulerProvider;

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
    ArrayList<DenominationUpdateBO> denominationUpdateBO=new ArrayList<>();

    private void downloadDenomintionData() {


        compositeDisposable.add(Observable.zip(
                DenominationHelper.getInstance().downloadDenomintionData(getActivity()),
                DenominationHelper.getInstance().downloadDenomintionSavedData(getActivity()),
                new BiFunction<ArrayList<DenominationBO>, ArrayList<DenominationUpdateBO>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<DenominationBO> denominationBOS,
                                         ArrayList<DenominationUpdateBO> denominationUpdate) throws Exception {
                        denominationInputValues.addAll(denominationBOS);
                        denominationUpdateBO.addAll(denominationUpdate);
                        return true;
                    }
                })
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        for (int i = 0; i < denominationInputValues.size(); i++) {
                            createDynamicRowForDenominationValues(i, denominationInputValues.get(i));
                        }
                    }
                })
        );

    }

    private void init() {
        Bundle extras = getArguments();
        try {
            setScreenTitle(extras.getString("screentitle"));
        } catch (NullPointerException e) {
            setScreenTitle("Denomination");
            Commons.printException(e);
        }
        appSchedulerProvider = new AppSchedulerProvider();
        compositeDisposable = new CompositeDisposable();
        currencyTextview.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        quentyTextview.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        amountTextview.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        mScrollView.addView(getRootLinearLayout());
        downLoadTotalCashInHand();

    }

    private void downLoadTotalCashInHand() {

        compositeDisposable.add((Disposable) DenominationHelper.getInstance().downLoadTotalCashInHand(getActivity())
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        initialTotalAmount = s;
                        mTodayCollectionTextview.setText(initialTotalAmount);
                    }
                }));

    }

    double totalValues = 0;
    private void createDynamicRowForDenominationValues(final int mNumber, DenominationBO denominationBO) {


        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        View view = inflater.inflate(R.layout.fragment_denomination_item_view, null);

        TextView dinominationTextview = view.findViewById(R.id.dinomination_textview);
        EditText dinominationValues = view.findViewById(R.id.dinomination_values_edittext);
        TextView dinominationAmount = view.findViewById(R.id.dinomination_amount_textview);

        dinominationTextview.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        dinominationValues.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        dinominationAmount.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        dinominationTextview.setText(denominationBO.getDenominationDisplayName() + " *");



        getRootLinearLayout().addView(view);

        editTextHashMap.put(mNumber, dinominationValues);
        textViewHashMap.put(mNumber, dinominationAmount);

        if(denominationUpdateBO.size()>0){
            for (int i = 0; i < denominationUpdateBO.size(); i++) {
                String value=denominationUpdateBO.get(i).getValue();
                String count=denominationUpdateBO.get(i).getCount();
                if(denominationBO.getDenominationDisplayNameValues().equalsIgnoreCase(value)){
                    dinominationValues.setText(count);
                    double lineAmount = Double.valueOf(denominationBO.getDenominationDisplayNameValues()) * Double.valueOf(count);
                    dinominationAmount.setText(String.valueOf(lineAmount));

                    totalValues = 0;
                    for (Integer key : editTextHashMap.keySet()) {
                        String values = getDynamicEditTextValues(key);
                        if (!StringUtils.isNullOrEmpty(values)) {
                            double temp = Double.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());
                            totalValues = totalValues + (Double.valueOf(values) * temp);
                            if (totalValues <= Double.valueOf(initialTotalAmount)) {
                                double amount = Double.valueOf(values) * temp;
                                textViewHashMap.get(key).setText(String.valueOf(amount));
                            }
                        } else
                            textViewHashMap.get(key).setText("0");
                    }

                    if (totalValues <= Double.valueOf(initialTotalAmount))
                        mTotalCollectionTextview.setText(getActivity().getResources().getString(R.string.total) + ":" + String.valueOf(totalValues));
                }
            }
        }


        editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {

                totalValues = 0;
                for (Integer key : editTextHashMap.keySet()) {
                    String values = getDynamicEditTextValues(key);
                    if (!StringUtils.isNullOrEmpty(values)) {
                        double temp = Double.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());
                        totalValues = totalValues + (Double.valueOf(values) * temp);
                        if (totalValues <= Double.valueOf(initialTotalAmount)) {
                            double amount = Double.valueOf(values) * temp;
                            textViewHashMap.get(key).setText(String.valueOf(amount));
                        }
                    } else
                        textViewHashMap.get(key).setText("0");
                }

                if (totalValues <= Double.valueOf(initialTotalAmount))
                    mTotalCollectionTextview.setText(getActivity().getResources().getString(R.string.total) + ":" + String.valueOf(totalValues));
                else
                    Toast.makeText(getActivity(), "" + getActivity().getResources().getString(R.string.denomination_error), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void saveCollectionReference() {

        double totalValues = 0;

        for (Integer key : editTextHashMap.keySet()) {

            String count = getDynamicEditTextValues(key);

            if (!StringUtils.isNullOrEmpty(count)) {

                double temp = Double.valueOf(denominationInputValues.get(key).getDenominationDisplayNameValues());

                totalValues = totalValues + (Double.valueOf(count) * temp);
            }
        }

        if (totalValues != Double.valueOf(initialTotalAmount)) {
            Toast.makeText(getActivity(), "" + getActivity().getResources().getString(R.string.denomination_error), Toast.LENGTH_SHORT).show();
        } else {
            if (totalValues > 0)
                insertData();
            else
                Toast.makeText(getActivity(), "" + getActivity().getResources().getString(R.string.no_data_tosave), Toast.LENGTH_SHORT).show();
        }

    }

    private void insertData() {
        ArrayList<DenominationBO> denominationList = new ArrayList<>();
        for (Integer key : editTextHashMap.keySet()) {
            String count = getDynamicEditTextValues(key);
            if (!StringUtils.isNullOrEmpty(count)) {
                DenominationBO denominationBO = new DenominationBO();
                denominationBO.setDenomintionId(denominationInputValues.get(key).getDenomintionId());
                denominationBO.setDenominationDisplayName(denominationInputValues.get(key).getDenominationDisplayName());
                denominationBO.setDenominationDisplayNameValues(denominationInputValues.get(key).getDenominationDisplayNameValues());
                denominationBO.setCount(count);
                denominationBO.setIsCoin(denominationInputValues.get(key).getIsCoin());
                denominationList.add(denominationBO);
            }
        }
        compositeDisposable.add(DenominationHelper.getInstance().insertDenomination(getActivity(),
                denominationList, initialTotalAmount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean s) throws Exception {

                        if (s) {
                            /*for (Integer key : editTextHashMap.keySet()) {
                                editTextHashMap.get(key).setText("");
                                textViewHashMap.get(key).setText("0");
                            }*/
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.saved_successfully),
                                    Toast.LENGTH_SHORT).show();
                        }

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


}
