package com.ivy.cpg.view.profile;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class DsitributorProfileFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    Vector<DistributorMasterBO> distributorMasterBOs = new Vector<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        View view = inflater.inflate(R.layout.fragment_distributor_profile, container,
                false);

        distributorMasterBOs = bmodel.distributorMasterHelper.getDistributorProfileList();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = view.findViewById(R.id.distributor_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView distributorName, address1, address2, address3, contactNumber, email;
            private TextView contactTitle, emailTitle;

            public MyViewHolder(View view) {
                super(view);

                distributorName = view.findViewById(R.id.distributor_name);
                address1 = view.findViewById(R.id.address_1);
                address2 = view.findViewById(R.id.address_2);
                address3 = view.findViewById(R.id.address_3);
                contactNumber = view.findViewById(R.id.contact);
                email = view.findViewById(R.id.email);
                contactTitle = view.findViewById(R.id.contact_title);
                emailTitle = view.findViewById(R.id.email_title);

                try {
                    contactTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(contactTitle.getTag()) != null)
                        contactTitle.setText(bmodel.labelsMasterHelper
                                .applyLabels(contactTitle.getTag()));

                    emailTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(emailTitle.getTag()) != null)
                        emailTitle.setText(bmodel.labelsMasterHelper
                                .applyLabels(emailTitle.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }

                distributorName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                address1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                address2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                address3.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                contactNumber.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                email.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.distributor_profile_item_view, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.distributorName.setText(distributorMasterBOs.get(position).getDName() != null ? distributorMasterBOs.get(position).getDName() : "");
            holder.address1.setText(distributorMasterBOs.get(position).getAddress1() != null ? distributorMasterBOs.get(position).getAddress1() : "");
            holder.address2.setText(distributorMasterBOs.get(position).getAddress2() != null ? distributorMasterBOs.get(position).getAddress2() : "");
            holder.address3.setText(distributorMasterBOs.get(position).getAddress3() != null ? distributorMasterBOs.get(position).getAddress3() : "");
            holder.contactNumber.setText(distributorMasterBOs.get(position).getCNumber() != null ? distributorMasterBOs.get(position).getCNumber() : "");
            holder.email.setText(distributorMasterBOs.get(position).getEmail() != null ? distributorMasterBOs.get(position).getEmail() : "");
        }


        @Override
        public int getItemCount() {
            return distributorMasterBOs.size();
        }
    }
}
