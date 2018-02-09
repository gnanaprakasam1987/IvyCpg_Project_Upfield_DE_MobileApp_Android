package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SalesReturnReportBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DamageStockFragment extends Fragment {
    SalesReturnHelper salesReturnHelper;
    private BusinessModel bmodel;
    private ListView lvwplist;
    private View tempView;
    private TextView productName;
    private MaterialSpinner reasonSpinner;
    private DamageStockAdapter damageAdapter;
    private List<SalesReturnReportBO> damageList;
    private String text;
    private Spannable spanYou;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tempView = inflater.inflate(R.layout.damage_stock_frag_layout,
                container, false);
        setHasOptionsMenu(true);
        return tempView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        salesReturnHelper = SalesReturnHelper.getInstance(getActivity());

        if (salesReturnHelper.getDamagedSalesReport() == null)
            return;
        productName = (TextView) tempView.findViewById(R.id.productName);
        lvwplist = (ListView) tempView.findViewById(R.id.list);
        reasonSpinner = (MaterialSpinner) tempView.findViewById(R.id.reasonSpinner);

        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)tempView.findViewById(R.id.product_txt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)tempView.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)tempView.findViewById(R.id.outerTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)tempView.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)tempView.findViewById(R.id.reasonTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(tempView.findViewById(
                    R.id.caseTitle).getTag()) != null)
                ((TextView) tempView.findViewById(R.id.caseTitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(tempView
                                .findViewById(R.id.caseTitle).getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(tempView.findViewById(
                    R.id.pcsTitle).getTag()) != null)
                ((TextView) tempView.findViewById(R.id.pcsTitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(tempView
                                .findViewById(R.id.pcsTitle).getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(tempView.findViewById(
                    R.id.reasonTitle).getTag()) != null)
                ((TextView) tempView.findViewById(R.id.reasonTitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(tempView
                                .findViewById(R.id.reasonTitle).getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        ArrayAdapter<SpinnerBO> reasonAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_blacktext_layout);
        reasonAdapter
                .setDropDownViewResource(R.layout.spinner_blacktext_list_item);
        damageList = new ArrayList<>();
        for (SalesReturnReportBO bo : salesReturnHelper
                .getDamagedSalesReport())
            damageList.add(bo);
        Set<String> reasonSet = new LinkedHashSet<>();
        reasonSet.add(getResources().getString(R.string.all));
        for (SalesReturnReportBO salBo : salesReturnHelper
                .getDamagedSalesReport())
            reasonSet.add(salBo.getReasonDesc());
        Iterator<String> itr = reasonSet.iterator();
        short i = 0;
        while (itr.hasNext()) {
            reasonAdapter.add(new SpinnerBO(i, itr.next()));
            ++i;
        }
        reasonSpinner.setAdapter(reasonAdapter);
        reasonSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (id == 0) {
                    if (damageAdapter == null) {
                        damageAdapter = new DamageStockAdapter(damageList);
                        lvwplist.setAdapter(damageAdapter);
                    } else {
                        updateDamageReport("All");
                        damageAdapter.notifyDataSetChanged();
                    }
                } else {
                    SpinnerBO tempBo = (SpinnerBO) parent.getSelectedItem();
                    updateDamageReport(tempBo.getSpinnerTxt());
                    if (damageAdapter == null) {
                        damageAdapter = new DamageStockAdapter(damageList);
                        lvwplist.setAdapter(damageAdapter);
                    } else {
                        damageAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // TO DO Auto-generated
            }

        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            outState.putString("SpinnerTxt", ((SpinnerBO) reasonSpinner
                    .getSelectedItem()).getSpinnerTxt());
            outState.putInt("SpinnerPos",
                    reasonSpinner.getSelectedItemPosition());

            ((DamageStockFragmentActivity) getActivity()).passData(outState);
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    protected void onRestoreInstance() {
        damageAdapter = null;
        damageAdapter = new DamageStockAdapter(damageList);
        lvwplist.setAdapter(damageAdapter);
    }

    protected void updateDamageReport(String spinnerTxt) {
        damageList.clear();
        if ("All".equals(spinnerTxt)) {
            for (SalesReturnReportBO bo : salesReturnHelper
                    .getDamagedSalesReport())
                damageList.add(bo);
        } else {
            for (SalesReturnReportBO bo : salesReturnHelper
                    .getDamagedSalesReport()) {
                if (bo.getReasonDesc().equals(spinnerTxt))
                    damageList.add(bo);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_unload_damage_stock) {
            Commons.print("unload");
            if (!damageList.isEmpty()) {
                showAlertOkCancel();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_damaged_stock_to_unload), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void UnloadDamageStock() {
        try {
            SalesReturnReportBO bo;
            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,dUomId,douomqty,dOuomid,date,type";
            for (int i = 0; i < damageList.size(); i++) {
                bo = damageList.get(i);
                String values = bmodel.QT(uid)
                        + ","
                        + bmodel.QT(bo.getProductid())
                        + ","
                        + bmodel.QT(bo.getProductName())
                        + ","
                        + bo.getBatchId()
                        + ","
                        + bmodel.QT(bo.getBatchNumber())
                        + ","
                        + bo.getSih()
                        + ","
                        + bo.getCaseQty()
                        + ","
                        + bo.getPieceQty()
                        + ","
                        + bo.getOuterQty()
                        + ","
                        + bo.getdUomQty()
                        + ","
                        + bo.getdUomId()
                        + ","
                        + bo.getdOuomQty()
                        + ","
                        + bo.getdOuomid()
                        + ","
                        + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate()) + "," + 0;
                db.insertSQL(DataMembers.tbl_vanunload_details, columns, values);
                db.executeQ("update SalesReturnHeader set unload=1");
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException("" + e);
        }
    }

    public void showAlertOkCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(
                R.string.do_you_want_to_unload_damage_stock));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        UnloadDamageStock();
                        Toast.makeText(getActivity(), "Damaged stock unloaded",
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TO DO Auto-generated

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private class DamageStockAdapter extends ArrayAdapter<SalesReturnReportBO> {
        String tv;
        SalesReturnReportBO product;
        private List<SalesReturnReportBO> items;

        public DamageStockAdapter(List<SalesReturnReportBO> items) {
            super(getActivity(), R.layout.row_damage_report, items);
            this.items = items;
        }

        public SalesReturnReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_damage_report, parent,
                        false);
                holder = new ViewHolder();
                holder.productname = (TextView) row
                        .findViewById(R.id.productname);
                holder.caseqty = (TextView) row.findViewById(R.id.caseqty);
                holder.pcsqty = (TextView) row.findViewById(R.id.pieceqty);
                holder.outerqty = (TextView) row.findViewById(R.id.outerqty);
                holder.reason = (TextView) row.findViewById(R.id.reason);
                holder.batchnum = (TextView) row.findViewById(R.id.batchnum);

                holder.productname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.pcsqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.outerqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.reason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.batchnum.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.productname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

              text = getResources().getString(R.string.batch_no)+": ";
              spanYou = new SpannableString("you");
                spanYou.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanYou.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        tv = holder.salBo.getProductName() + "";
                        productName.setText(tv);
                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.salBo = product;
            tv = product.getProductSortName() + "";
            holder.productname.setText(tv);
            tv = product.getCaseQty() + "";
            holder.caseqty.setText(tv);
            tv = product.getPieceQty() + "";
            holder.pcsqty.setText(tv);
            tv = product.getOuterQty() + "";
            holder.outerqty.setText(tv);
            tv = product.getReasonDesc() + "";
            holder.reason.setText(tv);
            tv = product.getBatchNumber() + "";
            holder.batchnum.setText(text+tv);

            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_odd_item_bg));
            }

            return row;
        }
    }

    class ViewHolder {
        SalesReturnReportBO salBo;
        TextView productname;
        TextView reason;
        TextView caseqty;
        TextView pcsqty;
        TextView outerqty;
        TextView batchnum;

    }
}