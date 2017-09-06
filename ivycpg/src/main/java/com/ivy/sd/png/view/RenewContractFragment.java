package com.ivy.sd.png.view;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import com.ivy.sd.png.bo.RetailerContractBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class RenewContractFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private ListView lvContract;
    private String mretailerID, Tid;
    private TextView tvDelete;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_retailer_contract,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lvContract = (ListView) view.findViewById(R.id.lvwplist);
        tvDelete = (TextView) view.findViewById(R.id.audit);
        tvDelete.setText("Delete");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        lvContract.setAdapter(new MyAdapter(bmodel.retailerContractHelper.getmRenewedContractList()));

    }

    private class MyAdapter extends BaseAdapter {
        private ArrayList<RetailerContractBO> items;

        public MyAdapter(ArrayList<RetailerContractBO> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = (View) inflater.inflate(
                        R.layout.row_retailer_contract, null);
                convertView.setTag(holder);

                holder.tvcontactname = (TextView) convertView
                        .findViewById(R.id.contractname);
                holder.tvcontracttype = (TextView) convertView
                        .findViewById(R.id.contracttype);
                holder.audit = (ImageButton) convertView
                        .findViewById(R.id.btn_audit);
                holder.tvendDate = (TextView) convertView
                        .findViewById(R.id.endDate1);
                holder.tvstartdate = (TextView) convertView
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

    class DeleteAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.retailerContractHelper.deleteRenewal(Tid);
                bmodel.retailerContractHelper.downloadRenewedContract(mretailerID);
                bmodel.retailerContractHelper.downloadRetailerContract(mretailerID);
                return Boolean.TRUE;
            } catch (Exception e) {
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.deleting),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();

            Toast.makeText(getActivity(),
                    getResources().getString(R.string.contract_deleted_sucessfully),
                    Toast.LENGTH_SHORT).show();

            onStart();


        }

    }

    public void showAlert(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAsyncTask().execute();
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }
}
