package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.TaskAssignBO;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 12-05-2016.
 */
public interface RemoveRetailerInterface {
    void removeRetailer(ArrayList<TaskAssignBO> retailerList,String userid);

}
