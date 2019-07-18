package com.ivy.ui.profile.attribute.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface IProfileAttributeDataManager extends AppDataManagerContract {

    Observable<ArrayList<AttributeBO>> prepareCommonAttributeList(boolean isProfileEdit);

    Observable<ArrayList<AttributeBO>> prepareChannelAttributeList(boolean isProfileEdit);

    Observable<HashMap<String,ArrayList<AttributeBO>>> prepareChildAttributeList(String retailerId);

    Single<Boolean> saveRetailerAttribute(final int userId,final String RetailerID,
                                                  final ArrayList<AttributeBO> selectedAttribList);
}
