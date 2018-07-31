package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

/**
 * Created by mansoor.k on 30-07-2018.
 */
public class RetailerContactFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private View view;
    private RecyclerView rvContacts;
    private boolean _hasLoadedOnce = false;
    private ArrayList<RetailerContactBo> retailerContactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        view = inflater.inflate(R.layout.fragment_retailer_contact, container,
                false);
        return view;
    }

    private void initializeViews() {
        retailerContactList = bmodel.profilehelper.downloadRetailerContact(bmodel.getRetailerMasterBO().getRetailerID());
        initialization();
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);


        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    private void initialization() {
        rvContacts = view.findViewById(R.id.rv_contacts);
        ContactsAdapter contactsAdapter = new ContactsAdapter(retailerContactList);
        rvContacts.setAdapter(contactsAdapter);

    }

    public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        private ArrayList<RetailerContactBo> items;

        public ContactsAdapter(ArrayList<RetailerContactBo> items) {
            this.items = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_contact_listitem, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final RetailerContactBo retailerContactBo = items.get(position);


        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView deliveryStatus_txt, deliveryStatus_val;

            public ViewHolder(View itemView) {
                super(itemView);


            }
        }
    }

}



