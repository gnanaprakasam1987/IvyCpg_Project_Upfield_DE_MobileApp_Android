package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 11/10/2017,1:03 PM.
 */
public class CompetitorFilterFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private View view;
    private BusinessModel bmodel;
    private Button btnOK;
    private Button cancelButton;
    private TextView grid_item_text;
    private ListView filtergridview;
    private ArrayList<CompetitorFilterLevelBO> competitorList;
    private CompetitorFilterInterface competitorFilterInterface;
    private ListView filterlistview;
    private HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();
    private Vector<CompetitorFilterLevelBO> sequence;
    private CompetitorFilterLevelBO mSelectedLevelBO = new CompetitorFilterLevelBO();

    private Vector<CompetitorFilterLevelBO> mSequence;
    private HashMap<Integer, Vector<CompetitorFilterLevelBO>> loadedFilterValues;

    private FilterAdapter adapter;
    private FilterGridAdapter gridadapter;
    private String filterText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.competitor_filter_fragment, container, false);

        Context context = getActivity();

        bmodel = (BusinessModel) context.getApplicationContext();

        //mSelectedIdByLevelId = (HashMap<Integer, Integer>)getArguments().getSerializable("selectedFilter");

        viewInitialization();


        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int size = sequence.size();
                for (int i = size - 1; i >= 0; i--) {
                    if (mSelectedIdByLevelId.get(sequence.get(i).getProductId()) != null && mSelectedIdByLevelId.get(sequence.get(i).getProductId()) > 0) {
                        for (CompetitorFilterLevelBO bo : loadedFilterValues.get(sequence.get(i).getProductId())) {
                            if (bo.getProductId() == mSelectedIdByLevelId.get(sequence.get(i).getProductId())) {
                                filterText = bo.getLevelName();
                                i = -1;
                                break;
                            }
                        }


                    }
                }

                Vector<CompetitorFilterLevelBO> finalParentList = updateProductLoad(sequence.size());

                competitorFilterInterface.updateCompetitorProducts(finalParentList,mSelectedIdByLevelId,filterText);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedIdByLevelId.clear();
                onStart();
            }
        });

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        loadedFilterValues = bmodel.productHelper.getCompetitorFiveLevelFilters();
        sequence = bmodel.productHelper.getCompetitorSequenceValues();



        if(sequence == null) {
            sequence = new Vector<>();
        }

        if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
            mSelectedIdByLevelId = new HashMap<>();

            for (CompetitorFilterLevelBO levelBO : sequence) {

                mSelectedIdByLevelId.put(levelBO.getProductId(), 0);
            }
        }

        if (!sequence.isEmpty()) {

            adapter = new FilterAdapter(sequence);
            filterlistview.setAdapter(adapter);
            mSelectedLevelBO = sequence.get(0);

            int levelID = sequence.get(0).getProductId();
            Vector<CompetitorFilterLevelBO> filterValues = new Vector<>();
            filterValues.addAll(loadedFilterValues.get(levelID));

            gridadapter = new FilterGridAdapter(filterValues);
            filtergridview.setAdapter(gridadapter);
            gridadapter.notifyDataSetChanged();
        }

    }


    /* @Override
     public void onAttach(Context activity) {
         super.onAttach(activity);
         if (activity instanceof BrandDialogInterface) {
             this.brandInterface = (BrandDialogInterface) activity;
         }
     }*/
    public void setCompetitorFilterInterface(Fragment competitorFilterInterface) {
        this.competitorFilterInterface = (CompetitorFilterInterface) competitorFilterInterface;
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
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @SuppressLint("ResourceAsColor")
    public class FilterGridAdapter extends BaseAdapter {

        private View gridrow;
        private final Vector<CompetitorFilterLevelBO> filteritem;

        public FilterGridAdapter(Vector<CompetitorFilterLevelBO> items) {
            filteritem = items;
        }


        public CompetitorFilterLevelBO getItem(int position) {
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
            holder.levelBO = filteritem.get(position);
            holder.text.setText(holder.levelBO.getLevelName());
            holder.text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            if (mSelectedIdByLevelId != null) {
                int selectedLevelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                        .getProductId());

                if (selectedLevelId ==  holder.levelBO.getProductId()) {
                    holder.selectedfilters.setVisibility(View.VISIBLE);
                } else {
                    holder.selectedfilters.setVisibility(View.GONE);
                }
            } else {
                holder.text.setTextColor(Color.BLACK);
            }

            gridrow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int levelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductId());
                    if (levelId == filteritem.get(position).getProductId()) {
                        mSelectedIdByLevelId.put(
                                mSelectedLevelBO.getProductId(), 0);
                    } else {
                        mSelectedIdByLevelId.put(mSelectedLevelBO
                                .getProductId(), filteritem.get(position)
                                .getProductId());
                    }

                    updateSelectedID();
                    gridadapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                    notifyDataSetChanged();


                }
            });

            return gridrow;
        }


        private void updateSelectedID() {
            boolean flag = false;

            for (CompetitorFilterLevelBO levelBO : sequence) {
                if (flag) {
                    mSelectedIdByLevelId.put(levelBO.getProductId(), 0);
                }
                if (mSelectedLevelBO.getProductId() == levelBO.getProductId()) {
                    int selectedLeveId = mSelectedIdByLevelId.get(levelBO.getProductId());
                    if (selectedLeveId != 0) {
                        flag = true;
                    }

                }

            }

        }
    }

    public class FilterAdapter extends BaseAdapter {
        private final Vector<CompetitorFilterLevelBO> filteritem;
        private View row;

        public FilterAdapter(Vector<CompetitorFilterLevelBO> itemss) {
            filteritem = itemss;
        }

        public CompetitorFilterLevelBO getItem(int position) {
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
                    .getProductId());

            if (holder.levelBO.getProductId() == mSelectedLevelBO
                    .getProductId()) {
                holder.text.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                holder.gridItem.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.half_Black));
            } else {
                holder.text.setTextColor(Color.BLACK);
                holder.gridItem.setBackgroundColor(Color.parseColor("#f7f7f7"));
            }

            if (levelId != 0) {
                holder.selectedfilters.setVisibility(View.VISIBLE);
            } else {
                holder.selectedfilters.setVisibility(View.GONE);
            }


            row.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {



                    mSelectedLevelBO = holder.levelBO;
                    int size = 0;

                    //checking whether selected level is attribute or product level
                    if (position < (sequence.size() - size)) {
                        //Product Level
                        Vector<CompetitorFilterLevelBO> filterList = updateFilterSelection(position);
                        gridadapter = new FilterGridAdapter(filterList);
                        filtergridview.setAdapter(gridadapter);
                    }

                    adapter.notifyDataSetChanged();
                    gridadapter.notifyDataSetChanged();


                }
            });
            return row;
        }

        private Vector<CompetitorFilterLevelBO> updateFilterSelection(int pos) {

            Vector<CompetitorFilterLevelBO> finalValuelist = new Vector<>();

            if (isFilterContentSelected(pos) && pos != 0) {

                int selectedGridLevelID = 0;
                ArrayList<Integer> parentIdList = null;

                for (int i = 0; i <= pos; i++) {
                    CompetitorFilterLevelBO levelBO = sequence.get(i);

                    if (i != 0) {

                        parentIdList = getParenIdList(selectedGridLevelID, parentIdList, levelBO);

                    }
                    selectedGridLevelID = mSelectedIdByLevelId.get(levelBO.getProductId());

                    if (i == pos) {

                        Vector<CompetitorFilterLevelBO> gridViewlist = loadedFilterValues
                                .get(levelBO.getProductId());
                        finalValuelist = new Vector<>();
                        if (!parentIdList.isEmpty()) {
                            for (int productID : parentIdList) {
                                for (CompetitorFilterLevelBO gridViewBO : gridViewlist) {
                                    if (productID == gridViewBO.getProductId()) {
                                        finalValuelist.add(gridViewBO);
                                    }

                                }
                            }
                        }
                    }

                }
            } else {

                finalValuelist = loadedFilterValues.get(sequence.get(pos)
                        .getProductId());

            }


            return finalValuelist;

        }
    }

    private ArrayList<Integer> getParenIdList(int selectedGridLevelID,
                                              ArrayList<Integer> list, CompetitorFilterLevelBO levelBO) {
        ArrayList<Integer> parentIdList = new ArrayList<>();
        Vector<CompetitorFilterLevelBO> gridViewlist = loadedFilterValues.get(levelBO
                .getProductId());
        if (selectedGridLevelID != 0) {
            if (gridViewlist != null) {
                for (CompetitorFilterLevelBO gridlevelBO : gridViewlist) {
                    if (selectedGridLevelID == gridlevelBO.getParentId()) {
                        parentIdList.add(gridlevelBO.getProductId());
                    }

                }
            }

        } else {

            if (gridViewlist != null&&list != null&&list.size() > 0) {
                for (int id : list) {
                    for (CompetitorFilterLevelBO gridlevelBO : gridViewlist) {
                        if (gridlevelBO.getParentId() == id) {
                            parentIdList
                                    .add(gridlevelBO.getProductId());
                        }
                    }
                }
            }

        }

        return parentIdList;
    }


    private boolean isFilterContentSelected(int pos) {
        for (int i = 0; i <= pos - 1; i++) {
            if (i <= sequence.size()) {
                CompetitorFilterLevelBO levelbo = sequence.get(i);
                if (mSelectedIdByLevelId.get(levelbo.getProductId()) != 0) {
                    return true;
                }
            }

        }
        return false;
    }

    class ViewHolder {
        private TextView text;
        //   private ImageView filtericons;
        private ImageView selectedfilters;
        private LinearLayout gridItem;

        private CompetitorFilterLevelBO levelBO;

    }

    private Vector<CompetitorFilterLevelBO> updateProductLoad(int pos) {

        Vector<CompetitorFilterLevelBO> finalValuelist = new Vector<>();

        if (isFilterContentSelected(pos)) {

            int selectedGridLevelID = 0;
            ArrayList<Integer> parentIdList = null;

            for (int i = 0; i < pos; i++) {
                CompetitorFilterLevelBO levelBO = sequence.get(i);

                if (i != 0) {

                    parentIdList = getParenIdList(selectedGridLevelID,
                            parentIdList, levelBO);

                }
                selectedGridLevelID = mSelectedIdByLevelId.get(levelBO
                        .getProductId());

                if (i == pos - 1) {

                    Vector<CompetitorFilterLevelBO> gridViewlist = loadedFilterValues
                            .get(levelBO.getProductId());
                    finalValuelist = new Vector<>();
                    if (selectedGridLevelID != 0) {
                        for (CompetitorFilterLevelBO gridViewBO : gridViewlist) {
                            if (selectedGridLevelID == gridViewBO.getProductId()) {
                                finalValuelist.add(gridViewBO);
                            }

                        }

                    } else {
                        if (parentIdList!=null)
                            if (!parentIdList.isEmpty()) {
                                for (int productID : parentIdList) {
                                    for (CompetitorFilterLevelBO gridViewBO : gridViewlist) {
                                        if (productID == gridViewBO.getProductId()) {
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
                        .getProductId());

        }
        return finalValuelist;


    }

}
