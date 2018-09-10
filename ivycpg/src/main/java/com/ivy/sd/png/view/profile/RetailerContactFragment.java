package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by mansoor.k on 30-07-2018.
 */
public class RetailerContactFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private View view;
    private RecyclerView rvContacts;
    private boolean _hasLoadedOnce = false;
    private ArrayList<RetailerContactBo> retailerContactList;
    private HashMap<String, String> contactMenuMap;

    private static String CODE_CONTACTNAME = "CONTACTNAME";
    private static String CODE_CONTACTNUMBER = "CONTACTNUMBER";
    private static String CODE_CONTACTPRIMARY = "CONTACTPRIMARY";
    private static String CODE_CONTACTMAIL = "CONTACTMAIL";
    private static String CODE_CONTACTAVAILABILITY = "CONTACTAVAILABILITY"; // for future devlopement
    private AppSchedulerProvider appSchedulerProvider;

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
        appSchedulerProvider = new AppSchedulerProvider();

        new CompositeDisposable().add((Disposable) bmodel.profilehelper.downloadRetailerContactMenu()
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(getHashMapObserver()));

    }

    private Observer<HashMap<String, String>> getHashMapObserver() {
        return new DisposableObserver<HashMap<String, String>>() {
            @Override
            public void onNext(HashMap<String, String> menuMap) {
                contactMenuMap = menuMap;
                if (contactMenuMap.size() > 0)
                    getDataToPopulate();
                else
                    Toast.makeText(getActivity(), getString(R.string.retailer_contact_menu), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void getDataToPopulate() {
        new CompositeDisposable().add((Disposable)bmodel.profilehelper.downloadRetailerContact(bmodel.getRetailerMasterBO().getRetailerID(),false)
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(arrayListObserver()));
    }

    private Observer<ArrayList<RetailerContactBo>> arrayListObserver(){
        return new DisposableObserver<ArrayList<RetailerContactBo>>() {
            @Override
            public void onNext(ArrayList<RetailerContactBo> contactList) {
                retailerContactList = contactList;
                if (retailerContactList.size() > 0)
                    populateData();
                else
                    Toast.makeText(getActivity(), getString(R.string.retailer_contact_list), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
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

    private void populateData() {
        rvContacts = view.findViewById(R.id.rv_contacts);
        rvContacts.setHasFixedSize(false);
        rvContacts.setNestedScrollingEnabled(false);
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContacts.addItemDecoration(new DividerItemDecoration(rvContacts.getContext(), DividerItemDecoration.HORIZONTAL));

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
            if (contactMenuMap.get(CODE_CONTACTNAME) != null) {
                if (retailerContactBo.getTitle().length() > 0)
                    holder.title.setText(retailerContactBo.getTitle());
                else
                    holder.title.setVisibility(View.GONE);
                holder.firstName.setText(retailerContactBo.getFistname() + " " + retailerContactBo.getLastname());
            }

            if (contactMenuMap.get(CODE_CONTACTPRIMARY) != null) {
                if (retailerContactBo.getIsPrimary() == 1)
                    holder.ivIsPrimary.setVisibility(View.VISIBLE);
                else
                    holder.ivIsPrimary.setVisibility(View.INVISIBLE);

            }
            if (contactMenuMap.get(CODE_CONTACTNUMBER) != null && retailerContactBo.getContactNumber().length() > 0)
                holder.textCno.setText(retailerContactBo.getContactNumber());
            else
                holder.textCno.setVisibility(View.GONE);


            if (contactMenuMap.get(CODE_CONTACTMAIL) != null && retailerContactBo.getContactMail().length() > 0)
                holder.textCEmail.setText(retailerContactBo.getContactMail());
            else
                holder.textCEmail.setVisibility(View.GONE);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title, firstName;
            private TextView textCno, textCEmail;
            private ImageView ivIsPrimary;

            public ViewHolder(View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.tvTitle);
                firstName = itemView.findViewById(R.id.tvFirstName);
                ivIsPrimary = itemView.findViewById(R.id.ivIsPrimary);
                textCno = itemView.findViewById(R.id.tvContactNo);
                textCEmail = itemView.findViewById(R.id.tvEmail);

                title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                firstName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                textCno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                textCEmail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

                if (contactMenuMap.get(CODE_CONTACTNAME) == null) {
                    title.setVisibility(View.GONE);
                    firstName.setVisibility(View.GONE);
                }

                if (contactMenuMap.get(CODE_CONTACTNUMBER) == null)
                    textCno.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTPRIMARY) == null)
                    ivIsPrimary.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTMAIL) == null)
                    textCEmail.setVisibility(View.GONE);

            }
        }
    }

}



