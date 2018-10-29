package com.ivy.sd.png.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.profile.RetailerContactBo;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


public class ContactCreationFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private ArrayList<ConfigureBO> contactConfig;

    private static String CODE_CONTACTNAME = "COlNTACTNAME";
    private static String CODE_CONTACTNUMBER = "CONTACTNUMBER";
    private static String CODE_CONTACTPRIMARY = "CONTACTPRIMARY";
    private static String CODE_CONTACTMAIL = "CONTACTMAIL";
    private boolean ISCONTACTNAME, ISCONTACTNO, ISCONTACTPRIMARY, ISCONTACTEMAIL;
    private Unbinder unbinder;

    //for editing new contact . created through newoutlet
    private boolean isEdit = false;
    private boolean isProfileEdit = false;
    private RetailerContactBo retailerContactBo = new RetailerContactBo();
    private ArrayList<StandardListBO> mcontactTitleList;
    private ArrayAdapter<StandardListBO> contactTitleAdapter;
    private ArrayList<RetailerContactBo> contactList;
    private HashMap<String, ConfigureBO> menuMap = new HashMap<>();

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


    @BindView(R.id.rv_contacts)
    RecyclerView rvContacts;
    private AppSchedulerProvider appSchedulerProvider;

    public static ContactCreationFragment getInstance(boolean isFromEditProfileView) {
        ContactCreationFragment creationFragment = new ContactCreationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit", isFromEditProfileView);
        creationFragment.setArguments(bundle);
        return creationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_contact_creation, container, false);
        unbinder = ButterKnife.bind(this, view);

        initializeViews();
        addEditTextChangedListener();
        addPrimaryCheckBoxChangedListener();
        addTitleSpinnertemSelectedListener();

        return view;
    }

    private void initializeViews() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            isProfileEdit = bundle.getBoolean("isEdit", false);
        }

        appSchedulerProvider = new AppSchedulerProvider();
        new CompositeDisposable().add((Disposable) bmodel.profilehelper.downloadContactModuleConfig(isProfileEdit)
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribeWith(getContactConfig()));
    }

    private Observer<ArrayList<ConfigureBO>> getContactConfig() {
        return new DisposableObserver<ArrayList<ConfigureBO>>() {
            @Override
            public void onNext(ArrayList<ConfigureBO> configureBOS) {
                contactConfig = configureBOS;
                populateData();
                if (contactConfig.size() == 0)
                    Toast.makeText(getActivity(), getString(R.string.retailer_contact_menu), Toast.LENGTH_SHORT).show();
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
                                    + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));
                            contactList.add(retailerContactBo);
                        } else
                            Toast.makeText(getActivity(), getActivity().getString(R.string.max_contacts_added), Toast.LENGTH_SHORT).show();
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
                                    + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));
                            contactList.add(retailerContactBo);
                        } else
                            Toast.makeText(getActivity(), getActivity().getString(R.string.max_contacts_added), Toast.LENGTH_SHORT).show();

                    }
                }
                bmodel.newOutletHelper.setRetailerContactList(contactList);
                loadRecyclerView();
                clearViews();
                isEdit = false;
            }

        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.no_data_tosave), Toast.LENGTH_SHORT).show();
        }
    }

    private void addEditTextChangedListener() {

        etFirstName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (s.toString().trim().equals(""))
                    retailerContactBo.setFistname("");
                else {
                    if (validRegex(menuMap.get(CODE_CONTACTNAME).getRegex(), s.toString().trim())) {
                        etFirstName.setSelection(s.toString().length());
                        retailerContactBo.setFistname(s.toString().trim());
                    } else {
                        s.delete(length - 1, length);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
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
                    if (validRegex(menuMap.get(CODE_CONTACTNAME).getRegex(), s.toString().trim())) {
                        etLastName.setSelection(s.toString().length());
                        retailerContactBo.setLastname(s.toString().trim());
                    } else {
                        s.delete(length - 1, length);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
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
                    if (validRegex(menuMap.get(CODE_CONTACTNUMBER).getRegex(), s.toString().trim())) {
                        etPhno.setSelection(s.toString().length());
                        retailerContactBo.setContactNumber(s.toString().trim());
                    } else {
                        s.delete(length - 1, length);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
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
                    if (validRegex(menuMap.get(CODE_CONTACTMAIL).getRegex(), s.toString().trim())) {
                        etEmail.setSelection(s.toString().length());
                        retailerContactBo.setContactMail(s.toString().trim());
                    } else {
                        s.delete(length - 1, length);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.enter_valid) + " " + menuMap.get(CODE_CONTACTNAME).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
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
                    if (isPrimaryAvaiable()) {
                        if (!isEdit) {
                            cbIsPrimary.setChecked(false);
                            Toast.makeText(getActivity(), getActivity().getString(R.string.primary_contact_available), Toast.LENGTH_SHORT).show();
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

    }

    private void populateData() {
        rvContacts.setHasFixedSize(false);
        rvContacts.setNestedScrollingEnabled(false);
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContacts.addItemDecoration(new DividerItemDecoration(rvContacts.getContext(), DividerItemDecoration.HORIZONTAL));
        addbutton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        for (ConfigureBO configureBO : contactConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTPRIMARY)) {
                ISCONTACTPRIMARY = true;
                tvTitlePrimary.setText(configureBO.getMenuName());
            }
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNAME)) {
                ISCONTACTNAME = true;
                menuMap.put(CODE_CONTACTNAME, configureBO);
            }
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNUMBER)) {
                ISCONTACTNO = true;
                tvTitlePhno.setText(configureBO.getMenuName());
                menuMap.put(CODE_CONTACTNUMBER, configureBO);
            }
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTMAIL)) {
                ISCONTACTEMAIL = true;
                tvTitleEmail.setText(configureBO.getMenuName());
                menuMap.put(CODE_CONTACTMAIL, configureBO);
            }
        }
        if (!ISCONTACTPRIMARY) {
            tvTitlePrimary.setVisibility(View.GONE);
            cbIsPrimary.setVisibility(View.GONE);
        } else
            tvTitlePrimary.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        if (!ISCONTACTNAME) {
            tvTitle.setVisibility(View.GONE);
            sp_reason.setVisibility(View.GONE);
            tvTitleFirstName.setVisibility(View.GONE);
            etFirstName.setVisibility(View.GONE);
            tvTitleLastName.setVisibility(View.GONE);
            etLastName.setVisibility(View.GONE);
        } else {
            tvTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvTitleFirstName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvTitleLastName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ArrayList<StandardListBO> list = bmodel.newOutletHelper.downlaodContactTitle();
            mcontactTitleList = new ArrayList<>();
            mcontactTitleList.add(0, new StandardListBO("-1", getResources().getString(R.string.select_str) + " " + "Title"));
            mcontactTitleList.addAll(list);

            mcontactTitleList.add(list.size() + 1, new StandardListBO("0", "Others"));
            Commons.print("Size Contact List title : " + bmodel.newOutletHelper.getContactTitleList().size());
            contactTitleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontactTitleList);
            contactTitleAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            sp_reason.setAdapter(contactTitleAdapter);
        }

        if (!ISCONTACTNO) {
            tvTitlePhno.setVisibility(View.GONE);
            etPhno.setVisibility(View.GONE);
        } else
            tvTitlePhno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        if (!ISCONTACTEMAIL) {
            tvTitleEmail.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
        } else
            tvTitleEmail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        loadRecyclerView();


    }

    private void loadRecyclerView() {
        contactList = bmodel.newOutletHelper.getRetailerContactList();
        ArrayList<RetailerContactBo> contactListTemp = new ArrayList<>();
        for (int i = 0; i < contactList.size(); i++) {
            if (!contactList.get(i).getStatus().equalsIgnoreCase("D")) {
                contactListTemp.add(contactList.get(i));
            }
        }
        ContactsAdapter contactsAdapter = new ContactsAdapter(contactListTemp);
        rvContacts.setAdapter(contactsAdapter);
    }

    private void clearViews() {
        retailerContactBo = new RetailerContactBo();
        sp_reason.setSelection(0);
        etEmail.setText("");
        etPhno.setText("");
        etOthers.setText("");
        etFirstName.setText("");
        etLastName.setText("");
        cbIsPrimary.setChecked(false);
        isEdit = false;
    }

    private boolean hasdata() {
        boolean isData = false;
        if (retailerContactBo.getFistname().length() > 0 || retailerContactBo.getLastname().length() > 0 ||
                (!retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1") && retailerContactBo.getContactTitleLovId().length() > 0) ||
                retailerContactBo.getTitle().length() > 0 || retailerContactBo.getContactMail().length() > 0
                || retailerContactBo.getContactNumber().length() > 0) {
            isData = true;

        }

        return isData;
    }

    private boolean validateData() {
        for (ConfigureBO configureBO : contactConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNAME)) {
                //first Name
                if (retailerContactBo.getFistname().length() == 0 && configureBO.getMandatory() == 1) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter_first_name), Toast.LENGTH_SHORT).show();
                    etFirstName.requestFocus();
                    return false;
                } else if (retailerContactBo.getFistname().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getFistname().length() > configureBO.getMaxLengthNo()) {
                        Toast.makeText(getActivity(),
                                configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo(), Toast.LENGTH_SHORT)
                                .show();
                        etFirstName.requestFocus();
                        return false;
                    }
                }
                //last name
                if (retailerContactBo.getLastname().length() == 0 && configureBO.getMandatory() == 1) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter_last_name), Toast.LENGTH_SHORT).show();
                    etLastName.requestFocus();
                    return false;
                } else if (retailerContactBo.getLastname().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getLastname().length() > configureBO.getMaxLengthNo()) {
                        Toast.makeText(getActivity(),
                                configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo(), Toast.LENGTH_SHORT)
                                .show();
                        etLastName.requestFocus();
                        return false;
                    }
                }
                //title spinner
                if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1")) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.select_contact_title), Toast.LENGTH_SHORT).show();
                    return false;
                }
                //others edit text
                if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") &&
                        retailerContactBo.getTitle().length() == 0 && configureBO.getMandatory() == 1) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter_other_name), Toast.LENGTH_SHORT).show();
                    etOthers.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") &&
                        retailerContactBo.getTitle().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0
                            && retailerContactBo.getTitle().length() > configureBO.getMaxLengthNo()) {
                        Toast.makeText(getActivity(),
                                configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo(), Toast.LENGTH_SHORT)
                                .show();
                        etOthers.requestFocus();
                        return false;
                    }
                }
            }
            //phone number
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTNUMBER)) {
                if (retailerContactBo.getContactNumber().length() == 0 && configureBO.getMandatory() == 1) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter) + " " + configureBO.getMenuName(), Toast.LENGTH_SHORT).show();
                    etPhno.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactNumber().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0 && retailerContactBo.getContactNumber().length() > configureBO.getMaxLengthNo()) {
                        Toast.makeText(getActivity(),
                                configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo(), Toast.LENGTH_SHORT)
                                .show();
                        etPhno.requestFocus();
                        return false;
                    }
                }
            }
            //email Id
            if (configureBO.getConfigCode().equalsIgnoreCase(CODE_CONTACTMAIL)) {
                if (retailerContactBo.getContactMail().length() == 0 && configureBO.getMandatory() == 1) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter) + " " + configureBO.getMenuName(), Toast.LENGTH_SHORT).show();
                    etEmail.requestFocus();
                    return false;
                } else if (retailerContactBo.getContactMail().length() > 0) {
                    if (configureBO.getMaxLengthNo() > 0 && retailerContactBo.getContactMail().length() > configureBO.getMaxLengthNo()) {
                        Toast.makeText(getActivity(),
                                configureBO.getMenuName() + " Length Must Be " + configureBO.getMaxLengthNo(), Toast.LENGTH_SHORT)
                                .show();
                        etEmail.requestFocus();
                        return false;
                    }
                    if (!isValidEmail(retailerContactBo.getContactMail())) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                        etEmail.requestFocus();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isPrimaryAvaiable() {
        for (RetailerContactBo retailerContactBo : contactList)
            if (retailerContactBo.getIsPrimary() == 1 && !retailerContactBo.getStatus().equalsIgnoreCase("D"))
                return true;

        return false;
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

            if (ISCONTACTNAME) {
                if (retailerContactBo.getTitle().length() > 0)
                    holder.title.setText(retailerContactBo.getTitle());
                else
                    holder.title.setVisibility(View.GONE);
                holder.textName.setText(retailerContactBo.getFistname() + " " + retailerContactBo.getLastname());
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

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title, textName;
            private TextView textCno, textCEmail;
            private ImageView ivIsPrimary, ivIsdelete;
            private LinearLayout llItem;

            public ViewHolder(View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.tvTitle);
                textName = itemView.findViewById(R.id.tvFirstName);
                ivIsPrimary = itemView.findViewById(R.id.ivIsPrimary);
                ivIsdelete = itemView.findViewById(R.id.ivIsdelete);
                textCno = itemView.findViewById(R.id.tvContactNo);
                textCEmail = itemView.findViewById(R.id.tvEmail);
                llItem = itemView.findViewById(R.id.llItem);

                title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                textName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                textCno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                textCEmail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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
                    } else
                        contactList.set(i, retailerContact);
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

        if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") && retailerContactBo.getTitle().length() == 0)
            sp_reason.setSelection(0);
        else
            sp_reason.setSelection(getSpinnerPostion(retailerContactBo.getContactTitleLovId()));
        if (retailerContactBo.getContactTitleLovId().equalsIgnoreCase("0") && retailerContactBo.getTitle().length() > 0)
            etOthers.setText(retailerContactBo.getTitle());
        etEmail.setText(retailerContactBo.getContactMail());
        etPhno.setText(retailerContactBo.getContactNumber());
        etFirstName.setText(retailerContactBo.getFistname());
        etLastName.setText(retailerContactBo.getLastname());
        if (retailerContactBo.getIsPrimary() == 1)
            cbIsPrimary.setChecked(true);

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

    //Email Id Validation
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean validRegex(String pattern, String str) {

        if (pattern.equals("")) {
            return true;
        }

        Pattern mPattern = Pattern.compile(pattern);
        Matcher matcher = mPattern.matcher(str);

        // Entered text does not match the pattern
        if (!matcher.matches()) {
            if (!matcher.lookingAt())
                return false;
            // It does not match partially too
            if (!matcher.hitEnd())
                return false;
        }
        return true;
    }
}



