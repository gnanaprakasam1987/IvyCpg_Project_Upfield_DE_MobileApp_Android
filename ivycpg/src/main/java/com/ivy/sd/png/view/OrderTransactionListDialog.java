package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.FontUtils;

/**
 * Created by mayuri.v on 9/22/2017
 */
public class OrderTransactionListDialog extends Dialog {
    private final newOrderOnClickListener newOrderClickListener;
    private final oldOrderOnClickListener oldOrderClickListener;
    private final OnDismissListener onDismissListener;
    private Context context;
    protected BusinessModel bmodel;
    private boolean hideNewOrder;

    public OrderTransactionListDialog(Context bContext, Context context, newOrderOnClickListener newOrderClickListener, oldOrderOnClickListener oldOrderClickListener, boolean hideNewOrder, OnDismissListener onDismissListener) {
        super(context);
        this.context = context;
        bmodel = (BusinessModel) bContext;
        this.newOrderClickListener = newOrderClickListener;
        this.oldOrderClickListener = oldOrderClickListener;
        this.hideNewOrder = hideNewOrder;
        this.onDismissListener = onDismissListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view;
        view = View.inflate(context, R.layout.order_transaction_dialog_layout, null);
        setContentView(view);

        RecyclerView transaction_list_view = findViewById(R.id.transaction_list);
        ViewGroup.LayoutParams params = transaction_list_view.getLayoutParams();
        if (bmodel.getOrderIDList().size() > 7) {
            params.height = (int) context.getResources().getDimension(R.dimen.dialog_height);
        } else {
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        }
        transaction_list_view.setLayoutParams(params);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        transaction_list_view.setLayoutManager(linearLayoutManager);
        transaction_list_view.setAdapter(new TransactionListAdapter());

        Button new_order_tv = findViewById(R.id.new_order_btn);
        if (hideNewOrder)
            new_order_tv.setVisibility(View.GONE);
        new_order_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newOrderClickListener.onNewOrderButtonClick();
                dismiss();
            }
        });
        new_order_tv.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
        Button close = findViewById(R.id.cancel_btn);
        close.setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onDismissListener.onDismiss();
            }
        });
    }

    public interface newOrderOnClickListener {
        void onNewOrderButtonClick();
    }

    public interface oldOrderOnClickListener {
        void onOldOrderButtonClick(String s);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.MyViewHolder> {

        private View itemView;


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_transaction_dialog_listitem, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.orderid.setText(bmodel.getOrderIDList().get(position));
            holder.orderid.setTag(position);

            holder.orderid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldOrderClickListener.onOldOrderButtonClick(bmodel.getOrderIDList().get((Integer) v.getTag()));
                    dismiss();
                }
            });


        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {

            return bmodel.getOrderIDList().size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView orderid;

            public MyViewHolder(View view) {
                super(view);
                orderid = view.findViewById(R.id.orderid);
                orderid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
    }

}
