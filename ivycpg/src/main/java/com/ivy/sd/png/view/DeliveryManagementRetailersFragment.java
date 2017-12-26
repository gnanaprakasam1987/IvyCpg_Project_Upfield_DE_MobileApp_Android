package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.DeliveryManagementHelper;

import java.util.ArrayList;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by dharmapriya.k on 26/12/17.
 */

public class DeliveryManagementRetailersFragment extends IvyBaseFragment {
    BusinessModel businessModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_retailers, container, false);
        RecyclerView retailer_selection = (RecyclerView) view.findViewById(R.id.retailer_selection);
        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(sizeLarge);

        businessModel = (BusinessModel) getActivity().getApplicationContext();

        setScreenTitle(getArguments().getString("screentitle"));
        GridLayoutManager gridLayoutManager;
        if (is7InchTablet)
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getActivity(), 1);

        retailer_selection.setLayoutManager(gridLayoutManager);

        retailer_selection.setAdapter(new RetailerSelectionAdapter(DeliveryManagementHelper.getInstance(getContext()).getInvoicedRetailerList()));
        return view;
    }

    class RetailerSelectionAdapter extends RecyclerView.Adapter<RetailerSelectionAdapter.ViewHolder> {

        private ArrayList<RetailerMasterBO> items;


        public RetailerSelectionAdapter(ArrayList<RetailerMasterBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_delivery_retailers, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.retailerMasterBO = items.get(position);
            holder.retailer_name.setText(holder.retailerMasterBO.getRetailerName());
            holder.retailer_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    businessModel.setRetailerMasterBO(holder.retailerMasterBO);
                    businessModel.configurationMasterHelper.loadDeliveryUOMConfiguration();
                    //businessModel.mSelectedActivityName = menu.getMenuName();
                    Intent i = new Intent(getActivity(), DeliveryManagement.class);
                    //i.putExtra("screentitle", menu.getMenuName());
                    i.putExtra("From", "HOME MENU");
                    startActivity(i);
                }
            });


        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView retailer_name;
            CardView retailer_card;
            RetailerMasterBO retailerMasterBO;


            public ViewHolder(View v) {
                super(v);
                retailer_name = (TextView) v.findViewById(R.id.tv_retailername);
                retailer_card = (CardView) v.findViewById(R.id.retailer_card);
                retailer_name.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }


        }
    }
}
