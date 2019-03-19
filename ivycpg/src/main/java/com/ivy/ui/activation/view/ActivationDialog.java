package com.ivy.ui.activation.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.activation.bo.ActivationBO;
import com.ivy.utils.FontUtils;

import java.util.List;

public class ActivationDialog extends Dialog implements OnClickListener {
    private OnDismissListener addBatch;
    private Activity activity;
    private ActivationListViewAdapter adapter;
    private List<ActivationBO> appUrls;

    public ActivationDialog(Context context) {
        super(context);
    }

    public ActivationDialog(Activity activity, OnDismissListener addBatch, List<ActivationBO> appUrls) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.activity = activity;
        this.addBatch = addBatch;
        this.appUrls = appUrls;
        setContentView(R.layout.dialog_activation);
        setCancelable(true);

        TextView title = findViewById(R.id.title);
        title.setTypeface(FontUtils.getFontBalooHai(activity, FontUtils.FontType.MEDIUM));
        Button add = findViewById(R.id.add);
        add.setTypeface(FontUtils.getFontBalooHai(activity, FontUtils.FontType.REGULAR));
        add.setOnClickListener(this);
        Button close = findViewById(R.id.close);
        close.setTypeface(FontUtils.getFontBalooHai(activity, FontUtils.FontType.REGULAR));
        close.setOnClickListener(this);

        ListView listView = findViewById(R.id.list);
        adapter = new ActivationListViewAdapter(appUrls);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.add) {
            if (isAtleastOneRadioSelected()) {
                addBatch.onDismiss(ActivationDialog.this);
            } else
                Toast.makeText(activity, R.string.please_select_item,
                        Toast.LENGTH_SHORT).show();
        } else if (id == R.id.close) {
            ActivationDialog.this.dismiss();
        }
    }

    ActivationBO activationObj;

    private class ActivationListViewAdapter extends ArrayAdapter<ActivationBO> {

        private List<ActivationBO> items;

        public ActivationListViewAdapter(List<ActivationBO> items) {
            super(activity, R.layout.row_activation_dialog, items);
            this.items = items;
        }

        public ActivationBO getItem(int position) {
            return items.get(position);
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
            activationObj = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = activity.getLayoutInflater();
                row = inflater.inflate(R.layout.row_activation_dialog, parent,
                        false);
                holder = new ViewHolder();
                holder.environmentRadioBtn = row
                        .findViewById(R.id.environmentRadioBtn);
                holder.environmentRadioBtn.setTypeface(FontUtils.getFontRoboto(activity, FontUtils.FontType.LIGHT));
                holder.environmentRadioBtn
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                for (ActivationBO bo : items)
                                    bo.setChecked(false);
                                if (((RadioButton) v).isChecked())
                                    holder.activationBO.setChecked(true);
                                else
                                    holder.activationBO.setChecked(false);
                                adapter.notifyDataSetChanged();
                            }
                        });

                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.activationBO = activationObj;
            holder.environmentRadioBtn.setText("" + holder.activationBO.getEnviroinment());

            holder.environmentRadioBtn.setChecked(holder.activationBO
                    .isChecked());

            return (row);
        }

    }

    protected boolean isAtleastOneRadioSelected() {
        for (ActivationBO bo :appUrls) {
            if (bo.isChecked() == true) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(activity).edit();
                editor.putString("appUrlNew", bo.getUrl());
                editor.putString("application", bo.getEnviroinment());
                editor.commit();
                return true;
            }
        }
        return false;
    }

    class ViewHolder {
        ActivationBO activationBO;
        RadioButton environmentRadioBtn;
    }

}
