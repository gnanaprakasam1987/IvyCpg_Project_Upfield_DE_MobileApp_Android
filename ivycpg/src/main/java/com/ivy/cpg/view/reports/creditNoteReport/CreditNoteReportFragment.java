package com.ivy.cpg.view.reports.creditNoteReport;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.print.CreditNotePrintPreviewScreen;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 14-07-2016.
 */
public class CreditNoteReportFragment extends Fragment {
    BusinessModel bModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.creditnote_report_fragment,
                container, false);
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());

        ListView listView = view.findViewById(R.id.creditnote_listview);
        Button btnPrint = view.findViewById(R.id.print);

        if (bModel.configurationMasterHelper.IS_PRINT_CREDIT_NOTE_REPORT)
            btnPrint.setVisibility(View.VISIBLE);

        ArrayList<CreditNoteListBO> creditNoteList = CreditNoteHelper.getInstance().loadCreditNote(getActivity());
        if (creditNoteList.size() > 0) {
            MyAdapter adapter = new MyAdapter(creditNoteList);
            listView.setAdapter(adapter);
        }

        btnPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),
                        CreditNotePrintPreviewScreen.class);
                startActivity(i);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    class MyAdapter extends ArrayAdapter<CreditNoteListBO> {
        ArrayList<CreditNoteListBO> items;

        private MyAdapter(ArrayList<CreditNoteListBO> items) {
            super(getActivity(), R.layout.row_credit_note, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            CreditNoteListBO creditNoteListBO = items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_creditnote_report, parent, false);
                holder = new ViewHolder();
                holder.tvRetName = (TextView) row.findViewById(R.id.retailerNameTv);

                holder.tvCreditId = (TextView) row.findViewById(R.id.creditIdTv);
                holder.tvCreditAmount = (TextView) row.findViewById(R.id.creditAmtTv);
                holder.isIssued = (TextView) row.findViewById(R.id.is_issued);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tvRetName.setText(creditNoteListBO.getRetailerName());
            holder.tvCreditId.setText(creditNoteListBO.getId());
            holder.tvCreditAmount.setText(bModel.formatValue(creditNoteListBO.getAmount()));
            if (creditNoteListBO.isUsed())
                holder.isIssued.setText("Y");
            else
                holder.isIssued.setText("N");

            return (row);
        }
    }

    class ViewHolder {

        TextView tvRetName;
        TextView tvCreditAmount, tvCreditId, isIssued;
    }

}
