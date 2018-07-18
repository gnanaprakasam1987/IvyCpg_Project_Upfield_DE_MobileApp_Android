package com.ivy.cpg.view.salesdeliveryreturn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;

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
    private Observer<? super Vector<SalesReturnDeliveryDataModel>> observer;

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
        compositeDisposable.add((Disposable) new SalesReturnDeliveryHelper().getSaleReturnDelivery(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    /*
    *
    * @Param
    *
    * */
    private void setUpSalesReturnDeliveryAdapter(Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
                new SalesReturnDeliveryAdapter(getActivity(), SalesReturnDeliveryFragment.this,
                        salesReturnDeliveryDataModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(salesReturnDeliveryAdapter);

    }

    @Override
    public void onItemClickListener(View view, int adapterPosition) {
        SalesReturnDeliveryDetailsFragment salesReturnDeliveryFragment = new SalesReturnDeliveryDetailsFragment();
        ((BaseActivity) getActivity()).addFragment(R.id.container_salesReturn, salesReturnDeliveryFragment, true, true, 0);
    }
}
