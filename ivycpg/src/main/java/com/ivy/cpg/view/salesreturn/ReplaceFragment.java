package com.ivy.cpg.view.salesreturn;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CustomKeyBoard;

import java.util.Iterator;

import static android.app.Activity.RESULT_OK;


/*Created by Mansoor.k 28-04-2017*/
public class ReplaceFragment extends IvyBaseFragment {
    BusinessModel bmodel;
    private String Pid;
    private InputMethodManager inputManager;
    private Button btnSave;
    private EditText mSelectedET;
    private ProductMasterBO productMasterBO;
    private SalesReturnHelper salesReturnHelper;
    private View view;

    private TextView tvReturnQty;
    private EditText etRepPiece, etRepCase, etRepOuter;
    private CustomKeyBoard dialogCustomKeyBoard;
    private int holderPosition, holderTop;
    private String moduleFrom;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_replace,
                container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputManager = (InputMethodManager) getActivity().getSystemService(
                getActivity().INPUT_METHOD_SERVICE);


        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        Pid = getArguments().getString("pid");
        holderPosition = getArguments().getInt("position", 0);
        holderTop = getArguments().getInt("top", 0);
        moduleFrom = getArguments().getString("from");

        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());

        initializeViews(view);
        setNumberPadlistener(view);
    }

    @Override
    public void onStart() {
        super.onStart();

        process();
    }

    private void initializeViews(View view) {
        mSelectedET = null;
        (view.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        btnSave = view.findViewById(R.id.btn_save);
        tvReturnQty = view.findViewById(R.id.tvReturnQty);

        etRepPiece = view.findViewById(R.id.et_rep_pcValue);
        etRepCase = view.findViewById(R.id.et_rep_csValue);
        etRepOuter = view.findViewById(R.id.et_rep_ouValue);

    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void process() {
        if (Pid != null) {
            if (getArguments().getString("from").equals("ORDER"))
                productMasterBO = bmodel.productHelper.getProductMasterBOById(Pid);
            else
                productMasterBO = salesReturnHelper.getSalesReturnProductBOById(Pid);
        }
        if (productMasterBO != null) {
            int total = 0, caseSize = 0, outerSize = 0;
            for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList()) {
                total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                caseSize = obj.getCaseSize();
                outerSize = obj.getOuterSize();
            }
            String strTotal = Integer.toString(total);
            tvReturnQty.setText(strTotal);

            if (caseSize > 0)
                ((TextView) view.findViewById(R.id.srcaseTitle)).setText(getResources().getString(R.string.avail_cases) + "(" + caseSize + " pcs)");
            else
                ((TextView) view.findViewById(R.id.srcaseTitle)).setText(getResources().getString(R.string.avail_cases));

            if (outerSize > 0)
                ((TextView) view.findViewById(R.id.sroutercaseTitle)).setText(getResources().getString(R.string.avail_outer) + "(" + outerSize + " pcs)");
            else
                ((TextView) view.findViewById(R.id.sroutercaseTitle)).setText(getResources().getString(R.string.avail_outer));


            etRepPiece.setText(productMasterBO.getRepPieceQty() + "");
            etRepCase.setText(productMasterBO.getRepCaseQty() + "");
            etRepOuter.setText(productMasterBO.getRepOuterQty() + "");

            etRepPiece.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // no operation
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // no operation
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (qty.length() > 0)
                        etRepPiece.setSelection(qty.length());

                    if (!("".equals(qty))) {
                        int enteredQty = SDUtil.convertToInt(qty);
                        int totalRepQty = enteredQty + (productMasterBO.getRepCaseQty() * productMasterBO.getCaseSize()) + (productMasterBO.getRepOuterQty() * productMasterBO.getOutersize());
                        int totalReturnQty = 0;
                        for (SalesReturnReasonBO reasonBO : productMasterBO.getSalesReturnReasonList()) {
                            totalReturnQty = totalReturnQty + reasonBO.getPieceQty() + (reasonBO.getCaseQty() * productMasterBO.getCaseSize()) + (reasonBO.getOuterQty() * productMasterBO.getOutersize());
                        }

                        if ((bmodel.configurationMasterHelper.IS_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL || totalReturnQty >= totalRepQty)
                                && (productMasterBO.getSIH() >= (totalRepQty + productMasterBO.getOrderedPcsQty()) || !bmodel.configurationMasterHelper.IS_SIH_VALIDATION)) {
                            productMasterBO.setRepPieceQty(enteredQty);
                        } else {
                            if (!("0".equals(qty))) {
                                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && productMasterBO.getSIH() < (totalRepQty + productMasterBO.getOrderedPcsQty()))
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    productMasterBO.getSIH()),
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(getResources().getString(R.string.total_items), totalReturnQty),
                                            Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                etRepPiece.setText(qty);
                            }
                        }
                    }
                }
            });

            etRepCase.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // no operation
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // no operation
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (productMasterBO.getCaseSize() == 0) {
                        etRepCase.removeTextChangedListener(this);
                        etRepCase.setText("0");
                        etRepCase.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();
                    if (qty.length() > 0)
                        etRepCase.setSelection(qty.length());
                    if (!("".equals(qty))) {

                        int totalRepQty = productMasterBO.getRepPieceQty() + (SDUtil.convertToInt(qty) * productMasterBO.getCaseSize()) + (productMasterBO.getRepOuterQty() * productMasterBO.getOutersize());
                        int totalReturnQty = 0;
                        for (SalesReturnReasonBO reasonBO : productMasterBO.getSalesReturnReasonList()) {
                            totalReturnQty = totalReturnQty + reasonBO.getPieceQty() + (reasonBO.getCaseQty() * productMasterBO.getCaseSize()) + (reasonBO.getOuterQty() * productMasterBO.getOutersize());
                        }

                        if ((moduleFrom.equals("ORDER") || totalReturnQty >= totalRepQty) && (productMasterBO.getSIH() >= (totalRepQty + productMasterBO.getOrderedCaseQty()) || !bmodel.configurationMasterHelper.IS_SIH_VALIDATION)) {
                            productMasterBO.setRepCaseQty(SDUtil
                                    .convertToInt(qty));
                        } else {
                            if (!("0".equals(qty))) {
                                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && productMasterBO.getSIH() < (totalRepQty + productMasterBO.getOrderedCaseQty()))
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    productMasterBO.getSIH()),
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(getResources().getString(R.string.total_items), totalReturnQty),
                                            Toast.LENGTH_SHORT).show();

                                /*
                                  Delete the last entered number and reset
                                  the qty
                                 */
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                etRepCase.setText(qty);
                            }
                        }
                    }
                }
            });

            etRepOuter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // no operation
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // no operation
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (productMasterBO.getOutersize() == 0) {
                        etRepOuter.removeTextChangedListener(this);
                        etRepOuter.setText("0");
                        etRepOuter.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();
                    if (qty.length() > 0)
                        etRepOuter.setSelection(qty.length());
                    if (!("".equals(qty))) {

                        int totalRepQty = productMasterBO.getRepPieceQty() + (productMasterBO.getRepCaseQty() * productMasterBO.getCaseSize()) + (SDUtil.convertToInt(qty) * productMasterBO.getOutersize());
                        int totalReturnQty = 0;
                        for (SalesReturnReasonBO reasonBO : productMasterBO.getSalesReturnReasonList()) {
                            totalReturnQty = totalReturnQty + reasonBO.getPieceQty() + (reasonBO.getCaseQty() * productMasterBO.getCaseSize()) + (reasonBO.getOuterQty() * productMasterBO.getOutersize());
                        }

                        if ((moduleFrom.equals("ORDER") || totalReturnQty >= totalRepQty) && (productMasterBO.getSIH() >= (totalRepQty + productMasterBO.getOrderedOuterQty()) || !bmodel.configurationMasterHelper.IS_SIH_VALIDATION)) {
                            productMasterBO.setRepOuterQty(SDUtil
                                    .convertToInt(qty));
                        } else {
                            if (!("0".equals(qty))) {
                                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && productMasterBO.getSIH() < (totalRepQty + productMasterBO.getOrderedOuterQty()))
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    productMasterBO.getSIH()),
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(
                                            getActivity(),
                                            String.format(getResources().getString(R.string.total_items), totalReturnQty),
                                            Toast.LENGTH_SHORT).show();

                                /*
                                  Delete the last entered number and reset
                                  the qty
                                 */
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                etRepOuter.setText(qty);
                            }
                        }
                    }
                }

            });
            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                etRepPiece.setFocusable(false);

                etRepPiece.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), etRepPiece);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }

                });
            } else {
                etRepPiece.setFocusable(true);
                etRepPiece.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = etRepPiece;
                        int inType = etRepPiece.getInputType();
                        etRepPiece.setInputType(InputType.TYPE_NULL);
                        etRepPiece.onTouchEvent(event);
                        etRepPiece.setInputType(inType);
                        etRepPiece.requestFocus();
                        if (etRepPiece.getText().length() > 0)
                            etRepPiece.setSelection(etRepPiece.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });
            }
            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                etRepCase.setFocusable(false);

                etRepCase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), etRepCase);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }

                });
            } else {
                etRepCase.setFocusable(true);

                etRepCase.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = etRepCase;
                        int inType = etRepCase.getInputType();
                        etRepCase.setInputType(InputType.TYPE_NULL);
                        etRepCase.onTouchEvent(event);
                        etRepCase.setInputType(inType);
                        etRepCase.requestFocus();
                        if (etRepCase.getText().length() > 0)
                            etRepCase.setSelection(etRepCase.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                etRepCase.getWindowToken(), 0);
                        return true;
                    }
                });
            }

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                etRepOuter.setFocusable(false);

                etRepOuter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), etRepOuter);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                etRepOuter.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = etRepOuter;
                        int inType = etRepOuter.getInputType();
                        etRepOuter.setInputType(InputType.TYPE_NULL);
                        etRepOuter.onTouchEvent(event);
                        etRepOuter.setInputType(inType);
                        etRepOuter.requestFocus();
                        if (etRepOuter.getText().length() > 0)
                            etRepOuter.setSelection(etRepOuter.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                mSelectedET.getWindowToken(), 0);
                        return true;
                    }
                });
            }
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isReasonAndOtherFieldsAvailable())
                    return;

                removeEmptyRow();

                Intent intent = new Intent();
                intent.putExtra("position", holderPosition);
                intent.putExtra("top", holderTop);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            }
        });

        if (!salesReturnHelper.SHOW_STOCK_REPLACE_PCS) {
            view.findViewById(R.id.ll_rep_piece).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.srpcsTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.srpcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.srpcsTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        if (!salesReturnHelper.SHOW_STOCK_REPLACE_CASE) {
            view.findViewById(R.id.ll_avail_case).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.srcaseTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.srcaseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.srcaseTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        if (!salesReturnHelper.SHOW_STOCK_REPLACE_OUTER) {
            view.findViewById(R.id.ll_avail_outer).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.sroutercaseTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.sroutercaseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.sroutercaseTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }


    }


    @Override
    public void onBackPressed() {
    }


    private void setNumberPadlistener(View view) {
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

    public boolean isReasonAndOtherFieldsAvailable() {
        for (SalesReturnReasonBO sb : productMasterBO.getSalesReturnReasonList()) {
            if (sb.getCaseQty() > 0 || sb.getPieceQty() > 0 || sb.getOuterQty() > 0) {
                if (sb.getReasonID().equals("0")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.select_reason) + "!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if ((salesReturnHelper.SHOW_SR_INVOICE_NUMBER_MANDATORY && (sb.getInvoiceno().equals("") || sb.getInvoiceno().equals("0"))) ||
                        (salesReturnHelper.SHOW_LOTNUMBER_MANDATORY && sb.getLotNumber().equals(""))) {//inv n lot num validation done based on their conifguration
                    Toast.makeText(getActivity(), "Mandatory fields empty!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private void removeEmptyRow() {
        try {
            Iterator<SalesReturnReasonBO> it = productMasterBO.getSalesReturnReasonList().iterator();
            while (it.hasNext()) {
                SalesReturnReasonBO srObj = it.next();
                if (srObj.getCaseQty() == 0 && srObj.getPieceQty() == 0 && srObj.getOuterQty() == 0) {
                    it.remove();
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

}
