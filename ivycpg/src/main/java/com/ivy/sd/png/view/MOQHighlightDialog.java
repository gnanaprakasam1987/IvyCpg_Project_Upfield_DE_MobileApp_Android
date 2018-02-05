package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

/**
 * Created by santhosh.c on 25-01-2018.
 * used to Update Piece value in listing
 */

@SuppressLint("ValidFragment")
public class MOQHighlightDialog extends DialogFragment implements View.OnClickListener {
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> productMstBo;
    private Vector<ProductMasterBO> productBoRfield;
    private Vector<ProductMasterBO> productBoRfieldChanges;
    private ListView lvwplist;
    private MyAdaper mSchedule;
    private savePcsValue listner;
    private Button save,closeButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        listner = (savePcsValue) getActivity();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            if (getDialog() != null) {
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            }

            getDialog().setCancelable(false);
            this.setCancelable(false);

            View view = inflater.inflate(R.layout.show_moq_highlight_dialog, container, false);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


            return view;
            }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        lvwplist = (ListView) getView().findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);
        productBoRfield=new Vector<>();
        productBoRfieldChanges=new Vector<>();
        productMstBo=bmodel.productHelper.getProductMaster();

        int count=productMstBo.size();
        for(int i=0;i<count;i++)
        {
            ProductMasterBO product = productMstBo.elementAt(i);
            if (product.getOrderedPcsQty() > 0 ) {

                if(!TextUtils.isEmpty(product.getRField1())) {
                    int res= SDUtil.convertToInt(product.getRField1());
                    if (product.getOrderedPcsQty() % res != 0)
                        productBoRfield.add(product);
                }
                Commons.print("product.getProductName()"+product.getProductShortName());
            }

        }
        //edtEmail.setWidth(outMetrics.widthPixels);
        mSchedule=new MyAdaper(productBoRfield);
        lvwplist.setAdapter(mSchedule);
        save = (Button) getView().findViewById(R.id.save_btn);
        closeButton= (Button) getView().findViewById(R.id.closeButton);
        closeButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        closeButton.setOnClickListener(this);
        save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        save.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.save_btn) {
            int size = bmodel.productHelper
                    .getProductMaster().size();
            int count = 0;
            for (int j = 0; j < size; ++j) {
                ProductMasterBO product = bmodel.productHelper
                        .getProductMaster().get(j);

                if (product.getOrderedPcsQty() > 0 && !TextUtils.isEmpty(product.getRField1())) {
                    //converting string Rfield1 value to integra
                    int res = SDUtil.convertToInt(product.getRField1());
                    if (product.getOrderedPcsQty() % res != 0)
                        count++;

                }
            }
            if(count==0) {
                for(ProductMasterBO value:productBoRfieldChanges)
                    updateData(value);

                listner.saveChanges();
                dismiss();
            }
            else
                Toast.makeText(bmodel,""+getString(R.string.Please_enter_multiple_of),Toast.LENGTH_SHORT).show();
        }
        if(i==R.id.closeButton)
            dismiss();

    }

    private class MyAdaper extends ArrayAdapter<ProductMasterBO>
    {
        private final Vector<ProductMasterBO> items;
        public MyAdaper(Vector<ProductMasterBO> items) {
            super(bmodel,
                    R.layout.ordered_piece_value, items);
            this.items = items;
        }

        @NonNull
        @Override
        @SuppressLint("RestrictedApi")
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            final ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                LayoutInflater inflater = getActivity().getLayoutInflater();

                //Configuration based row rendering
                row = inflater.inflate(
                        R.layout.ordered_piece_value, parent,
                        false);
                holder = new ViewHolder();
                holder.productBo=product;
                holder.productNameTxt = (TextView) row
                        .findViewById(R.id.orderPRODNAME);
                holder.orderQTYinpiece = (TextView) row
                        .findViewById(R.id.orderQTYinpiece);

                holder.rField1Txt = (EditText) row
                        .findViewById(R.id.rField1_qty);

                holder.productNameTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.rField1Txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                holder.productNameTxt.setText(""+product.getProductShortName());
                holder.orderQTYinpiece.setText( getString(R.string.multiple_of)+" "+product.getRField1());
                holder.rField1Txt.setText(""+product.getOrderedPcsQty());

                holder.rField1Txt.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int inType = holder.rField1Txt.getInputType();
                        holder.rField1Txt.setInputType(InputType.TYPE_CLASS_NUMBER);
                        holder.rField1Txt.onTouchEvent(event);
                        holder.rField1Txt.setInputType(inType);
                        holder.rField1Txt.setSelection(holder.rField1Txt.length());
                        holder.rField1Txt.requestFocus();

                        return true;
                    }
                });
                holder.rField1Txt.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        int res= SDUtil.convertToInt(product.getRField1());
                        String qty = s.toString();
                        Commons.print("qty"+qty+"res"+res);

                        if (!"".equals(qty)&&SDUtil.convertToInt(qty)%res==0) {
                            Commons.print("Value"+SDUtil.convertToInt(qty)%res);
                            holder.productBo.setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                            double tot = (holder.productBo.getOrderedCaseQty() * holder.productBo
                                    .getCsrp())
                                    + (holder.productBo.getOrderedPcsQty() * holder.productBo
                                    .getSrp())
                                    + (holder.productBo.getOrderedOuterQty() * holder.productBo
                                    .getOsrp());
                            Commons.print("tot"+tot);
                            holder.productBo.setTotalamount(tot);
                        }
                        if (SDUtil.convertToInt(qty)%res==0) {
                            productBoRfieldChanges.add(holder.productBo);
                        }
                    }
                });

                row.setTag(holder);
            }
            else
                holder = (ViewHolder) row.getTag();

            return row;
        }


        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

    }

    public class ViewHolder
    {
        ProductMasterBO productBo;
        TextView productNameTxt,orderQTYinpiece;
        EditText rField1Txt;
    }

    private void updateData(ProductMasterBO productBO) {
        int qty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

        if (qty == 0) {
            bmodel.productHelper.getmProductidOrderByEntry().remove((String) productBO.getProductID());
            bmodel.productHelper.getmProductidOrderByEntryMap().remove(Integer.parseInt(productBO.getProductID()));
        } else {
            int lastQty = 0;
            if (bmodel.productHelper.getmProductidOrderByEntryMap().get(Integer.parseInt(productBO.getProductID())) != null)
                lastQty = bmodel.productHelper.getmProductidOrderByEntryMap().get(Integer.parseInt(productBO.getProductID()));
            if (lastQty == qty) {
                // Dont do any thing
            } else {
                if (bmodel.productHelper.getmProductidOrderByEntry().contains(productBO.getProductID())) {
                    bmodel.productHelper.getmProductidOrderByEntry().remove((String) productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntry().add(productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntryMap().put(Integer.parseInt(productBO.getProductID()), qty);
                } else {
                    bmodel.productHelper.getmProductidOrderByEntry().add(productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntryMap().put(Integer.parseInt(productBO.getProductID()), qty);
                }
            }
        }

    }

   public interface savePcsValue
    {
        void saveChanges();

    }

}

