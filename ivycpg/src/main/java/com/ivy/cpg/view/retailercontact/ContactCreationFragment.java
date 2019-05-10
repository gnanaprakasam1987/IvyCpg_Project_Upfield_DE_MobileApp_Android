package com.ivy.cpg.view.retailercontact;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


public class ContactCreationFragment extends IvyBaseFragment implements ContactsTimeSlotAdapterEdit.DeleteTimeSlotListener{
    private BusinessModel bmodel;
    private ArrayList<ConfigureBO> contactConfig;

    private String CODE_CONTACTNAME = "CONTACTNAME";
    private String CODE_CONTACTNUMBER = "CONTACTNUMBER";
    private String CODE_CONTACTPRIMARY = "CONTACTPRIMARY";
    private String CODE_CONTACTMAIL = "CONTACTMAIL";
    private String CODE_CONTACTAVAILABILITY = "CONTACTAVAILABILITY";
    private String CODE_CONTACTEMAILPREF = "CONTACTEMAILPREF";
    private String CODE_CONTACTDESIGNATION = "CONTACTDESIGNATION";
    private String CODE_CONTACTSALUTATION = "CONTACTSALUTATION";
    private boolean ISCONTACTNAME, ISCONTACTNO, ISCONTACTPRIMARY,
            ISCONTACTEMAIL, IS_CONTACTAVAILABILITY , IS_CONTACTEMAILPREF ,
            IS_CONTACTDESIGNATION , IS_CONTACTSALUTATION ;
    private Unbinder unbinder;

    //for editing new contact . created through newoutlet
    private boolean isEdit = false;
    private boolean isProfileEdit = false;
    private RetailerContactBo retailerContactBo = new RetailerContactBo();
    private ArrayAdapter<StandardListBO> contactTitleAdapter;
    private ArrayAdapter<StandardListBO> contactSalutationAdapter;
    private ArrayList<RetailerContactBo> contactList;
    private HashMap<String, ConfigureBO> menuMap = new HashMap<>();

