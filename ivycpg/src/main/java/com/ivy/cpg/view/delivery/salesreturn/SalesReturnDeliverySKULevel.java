package com.ivy.cpg.view.delivery.salesreturn;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by ramkumard on 6/12/18.
 * Class which defines SKU Level Sales Return Entry.
 */

public class SalesReturnDeliverySKULevel extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private EditText QUANTITY;
    private int currentSelectedValue = 0;
    private String append = "";
    ArrayList<SalesReturnDeliveryDataModel> dataList = new ArrayList<>();
    private BusinessModel bmodel;
    private int returnCaseQty;
    private int returnPieceQty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salesreturn_delivery_skulevel);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            // getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            setScreenTitle(title);
        }

        Button btnDone = findViewById(R.id.btn_save);
        btnDone.setText(getResources().getString(R.string.done));
        btnDone.setOnClickListener(this);

        findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        setLabelMaserValue();
        String key = getIntent().getExtras().getString("key");

        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
        dataList = new ArrayList<>();
        if (!salesReturnDeliveryHelper.getSalesReturnDelDataMap().isEmpty()
                && salesReturnDeliveryHelper.getSalesReturnDelDataMap().containsKey(key))
            dataList = salesReturnDeliveryHelper.getSalesReturnDelDataMap().get(key);

        RecyclerView recyclerView = findViewById(R.id.SalesReturn_Details);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
                new SalesReturnDeliveryAdapter(this, dataList);
        recyclerView.setAdapter(salesReturnDeliveryAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLabelMaserValue() {

        BusinessModel businessModel = (BusinessModel) getApplicationContext();
        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();


        try {

            if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.actual_caseQty).getTag()) != null)
                ((TextView) findViewById(R.id.actual_caseQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.actual_caseQty)
                                        .getTag()));

            if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.actual_PcQty).getTag()) != null)
                ((TextView) findViewById(R.id.actual_PcQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.actual_PcQty)
                                        .getTag()));


            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_PCS) {
                (findViewById(R.id.actual_PcQty)).setVisibility(View.GONE);
            }

            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_CASE) {
                (findViewById(R.id.actual_caseQty)).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_save:
                if (validateQty())
                finish();
                else
                    Toast.makeText(this, getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                Button ed = findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            if ((!strQuantity.isEmpty()) && (currentSelectedValue >= Integer.parseInt(strQuantity))) {
                QUANTITY.setText(strQuantity);
            } else {
                Toast.makeText(this, getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");

            }

        } else {
            if ((!append.isEmpty()) && (currentSelectedValue >= Integer.parseInt(append))) {
                QUANTITY.setText(append);
            } else {
                Toast.makeText(this, getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");
            }
        }
    }

    public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliverySKULevel.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {

        private Context mContext;
        private ArrayList<SalesReturnDeliveryDataModel> list;


        /**
         * Initialize the values
         *
         * @param context                       : context reference
         * @param salesReturnDeliveryDataModels : data
         */

        SalesReturnDeliveryAdapter(Context context,
                                   ArrayList<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
            mContext = context;
            list = salesReturnDeliveryDataModels;
        }


        /**
         * @param parent   : parent ViewPgroup
         * @param viewType : viewType
         * @return ViewHolder
         * <p>
         * Inflate the Views
         * Create the each views and Hold for Reuse
         */
        @NonNull
        @Override
        public SalesReturnDeliverySKULevel.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.salesreturn_skulevel_list_item, parent, false);
            return new SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder(view);
        }

        /**
         * @param holder   :view Holder
         * @param position : position of each Row
         *                 set the values to the views
         */
        @Override
        public void onBindViewHolder(@NonNull final SalesReturnDeliverySKULevel.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, int position) {


            holder.productName.setText(list.get(holder.getAdapterPosition()).getProductName());
            holder.actualPieceQuantity.setText(String.valueOf(list.get(holder.getAdapterPosition()).getActualPieceQuantity()));
            holder.actualCaseQuantity.setText(String.valueOf(list.get(holder.getAdapterPosition()).getActualCaseQuantity()));
            holder.reason.setText(list.get(holder.getAdapterPosition()).getReason());

            holder.actualCaseQuantity.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    QUANTITY = holder.actualCaseQuantity;
                    currentSelectedValue = list.get(holder.getAdapterPosition()).getReturnCaseQuantity();
                    returnCaseQty = list.get(holder.getAdapterPosition()).getReturnCaseQuantity();
                    int inType = holder.actualCaseQuantity.getInputType();
                    holder.actualCaseQuantity.setInputType(InputType.TYPE_NULL);
                    holder.actualCaseQuantity.onTouchEvent(event);
                    holder.actualCaseQuantity.setInputType(inType);
                    holder.actualCaseQuantity.requestFocus();
                    if (holder.actualCaseQuantity.getText().length() > 0)
                        holder.actualCaseQuantity.setSelection(holder.actualCaseQuantity.getText().length());
                    return true;
                }
            });
            holder.actualPieceQuantity.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    QUANTITY = holder.actualPieceQuantity;
                    currentSelectedValue = list.get(holder.getAdapterPosition()).getReturnPieceQuantity();
                    returnPieceQty = list.get(holder.getAdapterPosition()).getReturnPieceQuantity();
                    int inType = holder.actualPieceQuantity.getInputType();
                    holder.actualPieceQuantity.setInputType(InputType.TYPE_NULL);
                    holder.actualPieceQuantity.onTouchEvent(event);
                    holder.actualPieceQuantity.setInputType(inType);
                    holder.actualPieceQuantity.requestFocus();
                    if (holder.actualPieceQuantity.getText().length() > 0)
                        holder.actualPieceQuantity.setSelection(holder.actualPieceQuantity.getText().length());
                    return true;
                }
            });


            holder.actualCaseQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if ((!(s.toString()).isEmpty())) {
                        if (s.toString().length() > 0)
                            holder.actualCaseQuantity.setSelection(s.toString().length());
                        list.get(holder.getAdapterPosition()).setActualCaseQuantity(Integer.valueOf(s.toString()));
                    } else {
                        holder.actualCaseQuantity.setText("0");
                    }

                }
            });

            holder.actualPieceQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!(s.toString()).isEmpty()) {
                        if (s.toString().length() > 0)
                            holder.actualPieceQuantity.setSelection(s.toString().length());
                        list.get(holder.getAdapterPosition()).setActualPieceQuantity(Integer.valueOf(s.toString()));
                    } else {
                        holder.actualPieceQuantity.setText("0");
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        /**
         * Create The view First Time and hold for reuse
         * View Holder for Create and Hold the view for ReUse the views instead of create again
         * Initialize the views
         */

        class SalesReturnDeliveryViewHolder extends RecyclerView.ViewHolder {

            TextView productName, reason;
            EditText actualCaseQuantity, actualPieceQuantity;


            private SalesReturnDeliveryViewHolder(View itemView) {
                super(itemView);
                reason = itemView.findViewById(R.id.txt_reason);
                productName = itemView.findViewById(R.id.txt_productName);
                actualCaseQuantity = itemView.findViewById(R.id.txt_actualCaseQuantity);
                actualPieceQuantity = itemView.findViewById(R.id.txt_actualPieceQuantity);

                SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
                if (!salesReturnDeliveryHelper.SHOW_SALES_RET_PCS) {
                    actualPieceQuantity.setVisibility(View.GONE);
                }

                if (!salesReturnDeliveryHelper.SHOW_SALES_RET_CASE) {
                    actualCaseQuantity.setVisibility(View.GONE);
                }

            }

        }


    }

    private boolean validateQty() {
        int actualPieceQty = 0;
        int actualCaseQty = 0;
        for (SalesReturnDeliveryDataModel model : dataList) {
            actualPieceQty += model.getActualPieceQuantity();
            actualCaseQty += model.getActualCaseQuantity();
        }

        return (returnCaseQty >= actualCaseQty && returnPieceQty >= actualPieceQty);
    }
}
