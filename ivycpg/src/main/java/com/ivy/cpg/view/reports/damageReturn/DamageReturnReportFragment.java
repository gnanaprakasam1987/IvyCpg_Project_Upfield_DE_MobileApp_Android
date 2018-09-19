package com.ivy.cpg.view.reports.damageReturn;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.ContactCreationFragment;
import com.ivy.sd.png.view.UserSettingsActivity;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by murugan on 17/9/18.
 */

public class DamageReturnReportFragment extends IvyBaseFragment {


    Unbinder unbinder;

    @BindView(R.id.pending_delivery_listview)
    ListView listView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_damage_return, container,
                false);
        unbinder = ButterKnife.bind(this, view);
        getContractDate();

        return view;
    }
    ArrayList<PandingDeliveryBO> pandingDeliveryBOS=new ArrayList<>();
    private void getContractDate() {

        if (DamageReturenReportHelper.getInstance().getPandingDeliveryBOS().size() > 0) {

            for(int i=0;i<DamageReturenReportHelper.getInstance().getPandingDeliveryBOS().size();i++){
                if(AppUtils.isEmptyString(DamageReturenReportHelper.getInstance().getPandingDeliveryBOS().get(i).getStatus())){
                    pandingDeliveryBOS.add(DamageReturenReportHelper.getInstance().getPandingDeliveryBOS().get(i));
                }
            }
            DamageReturnReportFragment.MyAdapter adapter = new DamageReturnReportFragment.MyAdapter(pandingDeliveryBOS);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
        }

    }

    @OnItemClick(R.id.pending_delivery_listview)
    void onItemSelected(int position){

        Intent i = new Intent(getActivity(), DamageDetailsActivity.class);
        i.putExtra("InvoiceNo", pandingDeliveryBOS.get(position).getInvoiceNo());
        getActivity().startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    class MyAdapter extends BaseAdapter {
        ArrayList<PandingDeliveryBO> arrayList;

        public MyAdapter(ArrayList<PandingDeliveryBO> conList) {
            arrayList = conList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public PandingDeliveryBO getItem(int arg0) {
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            DamageReturnReportFragment.ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.pending_delivery_list_item_, parent, false);
                holder = new DamageReturnReportFragment.ViewHolder(convertView);

                holder.statusTitle.setVisibility(View.GONE);
                holder.status.setVisibility(View.GONE);

                holder.invoiceNoTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invoiceNo.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.invoiceDateTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invoiceDate.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.invNetamounTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.invNetamount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                holder.storeNameTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                holder.txtStorename.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

                convertView.setTag(holder);
            } else {
                holder = (DamageReturnReportFragment.ViewHolder) convertView.getTag();
            }
            PandingDeliveryBO pandingDeliveryBO = arrayList.get(position);

            holder.invoiceNo.setText(pandingDeliveryBO.getInvoiceNo());
            holder.invoiceDate.setText(pandingDeliveryBO.getInvoiceDate());
            holder.invNetamount.setText(pandingDeliveryBO.getInvNetamount());
            holder.txtStorename.setText(pandingDeliveryBO.getRetailerName());

            return convertView;
        }

    }

    class ViewHolder {
        @BindView(R.id.invoiceNo_title)
        TextView invoiceNoTitle;

        @BindView(R.id.txtInvoiceNo)
        TextView invoiceNo;

        @BindView(R.id.invoiceDate_title)
        TextView invoiceDateTitle;

        @BindView(R.id.txtInvoiceDate)
        TextView invoiceDate;

        @BindView(R.id.invNetamount_title)
        TextView invNetamounTitle;

        @BindView(R.id.txtInvNetamount)
        TextView invNetamount;

        @BindView(R.id.txtStorename)
        TextView txtStorename;

        @BindView(R.id.storename_title)
        TextView storeNameTitle;

        @BindView(R.id.status_title)
        TextView statusTitle;

        @BindView(R.id.txtStatus)
        TextView status;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }


    }
}
