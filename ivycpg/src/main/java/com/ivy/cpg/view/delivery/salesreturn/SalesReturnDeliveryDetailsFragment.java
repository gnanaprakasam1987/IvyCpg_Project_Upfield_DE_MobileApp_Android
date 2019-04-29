package com.ivy.cpg.view.delivery.salesreturn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SalesReturnDeliveryDetailsFragment extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.SalesReturn_Details)
    RecyclerView recyclerView;

    private SalesReturnDeliveryDataBo salesReturnDeliveryDataBo = null;
    private BusinessModel bmodel;
    private int mSelectedPos = 0;
    private SalesReturnDeliveryAdapter salesReturnDeliveryAdapter;
    LinearLayout ll_keypad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salesreturn_deliverydetails, container, false);
        unbinder = ButterKnife.bind(this, view);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLabelMaserValue(view);
        if (getArguments() != null) {
            String data = getArguments().getString("DATA");
            salesReturnDeliveryDataBo =
                    new Gson().fromJson(data, SalesReturnDeliveryDataBo.class);
            //  uId = salesReturnDeliveryDataBo.getUId();
        }
        if (salesReturnDeliveryDataBo != null)
            getSalesReturnDeliveryDetails(salesReturnDeliveryDataBo.getUId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bmodel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL)
        setActualQty();
    }

    private void setLabelMaserValue(View view) {

        BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();


        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.cqty).getTag()) != null)
                ((TextView) view.findViewById(R.id.cqty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.cqty)
                                        .getTag()));
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.piececqty).getTag()) != null)
                ((TextView) view.findViewById(R.id.piececqty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.piececqty)
                                        .getTag()));

            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.actual_caseQty).getTag()) != null)
                ((TextView) view.findViewById(R.id.actual_caseQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.actual_caseQty)
                                        .getTag()));

            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.actual_PcQty).getTag()) != null)
                ((TextView) view.findViewById(R.id.actual_PcQty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.actual_PcQty)
                                        .getTag()));

            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_PCS) {
                (view.findViewById(R.id.piececqty)).setVisibility(View.GONE);
                (view.findViewById(R.id.actual_PcQty)).setVisibility(View.GONE);
            }

            if (!salesReturnDeliveryHelper.SHOW_SALES_RET_CASE) {
                (view.findViewById(R.id.cqty)).setVisibility(View.GONE);
                (view.findViewById(R.id.actual_caseQty)).setVisibility(View.GONE);
            }
            ll_keypad = (LinearLayout) view.findViewById(R.id.ll_keypad);
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    private void getSalesReturnDeliveryDetails(String uId) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) SalesReturnDeliveryHelper.getInstance().downloadSaleReturnDeliveryDetails(getActivity(), uId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));

    }

    public Observer<Vector<SalesReturnDeliveryDataModel>> getObserver() {
        return new DisposableObserver<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void onNext(Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
                setUpSalesReturnDeliveryDetailsAdapter(salesReturnDeliveryDataModels);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private void setUpSalesReturnDeliveryDetailsAdapter(Vector<SalesReturnDeliveryDataModel>
                                                                salesReturnDeliveryDataModels) {

        if (salesReturnDeliveryDataModels != null && salesReturnDeliveryDataModels.size() > 0) {
            salesReturnDeliveryAdapter =
                    new SalesReturnDeliveryAdapter(getActivity().getApplicationContext(), null,
                            salesReturnDeliveryDataModels, true);

            recyclerView.setAdapter(salesReturnDeliveryAdapter);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    mLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
        } else {
            Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
        }


    }

    @OnClick(R.id.btn_save)
    public void setSaveSalesReturn() {
        if(SalesReturnDeliveryHelper.getInstance().hasDatatoSave(salesReturnDeliveryDataModelsList)) {
            showConfirmAlert();
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_data_tosave),
                    Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_cancel)
    public void setCancelSalesReturn() {
        showConfirConfirmAlert();
    }

    private void showConfirmAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("IvyCpg")
                .setMessage(getActivity().getString(R.string.do_u_want_to_save))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isSuccess = SalesReturnDeliveryHelper.getInstance().saveSalesReturnDelivery(getActivity(), salesReturnDeliveryDataModelsList, salesReturnDeliveryDataBo);
                        if (isSuccess) {
                            BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
                            businessModel.saveModuleCompletion(HomeScreenTwo.MENU_SALES_RET_DELIVERY, true);
                            Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                            (getActivity()).onBackPressed();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.applyAlertDialogTheme(builder);
    }

    private void showConfirConfirmAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setTitle("IvyCpg")
        .setMessage(getActivity().getString(R.string.do_u_want_to_cancel))
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                boolean isSuccess = SalesReturnDeliveryHelper.getInstance().cancelSalesReturnDelivery(getActivity(), salesReturnDeliveryDataBo);
                if (isSuccess) {
                    BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
                    businessModel.saveModuleCompletion(HomeScreenTwo.MENU_SALES_RET_DELIVERY, true);Toast.makeText(getActivity(), "Cancel Successfully", Toast.LENGTH_SHORT).show();
                    (getActivity()).onBackPressed();
                }
            }
        })
        .setNegativeButton(android.R.string.no, null);
        BusinessModel businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.applyAlertDialogTheme(builder);
    }

    private EditText QUANTITY;


    private String append = "";

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                if (getView() != null) {
                    Button ed = getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    private int currentSelectedValue = 0;

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            if ((!strQuantity.isEmpty()) && (currentSelectedValue >= Integer.parseInt(strQuantity))) {
                QUANTITY.setText(strQuantity);
            } else {
                Toast.makeText(getActivity(), getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");

            }

        } else {
            if ((!append.isEmpty()) && (currentSelectedValue >= Integer.parseInt(append))) {
                QUANTITY.setText(append);
            } else {
                Toast.makeText(getActivity(), getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
                QUANTITY.setText("0");
            }
        }
    }

    private List<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModelsList;

    public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {
        private RecyclerViewItemClickListener recyclerViewItemClickListener;

        private Context mContext;
        private boolean isDetails;


        /**
         * Initialize the values
         *
         * @param context                       : context reference
         * @param recyclerViewItemClickListener : callBack Of ClickListener
         * @param salesReturnDeliveryDataModels : data
         */

        SalesReturnDeliveryAdapter(Context context, RecyclerViewItemClickListener recyclerViewItemClickListener,
                                          Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels, boolean isDetails) {
            this.recyclerViewItemClickListener = recyclerViewItemClickListener;
            mContext = context;
            salesReturnDeliveryDataModelsList = salesReturnDeliveryDataModels;
            this.isDetails = isDetails;
        }


        /**
         * @param parent   : parent ViewPgroup
         * @param viewType : viewType
         * @return ViewHolder
         * <p>
         * Inflate the Views
         * Create the each views and Hold for Reuse
         */
        @NonNull
        @Override
        public SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.salesreturn_deliverydetails, parent, false);
            return new SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder(view);
        }

        /**
         * @param holder   :view Holder
         * @param position : position of each Row
         *                 set the values to the views
         */
        @Override
        public void onBindViewHolder(@NonNull final SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, int position) {


            if (isDetails) {


                holder.productName.setText(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getProductName());
                holder.returnCaseQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getReturnCaseQuantity()));
                holder.returnPieceQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getReturnPieceQuantity()));
                holder.reason.setText(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getReason());
                holder.actualPieceQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getActualPieceQuantity()));
                holder.actualCaseQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getActualCaseQuantity()));

                holder.actualCaseQuantity.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.actualCaseQuantity;
                        currentSelectedValue = salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getReturnCaseQuantity();
                        int inType = holder.actualCaseQuantity.getInputType();
                        holder.actualCaseQuantity.setInputType(InputType.TYPE_NULL);
                        holder.actualCaseQuantity.onTouchEvent(event);
                        holder.actualCaseQuantity.setInputType(inType);
                        holder.actualCaseQuantity.requestFocus();
                        if (holder.actualCaseQuantity.getText().length() > 0)
                            holder.actualCaseQuantity.setSelection(holder.actualCaseQuantity.getText().length());
                        return true;
                    }
                });
                holder.actualPieceQuantity.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.actualPieceQuantity;
                        currentSelectedValue = salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).getReturnPieceQuantity();
                        int inType = holder.actualPieceQuantity.getInputType();
                        holder.actualPieceQuantity.setInputType(InputType.TYPE_NULL);
                        holder.actualPieceQuantity.onTouchEvent(event);
                        holder.actualPieceQuantity.setInputType(inType);
                        holder.actualPieceQuantity.requestFocus();
                        if (holder.actualPieceQuantity.getText().length() > 0)
                            holder.actualPieceQuantity.setSelection(holder.actualPieceQuantity.getText().length());
                        return true;
                    }
                });


                holder.actualCaseQuantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if ((!(s.toString()).isEmpty())) {
                            if (s.toString().length() > 0)
                                holder.actualCaseQuantity.setSelection(s.toString().length());
                            salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).setActualCaseQuantity(Integer.valueOf(s.toString()));
                        }

                    }
                });

                holder.actualPieceQuantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!(s.toString()).isEmpty()) {
                            if (s.toString().length() > 0)
                                holder.actualPieceQuantity.setSelection(s.toString().length());
                            salesReturnDeliveryDataModelsList.get(holder.getAdapterPosition()).setActualPieceQuantity(Integer.valueOf(s.toString()));
                        }
                    }
                });


            }
        }

        @Override
        public int getItemCount() {
            return salesReturnDeliveryDataModelsList.size();
        }


        /**
         * Create The view First Time and hold for reuse
         * View Holder for Create and Hold the view for ReUse the views instead of create again
         * Initialize the views
         */

        public class SalesReturnDeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView productName, returnCaseQuantity, returnPieceQuantity, reason;
            EditText actualCaseQuantity, actualPieceQuantity;


            private SalesReturnDeliveryViewHolder(View itemView) {
                super(itemView);
                if (isDetails) {
                    reason = itemView.findViewById(R.id.txt_reason);
                    productName = itemView.findViewById(R.id.txt_productName);
                    returnCaseQuantity = itemView.findViewById(R.id.txt_returnCaseQuantity);
                    returnPieceQuantity = itemView.findViewById(R.id.txt_returnPieceQuantity);
                    actualCaseQuantity = itemView.findViewById(R.id.txt_actualCaseQuantity);
                    actualPieceQuantity = itemView.findViewById(R.id.txt_actualPieceQuantity);

                    SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
                    if (!salesReturnDeliveryHelper.SHOW_SALES_RET_PCS) {
                        returnPieceQuantity.setVisibility(View.GONE);
                        actualPieceQuantity.setVisibility(View.GONE);
                    }

                    if (!salesReturnDeliveryHelper.SHOW_SALES_RET_CASE) {
                        returnCaseQuantity.setVisibility(View.GONE);
                        actualCaseQuantity.setVisibility(View.GONE);
                    }
                }
                if (bmodel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL) {
                    ll_keypad.setVisibility(View.GONE);
                    actualCaseQuantity.setOnClickListener(this);
                    actualPieceQuantity.setOnClickListener(this);
                }
                itemView.setOnClickListener(this);
            }


            @Override
            public void onClick(View view) {
                if (recyclerViewItemClickListener != null)
                    recyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());

                    if (bmodel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL) {
                        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
                        ArrayList<SalesReturnDeliveryDataModel> dataList = new ArrayList<>();
                        String key = salesReturnDeliveryDataModelsList.get(this.getAdapterPosition()).getProductId() +
                                salesReturnDeliveryDataModelsList.get(this.getAdapterPosition()).getReasonID() +
                                salesReturnDeliveryDataModelsList.get(this.getAdapterPosition()).getInVoiceNumber();
                        if (salesReturnDeliveryHelper.getSalesReturnDelDataMap() != null
                                &&!salesReturnDeliveryHelper.getSalesReturnDelDataMap().isEmpty()
                                && salesReturnDeliveryHelper.getSalesReturnDelDataMap().containsKey(key))
                            dataList = salesReturnDeliveryHelper.getSalesReturnDelDataMap().get(key);

                        if (!dataList.isEmpty()) {
                        Intent i = new Intent(getActivity(),
                                SalesReturnDeliverySKULevel.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("menuName", getActivity().getIntent().getExtras().getString("menuName"));
                        i.putExtra("key", key);
                        mSelectedPos = this.getAdapterPosition();
                        startActivity(i);
                    } else
                            Toast.makeText(getActivity(), getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
                }
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setActualQty() {
        SalesReturnDeliveryHelper salesReturnDeliveryHelper = SalesReturnDeliveryHelper.getInstance();
        ArrayList<SalesReturnDeliveryDataModel> dataList = new ArrayList<>();

        if (salesReturnDeliveryDataModelsList != null) {
            SalesReturnDeliveryDataModel dataModel = salesReturnDeliveryDataModelsList.get(mSelectedPos);
            String key = dataModel.getProductId() + dataModel.getReasonID() + dataModel.getInVoiceNumber();
            int cQty = 0;
            int pQty = 0;
            if (salesReturnDeliveryHelper.getSalesReturnDelDataMap() != null
                    && !salesReturnDeliveryHelper.getSalesReturnDelDataMap().isEmpty()
                    && salesReturnDeliveryHelper.getSalesReturnDelDataMap().containsKey(key))
                dataList = salesReturnDeliveryHelper.getSalesReturnDelDataMap().get(key);

                for (SalesReturnDeliveryDataModel srdData : dataList) {
                    cQty += srdData.getActualCaseQuantity();
                    pQty += srdData.getActualPieceQuantity();
                }
                dataModel.setActualPieceQuantity(pQty);
                dataModel.setActualCaseQuantity(cQty);
                if (salesReturnDeliveryAdapter != null)
                    salesReturnDeliveryAdapter.notifyDataSetChanged();
        }
    }
}
