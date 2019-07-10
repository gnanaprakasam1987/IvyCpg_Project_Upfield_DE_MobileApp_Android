package com.ivy.cpg.view.retailercontact;

import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by mansoor.k on 30-07-2018.
 */
public class RetailerContactFragment extends IvyBaseFragment implements BlockingStep {
    private BusinessModel bmodel;
    private View view;
    private RecyclerView rvContacts;
    private boolean _hasLoadedOnce = false;
    private ArrayList<RetailerContactBo> retailerContactList;
    private HashMap<String, String> contactMenuMap;

    private final String CODE_CONTACTNAME = "CONTACTNAME";
    private final String CODE_CONTACTNUMBER = "CONTACTNUMBER";
    private final String CODE_CONTACTPRIMARY = "CONTACTPRIMARY";
    private final String CODE_CONTACTMAIL = "CONTACTMAIL";
    private final String CODE_CONTACTAVAILABILITY = "CONTACTAVAILABILITY";
    private final String CODE_CONTACTEMAILPREF = "CONTACTEMAILPREF";
    private final String CODE_CONTACTDESIGNATION = "CONTACTDESIGNATION";
    private final String CODE_CONTACTSALUTATION = "CONTACTSALUTATION";
    private AppSchedulerProvider appSchedulerProvider;

    private String retailerId ="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        retailerId =  getArguments() != null?getArguments().getString("RetailerId"):"";

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
        new CompositeDisposable().add((Disposable) bmodel.profilehelper.downloadRetailerContact(retailerId, false)
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(arrayListObserver()));
    }

    private Observer<ArrayList<RetailerContactBo>> arrayListObserver() {
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

            String salutation = "";
            if (contactMenuMap.get(CODE_CONTACTSALUTATION) != null ){
                if (StringUtils.isNullOrEmpty(retailerContactBo.getSalutationTitle()))
                    salutation = retailerContactBo.getSalutationTitle();
            }

            if (contactMenuMap.get(CODE_CONTACTNAME) != null) {
                holder.firstName.setText(salutation +" " +retailerContactBo.getFistname() + " " + retailerContactBo.getLastname());
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

            if (contactMenuMap.get(CODE_CONTACTDESIGNATION) != null) {
                if (StringUtils.isNullOrEmpty(retailerContactBo.getTitle()))
                    holder.title.setText(retailerContactBo.getTitle());
                else
                    holder.title.setVisibility(View.GONE);
            }

            if (contactMenuMap.get(CODE_CONTACTEMAILPREF) != null && retailerContactBo.getIsEmailPrimary() == 1)
                holder.imgViewEmail.setVisibility(View.VISIBLE);
            else
                holder.imgViewEmail.setVisibility(View.GONE);

            if (contactMenuMap.get(CODE_CONTACTAVAILABILITY) != null && retailerContactBo.getContactAvailList().size() > 0) {
                holder.timeSlotLayout.setVisibility(View.VISIBLE);
                holder.rvTimeslot.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                ContactsTimeSlotAdapter timeSlotAdapter = new ContactsTimeSlotAdapter(getActivity(),retailerContactBo.getContactAvailList());
                holder.rvTimeslot.setAdapter(timeSlotAdapter);
            }else
                holder.timeSlotLayout.setVisibility(View.GONE);

            holder.imgShrinkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (retailerContactBo.getContactAvailList().size() > 0 && holder.rvTimeslot.getVisibility() == View.VISIBLE) {
                        holder.rvTimeslot.setVisibility(View.GONE);
                        holder.imgShrinkView.setImageResource(R.drawable.ic_action_up);
                    }else if (retailerContactBo.getContactAvailList().size() > 0) {
                        holder.rvTimeslot.setVisibility(View.VISIBLE);
                        holder.imgShrinkView.setImageResource(R.drawable.ic_action_down);
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title, firstName;
            private TextView textCno, textCEmail;
            private ImageView ivIsPrimary, ivIsdelete,imgViewEmail;
            private RelativeLayout timeSlotLayout;
            private RecyclerView rvTimeslot;
            private ImageView imgShrinkView;

            public ViewHolder(View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.tvTitle);
                firstName = itemView.findViewById(R.id.tvFirstName);
                ivIsPrimary = itemView.findViewById(R.id.ivIsPrimary);
                textCno = itemView.findViewById(R.id.tvContactNo);
                textCEmail = itemView.findViewById(R.id.tvEmail);
                ivIsdelete = itemView.findViewById(R.id.ivIsdelete);
                imgViewEmail = itemView.findViewById(R.id.img_email_pref);

                timeSlotLayout = itemView.findViewById(R.id.time_slot_layout);
                imgShrinkView = itemView.findViewById(R.id.img_shrink_view);
                rvTimeslot = itemView.findViewById(R.id.rv_timeslot);

                if (contactMenuMap.get(CODE_CONTACTNAME) == null) {
                    firstName.setVisibility(View.GONE);
                }

                if (contactMenuMap.get(CODE_CONTACTNUMBER) == null)
                    textCno.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTPRIMARY) == null)
                    ivIsPrimary.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTMAIL) == null)
                    textCEmail.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTAVAILABILITY) == null)
                    timeSlotLayout.setVisibility(View.GONE);
//
                if (contactMenuMap.get(CODE_CONTACTDESIGNATION) == null)
                    title.setVisibility(View.GONE);

                if (contactMenuMap.get(CODE_CONTACTEMAILPREF) == null)
                    imgViewEmail.setVisibility(View.GONE);

                ivIsdelete.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        callback.complete();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

}



