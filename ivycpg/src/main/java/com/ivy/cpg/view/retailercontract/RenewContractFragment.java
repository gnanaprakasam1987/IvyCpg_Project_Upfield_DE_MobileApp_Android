package com.ivy.cpg.view.retailercontract;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class RenewContractFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private ListView lvContract;
    private String mretailerID, Tid;
    private RetailerContractHelper retailerContractHelper;
    private AppSchedulerProvider appSchedulerProvider;
    private ProgressDialog progressDialogue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_retailer_contract,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        retailerContractHelper = RetailerContractHelper.getInstance(getActivity());
        appSchedulerProvider = new AppSchedulerProvider();

        lvContract = view.findViewById(R.id.list);
        TextView tvDelete = view.findViewById(R.id.audit);
        tvDelete.setText(getActivity().getResources().getString(R.string.delete));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        lvContract.setAdapter(new MyAdapter(retailerContractHelper.getmRenewedContractList()));

    }

    private class MyAdapter extends BaseAdapter {
        private ArrayList<RetailerContractBO> items;

        public MyAdapter(ArrayList<RetailerContractBO> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(
                        R.layout.row_retailer_contract, parent, false);
                convertView.setTag(holder);

                holder.tvcontactname = convertView
                        .findViewById(R.id.contractname);
                holder.tvcontracttype = convertView
                        .findViewById(R.id.contracttype);
                holder.audit = convertView
                        .findViewById(R.id.btn_audit);
                holder.tvendDate = convertView
                        .findViewById(R.id.endDate1);
                holder.tvstartdate = convertView
                        .findViewById(R.id.strDate1);


            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.rContractBO = items.get(position);
            holder.tvcontactname.setText(holder.rContractBO.getContractname());
            holder.tvcontracttype.setText(holder.rContractBO.getContracttype());
            holder.tvstartdate.setText(holder.rContractBO.getStartdate());
            holder.tvendDate.setText(holder.rContractBO.getEnddate());


            holder.audit.setImageResource(R.drawable.icon_close_);


            holder.audit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mretailerID = holder.rContractBO.getRetailerid();
                    Tid = holder.rContractBO.getTid();

                    showAlert(getResources().getString(
                            R.string.do_you_want_to_delete_the_contract));
                }
            });


            return convertView;
        }
    }

    class ViewHolder {
        RetailerContractBO rContractBO;
        TextView tvcontactname, tvcontracttype, tvendDate, tvstartdate;
        ImageButton audit;


    }


    public void showAlert(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        progressDialogue = ProgressDialog.show(getActivity(),
                                DataMembers.SD, getResources().getString(R.string.deleting),
                                true, false);
                        new CompositeDisposable().add(retailerContractHelper.deleteRetailerContract(Tid, mretailerID)
                                .subscribeOn(appSchedulerProvider.io())
                                .observeOn(appSchedulerProvider.ui())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) {
                                        updateUiAfterSave();
                                    }
                                }));
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    private void updateUiAfterSave() {
        progressDialogue.dismiss();

        Toast.makeText(getActivity(),
                getResources().getString(R.string.contract_deleted_sucessfully),
                Toast.LENGTH_SHORT).show();

        onStart();
    }

}
