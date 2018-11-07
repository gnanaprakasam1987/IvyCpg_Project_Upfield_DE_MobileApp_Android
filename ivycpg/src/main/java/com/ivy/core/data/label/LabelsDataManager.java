package com.ivy.core.data.label;

import com.ivy.core.data.AppDataManagerContract;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface LabelsDataManager extends AppDataManagerContract{

    Single<String> getLabel(String key);

    Observable<HashMap<String,String>> getLabels(String... key);

    Observable<HashMap<String,String>> getAllLabels();

}
