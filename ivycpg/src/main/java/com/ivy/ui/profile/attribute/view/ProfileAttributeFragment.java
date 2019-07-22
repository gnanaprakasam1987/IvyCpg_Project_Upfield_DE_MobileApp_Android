package com.ivy.ui.profile.attribute.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.di.DaggerProfileAttributeComponent;
import com.ivy.ui.profile.attribute.di.ProfileAttributeModule;
import com.ivy.ui.profile.attribute.presenter.ProfileAttributePresenterImpl;
import com.ivy.ui.profile.view.ProfileBaseBo;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static com.ivy.ui.profile.view.ProfileBaseFragment.selectedChannelId;

public class ProfileAttributeFragment extends BaseFragment
        implements IProfileAttributeContract.IProfileAttributeView,BlockingStep {

    private LinearLayout dynamicViewLayout;
    private HashMap<String,Spinner> attributeSpinner = new HashMap<>();
    private HashMap<String,AttributeBO> selectedSpinnerIds = new HashMap<>();
    private ArrayList<Integer> channelIds = new ArrayList<>();
    private boolean isProfileEdit = true;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,AttributeBO> filteredAttributeIds = new HashMap<>();

    private int channelId;

    private LinearLayout channelLayout;

    @Inject
    ProfileAttributePresenterImpl<IProfileAttributeContract.IProfileAttributeView> profileAttributePresenter;

    @Override
    public void initializeDi() {
        DaggerProfileAttributeComponent.builder()
                .ivyAppComponent(((BusinessModel) ((Activity)context).getApplication()).getComponent())
                .profileAttributeModule(new ProfileAttributeModule(this))
                .build().inject(this);
        setBasePresenter(profileAttributePresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.profile_attribute_layout;
    }

    @Override
    public void init(View view) {
        dynamicViewLayout = view.findViewById(R.id.dynamic_layout);
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

        profileAttributePresenter.prepareAttributeList(isProfileEdit);
    }

    private void saveAttribute(boolean isSave,StepperLayout.OnNextClickedCallback nextCallback,
                               StepperLayout.OnCompleteClickedCallback completCallback){

        //Master Attribute list
        ArrayList<AttributeBO> childMasterList = new ArrayList<>();

        for (Map.Entry<String, ArrayList<AttributeBO>> childAttribList : profileAttributePresenter.getChildAttribute().entrySet()) {
            for (AttributeBO childBos :childAttribList.getValue()){
                if (childBos.isMasterRecord())
                    childMasterList.add(childBos);
            }
        }

        filteredAttributeIds.clear();
        if (!selectedSpinnerIds.isEmpty()) {
            ArrayList<AttributeBO> ids = new ArrayList<>(selectedSpinnerIds.values());

            for (AttributeBO bo : ids){
                if (bo.isAttributeSelected()) {
                    if (profileAttributePresenter.getAttributeChildLst(bo.getAttributeId()+"").isEmpty()) {
                        if (!bo.isMasterRecord())
                            bo.setStatus("N");
                        else
                            bo.setStatus("M");

                        filteredAttributeIds.put(bo.getAttributeId(), bo);
                    }
                }
            }
        }

        if (profileAttributePresenter.validateAttribute(new ArrayList<>(filteredAttributeIds.values()))){

            for (AttributeBO childMasterBo : childMasterList){
                if (filteredAttributeIds.get(childMasterBo.getAttributeId()) == null){
                    childMasterBo.setStatus("D");
                    filteredAttributeIds.put(childMasterBo.getAttributeId(),childMasterBo);
                }else
                    filteredAttributeIds.remove(childMasterBo.getAttributeId());
            }

            ProfileBaseBo profileBaseBo = new ProfileBaseBo();
            profileBaseBo.setStatus(isSave?"Save":"Update");
            profileBaseBo.setFieldName("Attribute");
            profileBaseBo.setAttributeList(new ArrayList<>(filteredAttributeIds.values()));

            EventBus.getDefault().post(profileBaseBo);

            if (isSave)
                completCallback.complete();
            else
                nextCallback.goToNextStep();

        }
    }

    @Override
    public void showCommonAttributeSpinner(ArrayList<AttributeBO> commonAttributeList) {
        showAttributeSpinner(commonAttributeList);
    }

    @Override
    public void showChannelAttributeSpinner(ArrayList<AttributeBO> channelAttributeList) {

        ArrayList<AttributeBO> channelAttrbList = new ArrayList<>();

        if (!channelIds.isEmpty()) {
            for (AttributeBO channelBo : channelAttributeList) {
                if (channelIds.contains(channelBo.getChannelId()))
                    channelAttrbList.add(channelBo);
            }
        }

        if (channelAttrbList.isEmpty())
            return;

        LinearLayout.LayoutParams layoutParam_parent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        channelLayout = new LinearLayout(context);
        channelLayout.setOrientation(LinearLayout.HORIZONTAL);
        channelLayout.setLayoutParams(layoutParam_parent);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            channelLayout.setBackgroundColor(Color.WHITE);
        } else {
            channelLayout.setBackgroundColor(Color.WHITE);
        }

        dynamicViewLayout.addView(channelLayout);

        View view = getLayoutInflater().inflate(R.layout.dynamic_report_table_row_child, null);

        ((TextView)view.findViewById(R.id.table_row_child)).setText("Channel Attribute");

        channelLayout.addView(view);

        showAttributeSpinner(channelAttrbList);

        View seperatorView = getLayoutInflater().inflate(R.layout.seperator_line_layout, null);

        channelLayout.addView(seperatorView);

    }

    private void showAttributeSpinner(ArrayList<AttributeBO> attributeParentList) {

        for (AttributeBO attributeBO : attributeParentList){

            ArrayList<AttributeBO> attributeChildList = new ArrayList<>(profileAttributePresenter.getAttributeChildLst(attributeBO.getAttributeId()+""));

            View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

            ((TextView)view.findViewById(R.id.spinner_txt)).setText(attributeBO.getAttributeName());

            Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

            attributeSpinner.setTag(attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+1);

            setSpinnerAdapter(attributeSpinner,attributeChildList,false);

            if (!attributeBO.isEditable())
                attributeSpinner.setEnabled(false);

            attributeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0){

                        AttributeBO attributeBO1 = (AttributeBO)parent.getSelectedItem();

                        String[] splitTagName = parent.getTag().toString().split("__");
                        updateAttributeArray(parent.getTag().toString(), attributeBO1);
                        resetParentValues((Spinner) parent);

                        ArrayList<AttributeBO> attributeChildList = profileAttributePresenter.getAttributeChildLst(attributeBO1.getAttributeId()+"");

                        if (attributeChildList == null || attributeChildList.isEmpty()){
                            attributeChildList = new ArrayList<>();
                        }

                        Spinner childSpinner = ProfileAttributeFragment.this.attributeSpinner.get(splitTagName[0]+"__"+2);

                        if (childSpinner == null)
                            return;

                        setSpinnerAdapter(childSpinner,attributeChildList,true);
                    }else {
                        updateAttributeArray(parent.getTag().toString(), null);
                        resetSpinnerAdapter((Spinner) parent);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (attributeBO.getLevelCount() > 2){
                for (int i = 2 ; i < attributeBO.getLevelCount(); i++){
                    createChildSpinner(view,attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+i,attributeBO.isEditable());
                }
            }

            dynamicViewLayout.addView(view);

        }

    }

    private void createChildSpinner(final View baseView, String tagName,boolean isEditable){

        View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

        (view.findViewById(R.id.spinner_txt)).setVisibility(View.GONE);

        Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

        attributeSpinner.setTag(tagName);

        setSpinnerAdapter(attributeSpinner,new ArrayList<>(),false);

        this.attributeSpinner.put(tagName,attributeSpinner);

        if (!isEditable)
            attributeSpinner.setEnabled(false);

        attributeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){

                    AttributeBO attributeBO1 = (AttributeBO)parent.getSelectedItem();

                    String[] splitTagName = parent.getTag().toString().split("__");
                    updateAttributeArray(parent.getTag().toString(), attributeBO1);
                    resetParentValues((Spinner) parent);

                    ArrayList<AttributeBO> attributeChildList = profileAttributePresenter.getAttributeChildLst(attributeBO1.getAttributeId()+"");

                    if (attributeChildList == null || attributeChildList.isEmpty()){
                        attributeChildList = new ArrayList<>();
                    }

                    String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])+1);

                    Spinner childSpinner = ProfileAttributeFragment.this.attributeSpinner.get(tagName);

                    if (childSpinner == null)
                        return;

                    setSpinnerAdapter(childSpinner,attributeChildList,true);
                }else{
                    updateAttributeArray(parent.getTag().toString(), null);
                    resetSpinnerAdapter((Spinner) parent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((LinearLayout)baseView).addView(view);
    }

    private void resetSpinnerAdapter(Spinner spinner){
        String[] splitTagName = spinner.getTag().toString().split("__");

        String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])+1);

        Spinner childSpinner = attributeSpinner.get(tagName);

        if (childSpinner != null) {
            updateAttributeArray(tagName, null);
            setSpinnerAdapter(childSpinner, new ArrayList<>(), true);
        }
    }

    private void setSpinnerAdapter(Spinner attributeSpinner, ArrayList<AttributeBO> list,boolean isResetAdapter){

        AttributeBO attributeBO = new AttributeBO(-1,"Select");

        ArrayList<AttributeBO> spinnerList = new ArrayList<>();
        spinnerList.add(attributeBO);
        spinnerList.addAll(list);

        ArrayAdapter<AttributeBO> attributeAdapter  = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                spinnerList);
        attributeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        attributeSpinner.setAdapter(attributeAdapter);

        int pos =0;
        for (AttributeBO attribute : list) {

            if (attribute.isRetailerAttributeId() && !attribute.getStatus().equalsIgnoreCase("D")) {
                attributeSpinner.setSelected(true);
                attributeSpinner.setSelection(pos+1);
                break;
            }else if (attribute.isRetailerEditAttributeId() && !attribute.getStatus().equalsIgnoreCase("D")){
                attributeSpinner.setSelected(true);
                attributeSpinner.setSelection(pos+1);
                break;
            }

            pos++;
        }

        if (isResetAdapter)
            resetSpinnerAdapter(attributeSpinner);
    }

    private void resetParentValues(Spinner spinner){
        String[] splitTagName = spinner.getTag().toString().split("__");

        String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])-1);

        Spinner parentSpinner = attributeSpinner.get(tagName);

        if (parentSpinner != null
                && selectedSpinnerIds != null
                && selectedSpinnerIds.get(tagName) != null) {
            AttributeBO attributeBO = selectedSpinnerIds.get(tagName);
            attributeBO.setAttributeSelected(false);

            resetParentValues(parentSpinner);
        }
    }

    private void updateAttributeArray(String attribute, AttributeBO attributeBOSelected){

        if (selectedSpinnerIds.get(attribute) != null && attributeBOSelected == null){
            selectedSpinnerIds.remove(attribute);
        }else if (attributeBOSelected != null) {
            attributeBOSelected.setAttributeSelected(true);

            selectedSpinnerIds.put(attribute,attributeBOSelected);
        }
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        saveAttribute(false,callback,null);
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        saveAttribute(true,null,callback);
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

        if (channelId != selectedChannelId){

            if (channelLayout != null)
                channelLayout.removeAllViews();

            channelId = selectedChannelId;
            channelIds.add(selectedChannelId);
            showChannelAttributeSpinner(profileAttributePresenter.getChannelAttributeList());
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
