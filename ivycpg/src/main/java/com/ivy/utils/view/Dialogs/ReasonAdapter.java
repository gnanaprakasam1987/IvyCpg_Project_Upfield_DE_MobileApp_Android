package com.ivy.utils.view.Dialogs;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.utils.AppUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

class ReasonAdapter extends RecyclerView.Adapter<ReasonAdapter.ViewHolder> {

    private ArrayList<ReasonMaster> items;
    private int lastCheckedPosition = -1;
    private ReasonMaster selected_reason;
    private String remarks = "";
    private Context context;
    private onItemClickListener onItemClickListener;

    public ReasonAdapter(Context context, ArrayList<ReasonMaster> items, onItemClickListener onItemClickListener, int lastCheckedPosition) {
        this.items = items;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        this.lastCheckedPosition = lastCheckedPosition;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_reason_popup_recycler_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, final int position) {
        holder.reasonObj = items.get(position);
        holder.reason_radio_btn.setText(holder.reasonObj.getReasonDesc());

        holder.reason_radio_btn.setOnClickListener(v -> {
            lastCheckedPosition = getItemViewType(position);
            onItemClickListener.updateSelectedItem(holder.reasonObj, position);
        });

        holder.reason_radio_btn.setChecked(position == lastCheckedPosition);

        if (holder.reason_radio_btn.isChecked()
                && holder.reason_radio_btn.getText().toString()
                .equals(context.getResources().getString(R.string.other_reason))) {
            holder.edt_other_remarks.setVisibility(View.VISIBLE);
        } else {
            AppUtils.hideKeyboard(holder.edt_other_remarks, context);
            remarks = "";
            holder.edt_other_remarks.setText("");
            holder.edt_other_remarks.setVisibility(View.GONE);
        }

        holder.edt_other_remarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                remarks = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatRadioButton reason_radio_btn;
        ReasonMaster reasonObj;
        EditText edt_other_remarks;

        public ViewHolder(View v) {
            super(v);
            reason_radio_btn = v.findViewById(R.id.reason_radio_btn);
            edt_other_remarks = v.findViewById(R.id.edt_other_remarks);

        }
    }


    interface onItemClickListener {
        void updateSelectedItem(ReasonMaster selectedReasonBo, int lastSelectedPosition);
    }
}
