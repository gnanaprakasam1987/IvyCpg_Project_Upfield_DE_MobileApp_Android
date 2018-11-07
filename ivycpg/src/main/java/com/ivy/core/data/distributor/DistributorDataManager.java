package com.ivy.core.data.distributor;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface DistributorDataManager extends AppDataManagerContract {

    Observable<ArrayList<DistributorMasterBO>> fetchDistributorList();
}
