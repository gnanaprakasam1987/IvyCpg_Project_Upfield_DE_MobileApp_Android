package com.ivy.cpg.view.collection;

import android.database.Cursor;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class NoCollectionReasonActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private RecyclerView recyclerView;
    private ArrayList<NoCollectionReasonBo> mInvioceList = new ArrayList<>();

    private ArrayList<ReasonMaster> collectionReasonList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_collection_reason);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setScreenTitle(getString(R.string.no_collection_reason));

        recyclerView = findViewById(R.id.invoice_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        mInvioceList = CollectionHelper.getInstance(this).loadInvoiceList(bmodel.getRetailerMasterBO().getRetailerID(),this);
        loadCollectionReason();

        myAdapter.notifyDataSetChanged();

        findViewById(R.id.done_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReason();
            }
        });

    }

    private void saveReason() {

        try {
            DBUtil db = new DBUtil(NoCollectionReasonActivity.this, DataMembers.DB_NAME
            );
            db.openDataBase();

            boolean isReasonAdded = false;

            int pos = 0;
            for (NoCollectionReasonBo invoiceHeaderBO : mInvioceList) {
                if (!invoiceHeaderBO.getNoCollectionReasonId().equals("0") &&
                        !invoiceHeaderBO.getNoCollectionReasonId().equals("")) {

                    isReasonAdded = true;


                    String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
                    String uid = bmodel.QT(id);

                    String columns = "", values;
                    if (pos == 0) {

                        deletePreviousTransaction(db, invoiceHeaderBO);

                        columns = "Date,SubmittedDate,RetailerId,uid,DistributorId,ParentDistributorId";

                        values = bmodel.QT(invoiceHeaderBO.getInvoiceDate()) + "," +
                                bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," +
                                bmodel.QT(invoiceHeaderBO.getRetailerId()) + "," +
                                uid + "," +
                                bmodel.getRetailerMasterBO().getDistributorId() + "," +
                                bmodel.getRetailerMasterBO().getDistParentId();

                        db.insertSQL("CollectionDueHeader", columns, values);
                    }


                    columns = "InvoiceNo,ReasonId,uid, RetailerID";

                    values = bmodel.QT(invoiceHeaderBO.getInvoiceNo()) + "," +
                            bmodel.QT(invoiceHeaderBO.getNoCollectionReasonId()) + "," +
                            uid + "," + bmodel.QT(invoiceHeaderBO.getRetailerId());

                    db.insertSQL("CollectionDueDetails", columns, values);

                    pos = pos + 1;


                }
            }

            db.closeDB();

            if (isReasonAdded) {
                Toast.makeText(this, "No Collection Reasons Saved", Toast.LENGTH_SHORT).show();

                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void deletePreviousTransaction(DBUtil db, NoCollectionReasonBo noCollectionReasonBo) {


        Cursor c = db.selectSQL("select uid from CollectionDueHeader where RetailerId ='"+bmodel.getRetailerMasterBO().getRetailerID()+"'");

        if (c != null && c.moveToNext()) {

            db.deleteSQL("CollectionDueDetails", "uid = '" + c.getString(0) + "'", false);
            db.deleteSQL("CollectionDueHeader", "uid = '" + c.getString(0) + "'", false);

            c.close();
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private ArrayAdapter<ReasonMaster> collectionReasonAdapter;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private Spinner noCollectionReasonSpnr;
            private TextView invoiceNumber, invoiceAmt, invoicedate;

            public MyViewHolder(View view) {
                super(view);

                noCollectionReasonSpnr = view.findViewById(R.id.spinnerNooCollectionReason);
                invoiceNumber = view.findViewById(R.id.invoice_no);
                invoicedate = view.findViewById(R.id.invoice_date);
                invoiceAmt = view.findViewById(R.id.invoice_amount);


            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.no_collection_reason_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.invoiceNumber.setText(mInvioceList.get(holder.getAdapterPosition()).getInvoiceNo());
            holder.invoicedate.setText(mInvioceList.get(holder.getAdapterPosition()).getInvoiceDate());
            holder.invoiceAmt.setText(String.valueOf(mInvioceList.get(holder.getAdapterPosition()).getInvoiceAmount()));

            collectionReasonAdapter = new ArrayAdapter<ReasonMaster>(NoCollectionReasonActivity.this, R.layout.collection_spinner_item_layout);
            collectionReasonAdapter.add(new ReasonMaster("0", getResources().getString(R.string.select_reason)));
            collectionReasonAdapter.addAll(collectionReasonList);
            collectionReasonAdapter.setDropDownViewResource(R.layout.call_analysis_spinner_list_item);

            holder.noCollectionReasonSpnr.setAdapter(collectionReasonAdapter);

            if (mInvioceList.get(holder.getAdapterPosition()).getNoCollectionReasonId() != null &&
                    !mInvioceList.get(holder.getAdapterPosition()).getNoCollectionReasonId().isEmpty()) {

                int spinPos = 0;
                for (ReasonMaster reasonMaster : collectionReasonList) {
                    spinPos = spinPos + 1;
                    if (reasonMaster.getReasonID().equals(mInvioceList.get(holder.getAdapterPosition()).getNoCollectionReasonId())) {
                        holder.noCollectionReasonSpnr.setSelection(spinPos);
                        break;
                    }
                }
            }

            holder.noCollectionReasonSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int itemPosition, long id) {

                    ReasonMaster collectioReasonBO = (ReasonMaster) holder.noCollectionReasonSpnr.getSelectedItem();

                    mInvioceList.get(holder.getAdapterPosition()).setNoCollectionReasonId(collectioReasonBO.getReasonID());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return mInvioceList.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCollectionReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(NoCollectionReasonActivity.this, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.NO_COLLECTION_REASON_TYPE));

            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    collectionReasonList.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
