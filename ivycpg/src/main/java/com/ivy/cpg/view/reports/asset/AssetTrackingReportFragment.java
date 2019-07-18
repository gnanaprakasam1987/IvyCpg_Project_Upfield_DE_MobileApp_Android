package com.ivy.cpg.view.reports.asset;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by anandasir.v on 8/31/2017.
 */

public class AssetTrackingReportFragment extends IvyBaseFragment {

    private BusinessModel bmodel;

    private int retailerID = 0;
    private int brandID = 0;
    AssetTrackingReportsHelper assetTrackingReportsHelper;
    private Unbinder unbinder;
    private CompositeDisposable compositeDisposable;

    @BindView(R.id.list)
    ListView lv;

    @BindView(R.id.spinnerStore)
    Spinner spnBeat;

    @BindView(R.id.spinnerBrand)
    Spinner spnChoice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_asset_tracking_report,
                container, false);

        unbinder = ButterKnife.bind(this, view);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        assetTrackingReportsHelper = new AssetTrackingReportsHelper(getContext());


        getSpinnerData();

        spnBeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                System.out.println("Executing SpnBeat");
                retailerID = SDUtil.convertToInt(((RetailerMasterBO) parent
                        .getItemAtPosition(position)).getRetailerID());
                loadData(brandID, retailerID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spnChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                System.out.println("Executing SpnChoice");
                brandID = ((AssetTrackingBrandBO) parent
                        .getItemAtPosition(position)).getBrandID();
                loadData(brandID, retailerID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    ArrayList<RetailerNamesBO> assetRetailerList = null;
    ArrayList<AssetTrackingBrandBO> assetbrandList = null;

    private void getSpinnerData() {
        assetRetailerList = new ArrayList<>();
        assetbrandList = new ArrayList<>();
       /* final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
        /*customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/

        compositeDisposable.add((Disposable) Observable.zip(assetTrackingReportsHelper.downloadAssetTrackingRetailerMaster()
                , assetTrackingReportsHelper.downloadAssetTrackingBrandMaster(),
                new BiFunction<ArrayList<RetailerNamesBO>, ArrayList<AssetTrackingBrandBO>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<RetailerNamesBO> retailerNamesList, ArrayList<AssetTrackingBrandBO> assetTrackingBrandList) throws Exception {

                        if (retailerNamesList.size() > 0
                                && assetTrackingBrandList.size() > 0) {
                            assetRetailerList = retailerNamesList;
                            assetbrandList = assetTrackingBrandList;
                            return true;
                        } else {
                            return false;
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isFlag) {
                        if (isFlag)
                            loadSpinner();
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Throwable e) {
                        //alertDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        //alertDialog.dismiss();
                    }
                }));
    }


    private void loadSpinner() {

        //Load asset Retailer List
        ArrayAdapter<RetailerMasterBO> brandAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        brandAdapter.add(new RetailerMasterBO("0", getResources().getString(
                R.string.select)));
        for (int i = 0; i < assetRetailerList.size(); i++) {
            brandAdapter
                    .add(new RetailerMasterBO(String.valueOf(assetRetailerList.get(i).getRetailerId()),
                            assetRetailerList.get(i).getRetailerName()));
        }
        brandAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnBeat.setAdapter(brandAdapter);

        //Load asset brand List
        ArrayAdapter<AssetTrackingBrandBO> choiceAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        choiceAdapter.add(new AssetTrackingBrandBO(0, getResources().getString(
                R.string.all)));
        for (int i = 0; i < assetbrandList.size(); i++) {
            choiceAdapter
                    .add(assetbrandList.get(i));
        }
        choiceAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnChoice.setAdapter(choiceAdapter);


    }

    private void loadData(int brandID, int RetailerID) {
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add((Disposable) assetTrackingReportsHelper.downloadAssetTrackingreport(RetailerID, brandID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<AssetTrackingReportBO>>() {
                    @Override
                    public void onNext(ArrayList<AssetTrackingReportBO> assetTrackingReportBOS) {
                        MyAdapter adapter = new MyAdapter(assetTrackingReportBOS);
                        lv.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (compositeDisposable != null
                && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        unbinder.unbind();
    }

    class ViewHolder {
        AssetTrackingReportBO mAssetTrackingReportBO;
        int position;
        @BindView(R.id.txtAsset)
        TextView txtAsset;
        @BindView(R.id.txtBrand)
        TextView txtBrand;

        @BindView(R.id.txtTarget)
        TextView txtTarget;

        @BindView(R.id.txtActual)
        TextView txtActual;

        @BindView(R.id.txtReason)
        TextView txtReason;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class MyAdapter extends ArrayAdapter<AssetTrackingReportBO> {
        private final ArrayList<AssetTrackingReportBO> items;

        public MyAdapter(ArrayList<AssetTrackingReportBO> items) {
            super(getActivity(), R.layout.row_asset_tracking_report, items);
            this.items = items;
        }

        public AssetTrackingReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
                convertView = inflater.inflate(R.layout.row_asset_tracking_report, parent, false);
                holder = new ViewHolder(convertView);
                holder.txtAsset.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mAssetTrackingReportBO = items.get(position);
            holder.position = position;

            holder.txtAsset.setText(holder.mAssetTrackingReportBO.getAssetDescription() + "");

            holder.txtBrand.setText(holder.mAssetTrackingReportBO.getBrandname() + "");
            holder.txtTarget.setText(holder.mAssetTrackingReportBO.getTarget() + "");
            holder.txtActual.setText(holder.mAssetTrackingReportBO.getActual() + "");
            holder.txtReason.setText(holder.mAssetTrackingReportBO.getReason() + "");

            return convertView;
        }
    }
}
