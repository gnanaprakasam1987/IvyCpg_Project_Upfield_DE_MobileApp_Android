package com.ivy.sd.png.view;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import static android.app.Activity.RESULT_OK;


/*Created by Mansoor.k 28-04-2017*/
public class ReturnFragment extends IvyBaseFragment {
    BusinessModel bmodel;
    private String Pid;
    private InputMethodManager inputManager;
    private Button btnSave;
    private EditText mSelectedET;
    private ListView returnList;
    private ProductMasterBO productMasterBO;
    private SalesReturnHelper salesReturnHelper;
    private static String outPutDateFormat;
    private TextView tvAddreason;
    private MyAdapter adapter;
    private ArrayAdapter<SalesReturnReasonBO> spinnerAdapter;
    private View view;
    static Button dateBtn;
    private int holderPosition, holderTop;
    private ProgressDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_return,
                container, false);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        inputManager = (InputMethodManager) getActivity().getSystemService(
                getActivity().INPUT_METHOD_SERVICE);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        bmodel.configurationMasterHelper.checkSalesReturnValidateConfig();
        bmodel.configurationMasterHelper.checkSalesReturnSignConfig();
        Pid = getArguments().getString("pid");
        holderPosition = getArguments().getInt("position", 0);
        holderTop = getArguments().getInt("top", 0);

        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());
        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;

        initializeViews();
        process();

        setNumberPadlistener();
    }

    private void initializeViews() {
        mSelectedET = null;
        btnSave =  view.findViewById(R.id.btn_save);
        tvAddreason =  view.findViewById(R.id.tvAddreason);
        returnList =  view.findViewById(R.id.list);
        returnList.setCacheColorHint(0);

        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));
        tvAddreason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

    }

    private void process() {

        if (Pid != null) {
            if (getArguments().getString("from").equals("ORDER"))
                productMasterBO = bmodel.productHelper.getProductMasterBOById(Pid);
            else
                productMasterBO = bmodel.productHelper.getSalesReturnProductBOById(Pid);
        }
        if (productMasterBO != null && productMasterBO.getSalesReturnReasonList() != null) {
            //for pre saler
            if (productMasterBO.getSalesReturnReasonList().isEmpty())
                addrow();

            adapter = new MyAdapter((ArrayList<SalesReturnReasonBO>) productMasterBO.getSalesReturnReasonList());
            returnList.setAdapter(adapter);
        }
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        SalesReturnReasonBO reason = new SalesReturnReasonBO();
        reason.setReasonID("0");
        reason.setReasonDesc(getResources().getString(R.string.select_reason));
        reason.setReasonCategory("");
        spinnerAdapter.add(reason);

        for (SalesReturnReasonBO salesReturnReasonBO : bmodel.reasonHelper.getReasonSalesReturnMaster()) {
            spinnerAdapter.add(salesReturnReasonBO);
        }

        tvAddreason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addrow();
                adapter.notifyDataSetChanged();
                returnList.setSelection(adapter.getCount() - 1);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //implemented lot and invoice number validation like Reason validation ie only after any pc/cs/outer is entered
                if (!isReasonAndOtherFieldsAvailable()) {
                    return;
                }

                if (salesReturnHelper.SHOW_SR_INVOICE_NUMBER || salesReturnHelper.SHOW_LOTNUMBER)
                    for (SalesReturnReasonBO sb : productMasterBO.getSalesReturnReasonList()) {
                        if ((salesReturnHelper.SHOW_SR_INVOICE_NUMBER && sb.getInvoiceno().equals("0"))
                                || (salesReturnHelper.SHOW_LOTNUMBER && sb.getLotNumber().equals("0"))) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.mandatory_fileds_empty), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }


                if (isInvNoDuplicated()) {
                    Toast.makeText(getActivity(),
                            R.string.invoice_duplicated,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                removeEmptyRow();

                if (bmodel.configurationMasterHelper.IS_SALES_RETURN_VALIDATE) {
                    new validateSalesReturn().execute();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("position", holderPosition);
                    intent.putExtra("top", holderTop);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });

    }

    class validateSalesReturn extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialog = new ProgressDialog(getActivity());
            alertDialog.setMessage(getResources().getString(R.string.validating_sales));
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            return bmodel.synchronizationHelper.validateSalesReturn(productMasterBO);
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            alertDialog.dismiss();
            String message = "";
            if (s == 0) {
                message = "Invalid Sales Return!";
            } else if (s == 1) {
                message = "Valid Sales Return!";
            } else if (s == 2) {
                message = "Unable to process validation!";
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("position", holderPosition);
            intent.putExtra("top", holderTop);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();

        }
    }

    private boolean isInvNoDuplicated() {
        ArrayList<String> mSelectedReasonIds = new ArrayList<>();
        ArrayList<String> mSelectedInvNos = new ArrayList<>();
        ArrayList<String> mSelectedLotNos = new ArrayList<>();

        for (SalesReturnReasonBO sb : productMasterBO.getSalesReturnReasonList()) {
            if (sb.getReasonID() != null && !sb.getReasonID().equals("0")) {

                if (mSelectedReasonIds.contains(sb.getReasonID())
                        && (salesReturnHelper.SHOW_SR_INVOICE_NUMBER && mSelectedInvNos.contains(sb.getInvoiceno()))
                        && (salesReturnHelper.SHOW_LOTNUMBER && mSelectedLotNos.contains(sb.getLotNumber()))) {
                    return true;
                } else {
                    mSelectedReasonIds.add(sb.getReasonID());
                    mSelectedInvNos.add(sb.getInvoiceno());
                    mSelectedLotNos.add(sb.getLotNumber());
                }
            }
        }
        return false;
    }

    private boolean isReasonAndOtherFieldsAvailable() {
        for (SalesReturnReasonBO sb : productMasterBO.getSalesReturnReasonList()) {
            if (sb.getCaseQty() > 0 || sb.getPieceQty() > 0 || sb.getOuterQty() > 0) {
                if (sb.getReasonID().equals("0")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.select_reason) + "!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if ((salesReturnHelper.SHOW_SR_INVOICE_NUMBER && (sb.getInvoiceno().equals("") || sb.getInvoiceno().equals("0"))) ||
                        (salesReturnHelper.SHOW_LOTNUMBER && sb.getLotNumber().equals(""))) {//inv n lot num validation done based on their conifguration
                    Toast.makeText(getActivity(), "Mandatory fields empty!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private void removeEmptyRow() {
        Iterator<SalesReturnReasonBO> it = productMasterBO.getSalesReturnReasonList().iterator();
        while (it.hasNext()) {
            SalesReturnReasonBO srObj = it.next();
            if (srObj.getCaseQty() == 0 && srObj.getPieceQty() == 0 && srObj.getOuterQty() == 0) {
                it.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (salesReturnHelper.SHOW_SAL_RET_OLD_MRP || salesReturnHelper.SHOW_SRP_EDIT)
            onDotBtnEnable();
    }

    private void onDotBtnEnable() {
        try {
            view.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }


    private void addrow() {
        SalesReturnReasonBO item = new SalesReturnReasonBO();
        item.setCaseSize(productMasterBO.getCaseSize());
        item.setOuterSize(productMasterBO.getOutersize());
        item.setProductShortName(productMasterBO.getProductShortName());
        item.setOldMrp(productMasterBO.getMRP());
        item.setSrpedit(productMasterBO.getSrp());
        //changes done for validation
        Long timeStamp = System.currentTimeMillis();
        item.setRowId(timeStamp.toString());
        item.setStatus("2");
        productMasterBO.getSalesReturnReasonList().add(item);

    }

    class MyAdapter extends ArrayAdapter<SalesReturnReasonBO> {
        final ArrayList<SalesReturnReasonBO> items;

        MyAdapter(ArrayList<SalesReturnReasonBO> items) {
            super(getActivity(), R.layout.row_salesreturn_entry_listitem,
                    items);
            this.items = items;
        }

        public SalesReturnReasonBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            SalesReturnReasonBO salesReturnReasonBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());

                row = inflater.inflate(R.layout.row_salesreturn_entry_listitem, parent, false);

                holder = new ViewHolder();

                holder.reasonSpinner = (Spinner) row.findViewById(R.id.reasonSpinner);

                holder.caseQty = (EditText) row.findViewById(R.id.productqtyCases);
                holder.pieceQty = (EditText) row.findViewById(R.id.productqtyPieces);
                holder.outerQty = (EditText) row.findViewById(R.id.outerproductqtyCases);
                holder.mfgDate = (Button) row.findViewById(R.id.mfgDate);
                holder.expDate = (Button) row.findViewById(R.id.expDate);
                holder.oldMrp = (EditText) row.findViewById(R.id.oldMrp);
                holder.invoiceno = (EditText) row.findViewById(R.id.invoiceno);
                holder.srpedit = (EditText) row.findViewById(R.id.srpedit);
                holder.lotNumber = (EditText) row.findViewById(R.id.lotnumber);
                holder.ivClose = (ImageView) row.findViewById(R.id.ivClose);

                //typefaces
                holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.oldMrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.invoiceno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.srpedit.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.lotNumber.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mfgDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.expDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_invoiceno_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_lotno_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_oldmrp_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_srpEdit_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_piece_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_case_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_outer_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_mfd_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_exp_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                try {
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.tv_oldmrp_title).getTag()) != null)
                        ((TextView) row.findViewById(R.id.tv_oldmrp_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.tv_oldmrp_title)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.tv_srpEdit_title).getTag()) != null)
                        ((TextView) row.findViewById(R.id.tv_srpEdit_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.tv_srpEdit_title)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }

                if (!salesReturnHelper.SHOW_SALES_RET_CASE)
                    ((LinearLayout) row.findViewById(R.id.ll_case)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_case_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_case_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_case_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SALES_RET_PCS)
                    ((LinearLayout) row.findViewById(R.id.ll_piece)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_piece_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_piece_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_piece_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SALES_RET_OUTER_CASE)
                    ((LinearLayout) row.findViewById(R.id.ll_outer)).setVisibility(View.GONE);
                else {

                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_outer_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_outer_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_outer_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }

                if (!salesReturnHelper.SHOW_SAL_RET_OLD_MRP)
                    ((LinearLayout) row.findViewById(R.id.ll_oldmrp)).setVisibility(View.GONE);
                if (!salesReturnHelper.SHOW_SRP_EDIT)
                    ((LinearLayout) row.findViewById(R.id.ll_srpedit)).setVisibility(View.GONE);

                if (!salesReturnHelper.SHOW_SAL_RET_MFG_DATE)
                    (row.findViewById(R.id.ll_mfd)).setVisibility(View.GONE);
                if (!salesReturnHelper.SHOW_SAL_RET_EXP_DATE)
                    (row.findViewById(R.id.ll_exp)).setVisibility(View.GONE);

                if (!salesReturnHelper.SHOW_LOTNUMBER)
                    ((LinearLayout) row.findViewById(R.id.ll_lot_no)).setVisibility(View.GONE);
                if (!salesReturnHelper.SHOW_SR_INVOICE_NUMBER)
                    ((LinearLayout) row.findViewById(R.id.ll_invoie_no)).setVisibility(View.GONE);

                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (productMasterBO.getOutersize() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();

                        if (!"".equals(qty)) {
                            holder.reasonBO.setOuterQty(SDUtil
                                    .convertToInt(qty));
                        }
                    }
                });

                holder.pieceQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();
                        if (!"".equals(qty)) {
                            holder.reasonBO.setPieceQty(SDUtil
                                    .convertToInt(qty));
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }
                });

                holder.caseQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (productMasterBO.getCaseSize() == 0) {
                            holder.caseQty.removeTextChangedListener(this);
                            holder.caseQty.setText("0");
                            holder.caseQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();

                        if (!"".equals(qty)) {
                            holder.reasonBO.setCaseQty(SDUtil
                                    .convertToInt(qty));
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }
                });

                holder.oldMrp.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty)) {

                            holder.reasonBO.setOldMrp(SDUtil
                                    .convertToDouble(qty));
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }
                });

                holder.invoiceno.setImeOptions(EditorInfo.IME_ACTION_DONE);
                holder.invoiceno.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty) && !"0".equals(qty)) {
                            holder.reasonBO.setInvoiceno(qty);
                        } else
                            holder.reasonBO.setInvoiceno("");
                    }
                });

                holder.srpedit.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty)
                                && productMasterBO.getSrp() != SDUtil
                                .convertToFloat(qty)) {

                            holder.reasonBO.setSrpedit(SDUtil
                                    .convertToFloat(qty));
                        }
                    }
                });

                holder.lotNumber.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // no operation
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // no operation
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty)
                                && !"0".equals(qty)) {
                            holder.reasonBO.setLotNumber(qty);
                        }
                    }
                });

                holder.outerQty.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.outerQty;
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        holder.outerQty.selectAll();
                        holder.outerQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.caseQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.caseQty;
                        int inType = holder.caseQty.getInputType();
                        holder.caseQty.setInputType(InputType.TYPE_NULL);
                        holder.caseQty.onTouchEvent(event);
                        holder.caseQty.setInputType(inType);
                        holder.caseQty.selectAll();
                        holder.caseQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.pieceQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.pieceQty;
                        int inType = holder.pieceQty.getInputType();
                        holder.pieceQty.setInputType(InputType.TYPE_NULL);
                        holder.pieceQty.onTouchEvent(event);
                        holder.pieceQty.setInputType(inType);
                        holder.pieceQty.selectAll();
                        holder.pieceQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });


                holder.oldMrp.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.oldMrp;
                        int inType = holder.oldMrp.getInputType();
                        holder.oldMrp.setInputType(InputType.TYPE_NULL);
                        holder.oldMrp.onTouchEvent(event);
                        holder.oldMrp.setInputType(inType);
                        holder.oldMrp.selectAll();
                        holder.oldMrp.requestFocus();

                        return true;
                    }
                });

                holder.srpedit.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.srpedit;
                        int inType = holder.srpedit.getInputType();
                        holder.srpedit.setInputType(InputType.TYPE_NULL);
                        holder.srpedit.onTouchEvent(event);
                        holder.srpedit.setInputType(inType);
                        holder.srpedit.selectAll();
                        holder.srpedit.requestFocus();

                        return true;
                    }
                });
                //without following listener text is added beside existing "0"
                holder.invoiceno.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        mSelectedET = holder.invoiceno;
                        int inType = holder.invoiceno.getInputType();
                        holder.invoiceno.setInputType(InputType.TYPE_NULL);
                        holder.invoiceno.onTouchEvent(event);
                        holder.invoiceno.setInputType(inType);
                        holder.invoiceno.selectAll();
                        holder.invoiceno.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.reasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        SalesReturnReasonBO reString = (SalesReturnReasonBO) holder.reasonSpinner
                                .getSelectedItem();
                        holder.reasonBO.setReasonID(reString.getReasonID());
                        holder.reasonBO.setReasonDesc(reString.getReasonDesc());
                        holder.reasonBO.setReasonCategory(reString.getReasonCategory());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });


                holder.mfgDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mfgDate;
                        dateBtn.setTag(holder.reasonBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(),
                                "datePicker1");
                    }
                });

                holder.expDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.expDate;
                        dateBtn.setTag(holder.reasonBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(),
                                "datePicker2");
                    }
                });

                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        mSelectedET = holder.pieceQty;
                        holder.pieceQty.selectAll();
                        holder.pieceQty.requestFocus();
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();

            }

            holder.ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    productMasterBO.getSalesReturnReasonList().remove(getItem(position));
                    notifyDataSetChanged();


                }
            });

            holder.reasonBO = salesReturnReasonBO;

            holder.reasonSpinner.setAdapter(spinnerAdapter);

            if (holder.reasonBO.getReasonID() != null) {

                if (!holder.reasonBO.getReasonID().equals("0")) {
                    for (int i = 0; i < bmodel.reasonHelper.getReasonSalesReturnMaster().size(); i++) {
                        if (holder.reasonBO.getReasonID().equals(bmodel.reasonHelper.getReasonSalesReturnMaster().get(i).getReasonID()))
                            holder.reasonSpinner.setSelection(i + 1);
                    }
                }
            }

            if (holder.reasonBO.getCaseSize() > 0)
                ((TextView) row.findViewById(R.id.tv_case_title)).setText(getResources().getString(R.string.avail_cases) + "(" + holder.reasonBO.getCaseSize() + " pcs)");
            else
                ((TextView) row.findViewById(R.id.tv_case_title)).setText(getResources().getString(R.string.avail_cases));

            if (holder.reasonBO.getOuterSize() > 0)
                ((TextView) row.findViewById(R.id.tv_outer_title)).setText(getResources().getString(R.string.avail_outer) + "(" + holder.reasonBO.getOuterSize() + " pcs)");
            else
                ((TextView) row.findViewById(R.id.tv_outer_title)).setText(getResources().getString(R.string.avail_outer));


            String strCaseQty = holder.reasonBO.getCaseQty() + "";
            holder.caseQty.setText(strCaseQty);
            String strPieceQty = holder.reasonBO.getPieceQty() + "";
            holder.pieceQty.setText(strPieceQty);
            String strOuterQty = holder.reasonBO.getOuterQty() + "";
            holder.outerQty.setText(strOuterQty);

            String strOldMrp = bmodel.formatValue(holder.reasonBO.getOldMrp()) + "";
            holder.oldMrp.setText(strOldMrp);
            String strSrpEdit = holder.reasonBO.getSrpedit() + "";
            holder.srpedit.setText(strSrpEdit);

            if (holder.reasonBO.getLotNumber() != null)
                holder.lotNumber.setText(holder.reasonBO.getLotNumber());
            else
                holder.lotNumber.setText("");

            if (holder.reasonBO.getInvoiceno() != null) {
                String strInvoiceno = holder.reasonBO.getInvoiceno() + "";
                holder.invoiceno.setText(strInvoiceno);
            } else
                holder.invoiceno.setText("");

            if (holder.reasonBO.getCaseQty() == 0 && holder.reasonBO.getPieceQty() == 0
                    && holder.reasonBO.getOuterQty() == 0) {

                holder.mfgDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.expDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
            } else {
                holder.mfgDate
                        .setText((holder.reasonBO.getMfgDate() == null) ? DateUtil
                                .convertFromServerDateToRequestedFormat(
                                        SDUtil.now(SDUtil.DATE_GLOBAL),
                                        outPutDateFormat) : DateUtil.convertFromServerDateToRequestedFormat(
                                holder.reasonBO.getMfgDate(),
                                ConfigurationMasterHelper.outDateFormat));
                holder.expDate
                        .setText((holder.reasonBO.getExpDate() == null) ? DateUtil
                                .convertFromServerDateToRequestedFormat(
                                        SDUtil.now(SDUtil.DATE_GLOBAL),
                                        outPutDateFormat) : DateUtil.convertFromServerDateToRequestedFormat(
                                holder.reasonBO.getExpDate(),
                                ConfigurationMasterHelper.outDateFormat));
            }

            if (productMasterBO.getOuUomid() == 0 || !productMasterBO.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (productMasterBO.getCaseUomId() == 0 || !productMasterBO.isCaseMapped()) {
                holder.caseQty.setEnabled(false);
            } else {
                holder.caseQty.setEnabled(true);
            }
            if (productMasterBO.getPcUomid() == 0 || !productMasterBO.isPieceMapped()) {
                holder.pieceQty.setEnabled(false);
            } else {
                holder.pieceQty.setEnabled(true);
            }


            return row;
        }
    }

    class ViewHolder {
        private SalesReturnReasonBO reasonBO;
        private Spinner reasonSpinner;
        private EditText pieceQty;
        private EditText caseQty;
        private EditText oldMrp;
        private EditText outerQty;
        private EditText invoiceno;
        private EditText srpedit;
        private EditText lotNumber;
        private Button mfgDate;
        private Button expDate;
        private ImageView ivClose;
    }


    private void setNumberPadlistener() {
        view.findViewById(R.id.calczero)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcone)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calctwo)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcthree)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcfour)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcfive)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcsix)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcseven)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calceight)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcnine)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdel)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdot)
                .setOnClickListener(mNumperPadListener);
        view.findViewById(R.id.calcdot).setVisibility(View.GONE);
    }

    private final View.OnClickListener mNumperPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mSelectedET == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.please_select_item), 0);
            } else {
                int i = v.getId();
                if (i == R.id.calczero || i == R.id.calcone || i == R.id.calctwo || i == R.id.calcthree
                        || i == R.id.calcfour || i == R.id.calcfive || i == R.id.calcsix
                        || i == R.id.calcseven || i == R.id.calceight || i == R.id.calcnine) {
                    eff(((Button) v).getText().toString());
                } else if (i == R.id.calcdel) {
                    String s = mSelectedET.getText().toString();

                    if (!(s.length() == 0)) {
                        s = s.substring(0, s.length() - 1);
                        if (s.length() == 0)
                            s = "0";
                    }
                    mSelectedET.setText(s);

                } else if (i == R.id.calcdot) {
                    String s = mSelectedET.getText().toString();
                    String s1 = (String) mSelectedET.getTag();
                    if (s1 != null) {
                        if (!s.contains(".") && "DOT".equals(s1)) {
                            String strQty = s + ".";
                            mSelectedET.setText(strQty);
                        }
                    }

                }
            }

        }
    };

    private void eff(String val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {

            String s = mSelectedET.getText().toString();

            if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s))
                mSelectedET.setText(val);
            else
                mSelectedET.setText(mSelectedET.getText().append(val));
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unbindDrawables(view.findViewById(R.id.root));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);

            SalesReturnReasonBO bo;
            bo = (SalesReturnReasonBO) dateBtn.getTag();

            if ("datePicker1".equals(this.getTag())) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setMfgDate(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                } else {
                    bo.setMfgDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                }
            } else if ("datePicker2".equals(this.getTag())) {
                if (bo.getMfgDate() != null && bo.getMfgDate().length() > 0) {
                    Date dateMfg = DateUtil.convertStringToDateObject(
                            bo.getMfgDate(), ConfigurationMasterHelper.outDateFormat);
                    if (dateMfg != null && selectedDate.getTime() != null
                            && dateMfg.after(selectedDate.getTime())) {
                        Toast.makeText(getActivity(),
                                R.string.expdate_set_after_mfgdate,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        bo.setExpDate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                        dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                    }
                } else {
                    bo.setExpDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                }
            }

        }
    }

}
