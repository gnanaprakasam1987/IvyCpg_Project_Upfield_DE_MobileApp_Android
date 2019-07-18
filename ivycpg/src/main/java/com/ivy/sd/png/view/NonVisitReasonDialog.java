package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class NonVisitReasonDialog extends Dialog implements OnClickListener {

    private Context mContext;
    private BusinessModel bmodel;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Vector<NonproductivereasonBO> nonProductiveRetailers;
    public Activity c;
    private TypedArray typearr;
    private TextView messagetv;

    public NonVisitReasonDialog(Context context,
                                Vector<NonproductivereasonBO> retailersDC) {
        super(context);
        this.mContext = context;
        c = (Activity) context;
        nonProductiveRetailers = retailersDC;
        bmodel = (BusinessModel) context.getApplicationContext();
        typearr = mContext.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        // TODO Auto-generated constructor stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_nonvisitreason);
        setCancelable(false);
        setCanceledOnTouchOutside(false);


        RecyclerView lvwplist = (RecyclerView) findViewById(R.id.dialog_nonvisit_listview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        lvwplist.setLayoutManager(linearLayoutManager);
        lvwplist.setAdapter(new RetailerAdapter(nonProductiveRetailers));

        findViewById(R.id.dialog_nonvisitreason_back).setOnClickListener(this);
        findViewById(R.id.dialog_nonvisitreason_save).setOnClickListener(this);

        spinnerAdapter = new ArrayAdapter<ReasonMaster>(getContext(),
                R.layout.spinner_bluetext_layout);

        spinnerAdapter.add(new ReasonMaster(0 + "", "Select"));
        for (ReasonMaster temp : bmodel.reasonHelper.getNonVisitReasonMaster()) {
            spinnerAdapter.add(temp);
        }

        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

    }

    class RetailerAdapter extends RecyclerView.Adapter<RetailerAdapter.ViewHolder> {

        private Vector<NonproductivereasonBO> items;
        private String drawableId;

        public RetailerAdapter(Vector<NonproductivereasonBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_nonvisitreason_listview, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.obj = items.get(position);
            holder.retailerName.setText(holder.obj.getRetailerName());
            holder.retailerAddr.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.retailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            //holder.retailerAddr.setText(holder.obj.getDate());
            holder.value.setAdapter(spinnerAdapter);
            TypedArray typearr = mContext.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                holder.itemView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }
            holder.value.setSelection(getPositionRes(holder.obj.getReasonid()));
            holder.value.setSelected(true);
            holder.value
                    .setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {

                            ReasonMaster rm = (ReasonMaster) parent
                                    .getSelectedItem();
                            if (!rm.getReasonID().equals("0")) {
                                holder.obj.setReasonid(rm.getReasonID());
                                holder.obj.setReasontype("NV");
                                holder.obj.setDate(bmodel.userMasterHelper
                                        .getUserMasterBO()
                                        .getDownloadDate());
                            }
                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            NonproductivereasonBO obj;
            RetailerMasterBO obj1;
            TextView retailerName, retailerAddr;
            Spinner value;
            int ref;
            LinearLayout list_item;

            public ViewHolder(View v) {
                super(v);
                retailerName = (TextView) v.findViewById(R.id.dialog_nonvisitreason_listview_retailername);
                retailerAddr = (TextView) v.findViewById(R.id.dialog_nonvisitreason_listview_retaileraddr);
                value = (Spinner) v.findViewById(R.id.dialog_nonvisitreason_listview_spinner);
                list_item = (LinearLayout) v.findViewById(R.id.list_item);
            }
        }
    }

    private int getPositionRes(String reasonId) {

        if (reasonId.equals("0"))
            return 0;

        for (int i = 0; i < bmodel.reasonHelper.getNonVisitReasonMaster().size(); i++) {
            if (bmodel.reasonHelper.getNonVisitReasonMaster().get(i).getReasonID()
                    .equals(reasonId))
                return i + 1;
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.dialog_nonvisitreason_back)
            cancel();
        else {
            new SaveDate().execute();
        }

    }

    class SaveDate extends AsyncTask<Integer, Integer, Boolean> {

        //private ProgressDialog progressDialogue;

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                return bmodel
                        .saveNonProductiveRetailers(nonProductiveRetailers);
            } catch (Exception e) {
                Commons.printException(e);
                return false;
            }
        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(mContext, DataMembers.SD,
                    mContext.getResources().getString(R.string.loading), true,
					false);*/

            builder = new AlertDialog.Builder(mContext);

            customProgressDialog(builder);
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            //	progressDialogue.dismiss();
            if (result) {
                nonProductiveRetailers = bmodel.getMissedCallRetailers();
                if (nonProductiveRetailers.size() == 0) {

                    alertDialog.dismiss();
                    dismiss();
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.saved_successfully),
                            Toast.LENGTH_SHORT).show();

                } else {
                    /*adapter.clear();
                    adapter.addAll(nonProductiveRetailers);
                    adapter.notifyDataSetChanged();
                    lvwplist.setAdapter(adapter);*/
                    RecyclerView lvwplist = (RecyclerView) findViewById(R.id.dialog_nonvisit_listview);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                    lvwplist.setLayoutManager(linearLayoutManager);
                    lvwplist.setAdapter(new RetailerAdapter(nonProductiveRetailers));
                    alertDialog.dismiss();
                }
            } else {
                alertDialog.dismiss();
                Toast.makeText(
                        mContext,
                        mContext.getResources().getString(
                                R.string.please_select_item),
                        Toast.LENGTH_SHORT).show();
            }
            alertDialog.dismiss();
        }
    }

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    (ViewGroup) getOwnerActivity().findViewById(R.id.layout_root));

            TextView title = (TextView) layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) layout.findViewById(R.id.text);
            messagetv.setText(getOwnerActivity().getResources().getString(R.string.loading));

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
