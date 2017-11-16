package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

/**
 * Created by mayuri.v on 9/22/2017.
 */
public class OrderTransactionListDialog extends Dialog {
    private final newOrderOnClickListener newOrderClickListener;
    private final oldOrderOnClickListener oldOrderClickListener;
    private Context context;
    private Context bContext;
    protected BusinessModel bmodel;
    private View rootView;

    public OrderTransactionListDialog(Context bContext, Context context, newOrderOnClickListener newOrderClickListener, oldOrderOnClickListener oldOrderClickListener) {
        super(context);
        this.context = context;
        this.bContext = bContext;
        bmodel = (BusinessModel) bContext;
        this.newOrderClickListener = newOrderClickListener;
        this.oldOrderClickListener = oldOrderClickListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = null;
        view = View.inflate(context, R.layout.order_transaction_dialog_layout, null);
        setContentView(view);

        RecyclerView transaction_list_view = (RecyclerView) findViewById(R.id.transaction_list);
        ViewGroup.LayoutParams params = transaction_list_view.getLayoutParams();
        if (bmodel.getOrderIDList().size() > 7) {
            params.height = (int) context.getResources().getDimension(R.dimen.dialog_height);
            ;
        } else {
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        }
        transaction_list_view.setLayoutParams(params);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        transaction_list_view.setLayoutManager(linearLayoutManager);
        transaction_list_view.setAdapter(new TransactionListAdapter());

        TextView new_order_tv = ((TextView) findViewById(R.id.new_order_tv));
        new_order_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newOrderClickListener.onNewOrderButtonClick();
                dismiss();
            }
        });
        new_order_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
    }

    public interface newOrderOnClickListener {
        void onNewOrderButtonClick();
    }

    public interface oldOrderOnClickListener {
        void onOldOrderButtonClick(String s);
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
                orderid = (TextView) view.findViewById(R.id.orderid);
                orderid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
    }

}
