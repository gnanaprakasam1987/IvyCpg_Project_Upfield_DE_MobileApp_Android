package com.ivy.cpg.view.serializedAsset;

import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SerializedAssetApprovalActivity extends IvyBaseActivityNoActionBar {

    private SerializedAssetHelper assetTrackingHelper;
    private SerializedAssetApprovalAdapter assetApprovalAdapter;
    private ArrayList<SerializedAssetBO> approvalList = new ArrayList<>();
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusinessModel mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        assetTrackingHelper = SerializedAssetHelper.getInstance(this);
        compositeDisposable = new CompositeDisposable();

        setContentView(R.layout.activity_serialized_asset_approvel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView approvalListView = findViewById(R.id.approvel_list_view);
        approvalListView.setHasFixedSize(true);
        approvalListView.setLayoutManager(new LinearLayoutManager(this));
        approvalListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        assetApprovalAdapter = new SerializedAssetApprovalAdapter(this, approvalList, mBModel);
        approvalListView.setAdapter(assetApprovalAdapter);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.new_request));
        }

        loadData();

        findViewById(R.id.btn_save).setOnClickListener(v -> onSaveBtnClick());
    }

    private void loadData() {
        compositeDisposable.add(assetTrackingHelper.fetchAssetApprovalData(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<SerializedAssetBO>>() {
                    @Override
                    public void onNext(ArrayList<SerializedAssetBO> serializedAssetBOS) {
                        approvalList.clear();
                        approvalList.addAll(serializedAssetBOS);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        assetApprovalAdapter.notifyDataSetChanged();
                    }
                }));


    }

    private void onSaveBtnClick() {
        compositeDisposable.add(assetTrackingHelper.updateApproval(this, approvalList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isSaved -> {
                    if (isSaved) {
                        showMessage(getString(R.string.saved_successfully));
                        finish();
                    } else
                        showMessage(getString(R.string.something_went_wrong));
                }));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        approvalList = null;
        assetApprovalAdapter = null;

    }
}
