package com.ivy.ui.AssetServiceRequest;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.ui.AssetServiceRequest.di.AssetServiceRequestModule;
import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.task.view.SwipeRevealLayout;
import com.ivy.ui.task.view.ViewBinderHelper;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class AssetServiceReqFragment extends BaseFragment implements AssetServiceRequestContractor.AssetServiceListView{

    FloatingActionButton fab_newServiceRequest;
    RecyclerView recyclerView;
    @Inject
    AppDataProvider appDataProvider;
    private boolean isFromReport;

    private static String REQUEST_STATUS_PENDING="PENDING";
    private static String REQUEST_STATUS_CANCELLED="CANCELLED";

    @Inject
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetServiceView> presenter;

    RecyclerAdapter adapter;

    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    @Override
    public void initializeDi() {

        DaggerAssetServiceRequestComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .assetServiceRequestModule(new AssetServiceRequestModule(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_asset_service_request;
    }

    @Override
    public void init(View view) {


        recyclerView=view.findViewById(R.id.list_service_requests);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager layout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layout);

        fab_newServiceRequest=view.findViewById(R.id.fab);
        fab_newServiceRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(getActivity(),NewAssetServiceRequest.class);
                startActivity(intent);

            }
        });
        // to open only one row at a time
        binderHelper.setOpenOnlyOne(true);

    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            isFromReport = getArguments().getBoolean("isFromReport", false);
        }
    }

    @Override
    protected void setUpViews() {

        if(isFromReport){
            fab_newServiceRequest.setVisibility(View.GONE);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadServiceRequests(isFromReport);

    }

    @Override
    public void listServiceRequests(ArrayList<SerializedAssetBO> requestList) {

        adapter=new RecyclerAdapter(requestList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showErrorMessage(int type) {

        if(type==0){
            Toast.makeText(getActivity(),getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
        }
        else if(type==1){
            Toast.makeText(getActivity(),getResources().getString(R.string.no_data_exists),Toast.LENGTH_LONG).show();
        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        ArrayList<SerializedAssetBO> data;

        RecyclerAdapter(ArrayList<SerializedAssetBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_service_request, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.assetTrackingBO = data.get(position);

            binderHelper.bind(holder.swipeLayout, holder.assetTrackingBO.getRField());
            binderHelper.closeLayout(holder.assetTrackingBO.getRField());
            holder.tv_assetName.setText(holder.assetTrackingBO.getAssetName());

            String serialNum="S.No - "+holder.assetTrackingBO.getSerialNo();
            holder.tv_serialNum.setText(serialNum);
            holder.tv_status.setText(holder.assetTrackingBO.getAssetServiceReqStatus());

            holder.tv_createdby.setText("Created by "+holder.assetTrackingBO.getRemarks()+" @ "+holder.assetTrackingBO.getServiceRequestedRetailer());

            if(REQUEST_STATUS_PENDING.equalsIgnoreCase(holder.assetTrackingBO.getAssetServiceReqStatus())){
                holder.tv_status.setTextColor(getResources().getColor(R.color.colorPrimaryOrange));
            }
            else if(REQUEST_STATUS_CANCELLED.equalsIgnoreCase(holder.assetTrackingBO.getAssetServiceReqStatus())){
                holder.tv_status.setTextColor(getResources().getColor(R.color.RED));
                //stop swipe
                binderHelper.lockSwipe(holder.assetTrackingBO.getRField());
            }

            holder.btnEditRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(),NewAssetServiceRequest.class);
                    intent.putExtra("isEditMode",true);
                    intent.putExtra("obj",holder.assetTrackingBO);
                    startActivity(intent);
                }
            });

            holder.btnCancelRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelRequestAlert(holder.assetTrackingBO.getRField());
                }
            });

            holder.swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(),AssetServiceRequestViewActivity.class);
                    intent.putExtra("requestId",holder.assetTrackingBO.getRField());
                    intent.putExtra("obj",holder.assetTrackingBO);
                    intent.putExtra("isFromReport",isFromReport);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private SwipeRevealLayout swipeLayout;
            TextView tv_assetName,tv_serialNum,tv_status,tv_createdby;
            SerializedAssetBO assetTrackingBO;
            Button btnEditRequest;
            Button btnCancelRequest;
            RelativeLayout rowView;


            MyViewHolder(View itemView) {
                super(itemView);
                swipeLayout = itemView.findViewById(R.id.swipe_layout);
                tv_assetName = itemView.findViewById(R.id.tv_asset_name);
                tv_serialNum = itemView.findViewById(R.id.tv_serialNo);
                tv_status = itemView.findViewById(R.id.tv_status);
                btnCancelRequest = itemView.findViewById(R.id.delete_button);
                btnEditRequest = itemView.findViewById(R.id.edit_button);
                rowView = itemView.findViewById(R.id.layout_row);
                tv_createdby=itemView.findViewById(R.id.tv_createdby);

            }
        }
    }

    @Override
    public void onCancelledSuccessfully() {

        presenter.loadServiceRequests(isFromReport);
        Toast.makeText(getActivity(),getResources().getString(R.string.request_cancelled_successfully),Toast.LENGTH_LONG).show();

    }

    private void cancelRequestAlert(String requestId){
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.do_u_want_to_cancel),
                "", getResources().getString(R.string.yes), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                presenter.cancelServiceRequest(requestId);
            }
        }, getResources().getString(R.string.no), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

}
