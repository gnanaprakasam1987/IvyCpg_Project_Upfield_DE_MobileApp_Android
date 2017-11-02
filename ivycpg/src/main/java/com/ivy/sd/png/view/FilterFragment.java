package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@SuppressLint("ValidFragment")
public class FilterFragment<E> extends Fragment implements OnClickListener,
        OnItemClickListener {

    private Context context;
    private GridView brandOrSplGridView;
    private GridView categoryGridView;
    private ListView splFilterList;
    private CardView splFilterCard;
    private String buttonName;
    private Button cancelButton;
    private Button allButton;
    private BrandDialogInterface brandInterface;
    private Vector itm;
    private String isFrom;
    private View view;
    private final HashMap<String, String> mselectedFilterMap;
    private TextView mPreSelectFilter;
    private boolean mHideAllButtton = false;
    private TextView pLevelFilterheading;
    private TextView cLevelFilterHeading;
    private String parentTitle;
    private String childTitle;
    private CategoryGridAdapter categoryAdapter;
    private TypedArray typearr;
    private static final String GENERAL = "General";
    private static final String BRAND = "Brand";
    private static final String CATEGORY = "Category";
    BusinessModel bmodel;

    public FilterFragment(HashMap<String, String> selectedfilter) {
        mselectedFilterMap = selectedfilter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.filter_dialog, container, false);
        this.context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        viewInitialization();
        boolean isBrandFilter = false;
        try {
            buttonName = getArguments().getString("filterName");
            isBrandFilter = getArguments().getBoolean("isFormBrand");
            parentTitle = getArguments().getString("pfilterHeader");
            childTitle = getArguments().getString("filterHeader");
            isFrom = getArguments().getString("isFrom");
            isFrom = isFrom != null ? isFrom : "STK";
            mHideAllButtton = getArguments().getBoolean("ishideAll");

            if (!isBrandFilter) {
                if (isFrom != null) {
                    if ("stockproposal".equals(isFrom)) {
                        itm = (Vector<?>) getArguments().get("filterContent");
                    } else {
                        itm = (Vector) getArguments().get("serilizeContent");
                    }
                } else {
                    itm = bmodel.configurationMasterHelper.downloadFilterList();
                }
            } else {
                itm = (Vector) getArguments().get("serilizeContent");
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.chooseCategoryTitle).getTag()) != null)
                pLevelFilterheading.setText(bmodel.labelsMasterHelper
                        .applyLabels(view
                                .findViewById(R.id.chooseCategoryTitle)
                                .getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.chooseBrandTitle).getTag()) != null)
                cLevelFilterHeading.setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.chooseBrandTitle)
                                .getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        if (parentTitle != null) {
            String strParentTitle = getResources().getString(R.string.choose) + " " + parentTitle;
            pLevelFilterheading.setText(strParentTitle);
        } else
            pLevelFilterheading.setText(getResources().getString(
                    R.string.choose));

        if (childTitle != null) {
            String strChildTitle = getResources().getString(R.string.choose) + " " + childTitle;
            cLevelFilterHeading.setText(strChildTitle);
        } else
            cLevelFilterHeading.setText(getResources().getString(
                    R.string.choose));

        if (!isBrandFilter) {
            view.findViewById(R.id.chooseCategoryTitle)
                    .setVisibility(View.GONE);
            categoryGridView.setVisibility(View.GONE);
        }

        if (GENERAL.equals(buttonName)) {
            TextView tv = (TextView) view.findViewById(R.id.chooseBrandTitle);
            tv.setText(getActivity().getResources().getString(
                    R.string.choose_special_filter));
        }


        if (mHideAllButtton)
            allButton.setVisibility(View.GONE);

        brandOrSplGridView.setOnItemClickListener(this);
        categoryGridView.setOnItemClickListener(this);
        splFilterList.setOnItemClickListener(this);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                brandInterface.updateCancel();

            }
        });

        allButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (BRAND.equals(buttonName)) {
                    brandInterface.updatebrandtext(BRAND, -1);
                    mselectedFilterMap.put(BRAND, "All");
                    mselectedFilterMap.put(CATEGORY, "All");
                }
                if (GENERAL.equals(buttonName)) {
                    brandInterface.updategeneraltext(GENERAL);
                    mselectedFilterMap.put(GENERAL, "All");
                    mselectedFilterMap.put(BRAND, "All");
                    mselectedFilterMap.put(CATEGORY, "All");
                }

            }
        });

        // Set Data to category Filter
        if (isBrandFilter) {
            if (isFrom != null) {
                if ("STK".equals(isFrom)) {
                    categoryAdapter = new CategoryGridAdapter(
                            bmodel.productHelper.getParentLevelBo());
                    categoryGridView.setAdapter(categoryAdapter);
                } else {
                    categoryAdapter = new CategoryGridAdapter(
                            bmodel.productHelper.getPlevelMaster());
                    categoryGridView.setAdapter(categoryAdapter);
                }

            } else {
                categoryAdapter = new CategoryGridAdapter(
                        bmodel.productHelper.getParentLevelBo());
                categoryGridView.setAdapter(categoryAdapter);
            }
        }

        List<E> mylist = new ArrayList<>();
        for (int i = 0; i < itm.size(); i++) {
            mylist.add((E) itm.get(i));
        }

        Commons.print("Mylist.Size" + "ListSize" + mylist.size());

        if (GENERAL.equals(buttonName)) {
            splFilterCard.setVisibility(View.VISIBLE);
            brandOrSplGridView.setVisibility(View.GONE);
            MyListAdapter myListAdapter = new MyListAdapter(mylist);
            splFilterList.setAdapter(myListAdapter);
        } else {
            splFilterCard.setVisibility(View.GONE);
            brandOrSplGridView.setVisibility(View.VISIBLE);
            MyGridAdapter mSchedule = new MyGridAdapter(mylist);
            brandOrSplGridView.setAdapter(mSchedule);
        }


        try {
            updateBrandFilter(Integer.parseInt(mselectedFilterMap
                    .get(CATEGORY)));
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return view;
    }

    private void viewInitialization() {
        brandOrSplGridView = (GridView) view.findViewById(R.id.brandGrid);
        categoryGridView = (GridView) view.findViewById(R.id.categoryGrid);
        splFilterList = (ListView) view.findViewById(R.id.splFiterList);
        splFilterCard = (CardView) view.findViewById(R.id.splFilterCard);

        pLevelFilterheading = (TextView) view.findViewById(R.id.chooseCategoryTitle);
        cLevelFilterHeading = (TextView) view.findViewById(R.id.chooseBrandTitle);
        cancelButton = (Button) view.findViewById(R.id.btn_cancel);
        allButton = (Button) view.findViewById(R.id.btn_all);
        pLevelFilterheading.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        cLevelFilterHeading.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        cancelButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        allButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

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

    @SuppressWarnings("unchecked")
    private void updateBrandFilter(int parentId) {
        try {
            ArrayList<E> mylist = new ArrayList<>();
            for (int i = 0; i < itm.size(); i++) {
                ChildLevelBo childLevelBo = (ChildLevelBo) itm.get(i);
                if (childLevelBo.getParentid() == parentId)
                    mylist.add((E) childLevelBo);
            }
            if (GENERAL.equals(buttonName)) {
                splFilterCard.setVisibility(View.VISIBLE);
                brandOrSplGridView.setVisibility(View.GONE);
                MyListAdapter myListAdapter = new MyListAdapter(mylist);
                splFilterList.setAdapter(myListAdapter);
            } else {
                splFilterCard.setVisibility(View.GONE);
                brandOrSplGridView.setVisibility(View.VISIBLE);
                MyGridAdapter mSchedule = new MyGridAdapter(mylist);
                brandOrSplGridView.setAdapter(mSchedule);
            }


            MyGridAdapter mSchedule = new MyGridAdapter(mylist);
            brandOrSplGridView.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    class MyGridAdapter extends ArrayAdapter {
        final List items;
        private ChildLevelBo childBO;
        private ConfigureBO configBO;

        @SuppressWarnings("unchecked")
        public MyGridAdapter(List items) {
            super(context, R.layout.filter_grid_item, items);
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
                if (BRAND.equals(buttonName)) {
                    childBO = (ChildLevelBo) items.get(position);
                } else {
                    configBO = (ConfigureBO) items.get(position);
                }
                row = convertView;

                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater
                            .inflate(R.layout.two_filter_grid_item, parent, false);
                    holder = new ViewHolder();
                    holder.text = (CustomTextView) row.findViewById(R.id.grid_item);
                    row.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                if (BRAND.equals(buttonName)) {

                    holder.text.setText(childBO.getPlevelName());
                    holder.id = childBO.getProductid();

                    if ((Integer.toString(holder.id)).equals(mselectedFilterMap.get(BRAND))) {
                        holder.text.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));
                    } else {
                        holder.text.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
                    }
                } else if (GENERAL.equals(buttonName)) {
                    try {
                        holder.text.setText(configBO.getMenuName());

                        holder.speFiltID = configBO.getConfigCode();

                        if (holder.speFiltID.equals(mselectedFilterMap
                                .get(GENERAL))) {
                            holder.text.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));
                        } else {
                            holder.text.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));

                        }

                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                }

                holder.type = buttonName;
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            return row;
        }
    }

    class MyListAdapter extends ArrayAdapter {
        final List items;
        private ChildLevelBo childBO;
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
                    holder.splFilterText = (TextView) row.findViewById(R.id.grid_item_text);
                    holder.cbFiltered = (RadioButton) row.findViewById(R.id.selectedfilters);

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

    class CategoryGridAdapter extends ArrayAdapter<ParentLevelBo> {
        final Vector<ParentLevelBo> items;

        public CategoryGridAdapter(Vector<ParentLevelBo> items) {
            super(context, R.layout.filter_grid_item, items);
            this.items = items;
        }

        public ParentLevelBo getItem(int position) {
            return items.get(position);
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

            ParentLevelBo parentLevelBO = items.get(position);

            View row = convertView;
            try {
                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater
                            .inflate(R.layout.two_filter_grid_item, parent, false);
                    holder = new ViewHolder();
                    holder.text = (CustomTextView) row.findViewById(R.id.grid_item);
                    row.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.text.setText(parentLevelBO.getPl_levelName());
                holder.id = parentLevelBO.getPl_productid();
                holder.type = CATEGORY;
                if ((Integer.toString(holder.id)).equals(mselectedFilterMap.get(CATEGORY))) {
                    mPreSelectFilter = holder.text;
                    holder.text.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));
                    updateBrandFilter(holder.id);
                } else {
                    holder.text.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return row;

        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        // TODO Auto-generated method stub
        try {
            ViewHolder holder = (ViewHolder) arg1.getTag();
            // set color for the selected item
            if (GENERAL.equals(holder.type))
                holder.cbFiltered.setChecked(true);
            else
                holder.text.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));

            if (BRAND.equals(holder.type) || GENERAL.equals(holder.type)) {
                // Brand or Spl filter
                if (buttonName.equals(BRAND)) {
                    mselectedFilterMap.put(BRAND, Integer.toString(holder.id));
                    brandInterface.updatebrandtext((String) holder.text.getText(),
                            holder.id);
                } else if (GENERAL.equals(buttonName)) {
                    mselectedFilterMap.put(GENERAL, holder.speFiltID);
                    brandInterface.updategeneraltext(holder.speFiltID);
                    mselectedFilterMap.put(BRAND, "All");
                    mselectedFilterMap.put(CATEGORY, "All");

                }
            } else {
                // Category Filter is clicked

                // set previously selected filter to normal color
                if (mPreSelectFilter != null) {
                    TextViewCompat.setTextAppearance(holder.text, holder.text.a.getResourceId(R.styleable.MyTextView_filterStyle, 0));
                }
                // store selected filter in hashmap
                mselectedFilterMap.put(CATEGORY, Integer.toString(holder.id));

                mPreSelectFilter = holder.text;
                categoryAdapter.notifyDataSetChanged();
                updateBrandFilter(holder.id);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


}
