package com.ivy.cpg.view.van.damagestock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DamageStockFragment extends Fragment {
    DamageStockHelper damageStockHelper;
    private BusinessModel bmodel;
    private ListView lvwplist;
    private View tempView;
    private TextView productName;
    private MaterialSpinner reasonSpinner;
    private DamageStockAdapter damageAdapter;
    private ArrayList<SalesReturnReportBO> damageList;
    private String text;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        damageStockHelper = DamageStockHelper.getInstance(getActivity().getApplicationContext());

        if (damageStockHelper.getDamagedSalesReport() == null)
            return;

        productName = tempView.findViewById(R.id.productName);
        lvwplist = tempView.findViewById(R.id.list);
        reasonSpinner = tempView.findViewById(R.id.reasonSpinner);

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
        damageList.addAll(damageStockHelper
                .getDamagedSalesReport());
        Set<String> reasonSet = new LinkedHashSet<>();
        reasonSet.add(getResources().getString(R.string.all));
        for (SalesReturnReportBO salBo : damageStockHelper
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
            damageList.addAll(damageStockHelper
                    .getDamagedSalesReport());
        } else {
            for (SalesReturnReportBO bo : damageStockHelper
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


    public void showAlertOkCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(
                R.string.do_you_want_to_unload_damage_stock));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        damageStockHelper.UnloadDamageStock(getActivity(), damageList);
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

        @SuppressLint("SetTextI18n")
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
                holder.productname = row
                        .findViewById(R.id.productname);
                holder.caseqty = row.findViewById(R.id.caseqty);
                holder.pcsqty = row.findViewById(R.id.pieceqty);
                holder.outerqty = row.findViewById(R.id.outerqty);
                holder.reason = row.findViewById(R.id.reason);
                holder.batchnum = row.findViewById(R.id.batchnum);

                holder.productname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                text = getResources().getString(R.string.batch_no) + ": ";
                Spannable spanYou = new SpannableString("you");
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
            holder.batchnum.setText(text + tv);

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