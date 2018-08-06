package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class SpecialFilterFragment extends Fragment implements OnItemClickListener {

    private Context context;

    private String buttonName;

    private BrandDialogInterface brandInterface;
    private Vector specialFilterList;

    private final HashMap<String, String> mselectedFilterMap;

    private static final String GENERAL = "General";
    private static final String BRAND = "Brand";
    private static final String CATEGORY = "Category";

    private BusinessModel bmodel;

    public SpecialFilterFragment(){
        mselectedFilterMap= new HashMap<>();
    }
    public SpecialFilterFragment(HashMap<String, String> selectedfilter) {
        mselectedFilterMap = selectedfilter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.filter_dialog, container, false);

        this.context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();

        ListView splFilterList = view.findViewById(R.id.splFiterList);
        TextView cLevelFilterHeading = view.findViewById(R.id.chooseBrandTitle);

        Button cancelButton = view.findViewById(R.id.btn_cancel);
        Button allButton = view.findViewById(R.id.btn_all);

        splFilterList.setOnItemClickListener(this);

        cLevelFilterHeading.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        cancelButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        allButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        try {
            // Mostly "General"
            buttonName = getArguments().getString("filterName");
            // List to load in filter.
            specialFilterList = (Vector) getArguments().get("serilizeContent");

            // If data not sent from calling activity then load from database.
            if (specialFilterList == null || specialFilterList.size() == 0)
                specialFilterList = bmodel.configurationMasterHelper.downloadFilterList();

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                brandInterface.updateCancel();

            }
        });

        allButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                brandInterface.updateGeneralText(GENERAL);
                mselectedFilterMap.put(GENERAL, "All");
                mselectedFilterMap.put(BRAND, "All");
                mselectedFilterMap.put(CATEGORY, "All");


            }
        });


        List<ConfigureBO> mylist = new ArrayList<>();
        for (int i = 0; i < specialFilterList.size(); i++) {
            mylist.add((ConfigureBO) specialFilterList.get(i));
        }


        MyListAdapter myListAdapter = new MyListAdapter(mylist);
        splFilterList.setAdapter(myListAdapter);

        return view;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof BrandDialogInterface) {
            this.brandInterface = (BrandDialogInterface) activity;
        }
    }

    class MyListAdapter extends ArrayAdapter {
        final List items;
        private ConfigureBO configBO;

        @SuppressWarnings("unchecked")
        public MyListAdapter(List items) {
            super(context, R.layout.spl_filter_list_item, items);
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

        @SuppressWarnings("unchecked")
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = null;
            try {

                configBO = (ConfigureBO) items.get(position);
                row = convertView;

                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater
                            .inflate(R.layout.spl_filter_list_item, parent, false);
                    holder = new ViewHolder();
                    holder.splFilterText = row.findViewById(R.id.grid_item_text);
                    holder.cbFiltered = row.findViewById(R.id.selectedfilters);

                    holder.splFilterText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                try {
                    holder.splFilterText.setText(configBO.getMenuName());

                    holder.speFiltID = configBO.getConfigCode();

                    if (holder.speFiltID.equals(mselectedFilterMap
                            .get(GENERAL))) {
                        holder.cbFiltered.setChecked(true);
                    } else {
                        holder.cbFiltered.setChecked(false);

                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                holder.type = buttonName;
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            return row;
        }
    }

    class ViewHolder {
        CustomTextView text;
        int id;
        String type;
        String speFiltID;
        TextView splFilterText;
        RadioButton cbFiltered;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        try {
            ViewHolder holder = (ViewHolder) arg1.getTag();

            holder.cbFiltered.setChecked(true);

            mselectedFilterMap.put(GENERAL, holder.speFiltID);

            brandInterface.updateGeneralText(holder.speFiltID);

            mselectedFilterMap.put(BRAND, "All");
            mselectedFilterMap.put(CATEGORY, "All");


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


}
