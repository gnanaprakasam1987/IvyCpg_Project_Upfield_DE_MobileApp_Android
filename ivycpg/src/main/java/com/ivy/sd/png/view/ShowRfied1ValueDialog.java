package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by santhosh.c on 25-01-2018.
 * used to Update Piece value in listing
 */

@SuppressLint("ValidFragment")
public class ShowRfied1ValueDialog extends DialogFragment implements View.OnClickListener {
    private BusinessModel bmodel;
    Vector<ProductMasterBO> productBo;
    Vector<ProductMasterBO> productBoRfield;
    private ListView lvwplist;
    private MyAdaper mSchedule;
    private savePcsValue listner;
    private Button save,closeButton;
    private Context context;
    private EditText QUANTITY;
    public InputMethodManager inputManager;
    @SuppressLint("ValidFragment")
    public ShowRfied1ValueDialog(final Context context,String moduleName,savePcsValue listner) {
            super();
            this.context = context;
            this.listner=listner;
            }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        View view = inflater.inflate(R.layout.show_rfield1_dialog, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            // setContentView(R.layout.initiativedialog);
            setCancelable(true);
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
            lvwplist = (ListView) view.findViewById(R.id.list);
            lvwplist.setCacheColorHint(0);
            productBoRfield=new Vector<>();
            productBo=bmodel.productHelper.getProductMaster();

            DisplayMetrics outMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay()
            .getMetrics(outMetrics);
            int count=productBo.size();
            for(int i=0;i<count;i++)
            {
                ProductMasterBO product = productBo.elementAt(i);
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
            save = (Button) view.findViewById(R.id.save_btn);
            closeButton= (Button) view.findViewById(R.id.closeButton);
            closeButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            closeButton.setOnClickListener(this);
            save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            save.setOnClickListener(this);
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
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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
                listner.saveChanges();
                dismiss();
            }
            else
                Toast.makeText(bmodel,""+getString(R.string.Please_enter_multipal_of),Toast.LENGTH_SHORT).show();
        }
        if(i==R.id.closeButton)
            dismiss();

    }

    private class MyAdaper extends ArrayAdapter<ProductMasterBO>
    {
        private final Vector<ProductMasterBO> items;
        public MyAdaper(Vector<ProductMasterBO> items) {
            super(bmodel,
                    R.layout.ordered_piece_rfield_value, items);
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
                        R.layout.ordered_piece_rfield_value, parent,
                        false);
                holder = new ViewHolder();
                holder.productBo=product;
                holder.productNameTxt = (TextView) row
                        .findViewById(R.id.orderPRODNAME);
                holder.orderQTYinpiece = (TextView) row
                        .findViewById(R.id.orderQTYinpiece);

                holder.rField1Txt = (EditText) row
                        .findViewById(R.id.piece_qty);

                holder.productNameTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.rField1Txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                holder.productNameTxt.setText(""+product.getProductShortName());
                holder.orderQTYinpiece.setText( "Multipal of "+product.getRField1());
                holder.rField1Txt.setText(""+product.getOrderedPcsQty());
                holder.rField1Txt.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.rField1Txt;
                        int inType = holder.rField1Txt.getInputType();
                        holder.rField1Txt.setInputType(InputType.TYPE_NULL);
                        holder.rField1Txt.onTouchEvent(event);
                        holder.rField1Txt.setInputType(inType);
                        holder.rField1Txt.selectAll();
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
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
                        if(".".equals(qty))
                        {
                            qty="";
                        }

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
                            updateData(holder.productBo);
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

    interface savePcsValue
    {
        void saveChanges();

    }

    public void numberPressed(View vw) {


        if (QUANTITY == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_item), Toast.LENGTH_SHORT).show();
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                String enterText = (String) QUANTITY.getText().toString();
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            QUANTITY.setText(s + "");
                        } else {
                            s = s / 10;

                            QUANTITY.setText(splitValue[0] + "." + s);
                        }


                    } catch (ArrayIndexOutOfBoundsException e) {
                        QUANTITY.setText(SDUtil.convertToInt(enterText) + "");
                    }


                } else {

                    int s = SDUtil.convertToInt((String) QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(s + "");

                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }

            } else {
                Button ed = (Button) getDialog().findViewById(vw.getId());
                String append = ed.getText().toString();
                eff(append);

            }

        }
    }
    public void eff(String append) {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }
}

