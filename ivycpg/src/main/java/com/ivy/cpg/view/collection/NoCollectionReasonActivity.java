package com.ivy.cpg.view.collection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.FontUtils;

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

        setScreenTitle("No Collection Reason");

        ((TextView) findViewById(R.id.invoice_no)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.invoice_date)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.invoice_amount)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.invoice_reason)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));

        recyclerView = findViewById(R.id.invoice_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        loadInvoiceList(bmodel.getRetailerMasterBO().getRetailerID());
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
            DBUtil db = new DBUtil(NoCollectionReasonActivity.this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            boolean isReasonAdded = false;

            int pos = 0;
            for (NoCollectionReasonBo invoiceHeaderBO : mInvioceList) {
                if (!invoiceHeaderBO.getNoCollectionReasonId().equals("0") &&
                        !invoiceHeaderBO.getNoCollectionReasonId().equals("")) {

                    isReasonAdded = true;


                    String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + SDUtil.now(SDUtil.DATE_TIME_ID);
                    String uid = bmodel.QT(id);

                    String columns = "", values;
                    if (pos == 0) {

                        deletePreviousTransaction(db, invoiceHeaderBO);

                        columns = "Date,SubmittedDate,RetailerId,uid,DistributorId,ParentDistributorId";

                        values = bmodel.QT(invoiceHeaderBO.getInvoiceDate()) + "," +
                                bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," +
                                bmodel.QT(invoiceHeaderBO.getRetailerId()) + "," +
                                uid + "," +
                                bmodel.getRetailerMasterBO().getDistributorId() + "," +
                                bmodel.getRetailerMasterBO().getDistParentId();

                        db.insertSQL("CollectionDueHeader", columns, values);
                    }


                    columns = "InvoiceNo,ReasonId,uid";

                    values = bmodel.QT(invoiceHeaderBO.getInvoiceNo()) + "," +
                            bmodel.QT(invoiceHeaderBO.getNoCollectionReasonId()) + "," +
                            uid;

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

                invoiceNumber.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, NoCollectionReasonActivity.this));
                invoicedate.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, NoCollectionReasonActivity.this));
                invoiceAmt.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, NoCollectionReasonActivity.this));

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
            DBUtil db = new DBUtil(NoCollectionReasonActivity.this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

    private void loadInvoiceList(String id) {
        try {
            DBUtil db = new DBUtil(NoCollectionReasonActivity.this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt," +
                    " Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as RcvdAmt," +
                    " CDD.ReasonId,CDD.ReasonOthers,Inv.RetailerId FROM InvoiceMaster Inv LEFT JOIN payment ON payment.BillNumber = Inv.InvoiceNo " +
                    " LEFT JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid left join CollectionDueDetails CDD on CDD.InvoiceNo = Inv.InvoiceNo " +
                    " WHERE inv.Retailerid ='" + id + "'  AND inv.DocStatus ='COL'  GROUP BY Inv.InvoiceNo ORDER BY Inv.InvoiceDate");

            if (c != null) {
                while (c.moveToNext()) {
                    NoCollectionReasonBo invoiceHeaderBO = new NoCollectionReasonBo();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setPaidAmount(c.getDouble(3));
                    invoiceHeaderBO.setNoCollectionReasonId(c.getString(4));
                    invoiceHeaderBO.setNoCollectionReason(c.getString(5));
                    invoiceHeaderBO.setRetailerId(c.getString(6));

                    if (invoiceHeaderBO.getPaidAmount() == 0)
                        mInvioceList.add(invoiceHeaderBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }
}