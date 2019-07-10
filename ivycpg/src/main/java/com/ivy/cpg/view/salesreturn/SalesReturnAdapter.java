package com.ivy.cpg.view.salesreturn;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.CustomKeyBoard;

import java.util.ArrayList;

public class SalesReturnAdapter extends ArrayAdapter<ProductMasterBO> {


        private final ArrayList<ProductMasterBO> items;
        private CustomKeyBoard dialogCustomKeyBoard;
        private Context context;
        private ProductMasterBO productMasterBO;
        private BusinessModel bmodel;

        SalesReturnAdapter(ArrayList<ProductMasterBO> items, Context context, BusinessModel bmodel) {

            super(context, R.layout.row_salesreturn,
                    items);
            this.context=context;
            this.items = items;
            this.bmodel=bmodel;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            productMasterBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context
                        );

                row = inflater.inflate(R.layout.row_salesreturn, parent, false);

                holder = new ViewHolder();

                holder.psname = row.findViewById(R.id.productName);
                holder.productCode = row.findViewById(R.id.sales_return_prod_code);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.total = row.findViewById(R.id.total);
                holder.totalLL = row.findViewById(R.id.ll_total);

                holder.total.setPaintFlags(holder.total.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.productCode.setVisibility(View.GONE);

                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        productName.setText(holder.pname);


                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();
                        }
                    }
                });

                holder.totalLL.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        View vChild = lvwplist.getChildAt(0);
                        int holderPosition = lvwplist.getFirstVisiblePosition();
                        int holderTop = (vChild == null) ? 0 : (vChild.getTop() - lvwplist.getPaddingTop());

                        productName.setText(holder.pname);
                        showSalesReturnDialog(holder.productBO.getProductID(), v, holderPosition, holderTop);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = productMasterBO;
            if (holder.productBO.getSalesReturnReasonList() != null && holder.productBO.getSalesReturnReasonList().size() != 0)
                holder.reasonBO = holder.productBO.getSalesReturnReasonList().get(holder.productBO.getSelectedSalesReturnPosition());

            holder.pname = productMasterBO.getProductName();
            holder.psname.setText(productMasterBO.getProductShortName());
            if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = context.getResources().getString(R.string.prod_code) + ": " +
                        productMasterBO.getProductCode();
                holder.productCode.setText(prodCode);
            }


            int total = 0;
            for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
                total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
            String strTotal = Integer.toString(total);
            holder.total.setText(strTotal);
            return row;
        }


    class ViewHolder {
        private SalesReturnReasonBO reasonBO;
        private ProductMasterBO productBO;
        private String pname;
        private TextView psname, productCode;
        private TextView total;
        private LinearLayout totalLL;
    }

}
