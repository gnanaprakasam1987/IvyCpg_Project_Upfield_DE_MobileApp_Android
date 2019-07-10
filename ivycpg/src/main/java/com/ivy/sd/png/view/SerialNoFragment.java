package com.ivy.sd.png.view;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.SerialNoInterface;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 11-02-2016.
 */
public class SerialNoFragment extends IvyBaseFragment {
    private static final String TAG="SerialNo Fragment";
    private ListView mProductLV;
    private TextView mTotalQtyTV,mTotalScannedQtyTV;
    private BusinessModel bmodel;
    private SerialNoInterface serialNoInterface;
    private ArrayList<ProductMasterBO> mProductList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SerialNoInterface) {
            this.serialNoInterface = (SerialNoInterface) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


       // mProductList=new ArrayList<ProductMasterBO>();
        //return inflater.inflate(R.layout.fragment_serialno, container, false);
        View mainview = inflater.inflate(R.layout.fragment_serialno, null);
        return mainview;

    }

    @Override
    public void onStart() {
        super.onStart();

//        mProductList=(ArrayList<ProductMasterBO>)getActivity().getIntent().getSerializableExtra("orderList");

        mTotalQtyTV=(TextView)getView().findViewById(R.id.tv_total_qty);
        mTotalScannedQtyTV=(TextView)getView().findViewById(R.id.tv_total_scanned);
        updateOrderList();

    }


    public void updateOrderList(){
        int totalQty=0;
        int totalScannedQty=0;
        mProductList=new ArrayList<ProductMasterBO>();
        for(ProductMasterBO productBO:bmodel.productHelper.getProductMaster()){
            if(productBO.getScannedProduct()==1) {
                if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {
                    totalQty = totalQty + (productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize()));
                    totalScannedQty = totalScannedQty + productBO.getTotalScannedQty();
                    mProductList.add(productBO);

                }
            }

        }
        mTotalQtyTV.setText(totalQty+"");
        mTotalScannedQtyTV.setText(totalScannedQty+"");
        mProductLV=(ListView)getView().findViewById(R.id.lv_productlist);
        mProductLV.setAdapter(new MyAdapter());
    }





    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mProductList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if(convertView==null){
                holder=new Holder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater
                        .inflate(R.layout.list_scanned_product, parent, false);
                holder.tvProductName=(TextView)convertView.findViewById(R.id.tv_product_name);
                holder.tvQty=(TextView)convertView.findViewById(R.id.tv_qty);
                holder.tvScannedQty=(TextView)convertView.findViewById(R.id.tv_scanned);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        serialNoInterface.updateSerialNo(SDUtil.convertToInt(holder.productBO.getProductID()));
                    }
                });
                convertView.setTag(holder);
            }else{
                holder=(Holder)convertView.getTag();
            }
            holder.productBO=mProductList.get(position);
            holder.tvProductName.setText(holder.productBO.getProductShortName()+"");
            int totalQty=holder.productBO.getOrderedPcsQty()+(holder.productBO.getOrderedCaseQty()*holder.productBO.getCaseSize())
                         +(holder.productBO.getOrderedOuterQty()*holder.productBO.getOutersize());
            holder.tvQty.setText(totalQty+"");
            holder.tvScannedQty.setText(holder.productBO.getTotalScannedQty()+"");
            return convertView;
        }
    }
    private class Holder{
        TextView tvProductName;
        TextView tvQty;
        TextView tvScannedQty;
        ProductMasterBO productBO;
    }



}
