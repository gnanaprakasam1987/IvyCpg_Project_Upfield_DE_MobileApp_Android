package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private String mSelectedIdByLevelId = "";
    private CompetitorFilterInterface competitorFilterInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.competitor_filter_fragment, container, false);

        Context context = getActivity();

        bmodel = (BusinessModel) context.getApplicationContext();

        //mSelectedIdByLevelId = (HashMap<Integer, Integer>)getArguments().getSerializable("selectedFilter");

        viewInitialization();
        if (getArguments() != null && getArguments().getString("selectedCompetitorId") != null) {
            mSelectedIdByLevelId = getArguments().getString("selectedCompetitorId");
        }


        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                competitorFilterInterface.updateCompetitorProducts(mSelectedIdByLevelId);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedIdByLevelId = "";
                //competitorFilterInterface.updateCompetitorProducts(mSelectedIdByLevelId);
                ((BaseAdapter) filtergridview.getAdapter()).notifyDataSetChanged();
            }
        });

        return view;

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
        grid_item_text = (TextView) view.findViewById(R.id.grid_item_text);

        filtergridview = (ListView) view.findViewById(R.id.filtergridview);

        cancelButton = (Button) view.findViewById(R.id.btn_cancel);

        btnOK = (Button) view.findViewById(R.id.btn_ok);

        btnOK.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        competitorList = bmodel.productHelper.getCompetitorFilterList();
        if (competitorList != null && competitorList.size() > 0) {
            grid_item_text.setText(competitorList.get(0).getLevelName());
        }
        filtergridview.setAdapter(new FilterGridAdapter());

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

        public FilterGridAdapter() {

        }

        public CompetitorFilterLevelBO getItem(int position) {
            return competitorList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return competitorList.size();
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
            holder.levelBO = competitorList.get(position);
            holder.text.setText(holder.levelBO.getProductName());
            holder.text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            if (!mSelectedIdByLevelId.equals("")) {
                if (mSelectedIdByLevelId.equals(holder.levelBO.getProductId())) {
                    holder.selectedfilters.setVisibility(View.VISIBLE);
                } else {
                    holder.selectedfilters.setVisibility(View.GONE);
                }
            } else {
                holder.text.setTextColor(Color.BLACK);
                holder.selectedfilters.setVisibility(View.GONE);
            }

            gridrow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mSelectedIdByLevelId.equals(holder.levelBO.getProductId())) {
                        mSelectedIdByLevelId = "";
                    } else {
                        mSelectedIdByLevelId = holder.levelBO.getProductId();
                    }

                    //updateSelectedID();
                    //gridadapter.notifyDataSetChanged();
                    notifyDataSetChanged();


                }
            });

            return gridrow;
        }

        /*private void updateSelectedID() {
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

        }*/
    }

    class ViewHolder {
        private TextView text;
        //   private ImageView filtericons;
        private ImageView selectedfilters;
        private LinearLayout gridItem;

        private CompetitorFilterLevelBO levelBO;

    }
}
