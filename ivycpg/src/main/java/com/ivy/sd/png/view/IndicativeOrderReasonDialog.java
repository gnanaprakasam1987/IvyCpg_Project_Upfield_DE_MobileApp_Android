package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 12/9/2016.
 */

public class IndicativeOrderReasonDialog extends Dialog implements View.OnClickListener {

    BusinessModel bmodel;
    Context mContext;
    ArrayList<SpinnerBO> mReasonList;
    private ArrayAdapter<SpinnerBO> mReasonAdapter;
    ArrayList<ProductMasterBO> mProducts;
    ListView listView;
    Button btnOK;

    public IndicativeOrderReasonDialog(Context mContext, BusinessModel bmodel) {
        super(mContext);
        this.bmodel = bmodel;
        this.mContext = mContext;

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        setContentView(R.layout.dialog_indicativeorder_reason);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        // setTitle(mContext.getResources().getString(R.string.reason_req_for_indicative_order));
    }

    @Override
    protected void onStart() {
        super.onStart();

        listView = (ListView) findViewById(R.id.lv_products);
        btnOK = (Button) findViewById(R.id.okButton);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ((TextView) findViewById(R.id.titlebar)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        if (bmodel.configurationMasterHelper.IS_SHOW_ORDER_REASON) {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(((TextView)
                        findViewById(R.id.titlebar))
                        .getTag()) != null)
                    ((TextView) findViewById(R.id.titlebar)).setText(bmodel.labelsMasterHelper
                            .applyLabels(((TextView) findViewById(R.id.titlebar)).getTag()));
                else
                    ((TextView) findViewById(R.id.titlebar)).setText(mContext.getResources().getString(R.string.reason_for_ordered_prods));


            } catch (Exception e) {
                ((TextView) findViewById(R.id.titlebar)).setText(mContext.getResources().getString(R.string.reason_for_ordered_prods));
            }
        }


        mReasonList = new ArrayList<>();
        mReasonList.add(0, new SpinnerBO(0, "Select"));
        ArrayList<ReasonMaster> reasons = bmodel.reasonHelper.downloadIndicativeReasons();
        for (ReasonMaster bo : reasons)
            mReasonList.add(new SpinnerBO(SDUtil.convertToInt(bo.getReasonID()), bo.getReasonDesc()));

        mReasonAdapter = new ArrayAdapter<SpinnerBO>(mContext,
                android.R.layout.simple_spinner_item, mReasonList);
        mReasonAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProducts = new ArrayList<>();
        for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
            if (bmodel.configurationMasterHelper.IS_SHOW_ORDER_REASON) {
                if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                    mProducts.add(product);
                }
            } else {
                if (product.getOrderedCaseQty() > 0)
                    if (product.getOrderedCaseQty() < product.getIndicativeOrder_oc())
                        mProducts.add(product);
            }
        }

        MyAdapter adapter = new MyAdapter(mProducts);
        listView.setAdapter(adapter);

    }

    private class MyAdapter extends ArrayAdapter {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(mContext, R.layout.row_indicative_reason_dialog, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_indicative_reason_dialog, parent,
                        false);
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder = new ViewHolder();
                holder.tv_pname = (TextView) row.findViewById(R.id.productname);
                holder.tv_indicativeOrder = (TextView) row
                        .findViewById(R.id.io_case);
                holder.tv_cleanOrder = (TextView) row
                        .findViewById(R.id.co_case);
                holder.spn_reason = (Spinner) row.findViewById(R.id.reason);

                holder.tv_case = (TextView) row
                        .findViewById(R.id.ordered_case);
                holder.tv_outer = (TextView) row
                        .findViewById(R.id.ordered_outer);
                holder.tv_pcs = (TextView) row
                        .findViewById(R.id.ordered_pcs);
                holder.editText_remark = (EditText) row.
                        findViewById(R.id.remarks);

                holder.tv_pname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.tv_indicativeOrder.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cleanOrder.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_case.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_outer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_pcs.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.editText_remark.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                // set filter to not allow emoji's and mentioned(specialChars) symbol are not allowed
                InputFilter filter = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            String specialChars = "\"'<>";

                            int type = Character.getType(source.charAt(i));
                            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL
                                    || specialChars.contains("" + source)
                                    || Character.isWhitespace(0)) {
                                return "";
                            }
                        }
                        return null;
                    }
                };

                holder.editText_remark.setFilters(new InputFilter[]{filter});

                if (bmodel.configurationMasterHelper.IS_SHOW_ORDER_REASON) {
                    row.findViewById(R.id.layout_io).setVisibility(View.GONE);
                    row.findViewById(R.id.llio).setVisibility(View.GONE);
                    row.findViewById(R.id.llco).setVisibility(
                            View.GONE);
                } else {
                    row.findViewById(R.id.layout_io).setVisibility(View.VISIBLE);
                    try {
                        ((TextView) findViewById(R.id.ioTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.ioTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.ioTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.ioTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }

                    try {
                        ((TextView) findViewById(R.id.coTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.coTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.coTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.coTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }

                }


                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    row.findViewById(R.id.llCase).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    row.findViewById(R.id.llOuter).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    row.findViewById(R.id.llPiece).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }


                holder.editText_remark.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String remark = editable.toString();
                        holder.productObj.setRemarks(remark);


                    }
                });
                holder.spn_reason.setAdapter(mReasonAdapter);
                holder.spn_reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        SpinnerBO reString = (SpinnerBO) adapterView.getSelectedItem();
                        holder.productObj.setSoreasonId(reString.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productObj = items.get(position);

            holder.tv_pname.setText(holder.productObj.getProductShortName());
            holder.tv_indicativeOrder.setText(holder.productObj.getIndicative_flex_oc() + "");
            holder.tv_cleanOrder.setText(holder.productObj.getIndicativeOrder_oc() + "");

            holder.tv_case.setText(holder.productObj.getOrderedCaseQty() + "");
            holder.tv_outer.setText(holder.productObj.getOrderedOuterQty() + "");
            holder.tv_pcs.setText(holder.productObj.getOrderedPcsQty() + "");

            if (holder.productObj.getSoreasonId() != 0) {
                for (int i = 0; i < mReasonList.size(); i++)
                    if (mReasonList.get(i).getId() == holder.productObj.getSoreasonId()) {
                        holder.spn_reason.setSelection(i);
                        break;
                    }

            }

            holder.editText_remark.setText(holder.productObj.getRemarks());
           /* TypedArray typearr = mContext.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }*/
            return (row);
        }
    }

    class ViewHolder {

        ProductMasterBO productObj;
        TextView tv_pname, tv_indicativeOrder, tv_cleanOrder, tv_case, tv_outer, tv_pcs;
        Spinner spn_reason;
        EditText editText_remark;
    }

    @Override
    public void onClick(View view) {

    }
}
