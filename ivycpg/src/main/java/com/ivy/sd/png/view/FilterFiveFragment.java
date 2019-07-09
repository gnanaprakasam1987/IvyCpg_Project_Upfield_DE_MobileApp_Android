package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.reports.slaesvolumereport.SalesVolumeReportFragment;
import com.ivy.cpg.view.reports.slaesvolumereport.SalesVolumeReportHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.sf.SalesFundamentalHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@SuppressLint("ResourceAsColor")
public class FilterFiveFragment<E> extends Fragment {

    private ListView levelSelectionListview;
    private ListView productSelectionListview;

    private FilterLevelAdapter levelAdapter;
    private FilterProductsAdapter productsAdapter;

    private BusinessModel bmodel;

    private FiveLevelFilterCallBack fiveLevelFilterCallBack;

    private Vector<LevelBO> filterProductLevels;
    private HashMap<Integer, Vector<LevelBO>> filterProductsByLevelId;

    private LevelBO mSelectedLevelBO = new LevelBO();


    // Variable to pass back to calling activity to restore the last selected value.
    private HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();
    // select product among least level.
    private int filteredProductId = 0;
    // selected level name to diplay.
    private String filterText = "";

    private String fromScreen;
    private boolean isAttributeFilter = true;
    private boolean isTagged = false;

