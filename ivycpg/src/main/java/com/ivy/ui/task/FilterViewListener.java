package com.ivy.ui.task;

import com.ivy.ui.task.model.FilterBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface FilterViewListener {

    void apply(ArrayList<String> menuList, HashMap<String, ArrayList<FilterBo>> filterListHashMap, HashMap<String,ArrayList<Object>> selectedIdList, String filterName);

    void clearAll();
}
