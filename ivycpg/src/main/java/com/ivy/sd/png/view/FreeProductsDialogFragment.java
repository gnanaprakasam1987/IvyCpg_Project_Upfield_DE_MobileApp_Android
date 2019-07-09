package com.ivy.sd.png.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.List;

/**
 * Created by Rajkumar on 3/1/18.
 */

public class FreeProductsDialogFragment extends DialogFragment {


    BusinessModel mBModel;
    ListView listView;
    SchemeBO schemeBO;

    public FreeProductsDialogFragment() {

    }

    @SuppressLint("ValidFragment")
    public FreeProductsDialogFragment(SchemeBO schemeBO) {
        this.schemeBO = schemeBO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_products, container, false);

        Context context = getActivity();
        mBModel = (BusinessModel) context.getApplicationContext();
        listView = (ListView) view.findViewById(R.id.list);

        TextView title = (TextView) view.findViewById(R.id.tv_toolbar_title);
        title.setText(getResources().getString(R.string.free_products));

        Button button_close = (Button) view.findViewById(R.id.btn_close);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<SchemeProductBO> list = schemeBO.getFreeProducts();
        if (list != null) {
            MyAdapter adapter = new MyAdapter(list);
            listView.setAdapter(adapter);
        }
    }

    private class MyAdapter extends ArrayAdapter<SchemeProductBO> {
        SchemeProductBO product;
        private List<SchemeProductBO> items;

        public MyAdapter(List<SchemeProductBO> items) {
            super(getActivity(), R.layout.row_stock_report_listview, items);
            this.items = items;
        }

        public SchemeProductBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;
            product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_free_product_dialog,
                        parent, false);
                holder = new ViewHolder();


                holder.textView_product_name = (TextView) row.findViewById(R.id.product_name);
                holder.textView_quantity = (TextView) row.findViewById(R.id.quantity);

                holder.textView_product_name.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.textView_quantity.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.textView_product_name.setText(product.getProductName());
            holder.textView_quantity.setText(product.getQuantityMaximum() + " " + product.getUomDescription());

            return row;
        }
    }

    class ViewHolder {
        TextView textView_product_name;
        TextView textView_quantity;
    }
}
