package com.ivy.cpg.view.delivery.invoice;

import android.content.Intent;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by dharmapriya.k on 26/12/17.
 */

public class DeliveryManagementRetailersFragment extends IvyBaseFragment {
    private BusinessModel businessModel;
    private RecyclerView retailer_selection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_retailers, container, false);
        retailer_selection = view.findViewById(R.id.retailer_selection);

        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);

        businessModel = (BusinessModel) getActivity().getApplicationContext();

        setScreenTitle(getArguments().getString("screentitle"));
        GridLayoutManager gridLayoutManager;
        if (is7InchTablet)
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getActivity(), 1);

        retailer_selection.setLayoutManager(gridLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        retailer_selection.setAdapter(new RetailerSelectionAdapter(DeliveryManagementHelper.getInstance(getContext()).getInvoicedRetailerList()));
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
                    Intent i = new Intent(getActivity(), DeliveryManagement.class);
                    i.putExtra("screentitle", holder.retailerMasterBO.getRetailerName());
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
                retailer_name = v.findViewById(R.id.tv_retailername);
                retailer_card = v.findViewById(R.id.retailer_card);
                retailer_name.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

            }


        }
    }
}
