package com.ivy.cpg.view.profile;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by anish.k on 12/26/2017.
 */

public class SalesPerCategory extends IvyBaseFragment {

    protected BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected RecyclerAdapter recyclerAdapter;
    private boolean _hasLoadedOnce = false;
    private String lovLabel = "";
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        view = inflater.inflate(R.layout.fragment_sales_cateogry, container,
                false);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);


        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;

            }
        }
    }

    private void initializeViews() {
        if (view != null) {
            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_sales_cateogry);
            if (recyclerView != null) {
                recyclerView.setHasFixedSize(false);
            }
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            loadListData();
        }
    }

    private void loadListData() {
        mBModel.profilehelper.salesPerCategory();
        lovLabel = mBModel.profilehelper.getmSalesCategoryLabel();
        Vector<RetailerMasterBO> salesCateogryList = mBModel.profilehelper.getmSalesCategoryList();
        if(salesCateogryList!=null && salesCateogryList.size()>0) {
            recyclerAdapter = new RecyclerAdapter(salesCateogryList);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
        protected Vector<RetailerMasterBO> data;

        RecyclerAdapter(Vector<RetailerMasterBO> data) {
            this.data = data;
        }

        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_sales_category_list, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.retailerMasterBO = data.get(position);

            holder.TVCategoryName.setText(holder.retailerMasterBO.getSalesProductSName());
            holder.TVCategoryLabel.setText(lovLabel);
            holder.TVInvoiceId.setText(holder.retailerMasterBO.getSalesInvoiceId());
            holder.TVInvoiceValue.setText(holder.retailerMasterBO.getSalesInvoiceValue());
            holder.TVInvoiceQty.setText(holder.retailerMasterBO.getSalesQty());
            holder.TVInvoiceLpc.setText(holder.retailerMasterBO.getSalesLpc());
            if (position % 2 == 0)
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView TVCategoryName,TVCategoryLabel, TVInvoiceId,TVInvoiceValue,TVInvoiceQty,TVInvoiceLpc;
            RetailerMasterBO retailerMasterBO;
            LinearLayout layoutBackground;

            MyViewHolder(View itemView) {
                super(itemView);
                TVCategoryName = (TextView) itemView.findViewById(R.id.txt_categoryName);
                TVCategoryLabel = (TextView) itemView.findViewById(R.id.txt_categoryLabel);
                TVInvoiceId = (TextView) itemView.findViewById(R.id.txt_invoice_no);
                TVInvoiceValue=(TextView)itemView.findViewById(R.id.txt_invoice_value);
                TVInvoiceQty=(TextView)itemView.findViewById(R.id.txt_invoice_qty);
                TVInvoiceLpc = (TextView)itemView.findViewById(R.id.txt_invoice_lpc);

                layoutBackground=(LinearLayout)itemView.findViewById(R.id.list_background);
            }
        }

    }
}