    private SalesFundamentalHelper mSFHelper;


    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fivefilterdialog, container, false);

        Context context = getActivity();

        bmodel = (BusinessModel) context.getApplicationContext();
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());

        fromScreen = getArguments().getString("isFrom");
        fromScreen = (fromScreen == null) ? "STK" : fromScreen;

        isTagged = getArguments().getBoolean("isTag", false);

        isAttributeFilter = getArguments().get("isAttributeFilter") == null || getArguments().getBoolean("isAttributeFilter");

        mSelectedIdByLevelId = (HashMap<Integer, Integer>) getArguments().getSerializable("selectedFilter");

        levelSelectionListview = view.findViewById(R.id.filterlistview);
        productSelectionListview = view.findViewById(R.id.filtergridview);

        Button cancelButton = view.findViewById(R.id.btn_cancel);
        Button btnOK = view.findViewById(R.id.btn_ok);

        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int size = filterProductLevels.size();
                    for (int i = size - 1; i >= 0; i--) {
                        if (mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID()) != null && mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID()) > 0) {
                            for (LevelBO bo : filterProductsByLevelId.get(filterProductLevels.get(i).getProductID())) {
                                if (bo.getProductID() == mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID())) {
                                    filterText = bo.getLevelName();
                                    filteredProductId = bo.getProductID();
                                    i = -1;
                                    break;
                                }
                            }
                        }
                    }



                    if (isAttributeFilter && bmodel.productHelper.getmAttributeTypes() != null && bmodel.productHelper.getmAttributeTypes().size() > 0) {

                        if (isAttributeFilterSelected()) {

                            ArrayList<Integer> lstSelectedAttributesIds = new ArrayList<>();
                            for (LevelBO bo : filterProductLevels) {
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
                            fiveLevelFilterCallBack.updateFromFiveLevelFilter(filteredProductId, mSelectedIdByLevelId, lstFinalProductIds, filterText);
                            return;
                        }
                    }
                    fiveLevelFilterCallBack.updateFromFiveLevelFilter(filteredProductId, mSelectedIdByLevelId, null, filterText);
                    fiveLevelFilterCallBack.updateCancel();
                } catch (Exception ex) {
                    Commons.printException(ex);
                }

            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mSelectedIdByLevelId.clear();
                    onStart();
                } catch (Exception ex) {
                    Commons.printException(ex);
                }
            }
        });

        return view;

    }

    private boolean isAttributeFilterSelected() {
        for (LevelBO bo : filterProductLevels) {
            if (mSelectedIdByLevelId.get(bo.getProductID()) > 0 && bo.getProductID() < 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onStart() {
        super.onStart();

        try {
            filterProductsByLevelId = new HashMap<>();
            filterProductLevels = new Vector<>();

            if (fromScreen != null) {
                switch (fromScreen) {
                    case "STK":
                        if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER) {
                            bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_STK_ORD"));
                            bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                                    bmodel.productHelper.getFilterProductLevels(), true));
                        }
                        filterProductsByLevelId.putAll(bmodel.productHelper.getFilterProductsByLevelId());
                        filterProductLevels.addAll(bmodel.productHelper.getFilterProductLevels());
                        break;
                    case "SF":
                        filterProductsByLevelId.putAll(mSFHelper.getFiveLevelFilters());
                        filterProductLevels.addAll(mSFHelper.getSequenceValues());
                        break;
                    case "SVR":
                        filterProductsByLevelId.putAll(SalesVolumeReportHelper.getInstance(getActivity()).getMfilterlevelBo());
                        filterProductLevels.addAll(SalesVolumeReportHelper.getInstance(getActivity()).getSequencevalues());
                        break;
                    case "SR":
                        filterProductsByLevelId.putAll(SalesReturnHelper.getInstance(getActivity()).getFilterProductsByLevelId());
                        filterProductLevels.addAll(SalesReturnHelper.getInstance(getActivity()).getFilterProductLevels());
                        break;
                    default:
                        filterProductsByLevelId.putAll(bmodel.productHelper.getRetailerModuleFilterProductsByLevelId());
                        filterProductLevels.addAll(bmodel.productHelper.getRetailerModuleSequenceValues());
                        break;
                }
            } else {
                filterProductsByLevelId.putAll(bmodel.productHelper.getFilterProductsByLevelId());
                filterProductLevels.addAll(bmodel.productHelper.getFilterProductLevels());
            }

            if (filterProductsByLevelId != null) {
                if (isAttributeFilter && filterProductsByLevelId.get(-1) == null) {
                    if (bmodel.productHelper.getmAttributesList() != null && bmodel.productHelper.getmAttributesList().size() > 0) {
                        int newAttributeId = 0;
                        for (AttributeBO bo : bmodel.productHelper.getmAttributeTypes()) {
                            newAttributeId -= 1;
                            filterProductLevels.add(new LevelBO(bo.getAttributeTypename(), newAttributeId, -1));
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
                            filterProductsByLevelId.put(newAttributeId, lstAttributes);

                        }
                    }
                }
            }

            if (filterProductLevels == null) {
                filterProductLevels = new Vector<>();
            }

            if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
                mSelectedIdByLevelId = new HashMap<>();
                for (LevelBO levelBO : filterProductLevels) {
                    mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
                }
            }


            if (!filterProductLevels.isEmpty()) {

                levelAdapter = new FilterLevelAdapter(filterProductLevels);
                levelSelectionListview.setAdapter(levelAdapter);

                mSelectedLevelBO = filterProductLevels.get(0);

                int levelID = filterProductLevels.get(0).getProductID();

                // To restrict filter's based on tagged products
                if (bmodel.configurationMasterHelper.IS_FILTER_TAG_PRODUCTS && isTagged)
                    loadTagProductFilters(levelID);

                Vector<LevelBO> filterValues = new Vector<>(filterProductsByLevelId.get(levelID));
                if (filterValues.size() > 0) {
                    productsAdapter = new FilterProductsAdapter(filterValues);
                    productSelectionListview.setAdapter(productsAdapter);
                    productsAdapter.notifyDataSetChanged();
                }
            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (filterProductsByLevelId != null)
            filterProductsByLevelId.clear();
        if (filterProductLevels != null)
            filterProductLevels.clear();
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        if (fiveLevelFilterCallBack == null) {
            if (activity instanceof BrandDialogInterface) {
                this.fiveLevelFilterCallBack = (FiveLevelFilterCallBack) activity;
            }
        }
    }


    public class FilterLevelAdapter extends BaseAdapter {
        private final Vector<LevelBO> filteritem;
        private View row;

        FilterLevelAdapter(Vector<LevelBO> itemss) {
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
                holder.text = row.findViewById(R.id.grid_item_text);
                holder.selectedfilters = row.findViewById(R.id.selectedfilters);
                holder.gridItem = row.findViewById(R.id.GridItem);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.levelBO = filteritem.get(position);
            holder.text.setText(holder.levelBO.getLevelName());

            int levelId = 0;
            if (mSelectedIdByLevelId.get(holder.levelBO
                    .getProductID()) != null) {
                levelId = mSelectedIdByLevelId.get(holder.levelBO
                        .getProductID());
            }

            if (holder.levelBO.getProductID() == mSelectedLevelBO
                    .getProductID()) {
                holder.text.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                holder.gridItem.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.half_Black));
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
                    // To restrict filter's based on tagged products
                    if (bmodel.configurationMasterHelper.IS_FILTER_TAG_PRODUCTS && isTagged)
                        loadTagProductFilters(mSelectedLevelBO.getProductID());

                    int size = 0;
                    if (isAttributeFilter && bmodel.productHelper.getmAttributeTypes() != null)
                        size = bmodel.productHelper.getmAttributeTypes().size();
                    //checking whether selected level is attribute or product level
                    if (position < (filterProductLevels.size() - size)) {
                        //Product Level
                        Vector<LevelBO> filterList = updateFilterSelection(position);
                        if (filterList != null && filterList.size() > 0) {
                            productsAdapter = new FilterProductsAdapter(filterList);
                            productSelectionListview.setAdapter(productsAdapter);
                        }
                    } else {
                        //Attribute level
                        if (isFilterContentSelected(position)) {
                            // Loading attributes on grid view based on the selected product levels
                            Vector<LevelBO> leastBrandIds = updateProductLoad(filterProductLevels.size() - size);

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

                            LevelBO mselectedAttrLevelBO = filterProductLevels.get(position);

                            Vector<LevelBO> filterList = new Vector<>();
                            for (LevelBO bo : filterProductsByLevelId.get(mselectedAttrLevelBO.getProductID())) {
                                for (int attrId : mAttributesList) {
                                    if (bo.getProductID() == attrId) {
                                        filterList.add(bo);
                                    }
                                }

                            }
                            productsAdapter = new FilterProductsAdapter(filterList);
                            productSelectionListview.setAdapter(productsAdapter);

                        } else {
                            Vector<LevelBO> filterList = updateFilterSelection(position);
                            if (filterList != null && filterList.size() > 0) {
                                productsAdapter = new FilterProductsAdapter(filterList);
                                productSelectionListview.setAdapter(productsAdapter);
                            }
                        }
                    }

                    if (levelAdapter != null)
                        levelAdapter.notifyDataSetChanged();
                    if (productsAdapter != null)
                        productsAdapter.notifyDataSetChanged();


                }
            });
            return row;
        }

        private Vector<LevelBO> updateFilterSelection(int pos) {

            Vector<LevelBO> finalValuelist = new Vector<>();

            if (isFilterContentSelected(pos) && pos != 0) {
                int size = filterProductLevels.size();
                int selectedPid = 0;
                for (int i = size - 1; i >= 0; i--) {
                    if (mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID()) != null && mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID()) > 0) {
                        if (filterProductLevels.get(pos).getProductID() > filterProductLevels.get(i).getProductID())
                            for (LevelBO bo : filterProductsByLevelId.get(filterProductLevels.get(i).getProductID())) {
                                if (bo.getProductID() == mSelectedIdByLevelId.get(filterProductLevels.get(i).getProductID())) {
                                    selectedPid = bo.getProductID();
                                    i = -1;
                                    break;
                                }
                            }
                    }
                }
                LevelBO levelBO = filterProductLevels.get(pos);
                Vector<LevelBO> gridViewlist = filterProductsByLevelId
                        .get(levelBO.getProductID());
                for (LevelBO gridViewBO : gridViewlist) {
                    if (gridViewBO.getParentHierarchy().contains("/" + selectedPid + "/")) {
                        finalValuelist.add(gridViewBO);
                    }

                }


            } else {

                finalValuelist = filterProductsByLevelId.get(filterProductLevels.get(pos)
                        .getProductID());

            }

            return finalValuelist;

        }
    }

    class ViewHolder {
        private TextView text;
        private LevelBO levelBO;
        private ImageView selectedfilters;
        private LinearLayout gridItem;
    }

    @SuppressLint("ResourceAsColor")
    public class FilterProductsAdapter extends BaseAdapter {
        private final Vector<LevelBO> filteritem;
        private LevelBO levelBO;
        private View gridrow;

        FilterProductsAdapter(Vector<LevelBO> itemss) {
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
                holder.text = gridrow
                        .findViewById(R.id.grid_item_text);
                holder.selectedfilters = gridrow.findViewById(R.id.selectedfilters);
                gridrow.setTag(holder);
            } else {
                holder = (ViewHolder) gridrow.getTag();
            }

            holder.text.setText(levelBO.getLevelName());

            if (mSelectedIdByLevelId != null) {

                int selectedLevelId = 0;
                if (mSelectedIdByLevelId.get(mSelectedLevelBO
                        .getProductID()) != null) {
                    selectedLevelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());
                }


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
                    productsAdapter.notifyDataSetChanged();
                    levelAdapter.notifyDataSetChanged();


                }
            });

            return gridrow;
        }

        private void updateSelectedID() {
            boolean flag = false;

            for (LevelBO levelBO : filterProductLevels) {
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
            if (i <= filterProductLevels.size()) {
                LevelBO levelbo = filterProductLevels.get(i);
                if (mSelectedIdByLevelId.get(levelbo.getProductID()) != 0) {
                    return true;
                }
            }

        }
        return false;
    }

    public void setBrandDialogInterface(SalesVolumeReportFragment sellerOrderReportFragment) {
        this.fiveLevelFilterCallBack = sellerOrderReportFragment;
    }


    // still used for attribute need to re visit it //Mansoor
    private Vector<LevelBO> updateProductLoad(int pos) {

        Vector<LevelBO> finalValuelist = new Vector<>();

        if (isFilterContentSelected(pos)) {

            int selectedGridLevelID = 0;
            ArrayList<Integer> parentIdList = null;

            for (int i = 0; i < pos; i++) {
                LevelBO levelBO = filterProductLevels.get(i);

                if (i != 0) {

                    parentIdList = getParenIdList(selectedGridLevelID,
                            parentIdList, levelBO);

                }
                selectedGridLevelID = mSelectedIdByLevelId.get(levelBO
                        .getProductID());

                if (i == pos - 1) {

                    Vector<LevelBO> gridViewlist = filterProductsByLevelId
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
                finalValuelist = filterProductsByLevelId.get(filterProductLevels.get(pos - 1)
                        .getProductID());

        }
        return finalValuelist;


    }

    //used for attribute type filter need to re visit it Mansoor
    private ArrayList<Integer> getParenIdList(int selectedGridLevelID,
                                              ArrayList<Integer> list, LevelBO levelBO) {
        ArrayList<Integer> parentIdList = new ArrayList<>();
        Vector<LevelBO> gridViewlist = filterProductsByLevelId.get(levelBO
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

    //Compute Filter's based on Tagged Products Parent Hierarchy
    private void loadTagProductFilters(int levelId) {
        Vector<LevelBO> taggedProductFilter = new Vector<>();
        for (LevelBO levelBO : filterProductsByLevelId.get(levelId)) {
            for (ProductMasterBO productMasterBO : ProductTaggingHelper.getInstance(getActivity()).getTaggedProducts()) {
                List<String> hierarchy = Arrays.asList(productMasterBO.getParentHierarchy().split("/"));
                if (hierarchy.contains(String.valueOf(levelBO.getProductID()))) {
                    taggedProductFilter.add(levelBO);
                    break;
                }
            }
        }

        filterProductsByLevelId.put(levelId, taggedProductFilter);
    }

}
