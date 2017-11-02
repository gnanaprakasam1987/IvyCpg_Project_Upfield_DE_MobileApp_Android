package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.reports.SalesVolumeReportFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

@SuppressLint("ResourceAsColor")
public class FilterFiveFragment<E> extends Fragment implements OnClickListener,
        OnItemClickListener {

    private ListView filterlistview;
    private ListView filtergridview;
    private View view;
    private BusinessModel bmodel;
    private Button cancelButton;
    private Button btnOK;
    private BrandDialogInterface brandInterface;
    private FilterGridAdapter gridadapter;
    private HashMap<Integer, Vector<LevelBO>> loadedFilterValues;
    private HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();
    private LevelBO mSelectedLevelBO = new LevelBO();
    private Vector<LevelBO> sequence;

    private FilterAdapter adapter;
    private String filterText = "";

    private String isFrom;

    private boolean isAttributeFilter = true;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fivefilterdialog, container, false);

        Context context = getActivity();

        bmodel = (BusinessModel) context.getApplicationContext();

        isFrom = getArguments().getString("isFrom");
        isFrom = (isFrom == null) ? "STK" : isFrom;

        isAttributeFilter = getArguments().get("isAttributeFilter") == null || getArguments().getBoolean("isAttributeFilter");

        mSelectedIdByLevelId = (HashMap<Integer, Integer>) getArguments().getSerializable("selectedFilter");

        viewInitialization();


        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int size = sequence.size();
                for (int i = size - 1; i >= 0; i--) {
                    if (mSelectedIdByLevelId.get(sequence.get(i).getProductID()) != null && mSelectedIdByLevelId.get(sequence.get(i).getProductID()) > 0) {
                        for (LevelBO bo : loadedFilterValues.get(sequence.get(i).getProductID())) {
                            if (bo.getProductID() == mSelectedIdByLevelId.get(sequence.get(i).getProductID())) {
                                filterText = bo.getLevelName();
                                i = -1;
                                break;
                            }
                        }
                    }
                }


                Vector<LevelBO> finalParentList = new Vector<>();

                if (isAttributeFilter && bmodel.productHelper.getmAttributeTypes() != null && bmodel.productHelper.getmAttributeTypes().size() > 0) {

                    if (isAttributeFilterSelected()) {
                        //if product filter is also selected then, final parent id list will prepared to show products based on both attribute and product filter
                        if (isFilterContentSelected(sequence.size() - bmodel.productHelper.getmAttributeTypes().size())) {
                            finalParentList = updateProductLoad((sequence.size() - bmodel.productHelper.getmAttributeTypes().size()));
                        }

                        ArrayList<Integer> lstSelectedAttributesIds = new ArrayList<>();
                        for (LevelBO bo : sequence) {
                            for (int i = 0; i < mSelectedIdByLevelId.size(); i++) {
                                if (mSelectedIdByLevelId.get(bo.getProductID()) > 0) {
                                    lstSelectedAttributesIds.add(mSelectedIdByLevelId.get(bo.getProductID()));
                                }

                            }
                        }

                        ArrayList<Integer> lstFinalProductIds = new ArrayList<>();
                        for (int j = 0; j < lstSelectedAttributesIds.size(); j++) {
                            for (int k = 0; k < bmodel.productHelper.getLstProductAttributeMapping().size(); k++) {

                                if (bmodel.productHelper.getLstProductAttributeMapping().get(k).getAttributeId() == lstSelectedAttributesIds.get(j)
                                        && !lstFinalProductIds.contains(bmodel.productHelper.getLstProductAttributeMapping().get(k).getProductId())) {
                                    lstFinalProductIds.add(bmodel.productHelper.getLstProductAttributeMapping().get(k).getProductId());
                                }
                            }

                        }
                        brandInterface.updatefromFiveLevelFilter(finalParentList, mSelectedIdByLevelId, lstFinalProductIds, filterText);
                        return;
                    } else
                        finalParentList = updateProductLoad(sequence.size() - bmodel.productHelper.getmAttributeTypes().size());


                } else
                    finalParentList = updateProductLoad(sequence.size());

                brandInterface.updatefromFiveLevelFilter(finalParentList, mSelectedIdByLevelId, null, filterText);
                brandInterface.updateCancel();

            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedIdByLevelId.clear();
                onStart();
            }
        });

        return view;

    }

    private boolean isAttributeFilterSelected() {
        for (LevelBO bo : sequence) {
            if (mSelectedIdByLevelId.get(bo.getProductID()) > 0 && bo.getProductID() < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub

    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onStart() {
        super.onStart();

        if (isFrom != null) {
            switch (isFrom) {
                case "STK":
                    if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER)
                        bmodel.productHelper.downloadFiveFilterLevels("MENU_STK_ORD");
                    loadedFilterValues = bmodel.productHelper.getFiveLevelFilters();
                    sequence = bmodel.productHelper.getSequenceValues();
                    break;
                case "SF":
                    loadedFilterValues = bmodel.salesFundamentalHelper.getFiveLevelFilters();
                    sequence = bmodel.salesFundamentalHelper.getSequenceValues();
                    break;
                case "SVR":
                    loadedFilterValues = bmodel.reportHelper.getMfilterlevelBo();
                    sequence = bmodel.reportHelper.getSequencevalues();
                    break;
                default:
                    loadedFilterValues = bmodel.productHelper.getRetailerModuleFilerContentBySequenct();
                    sequence = bmodel.productHelper.getRetailerModuleSequenceValues();
                    break;
            }
        } else {
            loadedFilterValues = bmodel.productHelper.getFiveLevelFilters();
            sequence = bmodel.productHelper.getSequenceValues();
        }

        if (loadedFilterValues != null) {
            if (isAttributeFilter && loadedFilterValues.get(-1) == null) {
                if (bmodel.productHelper.getmAttributesList() != null && bmodel.productHelper.getmAttributesList().size() > 0) {
                    int newAttributeId = 0;
                    for (AttributeBO bo : bmodel.productHelper.getmAttributeTypes()) {
                        newAttributeId -= 1;
                        sequence.add(new LevelBO(bo.getAttributeTypename(), newAttributeId, -1));
                        Vector<LevelBO> lstAttributes = new Vector<>();
                        LevelBO attLevelBO;
                        for (AttributeBO attrBO : bmodel.productHelper.getmAttributesList()) {
                            attLevelBO = new LevelBO();
                            if (bo.getAttributeTypeId() == attrBO.getAttributeLovId()) {
                                attLevelBO.setProductID(attrBO.getAttributeId());
                                attLevelBO.setLevelName(attrBO.getAttributeName());
                                lstAttributes.add(attLevelBO);
                            }
                        }
                        loadedFilterValues.put(newAttributeId, lstAttributes);

                    }
                }
            }
        }

        if (sequence == null) {
            sequence = new Vector<LevelBO>();
        }

        if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
            mSelectedIdByLevelId = new HashMap<>();

            for (LevelBO levelBO : sequence) {

                mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
            }
        }


        if (!sequence.isEmpty()) {
            adapter = new FilterAdapter(sequence);
            filterlistview.setAdapter(adapter);
            mSelectedLevelBO = sequence.get(0);

            int levelID = sequence.get(0).getProductID();
            Vector<LevelBO> filterValues = new Vector<>();
            filterValues.addAll(loadedFilterValues.get(levelID));

            gridadapter = new FilterGridAdapter(filterValues);
            filtergridview.setAdapter(gridadapter);
            gridadapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    private void viewInitialization() {
        filterlistview = (ListView) view.findViewById(R.id.filterlistview);

        filtergridview = (ListView) view.findViewById(R.id.filtergridview);

        cancelButton = (Button) view.findViewById(R.id.btn_cancel);

        btnOK = (Button) view.findViewById(R.id.btn_ok);

        btnOK.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (brandInterface == null) {
            if (activity instanceof BrandDialogInterface) {
                this.brandInterface = (BrandDialogInterface) activity;
            }
        }
    }

    public void setBrandDialogInterface(SalesVolumeReportFragment sellerOrderReportFragment) {
        this.brandInterface = sellerOrderReportFragment;
    }

    public class FilterAdapter extends BaseAdapter {
        private final Vector<LevelBO> filteritem;
        private View row;

        public FilterAdapter(Vector<LevelBO> itemss) {
            filteritem = itemss;
        }

        public LevelBO getItem(int position) {
            return filteritem.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return filteritem.size();
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;

            row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.filter_grid_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) row.findViewById(R.id.grid_item_text);
                holder.selectedfilters = (ImageView) row.findViewById(R.id.selectedfilters);
                holder.gridItem = (LinearLayout) row.findViewById(R.id.GridItem);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.levelBO = filteritem.get(position);
            holder.text.setText(holder.levelBO.getLevelName());
            holder.text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            int levelId = mSelectedIdByLevelId.get(holder.levelBO
                    .getProductID());

            if (holder.levelBO.getProductID() == mSelectedLevelBO
                    .getProductID()) {
                holder.text.setTextColor(ContextCompat.getColor(getActivity(), R.color.WHITE));
                holder.gridItem.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));
            } else {
                holder.text.setTextColor(Color.BLACK);
                holder.gridItem.setBackgroundColor(Color.parseColor("#f7f7f7"));
            }

            if (levelId != 0) {
                holder.selectedfilters.setVisibility(View.VISIBLE);
            } else {
                holder.selectedfilters.setVisibility(View.GONE);
            }


            row.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    mSelectedLevelBO = holder.levelBO;
                    int size = 0;
                    if (isAttributeFilter && bmodel.productHelper.getmAttributeTypes() != null)
                        size = bmodel.productHelper.getmAttributeTypes().size();
                    //checking whether selected level is attribute or product level
                    if (position < (sequence.size() - size)) {
                        //Product Level
                        Vector<LevelBO> filterList = updateFilterSelection(position);
                        gridadapter = new FilterGridAdapter(filterList);
                        filtergridview.setAdapter(gridadapter);
                    } else {
                        //Attribute level
                        if (isFilterContentSelected(position)) {
                            // Loading attributes on grid view based on the selected product levels
                            Vector<LevelBO> leastBrandIds = updateProductLoad(sequence.size() - size);

                            Vector<Integer> lstProducts = new Vector<>();
                            for (LevelBO bo : leastBrandIds) {
                                if (bmodel.productHelper.getmProductIdByBrandId().get(bo.getProductID()) != null)
                                    lstProducts.addAll(bmodel.productHelper.getmProductIdByBrandId().get(bo.getProductID()));
                            }

                            ArrayList<Integer> mAttributesList = new ArrayList<>();
                            for (int prodId : lstProducts) {
                                if (bmodel.productHelper.getmAttributeByProductId().get(prodId) != null) {
                                    for (int attId : bmodel.productHelper.getmAttributeByProductId().get(prodId)) {
                                        if (!mAttributesList.contains(attId)) {
                                            mAttributesList.add(attId);
                                        }
                                    }
                                }

                            }

                            LevelBO mselectedAttrLevelBO = sequence.get(position);

                            Vector<LevelBO> filterList = new Vector<>();
                            for (LevelBO bo : loadedFilterValues.get(mselectedAttrLevelBO.getProductID())) {
                                for (int attrId : mAttributesList) {
                                    if (bo.getProductID() == attrId) {
                                        filterList.add(bo);
                                    }
                                }

                            }

                            gridadapter = new FilterGridAdapter(filterList);
                            filtergridview.setAdapter(gridadapter);

                        } else {
                            Vector<LevelBO> filterList = updateFilterSelection(position);
                            gridadapter = new FilterGridAdapter(filterList);
                            filtergridview.setAdapter(gridadapter);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    gridadapter.notifyDataSetChanged();


                }
            });
            return row;
        }

        private Vector<LevelBO> updateFilterSelection(int pos) {

            Vector<LevelBO> finalValuelist = new Vector<>();

            if (isFilterContentSelected(pos) && pos != 0) {

                int selectedGridLevelID = 0;
                ArrayList<Integer> parentIdList = null;

                for (int i = 0; i <= pos; i++) {
                    LevelBO levelBO = sequence.get(i);

                    if (i != 0) {

                        parentIdList = getParenIdList(selectedGridLevelID, parentIdList, levelBO);

                    }
                    selectedGridLevelID = mSelectedIdByLevelId.get(levelBO.getProductID());

                    if (i == pos) {

                        Vector<LevelBO> gridViewlist = loadedFilterValues
                                .get(levelBO.getProductID());
                        finalValuelist = new Vector<>();
                        if (!parentIdList.isEmpty()) {
                            for (int productID : parentIdList) {
                                for (LevelBO gridViewBO : gridViewlist) {
                                    if (productID == gridViewBO.getProductID()) {
                                        finalValuelist.add(gridViewBO);
                                    }

                                }
                            }
                        }
                    }

                }
            } else {

                finalValuelist = loadedFilterValues.get(sequence.get(pos)
                        .getProductID());

            }

            return finalValuelist;

        }
    }

    class ViewHolder {
        private TextView text;
        private LevelBO levelBO;
        //   private ImageView filtericons;
        private ImageView selectedfilters;
        private LinearLayout gridItem;
    }

    @SuppressLint("ResourceAsColor")
    public class FilterGridAdapter extends BaseAdapter {
        private final Vector<LevelBO> filteritem;
        private LevelBO levelBO;
        private View gridrow;

        public FilterGridAdapter(Vector<LevelBO> itemss) {
            filteritem = itemss;
        }

        public LevelBO getItem(int position) {
            return filteritem.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return filteritem.size();
        }

        @SuppressLint({"NewApi", "ResourceAsColor"})
        @SuppressWarnings("unchecked")
        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;

            levelBO = filteritem.get(position);

            gridrow = convertView;

            if (gridrow == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                gridrow = inflater.inflate(R.layout.filter_secondary_list_item, parent,
                        false);
                holder = new ViewHolder();
                holder.text = (TextView) gridrow
                        .findViewById(R.id.grid_item_text);
                holder.selectedfilters = (ImageView) gridrow.findViewById(R.id.selectedfilters);
                gridrow.setTag(holder);
            } else {
                holder = (ViewHolder) gridrow.getTag();
            }

            holder.text.setText(levelBO.getLevelName());
            holder.text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            if (mSelectedIdByLevelId != null) {
                int selectedLevelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                        .getProductID());

                if (selectedLevelId == levelBO.getProductID()) {
                    holder.selectedfilters.setVisibility(View.VISIBLE);
                } else {
                    holder.selectedfilters.setVisibility(View.GONE);
                }
            } else {
                holder.text.setTextColor(Color.BLACK);
            }

            gridrow.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int levelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());
                    if (levelId == filteritem.get(position).getProductID()) {
                        mSelectedIdByLevelId.put(
                                mSelectedLevelBO.getProductID(), 0);
                    } else {
                        mSelectedIdByLevelId.put(mSelectedLevelBO
                                .getProductID(), filteritem.get(position)
                                .getProductID());
                    }
                    updateSelectedID();
                    gridadapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();


                }
            });

            return gridrow;
        }

        private void updateSelectedID() {
            boolean flag = false;

            for (LevelBO levelBO : sequence) {
                if (flag) {
                    mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
                }
                if (mSelectedLevelBO.getProductID() == levelBO.getProductID()) {
                    int selectedLeveId = mSelectedIdByLevelId.get(levelBO.getProductID());
                    if (selectedLeveId != 0) {
                        flag = true;
                    }

                }

            }

        }
    }

    private boolean isFilterContentSelected(int pos) {
        for (int i = 0; i <= pos - 1; i++) {
            if (i <= sequence.size()) {
                LevelBO levelbo = sequence.get(i);
                if (mSelectedIdByLevelId.get(levelbo.getProductID()) != 0) {
                    return true;
                }
            }

        }
        return false;
    }


    private Vector<LevelBO> updateProductLoad(int pos) {

        Vector<LevelBO> finalValuelist = new Vector<>();

        if (isFilterContentSelected(pos)) {

            int selectedGridLevelID = 0;
            ArrayList<Integer> parentIdList = null;

            for (int i = 0; i < pos; i++) {
                LevelBO levelBO = sequence.get(i);

                if (i != 0) {

                    parentIdList = getParenIdList(selectedGridLevelID,
                            parentIdList, levelBO);

                }
                selectedGridLevelID = mSelectedIdByLevelId.get(levelBO
                        .getProductID());

                if (i == pos - 1) {

                    Vector<LevelBO> gridViewlist = loadedFilterValues
                            .get(levelBO.getProductID());
                    finalValuelist = new Vector<>();
                    if (selectedGridLevelID != 0) {
                        for (LevelBO gridViewBO : gridViewlist) {
                            if (selectedGridLevelID == gridViewBO.getProductID()) {
                                finalValuelist.add(gridViewBO);
                            }

                        }

                    } else {
                        if (parentIdList != null)
                            if (!parentIdList.isEmpty()) {
                                for (int productID : parentIdList) {
                                    for (LevelBO gridViewBO : gridViewlist) {
                                        if (productID == gridViewBO.getProductID()) {
                                            finalValuelist.add(gridViewBO);
                                        }

                                    }
                                }
                            }
                    }
                }

            }


        } else {
            if (pos > 0)
                finalValuelist = loadedFilterValues.get(sequence.get(pos - 1)
                        .getProductID());

        }
        return finalValuelist;


    }

    private ArrayList<Integer> getParenIdList(int selectedGridLevelID,
                                              ArrayList<Integer> list, LevelBO levelBO) {
        ArrayList<Integer> parentIdList = new ArrayList<>();
        Vector<LevelBO> gridViewlist = loadedFilterValues.get(levelBO
                .getProductID());
        if (selectedGridLevelID != 0) {
            if (gridViewlist != null) {
                for (LevelBO gridlevelBO : gridViewlist) {
                    if (selectedGridLevelID == gridlevelBO.getParentID()) {
                        parentIdList.add(gridlevelBO.getProductID());
                    }

                }
            }

        } else {

            if (gridViewlist != null && list != null && list.size() > 0) {
                for (int id : list) {
                    for (LevelBO gridlevelBO : gridViewlist) {
                        if (gridlevelBO.getParentID() == id) {
                            parentIdList
                                    .add(gridlevelBO.getProductID());
                        }
                    }
                }
            }

        }

        return parentIdList;
    }

}
