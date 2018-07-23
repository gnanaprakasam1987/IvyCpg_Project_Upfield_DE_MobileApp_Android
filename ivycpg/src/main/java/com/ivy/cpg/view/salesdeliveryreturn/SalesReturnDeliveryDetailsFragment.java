package com.ivy.cpg.view.salesdeliveryreturn;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

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
    private CompositeDisposable compositeDisposable;

    @BindView(R.id.SalesReturn_Details)
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salesreturn_deliverydetails, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String uId = "";
        if (getArguments() != null) {
            uId = getArguments().getString("UID");


        }
        getSalesReturnDeliveryDetails(uId);
    }


    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    private void getSalesReturnDeliveryDetails(String uId) {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) SalesReturnDeliveryHelper.getInstance().getSaleReturnDeliveryDetails(getActivity(), uId)
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
        SalesReturnDeliveryAdapter salesReturnDeliveryAdapter =
                new SalesReturnDeliveryAdapter(getActivity().getApplicationContext(), null,
                        salesReturnDeliveryDataModels, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(salesReturnDeliveryAdapter);

    }

    @OnClick(R.id.btn_save)
    public void setSaveSalesReturn() {

        boolean isSuccess = SalesReturnDeliveryHelper.getInstance().saveSalesReturnDelivery(getActivity(), salesReturnDeliveryDataModelsList);
        //busnessmodel.saveModuleCompletion(getActivity().getIntent().getExtras().getString("menuCode"));
        if (isSuccess) {

            Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
            ((SalesReturnDeliveryActivity) getActivity()).onBackPressed();
        }
    }

    private EditText QUANTITY;


    private String append = "";

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            //businessModel.showAlert(
            // getResources().getString(R.string.please_select_item), 0);
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
                    Button ed = (Button) getView().findViewById(vw.getId());
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

        public SalesReturnDeliveryAdapter(Context context, RecyclerViewItemClickListener recyclerViewItemClickListener,
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
        @Override
        public SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.salesreturn_deliverydetails, parent, false);
            return new SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder(view);
        }

        /**
         * @param holder   :view Holder
         * @param position : position of each Row
         *                 set the values to the views
         */
        @Override
        public void onBindViewHolder(final SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, final int position) {


            if (isDetails) {


                holder.productName.setText(salesReturnDeliveryDataModelsList.get(position).getProductName());
                holder.returnCaseQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(position).getReturnCaseQuantity()));
                holder.returnPieceQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(position).getReturnPieceQuantity()));
                holder.reason.setText(salesReturnDeliveryDataModelsList.get(position).getReason());
                holder.actualPieceQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(position).getActualPieceQuantity()));
                holder.actualCaseQuantity.setText(String.valueOf(salesReturnDeliveryDataModelsList.get(position).getActualCaseQuantity()));

                holder.actualCaseQuantity.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.actualCaseQuantity;
                        currentSelectedValue = salesReturnDeliveryDataModelsList.get(position).getReturnCaseQuantity();
                        int inType = holder.actualCaseQuantity.getInputType();
                        holder.actualCaseQuantity.setInputType(InputType.TYPE_NULL);
                        holder.actualCaseQuantity.onTouchEvent(event);
                        holder.actualCaseQuantity.setInputType(inType);
                        holder.actualCaseQuantity.selectAll();
                        holder.actualCaseQuantity.requestFocus();

                        return true;
                    }
                });
                holder.actualPieceQuantity.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.actualPieceQuantity;
                        currentSelectedValue = salesReturnDeliveryDataModelsList.get(position).getReturnPieceQuantity();
                        int inType = holder.actualPieceQuantity.getInputType();
                        holder.actualPieceQuantity.setInputType(InputType.TYPE_NULL);
                        holder.actualPieceQuantity.onTouchEvent(event);
                        holder.actualPieceQuantity.setInputType(inType);
                        holder.actualPieceQuantity.selectAll();
                        holder.actualPieceQuantity.requestFocus();
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
                            salesReturnDeliveryDataModelsList.get(position).setActualCaseQuantity(Integer.valueOf(s.toString()));
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
                        if (!(s.toString()).isEmpty())
                            salesReturnDeliveryDataModelsList.get(position).setActualPieceQuantity(Integer.valueOf(s.toString()));
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
                }
                itemView.setOnClickListener(this);
            }


            @Override
            public void onClick(View view) {
                if (recyclerViewItemClickListener != null)
                    recyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

   /* private void onBackButtonClick() {
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }*/
}
