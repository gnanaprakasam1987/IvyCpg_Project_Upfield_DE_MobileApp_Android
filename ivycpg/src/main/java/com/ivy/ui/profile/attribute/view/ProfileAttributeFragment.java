package com.ivy.ui.profile.attribute.view;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.di.DaggerProfileAttributeComponent;
import com.ivy.ui.profile.attribute.di.ProfileAttributeModule;
import com.ivy.ui.profile.attribute.presenter.ProfileAttributePresenterImpl;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

public class ProfileAttributeFragment extends BaseFragment
        implements IProfileAttributeContract.IProfileAttributeView {

    private LinearLayout dynamicViewLayout;

    private HashMap<String,Spinner> attributeSpinner = new HashMap<>();

    private HashMap<String,AttributeBO> selectedSpinnerIds = new HashMap<>();

    @Inject
    ProfileAttributePresenterImpl<IProfileAttributeContract.IProfileAttributeView> profileAttributePresenter;

    @Override
    public void initializeDi() {
        DaggerProfileAttributeComponent.builder()
                .ivyAppComponent(((BusinessModel) ((Activity)context).getApplication()).getComponent())
                .profileAttributeModule(new ProfileAttributeModule(this))
                .build().inject(this);
        setBasePresenter((BasePresenter) profileAttributePresenter);
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
        profileAttributePresenter.prepareAttributeList();
    }

    private ArrayList<String> filteredAttributeIds = new ArrayList<>();
    private void saveAttribute(){
        if (!selectedSpinnerIds.isEmpty()) {

            ArrayList<AttributeBO> ids = new ArrayList<>(selectedSpinnerIds.values());

            filteredAttributeIds.clear();
            for (AttributeBO bo : ids){
                if (bo.isAttributeSelected()) {
                    String[] spliArr = bo.getAttributeName().split("##");
                    String[] spliArr1 = spliArr[1].split("__");
                    filteredAttributeIds.add(spliArr1[0]);
                }
            }
        }
    }

    @Override
    public void showCommonAttributeSpinner(ArrayList<AttributeBO> commonAttributeList) {
        showAttributeSpinner(commonAttributeList);
    }

    @Override
    public void showChannelAttributeSpinner(ArrayList<AttributeBO> channelAttributeList) {

        if (channelAttributeList.isEmpty())
            return;

        View view = getLayoutInflater().inflate(R.layout.task_report_recycle_header, null);

        ((TextView)view.findViewById(R.id.tv_task_header)).setText("Channel Attribute");

        dynamicViewLayout.addView(view);

        showAttributeSpinner(channelAttributeList);

        View seperatorView = getLayoutInflater().inflate(R.layout.seperator_line_layout, null);

        dynamicViewLayout.addView(seperatorView);

    }

    public void showAttributeSpinner(ArrayList<AttributeBO> attributeParentList) {

        for (AttributeBO attributeBO : attributeParentList){

            ArrayList<AttributeBO> attributeChildList = new ArrayList<>(profileAttributePresenter.getAttributeChildLst(attributeBO.getAttributeId()+""));

            View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

            ((TextView)view.findViewById(R.id.spinner_txt)).setText(attributeBO.getAttributeName());

            Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

            attributeSpinner.setTag(attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+1);

            setSpinnerAdapter(attributeSpinner,attributeChildList,false);

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
                    createChildSpinner(view,attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+i);
                }
            }

            dynamicViewLayout.addView(view);

        }

    }

    private void createChildSpinner(final View baseView, String tagName){

        View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

        (view.findViewById(R.id.spinner_txt)).setVisibility(View.GONE);

        Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

        attributeSpinner.setTag(tagName);

        setSpinnerAdapter(attributeSpinner,new ArrayList<>(),false);

        this.attributeSpinner.put(tagName,attributeSpinner);

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

        /*if (planFilterBo != null
                && planFilterBo.getFilterAttributeIdMap() != null
                && planFilterBo.getFilterAttributeIdMap().get(attributeSpinner.getTag().toString())!= null
                && list != null && !list.isEmpty()) {

            int pos =0;
            AttributeBO attributeBO1 = planFilterBo.getFilterAttributeIdMap().get(attributeSpinner.getTag().toString());
            for (AttributeBO aId : list) {

                if (aId.getAttributeId() == attributeBO1.getAttributeId()) {
                    attributeSpinner.setSelected(true);
                    attributeSpinner.setSelection(pos+1);
                    break;
                }
                pos++;
            }
        }*/

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

}
