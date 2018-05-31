package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by Hanifa.M on 28/5/18.
 */

public class NewOutletEditSortDialog extends Dialog {
    private BusinessModel bmodel;
    private Context context;
    private String listLoad;
    private TextView headerText;
    private boolean isdialog = false;
    private int lastCheckedPosition = -1;
    private ShortSelectionListener shortSelectionListener;
    private ArrayList<NewOutletBO> retailerEditList = new ArrayList<>();

    protected NewOutletEditSortDialog(final Context context, final String listLoad, ArrayList<NewOutletBO> newRetailerList, int lastCheckPos) {
        super(context);

        this.context = context;
        this.listLoad = listLoad;
        this.retailerEditList = newRetailerList;
        this.lastCheckedPosition = lastCheckPos;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        bmodel = (BusinessModel) context.getApplicationContext();
        setContentView(R.layout.custom_dialog_newoutlet_edit);
        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7inch" tablet
        if (sizeLarge == 3)
            isdialog = true;

        if (isdialog)
            getWindow().setLayout(1000, 600);
        else if (!isdialog)
            getWindow().setLayout(1000, 760);
        this.setCancelable(false);


        ArrayList<String> listName = new ArrayList<>();
        RecyclerView reason_recycler = (RecyclerView) findViewById(R.id.reason_recycler);
        reason_recycler.setLayoutManager(new LinearLayoutManager(context));
        ArrayList<NewOutletBO> sortList = new ArrayList<>();
        NewOutletBO outletBO;
        listName.add("A - Z");
        listName.add("Z - A");
//            listName.add("New Retailers");
//            listName.add("Last Modified");

        for (int i = 0; i < listName.size(); i++) {
            outletBO = new NewOutletBO();
            outletBO.setSrotText(listName.get(i));
            sortList.add(outletBO);
        }

        reason_recycler.setAdapter(new NewOutletEditSortDialog.SortListAdapter(sortList));
    }


    class SortListAdapter extends RecyclerView.Adapter<NewOutletEditSortDialog.SortListAdapter.ViewHolder> {
        private ArrayList<NewOutletBO> items;

        public SortListAdapter(ArrayList<NewOutletBO> items) {
            this.items = items;
        }


        @Override
        public NewOutletEditSortDialog.SortListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.common_reason_popup_recycler_items, parent, false);
            return new SortListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final NewOutletEditSortDialog.SortListAdapter.ViewHolder holder, final int position) {
            holder.sortonObj = items.get(position);
            holder.sortlist_radio_btn.setText(holder.sortonObj.getSrotText());
            if (holder.sortonObj.isCheckedList())
                holder.sortlist_radio_btn.setChecked(true);
            else
                holder.sortlist_radio_btn.setChecked(false);

            holder.sortlist_radio_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewOutletBO selected_sortlist;
                    lastCheckedPosition = getItemViewType(position);
                    selected_sortlist = holder.sortonObj;

                    if (holder.sortlist_radio_btn.isChecked()) {
                        holder.sortonObj.setCheckedList(true);
                    } else {
                        holder.sortonObj.setCheckedList(false);
                    }

                    if (selected_sortlist.getSrotText().equals("A - Z")) {
                        Collections.sort(retailerEditList, new Comparator<NewOutletBO>() {
                            @Override
                            public int compare(NewOutletBO fstr, NewOutletBO sstr) {
                                return fstr.getOutletName().compareToIgnoreCase(sstr.getOutletName());
                            }
                        });
                        shortSelectionListener.updateShortList(retailerEditList,lastCheckedPosition);
                    } else if (selected_sortlist.getSrotText().equals("Z - A")) {
                        Collections.sort(retailerEditList, new Comparator<NewOutletBO>() {
                            @Override
                            public int compare(NewOutletBO fstr, NewOutletBO sstr) {
                                return sstr.getOutletName().compareToIgnoreCase(fstr.getOutletName());
                            }
                        });
                        shortSelectionListener.updateShortList(retailerEditList,lastCheckedPosition);
                    }

                    notifyDataSetChanged();
                }
            });
            holder.sortlist_radio_btn.setChecked(position == lastCheckedPosition);
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
            AppCompatRadioButton sortlist_radio_btn;
            NewOutletBO sortonObj;

            public ViewHolder(View itemView) {
                super(itemView);

                sortlist_radio_btn = (AppCompatRadioButton) itemView.findViewById(R.id.reason_radio_btn);
                sortlist_radio_btn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
    }

    public interface ShortSelectionListener {
        void updateShortList(ArrayList<NewOutletBO> retailerEditList, int lastCheckedPos);
    }

    public void setChannelSelectionListener(Fragment listener) {
        this.shortSelectionListener = (ShortSelectionListener) listener;
    }

}