    private ArrayList<InputFilter> inputFilters = new ArrayList<>();
    //views
    @BindView(R.id.tvTitlePrimary)
    TextView tvTitlePrimary;
    @BindView(R.id.cbIsPrimary)
    CheckBox cbIsPrimary;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.sp_reason)
    Spinner sp_reason;
    @BindView(R.id.tvTitleFirstName)
    TextView tvTitleFirstName;
    @BindView(R.id.etFirstName)
    EditText etFirstName;
    @BindView(R.id.tvTitleLastName)
    TextView tvTitleLastName;
    @BindView(R.id.etLastName)
    EditText etLastName;
    @BindView(R.id.tvTitlePhno)
    TextView tvTitlePhno;
    @BindView(R.id.etPhno)
    EditText etPhno;
    @BindView(R.id.tvTitleEmail)
    TextView tvTitleEmail;
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.addbutton)
    Button addbutton;
    @BindView(R.id.clear_button)
    Button clearButton;
    @BindView(R.id.etOthers)
    EditText etOthers;

    @BindView(R.id.salutation_Title)
    TextView tvSalutationTitle;

    @BindView(R.id.tvTitlePrimaryEmail)
    TextView tvIsPrimaryEmail;

    @BindView(R.id.tv_time_slot)
    TextView tvTimslotText;

    @BindView(R.id.add_time_slot)
    ImageView addTimeSlot;

    @BindView(R.id.cbIsPrimaryEmail)
    CheckBox cbIsPrimaryEmail;

    @BindView(R.id.sp_salutation)
    Spinner salutationSpinner;

    @BindView(R.id.rv_contacts)
    RecyclerView rvContacts;

    @BindView(R.id.rv_timeslot)
    RecyclerView rvTimeslot;

    @BindView(R.id.time_slot_layout)
    RelativeLayout timeSlotLayout;

    @BindView(R.id.img_shrink_view)
    ImageView imgShrinkView;

    private Context context;

    private ContactsTimeSlotAdapterEdit timeSlotAdapter;
    private TimeSlotPickFragment timeSlotPickFragment;

    public static ContactCreationFragment getInstance(boolean isFromEditProfileView) {
        ContactCreationFragment creationFragment = new ContactCreationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit", isFromEditProfileView);
        creationFragment.setArguments(bundle);
        return creationFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(getActivity());

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Activity)context).getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_contact_creation, container, false);
        unbinder = ButterKnife.bind(this, view);

        initializeViews();
        addEditTextChangedListener();
        addPrimaryCheckBoxChangedListener();
        addTitleSpinnertemSelectedListener();

        addTimePickerListener();

        imgShrinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!retailerContactBo.getContactAvailList().isEmpty() && rvTimeslot.getVisibility() == View.VISIBLE) {
                    rvTimeslot.setVisibility(View.GONE);
                    imgShrinkView.setImageResource(R.drawable.ic_action_up);
                }else if (!retailerContactBo.getContactAvailList().isEmpty()) {
                    rvTimeslot.setVisibility(View.VISIBLE);
                    imgShrinkView.setImageResource(R.drawable.ic_action_down);
                }

            }
        });

        return view;
    }

    private void addTimePickerListener() {

        addTimeSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                timeSlotPickFragment = new TimeSlotPickFragment();
                timeSlotPickFragment.setCancelable(true);
                timeSlotPickFragment.show(ft, "TimeSlotFragment");
            }
        });
    }

    private void initializeViews() {

        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = ((Activity)context).getIntent().getExtras();
        if (bundle != null) {
            isProfileEdit = bundle.getBoolean("isEdit", false);
        }

        AppSchedulerProvider appSchedulerProvider = new AppSchedulerProvider();
        new CompositeDisposable().add((Disposable) bmodel.profilehelper.downloadContactModuleConfig(isProfileEdit)
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(getContactConfig()));
    }

    private Observer<ArrayList<ConfigureBO>> getContactConfig() {
        return new DisposableObserver<ArrayList<ConfigureBO>>() {
            @Override
            public void onNext(ArrayList<ConfigureBO> configureBOS) {
                if (!isProfileEdit) {
                    CODE_CONTACTNAME = "N" + CODE_CONTACTNAME;
                    CODE_CONTACTNUMBER = "N" + CODE_CONTACTNUMBER;
                    CODE_CONTACTPRIMARY = "N" + CODE_CONTACTPRIMARY;
                    CODE_CONTACTMAIL = "N" + CODE_CONTACTMAIL;

                    CODE_CONTACTAVAILABILITY = "N" + CODE_CONTACTAVAILABILITY;
                    CODE_CONTACTDESIGNATION = "N" + CODE_CONTACTDESIGNATION;
                    CODE_CONTACTEMAILPREF = "N" + CODE_CONTACTEMAILPREF;
                    CODE_CONTACTSALUTATION = "N" + CODE_CONTACTSALUTATION;
                }
                contactConfig = configureBOS;
                populateData();
                if (contactConfig.isEmpty())
                    showMessage(getString(R.string.retailer_contact_menu));
            }

            @Override
            public void onError(Throwable e) {
                Commons.print(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick({R.id.addbutton, R.id.clear_button})
    public void buttonOnclick(Button button) {

        switch (button.getId()) {
            case R.id.addbutton:
                addContactInList();
                break;
            case R.id.clear_button:
                clearViews();
                break;
        }
    }

    private void addContactInList() {
        if (hasdata()) {
            if (validateData()) {
                if (isProfileEdit) {
                    if (isEdit) {
                        if (!"I".equalsIgnoreCase(retailerContactBo.getStatus())) {
                            retailerContactBo.setStatus("U");
                        }
                        for (int i = 0; i < contactList.size(); i++) {
                            if (contactList.get(i).getCpId().equalsIgnoreCase(retailerContactBo.getCpId())) {
                                contactList.set(i, retailerContactBo);
                                break;
                            }
                        }
                        for (RetailerContactAvailBo availBo : retailerContactBo.getContactAvailList()){
                            if (availBo.getStatus().isEmpty())
                                availBo.setStatus("U");
                        }
                    } else {
                        int count = 0;
                        for (int i = 0; i < contactList.size(); i++) {
                            if (!contactList.get(i).getStatus().equalsIgnoreCase("D")) {
                                count += 1;
                            }
                        }
                        if (count < bmodel.configurationMasterHelper.RETAILER_CONTACT_COUNT) {
                            retailerContactBo.setStatus("I");
                            retailerContactBo.setCpId("" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                            contactList.add(retailerContactBo);
                        } else
                            showMessage(context.getString(R.string.max_contacts_added));
                    }
                } else {
                    if (isEdit) {
                        for (int i = 0; i < contactList.size(); i++) {
                            if (contactList.get(i).getCpId().equalsIgnoreCase(retailerContactBo.getCpId())) {
                                contactList.set(i, retailerContactBo);
                                break;
                            }
                        }
                    } else {
                        if (contactList.size() < bmodel.configurationMasterHelper.RETAILER_CONTACT_COUNT) {
                            retailerContactBo.setCpId("" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                            contactList.add(retailerContactBo);
                        } else
                            showMessage(context.getString(R.string.max_contacts_added));

                    }
                }
                bmodel.newOutletHelper.setRetailerContactList(contactList);
                loadRecyclerView();
                clearViews();
                isEdit = false;
            }

        } else {
            showMessage(context.getString(R.string.no_data_tosave));
        }
    }

    private void addEditTextChangedListener() {

        etFirstName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (s.toString().trim().equals(""))
                    retailerContactBo.setFistname("");
                else {
                    if (menuMap.get(CODE_CONTACTNAME) != null ) {
                        if (StringUtils.validRegex(menuMap.get(CODE_CONTACTNAME).getRegex(), s.toString().trim())) {
                            etFirstName.setSelection(s.toString().length());
                            retailerContactBo.setFistname(s.toString().trim());
                        } else {
                            s.delete(length - 1, length);
                            showMessage(getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName());
                        }
                    }
                }


            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        etLastName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (s.toString().trim().equals(""))
                    retailerContactBo.setLastname("");
                else {
                    if (menuMap.get(CODE_CONTACTNAME) != null ) {
                        if (StringUtils.validRegex(menuMap.get(CODE_CONTACTNAME).getRegex(), s.toString().trim())) {
                            etLastName.setSelection(s.toString().length());
                            retailerContactBo.setLastname(s.toString().trim());
                        } else {
                            s.delete(length - 1, length);
                            showMessage(getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName());
                        }
                    }
                }

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        etPhno.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (s.toString().trim().equals(""))
                    retailerContactBo.setContactNumber("");
                else {
                    if (menuMap.get(CODE_CONTACTNUMBER) != null) {
                        if (StringUtils.validRegex(menuMap.get(CODE_CONTACTNUMBER).getRegex(), s.toString().trim())) {
                            etPhno.setSelection(s.toString().length());
                            retailerContactBo.setContactNumber(s.toString().trim());
                        } else {
                            s.delete(length - 1, length);
                            showMessage(getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName());
                        }
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (s.toString().trim().equals(""))
                    retailerContactBo.setContactMail("");
                else {
                    if (menuMap.get(CODE_CONTACTMAIL) != null)
                    if (StringUtils.validRegex(menuMap.get(CODE_CONTACTMAIL).getRegex(), s.toString().trim())) {
                        etEmail.setSelection(s.toString().length());
                        retailerContactBo.setContactMail(s.toString().trim());
                    } else {
                        s.delete(length - 1, length);
                        showMessage(getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName());
                    }
                }


            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        etOthers.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0)

                    if (s.toString().trim().equals(""))
                        retailerContactBo.setTitle("");
                    else {
                        etOthers.setSelection(s.toString().length());
                        retailerContactBo.setTitle(s.toString().trim());
                    }

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
    }

    private void addPrimaryCheckBoxChangedListener() {
        cbIsPrimary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isPrimaryAvaiable(true)) {
                        if (!isEdit) {
                            cbIsPrimary.setChecked(false);
                            showMessage(context.getString(R.string.primary_contact_available));
                        } else {
                            if (retailerContactBo.getIsPrimary() == 0) {
                                cbIsPrimary.setChecked(false);
                            }
                        }
                    } else {
                        retailerContactBo.setIsPrimary(1);
                    }
                } else
                    retailerContactBo.setIsPrimary(0);
            }
        });

        cbIsPrimaryEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isPrimaryAvaiable(false)) {
                        if (!isEdit) {
                            cbIsPrimaryEmail.setChecked(false);
                            showMessage(context.getString(R.string.primary_email_pref_already_given
                            ));
                        } else {
                            if (retailerContactBo.getIsEmailPrimary() == 0) {
                                cbIsPrimaryEmail.setChecked(false);
                            }
                        }
                    } else {
                        retailerContactBo.setIsEmailPrimary(1);
                    }
                } else
                    retailerContactBo.setIsEmailPrimary(0);
            }
        });

    }

    private void addTitleSpinnertemSelectedListener() {

        sp_reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StandardListBO standardListBO = (StandardListBO) parent.getSelectedItem();
                if (standardListBO.getListID().equalsIgnoreCase("0"))
                    etOthers.setVisibility(View.VISIBLE);

                else
                    etOthers.setVisibility(View.GONE);

                if (SDUtil.convertToInt(standardListBO.getListID()) > 0)
                    retailerContactBo.setTitle(standardListBO.getListName());


                retailerContactBo.setContactTitleLovId(standardListBO.getListID());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        salutationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StandardListBO standardListBO = (StandardListBO) parent.getSelectedItem();

                if (SDUtil.convertToInt(standardListBO.getListID()) > 0)
                    retailerContactBo.setSalutationTitle(standardListBO.getListName());


                retailerContactBo.setContactSalutationId(standardListBO.getListID());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void populateData() {
        rvContacts.setHasFixedSize(false);
        rvContacts.setNestedScrollingEnabled(false);
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContacts.addItemDecoration(new DividerItemDecoration(rvContacts.getContext(), DividerItemDecoration.VERTICAL));

        for (ConfigureBO configureBO : contactConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTPRIMARY)) {
                ISCONTACTPRIMARY = true;
                tvTitlePrimary.setText(configureBO.getMenuName());
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNAME)) {
                ISCONTACTNAME = true;
                menuMap.put(CODE_CONTACTNAME, configureBO);
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNUMBER)) {
                ISCONTACTNO = true;
                tvTitlePhno.setText(configureBO.getMenuName());
                menuMap.put(CODE_CONTACTNUMBER, configureBO);
                addLengthFilter(configureBO.getRegex());
                checkRegex(configureBO.getRegex());
                if (inputFilters != null && !inputFilters.isEmpty() && etPhno != null) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    etPhno.setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        etPhno.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTMAIL)) {
                ISCONTACTEMAIL = true;
                tvTitleEmail.setText(configureBO.getMenuName());
                menuMap.put(CODE_CONTACTMAIL, configureBO);
                addLengthFilter(configureBO.getRegex());
                checkRegex(configureBO.getRegex());
                if (inputFilters != null && !inputFilters.isEmpty() && etEmail != null) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    etEmail.setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        etEmail.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTAVAILABILITY)) {
                IS_CONTACTAVAILABILITY = true;
                tvTimslotText.setText(configureBO.getMenuName());
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTEMAILPREF)) {
                IS_CONTACTEMAILPREF = true;
                tvIsPrimaryEmail.setText(configureBO.getMenuName());
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTDESIGNATION)) {
                IS_CONTACTDESIGNATION = true;
                tvTitle.setText(configureBO.getMenuName());
            } else if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTSALUTATION)) {
                IS_CONTACTSALUTATION = true;
                tvSalutationTitle.setText(configureBO.getMenuName());
            }

        }
        if (!ISCONTACTPRIMARY) {
            tvTitlePrimary.setVisibility(View.GONE);
            cbIsPrimary.setVisibility(View.GONE);
        }


        if (!IS_CONTACTSALUTATION) {
            salutationSpinner.setVisibility(View.GONE);
            tvSalutationTitle.setVisibility(View.GONE);
        } else {
            ArrayList<StandardListBO> list = bmodel.newOutletHelper.downlaodContactSalutation();
            ArrayList<StandardListBO> mcontactSalutationList = new ArrayList<>();
            mcontactSalutationList.add(0, new StandardListBO("-1", getResources().getString(R.string.select_str) + " " + "Title"));
            mcontactSalutationList.addAll(list);

            contactSalutationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, mcontactSalutationList);
            contactSalutationAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            salutationSpinner.setAdapter(contactSalutationAdapter);
        }

        if (!ISCONTACTNAME) {
            tvTitleFirstName.setVisibility(View.GONE);
            etFirstName.setVisibility(View.GONE);
            tvTitleLastName.setVisibility(View.GONE);
            etLastName.setVisibility(View.GONE);
        }

        if (!ISCONTACTNO) {
            tvTitlePhno.setVisibility(View.GONE);
            etPhno.setVisibility(View.GONE);
        }

        if (!ISCONTACTEMAIL) {
            tvTitleEmail.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
        }

        if (!IS_CONTACTAVAILABILITY) {
            timeSlotLayout.setVisibility(View.GONE);
        } else {
            timeSlotAdapter = new ContactsTimeSlotAdapterEdit(context,this);
            rvTimeslot.setLayoutManager(new GridLayoutManager(context,2));
            rvTimeslot.setAdapter(timeSlotAdapter);
            timeSlotAdapter.listValues(retailerContactBo.getContactAvailList());
            timeSlotAdapter.notifyDataSetChanged();
        }

        if (!IS_CONTACTEMAILPREF) {
            cbIsPrimaryEmail.setVisibility(View.GONE);
            tvIsPrimaryEmail.setVisibility(View.GONE);
        }

        if (!IS_CONTACTDESIGNATION) {
            tvTitle.setVisibility(View.GONE);
            sp_reason.setVisibility(View.GONE);
        } else {
            ArrayList<StandardListBO> list = bmodel.newOutletHelper.downlaodContactTitle();
            ArrayList<StandardListBO> mcontactTitleList = new ArrayList<>();
            mcontactTitleList.add(0, new StandardListBO("-1", getResources().getString(R.string.select_str) + " " + "Title"));
            mcontactTitleList.addAll(list);

            mcontactTitleList.add(list.size() + 1, new StandardListBO("0", "Others"));
            Commons.print("Size Contact List title : " + bmodel.newOutletHelper.getContactTitleList().size());
            contactTitleAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, mcontactTitleList);
            contactTitleAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            sp_reason.setAdapter(contactTitleAdapter);
        }

        loadRecyclerView();

    }

    private void loadRecyclerView() {
        contactList = bmodel.newOutletHelper.getRetailerContactList();
        ArrayList<RetailerContactBo> contactListTemp = new ArrayList<>();
        for (RetailerContactBo retailerContactBo : contactList) {
            if (!retailerContactBo.getStatus().equalsIgnoreCase("D")) {
                contactListTemp.add(retailerContactBo);
            }
        }
        ContactsAdapter contactsAdapter = new ContactsAdapter(contactListTemp);
        rvContacts.setAdapter(contactsAdapter);
    }

    private void clearViews() {
        retailerContactBo = new RetailerContactBo();
        sp_reason.setSelection(0);
        salutationSpinner.setSelection(0);
        etEmail.setText("");
        etPhno.setText("");
        etOthers.setText("");
        etFirstName.setText("");
        etLastName.setText("");
        cbIsPrimary.setChecked(false);
        cbIsPrimaryEmail.setChecked(false);
        isEdit = false;

        if (timeSlotAdapter != null) {
            timeSlotAdapter.listValues(retailerContactBo.getContactAvailList());
            timeSlotAdapter.notifyDataSetChanged();
        }
    }

    private boolean hasdata() {
        boolean isData = false;
        if (retailerContactBo.getFistname().length() > 0 || retailerContactBo.getLastname().length() > 0 ||
                (!retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1") && retailerContactBo.getContactTitleLovId().length() > 0) ||
                retailerContactBo.getTitle().length() > 0 || retailerContactBo.getContactMail().length() > 0
                || retailerContactBo.getContactNumber().length() > 0
                || !retailerContactBo.getContactAvailList().isEmpty()) {
            isData = true;

        }

        return isData;
    }

    private boolean validateData() {
        for (ConfigureBO configureBO : contactConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNAME)) {
                //first Name
                if (retailerContactBo.getFistname().length() == 0 && configureBO.getMandatory() == 1) {
                    showMessage(context.getString(R.string.enter_first_name));
                    etFirstName.requestFocus();
                    return false;
                } else if (retailerContactBo.getFistname().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getFistname().length() > configureBO.getMaxLengthNo()) {
                        showMessage(configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo());
                        etFirstName.requestFocus();
                        return false;
                    }
                }
                //last name
                if (retailerContactBo.getLastname().length() == 0 && configureBO.getMandatory() == 1) {
                    showMessage(context.getString(R.string.enter_last_name));
                    etLastName.requestFocus();
                    return false;
                } else if (retailerContactBo.getLastname().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getLastname().length() > configureBO.getMaxLengthNo()) {
                        showMessage(configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo());
                        etLastName.requestFocus();
                        return false;
                    }
                }

            }
            //phone number
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNUMBER)) {
                if (retailerContactBo.getContactNumber().length() == 0 && configureBO.getMandatory() == 1) {
                    showMessage(context.getString(R.string.enter) + " " + configureBO.getMenuName());
                    etPhno.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactNumber().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0 && retailerContactBo.getContactNumber().length() > configureBO.getMaxLengthNo()) {
                        showMessage(configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo());
                        etPhno.requestFocus();
                        return false;
                    }
                }
            }
            //email Id
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTMAIL)) {
                if (retailerContactBo.getContactMail().length() == 0 && configureBO.getMandatory() == 1) {
                    showMessage(context.getString(R.string.enter) + " " + configureBO.getMenuName());
                    etEmail.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactMail().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0 && retailerContactBo.getContactMail().length() > configureBO.getMaxLengthNo()) {
                        showMessage(configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo());
                        etEmail.requestFocus();
                        return false;
                    }
                    if (!StringUtils.isValidEmail(retailerContactBo.getContactMail())) {
                        showMessage(context.getString(R.string.invalid_email_address));
                        etEmail.requestFocus();
                        return false;
                    }
                }
            }

            //title spinner
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTDESIGNATION)) {
                //title spinner
                if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1")) {
                    showMessage(context.getString(R.string.select_contact_title));
                    return false;
                }
                //others edit text
                if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") &&
                        retailerContactBo.getTitle().length() == 0 && configureBO.getMandatory() == 1) {
                    showMessage(context.getString(R.string.enter_other_name));
                    etOthers.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") &&
                        retailerContactBo.getTitle().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getTitle().length() > configureBO.getMaxLengthNo()) {
                        showMessage(configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo());
                        etOthers.requestFocus();
                        return false;
                    }
                }
            }

            //Salutation
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTSALUTATION)) {
                if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1")) {
                    showMessage(context.getString(R.string.select_salutation));
                    return false;
                }
            }

            //Contact Availability Time Slots
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTAVAILABILITY)){
                if (configureBO.getMandatory() == 1 && retailerContactBo.getContactAvailList().isEmpty()){
                    showMessage(context.getString(R.string.enter_time_slot));
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isPrimaryAvaiable(boolean isContactPrimaryChk) {
        for (RetailerContactBo retailerContactBo : contactList) {
            if (isContactPrimaryChk && retailerContactBo.getIsPrimary() == 1
                    && !retailerContactBo.getStatus().equalsIgnoreCase("D"))
                return true;
            else if (!isContactPrimaryChk && retailerContactBo.getIsEmailPrimary() == 1
                    && !retailerContactBo.getStatus().equalsIgnoreCase("D"))
                return true;
        }

        return false;
    }

    @Override
    public void deleteSlot(RetailerContactAvailBo contactAvailBo) {

        if (isProfileEdit)
            if ("I".equalsIgnoreCase(contactAvailBo.getStatus()))
                retailerContactBo.getContactAvailList().remove(contactAvailBo);
            else
                for (RetailerContactAvailBo availBo :retailerContactBo.getContactAvailList()) {
                    if (contactAvailBo.equals(availBo))
                        availBo.setStatus("D");
                }
        else
            retailerContactBo.getContactAvailList().remove(contactAvailBo);


        updateTimeSlotList();
    }

    private void updateTimeSlotList() {
        ArrayList<RetailerContactAvailBo> contactAvailBos = new ArrayList<>();
        for (RetailerContactAvailBo retailerContactAvailBo: retailerContactBo.getContactAvailList()){
            if (!"D".equalsIgnoreCase(retailerContactAvailBo.getStatus()))
                contactAvailBos.add(retailerContactAvailBo);

        }

        timeSlotAdapter.listValues(contactAvailBos);
        timeSlotAdapter.notifyDataSetChanged();
    }

    public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        private ArrayList<RetailerContactBo> items;

        ContactsAdapter(ArrayList<RetailerContactBo> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_contact_listitem, parent, false);

            return new ViewHolder(view);
        }

        int selectedPosition = -1;

        @Override
        public void onBindViewHolder(ContactsAdapter.ViewHolder holder, final int position) {
            if (selectedPosition == position)
                holder.itemView.setBackgroundColor(Color.parseColor("#0091D8"));
            else
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));

            RetailerContactBo retailerContactBo = items.get(position);

            String salutation = "";

            if (IS_CONTACTSALUTATION) {
                if (StringUtils.isEmptyString(retailerContactBo.getSalutationTitle()))
                    salutation = retailerContactBo.getSalutationTitle();
            }

            if (ISCONTACTNAME) {
                holder.textName.setText(salutation + " " + retailerContactBo.getFistname() + " " + retailerContactBo.getLastname());
            }

            if (IS_CONTACTDESIGNATION) {
                if (StringUtils.isEmptyString(retailerContactBo.getTitle()))
                    holder.title.setText(retailerContactBo.getTitle());
                else
                    holder.title.setVisibility(View.GONE);
            }

            if (ISCONTACTPRIMARY) {
                if (retailerContactBo.getIsPrimary() == 1)
                    holder.ivIsPrimary.setVisibility(View.VISIBLE);
                else
                    holder.ivIsPrimary.setVisibility(View.INVISIBLE);

            }
            if (ISCONTACTNO && retailerContactBo.getContactNumber().length() > 0)
                holder.textCno.setText(retailerContactBo.getContactNumber());
            else
                holder.textCno.setVisibility(View.GONE);


            if (ISCONTACTEMAIL && retailerContactBo.getContactMail().length() > 0)
                holder.textCEmail.setText(retailerContactBo.getContactMail());
            else
                holder.textCEmail.setVisibility(View.GONE);

            holder.ivIsdelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteContact(items.get(position));
                }
            });

            holder.llItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = position;
                    //notifyDataSetChanged();
                    editContact(items.get(position));
                }
            });

            if (IS_CONTACTEMAILPREF && retailerContactBo.getIsEmailPrimary() == 1)
                holder.imgViewEmail.setVisibility(View.VISIBLE);
            else
                holder.imgViewEmail.setVisibility(View.GONE);

            if (IS_CONTACTAVAILABILITY && !retailerContactBo.getContactAvailList().isEmpty()) {
                holder.timeSlotLayout.setVisibility(View.VISIBLE);
                holder.rvTimeslot.setLayoutManager(new GridLayoutManager(context, 2));

                ArrayList<RetailerContactAvailBo> availBos = new ArrayList<>();
                for (RetailerContactAvailBo availBo : retailerContactBo.getContactAvailList())
                    if (!"D".equalsIgnoreCase(availBo.getStatus()))
                        availBos.add(availBo);

                ContactsTimeSlotAdapter timeSlotAdapter = new ContactsTimeSlotAdapter(context,availBos);
                holder.rvTimeslot.setAdapter(timeSlotAdapter);
            }else
                holder.timeSlotLayout.setVisibility(View.GONE);

            holder.imgShrinkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!retailerContactBo.getContactAvailList().isEmpty() && holder.rvTimeslot.getVisibility() == View.VISIBLE) {
                        holder.rvTimeslot.setVisibility(View.GONE);
                        holder.imgShrinkView.setImageResource(R.drawable.ic_action_up);
                    }else if (!retailerContactBo.getContactAvailList().isEmpty()) {
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

            private TextView title, textName;
            private TextView textCno, textCEmail;
            private ImageView ivIsPrimary, ivIsdelete, imgViewEmail;
            private LinearLayout llItem;
            private RelativeLayout timeSlotLayout;
            private RecyclerView rvTimeslot;
            private ImageView imgShrinkView;

            public ViewHolder(View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.tvTitle);
                textName = itemView.findViewById(R.id.tvFirstName);
                ivIsPrimary = itemView.findViewById(R.id.ivIsPrimary);
                ivIsdelete = itemView.findViewById(R.id.ivIsdelete);
                textCno = itemView.findViewById(R.id.tvContactNo);
                textCEmail = itemView.findViewById(R.id.tvEmail);
                imgViewEmail = itemView.findViewById(R.id.img_email_pref);

                timeSlotLayout = itemView.findViewById(R.id.time_slot_layout);
                imgShrinkView = itemView.findViewById(R.id.img_shrink_view);
                rvTimeslot = itemView.findViewById(R.id.rv_timeslot);

                llItem = itemView.findViewById(R.id.llItem);

                if (!ISCONTACTNAME) {
                    title.setVisibility(View.GONE);
                    textName.setVisibility(View.GONE);
                }

                if (!ISCONTACTNO)
                    textCno.setVisibility(View.GONE);

                if (!ISCONTACTPRIMARY)
                    ivIsPrimary.setVisibility(View.GONE);

                if (!ISCONTACTEMAIL)
                    textCEmail.setVisibility(View.GONE);

                if (!IS_CONTACTAVAILABILITY)
                    timeSlotLayout.setVisibility(View.GONE);

                ivIsdelete.setVisibility(View.VISIBLE);

            }
        }
    }

    private void deleteContact(RetailerContactBo retailerContact) {

        if (isProfileEdit) {
            retailerContact.setStatus("D");
            for (int i = 0; i < contactList.size(); i++) {
                if (contactList.get(i).getCpId().equalsIgnoreCase(retailerContact.getCpId())) {
                    if (contactList.get(i).getStatus().equalsIgnoreCase("I")) {
                        contactList.remove(i);
                    } else {
                        for (RetailerContactAvailBo availBo : contactList.get(i).getContactAvailList()){
                            availBo.setStatus("D");
                        }
                        contactList.set(i, retailerContact);
                    }
                    break;
                }
            }

        } else {
            for (int i = 0; i < contactList.size(); i++) {
                if (contactList.get(i).getCpId().equalsIgnoreCase(retailerContact.getCpId())) {
                    contactList.remove(i);
                    break;
                }
            }
        }
        bmodel.newOutletHelper.setRetailerContactList(contactList);
        loadRecyclerView();
    }

    private void editContact(RetailerContactBo retailerContactBo) {
        clearViews();
        isEdit = true;
        this.retailerContactBo.setTitle(retailerContactBo.getTitle());
        this.retailerContactBo.setFistname(retailerContactBo.getFistname());
        this.retailerContactBo.setLastname(retailerContactBo.getLastname());
        this.retailerContactBo.setContactNumber(retailerContactBo.getContactNumber());
        this.retailerContactBo.setContactMail(retailerContactBo.getContactMail());
        this.retailerContactBo.setIsPrimary(retailerContactBo.getIsPrimary());
        this.retailerContactBo.setContactTitleLovId(retailerContactBo.getContactTitleLovId());
        this.retailerContactBo.setCpId(retailerContactBo.getCpId());
        this.retailerContactBo.setStatus(retailerContactBo.getStatus());

        this.retailerContactBo.setContactSalutationId(retailerContactBo.getContactSalutationId());
        this.retailerContactBo.setIsEmailPrimary(retailerContactBo.getIsEmailPrimary());

        this.retailerContactBo.getContactAvailList().addAll(retailerContactBo.getContactAvailList());

        if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") && retailerContactBo.getTitle().length() == 0)
            sp_reason.setSelection(0);
        else
            sp_reason.setSelection(getSpinnerPostion(retailerContactBo.getContactTitleLovId()));

        if (retailerContactBo.getContactSalutationId().equalsIgnoreCase("0") && retailerContactBo.getSalutationTitle().length() == 0)
            salutationSpinner.setSelection(0);
        else
            salutationSpinner.setSelection(getSalutationPostion(retailerContactBo.getContactSalutationId()));

        if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") && retailerContactBo.getTitle().length() > 0)
            etOthers.setText(retailerContactBo.getTitle());
        etEmail.setText(retailerContactBo.getContactMail());
        etPhno.setText(retailerContactBo.getContactNumber());
        etFirstName.setText(retailerContactBo.getFistname());
        etLastName.setText(retailerContactBo.getLastname());
        if (retailerContactBo.getIsPrimary() == 1)
            cbIsPrimary.setChecked(true);

        if (retailerContactBo.getIsEmailPrimary() == 1)
            cbIsPrimaryEmail.setChecked(true);

        if (timeSlotAdapter != null) {
            timeSlotAdapter.listValues(retailerContactBo.getContactAvailList());
            timeSlotAdapter.notifyDataSetChanged();
        }



    }

    private int getSpinnerPostion(String listId) {
        int default_value = 0;
        if (contactTitleAdapter != null)
            for (int i = 0; i < contactTitleAdapter.getCount(); i++) {
                StandardListBO tempBo = contactTitleAdapter.getItem(i);
                assert tempBo != null;
                if (tempBo.getListID().equalsIgnoreCase(listId)) {
                    return i;
                }
            }
        return default_value;
    }

    private int getSalutationPostion(String listId) {
        int default_value = 0;
        if (contactSalutationAdapter != null)
            for (int i = 0; i < contactSalutationAdapter.getCount(); i++) {
                StandardListBO tempBo = contactSalutationAdapter.getItem(i);
                assert tempBo != null;
                if (tempBo.getListID().equalsIgnoreCase(listId)) {
                    return i;
                }
            }
        return default_value;
    }

    private void addLengthFilter(String regex) {
        inputFilters = new ArrayList<>();
        InputFilter fil = new InputFilter.LengthFilter(25);
        String str = regex;
        if (str != null && !str.isEmpty()) {
            if (str.contains("<") && str.contains(">")) {

                String len = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                if (len != null && !len.isEmpty()) {
                    if (len.contains(",")) {
                        try {
                            fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len.split(",")[1]));
                        } catch (Exception ex) {
                            Commons.printException("regex length split", ex);
                        }
                    } else {
                        fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len));
                    }
                }
            }
        }

        inputFilters.add(fil);
    }

    private void checkRegex(String regex) {
        final String reg;

        try {
            if (regex != null && !regex.isEmpty()) {
                if (regex.contains("<") && regex.contains(">")) {
                    reg = regex.replaceAll("\\<.*?\\>", "");
                } else {
                    reg = regex;
                }

                InputFilter filter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            String checkMe = String.valueOf(source.charAt(i));

                            if (!Pattern.compile(reg).matcher(checkMe).matches()) {
                                Log.d("", "invalid");
                                return "";
                            }

                        }
                        return null;
                    }
                };
                inputFilters.add(filter);

            }
        } catch (Exception ex) {
            Commons.printException("regex check", ex);
        }
    }

    @Subscribe
    public void onMessageEvent(RetailerContactAvailBo contactAvailBo) {

        for (RetailerContactAvailBo timeSlot : retailerContactBo.getContactAvailList()){
            if (timeSlot.getDay().contains(contactAvailBo.getDay())){
                if (DateTimeUtils.isBetweenTime(timeSlot.getFrom(),timeSlot.getTo(),contactAvailBo.getFrom(),true)){
                    Toast.makeText(context, context.getString(R.string.time_slot_already_selected), Toast.LENGTH_SHORT).show();
                    return;
                }else if (DateTimeUtils.isBetweenTime(timeSlot.getFrom(),timeSlot.getTo(),contactAvailBo.getTo(),false)){
                    Toast.makeText(context, context.getString(R.string.time_slot_already_selected), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        contactAvailBo.setStatus("I");

        retailerContactBo.getContactAvailList().add(contactAvailBo);

        if (timeSlotPickFragment != null)
            timeSlotPickFragment.dismiss();

        updateTimeSlotList();
    }

}



