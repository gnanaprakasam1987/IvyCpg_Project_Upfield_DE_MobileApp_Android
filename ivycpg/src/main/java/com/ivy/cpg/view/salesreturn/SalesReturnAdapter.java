package com.ivy.cpg.view.salesreturn;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.CustomKeyBoard;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class SalesReturnAdapter extends RecyclerView.Adapter<SalesReturnAdapter.MyViewHolder>  {


    private final Vector<ProductMasterBO> items;
    private CustomKeyBoard dialogCustomKeyBoard;
    private Context context;
    private ProductMasterBO productMasterBO;
    private BusinessModel bmodel;
    private SalesReturnInterface salesReturnInterface;
    private ArrayList<String> productIdList;
    private RequestManager glide;
    private File appImageFolderPath;
    private SalesReturnHelper helper;

    SalesReturnAdapter(Vector<ProductMasterBO> items, Context context, BusinessModel bmodel, RequestManager glide) {

        this.context=context;
        this.items = items;
        this.bmodel=bmodel;
        this.glide = glide;
        appImageFolderPath = bmodel.synchronizationHelper.getStorageDir(context.getResources().getString(R.string.app_name));
        helper=SalesReturnHelper.getInstance(context);
        if (salesReturnInterface == null) {
            if (context instanceof SalesReturnInterface) {
                this.salesReturnInterface = (SalesReturnInterface) context;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (helper.IS_SHOW_SR_CATALOG)
            return 2;
        else
            return 1;

    }

    @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        if(viewType==1)
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_salesreturn, parent, false);
        else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_salesreturn_catalog, parent, false);
        }

            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            productMasterBO = items.get(position);

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
            else {
                holder.productCode.setVisibility(View.GONE);
            }


            int total = 0;
            for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
                total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());

            String strTotal =Integer.toString(total);
            if(helper.IS_SHOW_SR_CATALOG) {
                strTotal = context.getResources().getString(R.string.total) + ": " +Integer.toString(total);
            }
            holder.total.setText(strTotal);

            holder.layout_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    salesReturnInterface.onListItemSelected(holder.productBO.getProductID());

                }
            });
            holder.total.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                   salesReturnInterface.showSalesReturnDialog(holder.productBO.getProductID());
                }
            });

            if(helper.IS_SHOW_SR_CATALOG) {
                holder.pdt_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bmodel.selectedPdt = holder.productBO;

                   /* Intent i = new Intent(context, ProductDetailsCatalogActivity.class);
                    context.startActivity(i);*/
                    }
                });

                if (holder.pdt_image != null) {

                    File mFile = new File(
                            appImageFolderPath
                                    + "/"
                                    + DataMembers.CATALOG + "/" + holder.productBO.getProductCode() + ".jpg");
                    if (!mFile.exists())
                        mFile = new File(
                                appImageFolderPath
                                        + "/"
                                        + DataMembers.CATALOG + "/" + holder.productBO.getProductCode() + ".png");

                    Uri path;
                    if (Build.VERSION.SDK_INT >= 24) {
                        path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", mFile);
                    } else {
                        path = Uri.fromFile(mFile);
                    }
                    //Glide.with(getApplicationContext())
                    glide.load(path)
                            .error(ContextCompat.getDrawable(context.getApplicationContext(), R.drawable.no_image_available))
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.pdt_image);

                }
            }

        }


        @Override
        public void onViewRecycled(@NonNull MyViewHolder holder) {
            super.onViewRecycled(holder);

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {


            private SalesReturnReasonBO reasonBO;
            private ProductMasterBO productBO;
            private String pname;
            private TextView psname, productCode;
            private TextView total;
            private LinearLayout layout_row;
            private ImageView pdt_image;

            public MyViewHolder(View row) {
                super(row);

                psname = row.findViewById(R.id.productName);
                productCode = row.findViewById(R.id.sales_return_prod_code);
                psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                total = row.findViewById(R.id.total);
                layout_row= row.findViewById(R.id.layout_row);
                pdt_image=row.findViewById(R.id.pdt_image);

                
            }
        }


interface SalesReturnInterface{
    void onListItemSelected(String pid);
    void showSalesReturnDialog(String pid);
}

}
