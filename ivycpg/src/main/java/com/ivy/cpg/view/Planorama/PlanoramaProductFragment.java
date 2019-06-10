package com.ivy.cpg.view.Planorama;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PlanoramaProductFragment extends IvyBaseFragment {


    private View view;
    private ListView listView;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    BusinessModel bModel;
    PlanoramaHelper planoramaHelper;
    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    int mSelectedLocationIndex=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planorama_product, container, false);

        bModel=(BusinessModel)getActivity().getApplicationContext();
        bModel.setContext(getActivity());

        inputManager = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);

        listView=view.findViewById(R.id.list);

        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bModel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE"))
                spinnerAdapter.add(temp);
        }

        planoramaHelper=PlanoramaHelper.getInstance(getActivity());

        listView.setAdapter(new MyAdapter(planoramaHelper.getmProductList()));

        return view;
    }


    private class MyAdapter extends BaseAdapter {
        private final ArrayList<PlanoramaProductBO> items;

        public MyAdapter(ArrayList<PlanoramaProductBO> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_planorama_product, parent, false);

                row.setTag(holder);

                holder.textView_productName = row
                        .findViewById(R.id.tvProductNameTitle);
                holder.textView_no_facing = row
                        .findViewById(R.id.textView_facings);
                holder.spinner_reason = row
                        .findViewById(R.id.spinner_reason);

                holder.spinner_reason.setAdapter(spinnerAdapter);

                holder.editText_sc = row
                        .findViewById(R.id.editText_sc);

                holder.editText_sho = row
                        .findViewById(R.id.editText_sho);

                holder.editText_sp = row
                        .findViewById(R.id.editText_sp);

                holder.editText_sp
                        .setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {

                                QUANTITY = holder.editText_sp;
                                QUANTITY.setTag(holder.planoramaProductBO);
                                int inType = holder.editText_sp
                                        .getInputType();
                                holder.editText_sp
                                        .setInputType(InputType.TYPE_NULL);
                                holder.editText_sp.onTouchEvent(event);
                                holder.editText_sp.setInputType(inType);
                                holder.editText_sp.requestFocus();
                                if (holder.editText_sp.getText().length() > 0)
                                    holder.editText_sp.setSelection(holder.editText_sp.getText().length());
                                /*inputManager.hideSoftInputFromWindow(
                                        mEdt_searchProductName
                                                .getWindowToken(), 0);*/
                                return true;
                            }
                        });

                holder.editText_sp
                        .addTextChangedListener(new TextWatcher() {

                            @SuppressLint("SetTextI18n")
                            public void afterTextChanged(Editable s) {

                               /* if (holder.planoramaProductBO.getPcUomid() == 0) {
                                    holder.shelfPcsQty.removeTextChangedListener(this);
                                    holder.shelfPcsQty.setText("");
                                    holder.shelfPcsQty.addTextChangedListener(this);
                                    return;
                                }*/

                                String qty = s.toString();
                                if (qty.length() > 0)
                                    holder.editText_sp.setSelection(qty.length());

                                if (!qty.equals("")) {
                                    int sp_qty = SDUtil
                                            .convertToInt(holder.editText_sp
                                                    .getText().toString());

                                    holder.planoramaProductBO.getLocations()
                                            .get(mSelectedLocationIndex)
                                            .setShelfPiece(sp_qty);

                                    if (sp_qty > 0
                                            || SDUtil.convertToInt(holder.editText_sc.getText().toString()) > 0
                                            || SDUtil.convertToInt(holder.editText_sho.getText().toString()) > 0) {
                                        holder.planoramaProductBO.getLocations()
                                                .get(mSelectedLocationIndex).setAvailability(1);
                                        /*CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                                        holder.imageButton_availability.setChecked(true);*/

                                    } else if (sp_qty == 0) {
                                        holder.planoramaProductBO.getLocations()
                                                .get(mSelectedLocationIndex).setAvailability(0);
                                        /*CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.RED)));
                                        holder.imageButton_availability.setChecked(true);*/
                                    }

                                } else {
                                    holder.planoramaProductBO.getLocations()
                                            .get(mSelectedLocationIndex)
                                            .setShelfPiece(-1);

                                    if (qty.length() == 0
                                            && holder.editText_sc.getText().toString().length() == 0
                                            && holder.editText_sho.getText().toString().length() == 0) {

                                        holder.planoramaProductBO.getLocations()
                                                .get(mSelectedLocationIndex).setAvailability(-1);
                                        /*CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.checkbox_default_color)));
                                        holder.imageButton_availability.setChecked(false);*/
                                    }
                                }

                                /*int totValue = getProductTotalValue(holder.planoramaProductBO);
                                holder.total.setText(totValue + "");
                                if (totValue > 0) {
                                    holder.mReason.setEnabled(false);
                                    holder.mReason.setSelected(false);
                                    holder.mReason.setSelection(0);
                                    holder.planoramaProductBO.getStoreLocations()
                                            .get(mSelectedLocationIndex)
                                            .setReasonId(0);
                                } else {
                                    holder.mReason.setEnabled(true);
                                    holder.mReason.setSelected(true);
                                    holder.mReason.setSelection(getReasonIndex(holder.planoramaProductBO
                                            .getStoreLocations().get(mSelectedLocationIndex).getReasonId() + ""));
                                }

                                updateFooter();*/
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }
                        });

               /* holder.shelfCaseQty
                        .setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {

                                QUANTITY = holder.shelfCaseQty;
                                QUANTITY.setTag(holder.planoramaProductBO);
                                int inType = holder.shelfCaseQty
                                        .getInputType();
                                holder.shelfCaseQty
                                        .setInputType(InputType.TYPE_NULL);
                                holder.shelfCaseQty.onTouchEvent(event);
                                holder.shelfCaseQty.setInputType(inType);
                                holder.shelfCaseQty.requestFocus();
                                if (holder.shelfCaseQty.getText().length() > 0)
                                    holder.shelfCaseQty.setSelection(holder.shelfCaseQty.getText().length());
                                inputManager.hideSoftInputFromWindow(
                                        mEdt_searchProductName
                                                .getWindowToken(), 0);
                                return true;
                            }
                        });

                holder.shelfouter.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.shelfouter;
                        QUANTITY.setTag(holder.planoramaProductBO);
                        int inType = holder.shelfouter.getInputType();
                        holder.shelfouter.setInputType(InputType.TYPE_NULL);
                        holder.shelfouter.onTouchEvent(event);
                        holder.shelfouter.setInputType(inType);
                        holder.shelfouter.requestFocus();
                        if (holder.shelfouter.getText().length() > 0)
                            holder.shelfouter.setSelection(holder.shelfouter.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchProductName.getWindowToken(), 0);
                        return true;
                    }
                });*/
                

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.planoramaProductBO = items.get(position);

            holder.textView_productName.setText(holder.planoramaProductBO.getProductName());
            holder.textView_no_facing.setText("Facing: "+String.valueOf(holder.planoramaProductBO.getNumberOfFacings()));

            if(holder.planoramaProductBO.isAvailable()) {
                holder.spinner_reason.setEnabled(false);
                holder.editText_sc.setEnabled(true);
                holder.editText_sp.setEnabled(true);
                holder.editText_sho.setEnabled(true);
                holder.textView_no_facing.setTextColor(getResources().getColor(R.color.colorPrimaryDarkGreen));

                holder.spinner_reason.setSelection(0);
            }
            else {
                holder.spinner_reason.setEnabled(true);
                holder.editText_sc.setEnabled(false);
                holder.editText_sp.setEnabled(false);
                holder.editText_sho.setEnabled(false);
                holder.textView_no_facing.setTextColor(getResources().getColor(R.color.RED));

                holder.spinner_reason.setSelection(getReasonIndex(holder.planoramaProductBO
                        .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
            }


            if (planoramaHelper.SHOW_STOCK_SP) {
                if (holder.planoramaProductBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfPiece() >= 0) {
                    String strShelfPiece = holder.planoramaProductBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfPiece()
                            + "";
                    holder.editText_sp.setText(strShelfPiece);
                } else {
                    holder.editText_sp.setText("");
                }
            }

            if (planoramaHelper.SHOW_STOCK_SC) {
                if (holder.planoramaProductBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfCase() >= 0) {
                    String strShelfCase = holder.planoramaProductBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfCase()
                            + "";
                    holder.editText_sc.setText(strShelfCase);
                } else {
                    holder.editText_sc.setText("");
                }
            }
            if (planoramaHelper.SHOW_SHELF_OUTER) {
                if (holder.planoramaProductBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfOuter() >= 0) {
                    String strShelfOuter = holder.planoramaProductBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfOuter()
                            + "";
                    holder.editText_sho.setText(strShelfOuter);
                } else {
                    holder.editText_sho.setText("");
                }
            }


            return row;
        }
    }

    class ViewHolder {
        PlanoramaProductBO planoramaProductBO;
        TextView textView_productName,textView_no_facing;
        Spinner spinner_reason;
        EditText editText_sp,editText_sc,editText_sho;

    }

    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId reason id for which the index need to be found
     * @return position of the reason id
     */
    public int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bModel.showAlert(
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
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

}
