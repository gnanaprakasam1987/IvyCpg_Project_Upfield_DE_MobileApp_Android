package com.ivy.cpg.view.retailercontract;


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

import java.util.ArrayList;

public class RetailerContractFragment extends IvyBaseFragment {


    BusinessModel bmodel;
    private ListView lvContract;
    RenewContractDialog dialogFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_retailer_contract,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lvContract = view.findViewById(R.id.list);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        lvContract.setAdapter(new MyAdapter(RetailerContractHelper.getInstance(getActivity()).getRetailerContractList()));

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

            if (holder.rContractBO.isRenewed())
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.audit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!holder.rContractBO.isRenewed()) {
                        if (dialogFragment == null) {
                            dialogFragment = new RenewContractDialog();
                            dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    onStart();
                                    dialogFragment = null;
                                }
                            });
                            Bundle args = new Bundle();
                            args.putString("ContractID", holder.rContractBO.getContractid());
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getActivity().getSupportFragmentManager(), "RenewContractDialog");
                        }

                    } else {
                        if (holder.rContractBO.isRenewed() && holder.rContractBO.isUploaded())
                            Toast.makeText(getActivity(), getResources().getString(R.string.alert_already_renewed_uploaded), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.alert_already_renewed), Toast.LENGTH_LONG).show();
                    }
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

}
