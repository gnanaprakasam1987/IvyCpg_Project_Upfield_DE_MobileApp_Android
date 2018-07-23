package com.ivy.cpg.view.salesdeliveryreturn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SalesReturnDeliveryFragment extends Fragment implements RecyclerViewItemClickListener {

    private Unbinder unbinder;

    @BindView(R.id.recycler_salesDeliveryReturn)
    RecyclerView recyclerView;

    private CompositeDisposable compositeDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salesreturn_delivery, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSalesReturnDelivery();
    }

    private void getSalesReturnDelivery() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) SalesReturnDeliveryHelper.getInstance().getSaleReturnDelivery(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        unbinder.unbind();
    }

    public Observer<Vector<SalesReturnDeliveryDataModel>> getObserver() {
        return new DisposableObserver<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void onNext(Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
                setUpSalesReturnDeliveryAdapter(salesReturnDeliveryDataModels);

            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModelsList;

    private void setUpSalesReturnDeliveryAdapter(Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
        this.salesReturnDeliveryDataModelsList = salesReturnDeliveryDataModels;

        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
                new SalesReturnDeliveryAdapter(getActivity().getApplicationContext(), SalesReturnDeliveryFragment.this,
                        salesReturnDeliveryDataModels, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(salesReturnDeliveryAdapter);

    }

    @Override
    public void onItemClickListener(View view, int adapterPosition) {

        Bundle bundle = new Bundle();
        bundle.putString("UID", salesReturnDeliveryDataModelsList.get(adapterPosition).getUId());
        bundle.putInt("LPC", salesReturnDeliveryDataModelsList.get(adapterPosition).getLpc());
        bundle.putString("RETURN", salesReturnDeliveryDataModelsList.get(adapterPosition).getReturnValue());
        SalesReturnDeliveryDetailsFragment salesReturnDeliveryFragment = new SalesReturnDeliveryDetailsFragment();
        salesReturnDeliveryFragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();


        transaction.replace(R.id.container_salesReturn, salesReturnDeliveryFragment, salesReturnDeliveryFragment.getClass().toString());
        transaction.addToBackStack(null);
        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();


        // ((SalesReturnDeliveryActivity) getActivity()).addFragment( salesReturnDeliveryFragment, true, true);


    }

    public void numberPressed(View vw) {

    }
}
