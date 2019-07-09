package com.ivy.cpg.view.delivery.salesreturn;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;

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

public class SalesReturnDeliveryFragment extends IvyBaseFragment implements RecyclerViewItemClickListener {

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
        compositeDisposable.add((Disposable) SalesReturnDeliveryHelper.getInstance().downloadSaleReturnDelivery(getActivity())
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

    public Observer<Vector<SalesReturnDeliveryDataBo>> getObserver() {
        return new DisposableObserver<Vector<SalesReturnDeliveryDataBo>>() {
            @Override
            public void onNext(Vector<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModels) {
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

    private Vector<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModelsList;

    private void setUpSalesReturnDeliveryAdapter(Vector<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModels) {
        this.salesReturnDeliveryDataModelsList = salesReturnDeliveryDataModels;

        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
                new SalesReturnDeliveryAdapter(SalesReturnDeliveryFragment.this,
                        salesReturnDeliveryDataModels);
        recyclerView.setAdapter(salesReturnDeliveryAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

    }

    @Override
    public void onItemClickListener(View view, int adapterPosition) {

        Bundle bundle = new Bundle();
        String data = new Gson().toJson(salesReturnDeliveryDataModelsList.get(adapterPosition));
        bundle.putString("DATA", data);

        SalesReturnDeliveryDetailsFragment salesReturnDeliveryFragment = new SalesReturnDeliveryDetailsFragment();
        salesReturnDeliveryFragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();


        transaction.replace(R.id.container_salesReturn, salesReturnDeliveryFragment, salesReturnDeliveryFragment.getClass().toString());
        transaction.addToBackStack(null);
        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();

    }

}
