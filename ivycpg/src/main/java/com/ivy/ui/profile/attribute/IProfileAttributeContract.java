package com.ivy.ui.profile.attribute;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

public interface IProfileAttributeContract {

    interface IProfileAttributeView extends BaseIvyView{

        void showCommonAttributeSpinner(ArrayList<AttributeBO> commonAttributeList);

        void showChannelAttributeSpinner(ArrayList<AttributeBO> channelAttributeList);
    }

    interface IProfileAttributePresenter<V extends IProfileAttributeView> extends BaseIvyPresenter<V>{
        void prepareAttributeList();

        ArrayList<AttributeBO> getCommonAttributeList();

        ArrayList<AttributeBO> getChannelAttributeList();

        ArrayList<AttributeBO> getAttributeChildLst(String parentId);

        HashMap<String, ArrayList<AttributeBO>> getChildAttribute();
    }
}
