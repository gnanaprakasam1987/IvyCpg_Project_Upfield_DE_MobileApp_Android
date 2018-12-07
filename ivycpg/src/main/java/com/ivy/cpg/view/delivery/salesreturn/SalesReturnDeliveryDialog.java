package com.ivy.cpg.view.delivery.salesreturn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by ramkumard on 6/12/18.
 * Class which defines SKU Level Sales Return Entry.
 */

public class SalesReturnDeliveryDialog extends DialogFragment implements View.OnClickListener{

    private EditText QUANTITY;
    private int currentSelectedValue = 0;
    private String append = "";
    ArrayList<SalesReturnDeliveryDataModel> dataList = new ArrayList<>();
    private SalesReturnDeliveryDataBo salesReturnDeliveryDataBo = null;
    private BusinessModel bmodel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        getDialog().setCancelable(false);
        this.setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_salesreturn_delivery, container);
        view.findViewById(R.id.btn_save).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        setLabelMaserValue(view);
        String key = "";
        if (getArguments() != null) {
            key = getArguments().getString("key");
            String data = getArguments().getString("DATA");
            salesReturnDeliveryDataBo =
                    new Gson().fromJson(data, SalesReturnDeliveryDataBo.class);
        }
        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
        dataList = new ArrayList<>();
        if (!salesReturnDeliveryHelper.getSalesReturnDelDataMap().isEmpty()
                && salesReturnDeliveryHelper.getSalesReturnDelDataMap().containsKey(key))
        dataList = salesReturnDeliveryHelper.getSalesReturnDelDataMap().get(key);

        RecyclerView recyclerView = view.findViewById(R.id.SalesReturn_Details);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
        new SalesReturnDeliveryAdapter(getActivity().getApplicationContext(), dataList);
        recyclerView.setAdapter(salesReturnDeliveryAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = getDialog().getWindow();
        if (window != null)
            window.setAttributes(lp);


    }

    private void showConfirmAlert() {

        new AlertDialog.Builder(getActivity(), R.style.DatePickerDialogStyle)
                .setTitle("IvyCpg")
                .setMessage(getActivity().getString(R.string.do_u_want_to_save))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isSuccess = SalesReturnDeliveryHelper.getInstance().saveSalesReturnDelivery(getActivity(), dataList, salesReturnDeliveryDataBo);
                        if (isSuccess) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                            (getActivity()).onBackPressed();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void showCancelAlert() {

        new AlertDialog.Builder(getActivity())
                .setTitle("IvyCpg")
                .setMessage(getActivity().getString(R.string.do_u_want_to_cancel))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isSuccess = SalesReturnDeliveryHelper.getInstance().cancelSalesReturnDelivery(getActivity(), salesReturnDeliveryDataBo);
                        if (isSuccess) {
                            Toast.makeText(getActivity(), "Cancel Successfully", Toast.LENGTH_SHORT).show();
                            (getActivity()).onBackPressed();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void setLabelMaserValue(View view) {

        BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();


        try {

            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.actual_caseQty).getTag()) != null)
                ((TextView) view.findViewById(R.id.actual_caseQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.actual_caseQty)
                                        .getTag()));

            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.actual_PcQty).getTag()) != null)
                ((TextView) view.findViewById(R.id.actual_PcQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.actual_PcQty)
                                        .getTag()));

            ((TextView) view.findViewById(R.id.actual_caseQty)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            ((TextView) view.findViewById(R.id.actual_PcQty)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));

            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_PCS) {
                (view.findViewById(R.id.actual_PcQty)).setVisibility(View.GONE);
            }

            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_CASE) {
                (view.findViewById(R.id.actual_caseQty)).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_save:
                showConfirmAlert();
                break;
            case R.id.btn_cancel:
                showCancelAlert();
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
                if (getView() != null) {
                    Button ed = getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
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
                Toast.makeText(getActivity(), getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");

            }

        } else {
            if ((!append.isEmpty()) && (currentSelectedValue >= Integer.parseInt(append))) {
                QUANTITY.setText(append);
            } else {
                Toast.makeText(getActivity(), getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");
            }
        }
    }

    public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliveryDialog.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {

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
        public SalesReturnDeliveryDialog.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.salesreturn_deliverydetails, parent, false);
            return new SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder(view);
        }

        /**
         * @param holder   :view Holder
         * @param position : position of each Row
         *                 set the values to the views
         */
        @Override
        public void onBindViewHolder(@NonNull final SalesReturnDeliveryDialog.SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, int position) {


                holder.productName.setText(list.get(holder.getAdapterPosition()).getProductName());
                holder.actualPieceQuantity.setText(String.valueOf(list.get(holder.getAdapterPosition()).getActualPieceQuantity()));
                holder.actualCaseQuantity.setText(String.valueOf(list.get(holder.getAdapterPosition()).getActualCaseQuantity()));
                holder.reason.setText(list.get(holder.getAdapterPosition()).getReason());

                holder.actualCaseQuantity.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.actualCaseQuantity;
                        currentSelectedValue = list.get(holder.getAdapterPosition()).getReturnCaseQuantity();
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

        class SalesReturnDeliveryViewHolder extends RecyclerView.ViewHolder{

            TextView productName, returnCaseQuantity, returnPieceQuantity, reason;
            EditText actualCaseQuantity, actualPieceQuantity;


            private SalesReturnDeliveryViewHolder(View itemView) {
                super(itemView);
                    reason = itemView.findViewById(R.id.txt_reason);
                    productName = itemView.findViewById(R.id.txt_productName);
                    returnCaseQuantity = itemView.findViewById(R.id.txt_returnCaseQuantity);
                    returnCaseQuantity.setVisibility(View.GONE);
                    returnPieceQuantity = itemView.findViewById(R.id.txt_returnPieceQuantity);
                    returnPieceQuantity.setVisibility(View.GONE);
                    actualCaseQuantity = itemView.findViewById(R.id.txt_actualCaseQuantity);
                    actualPieceQuantity = itemView.findViewById(R.id.txt_actualPieceQuantity);

                    productName.setTypeface(FontUtils.getProductNameFont(mContext));
                    reason.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, mContext));
                    actualCaseQuantity.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, mContext));
                    actualPieceQuantity.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, mContext));

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
}
