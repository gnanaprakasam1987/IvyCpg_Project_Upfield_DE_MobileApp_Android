package com.ivy.ui.profile.attribute.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

public interface IProfileAttributeDataManager extends AppDataManagerContract {

    Observable<ArrayList<AttributeBO>> prepareCommonAttributeList();

    Observable<ArrayList<AttributeBO>> prepareChannelAttributeList();

    Observable<HashMap<String,ArrayList<AttributeBO>>> prepareChildAttributeList(String retailerId);
}
