package com.ivy.core.data.reason;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ReasonDataManager extends AppDataManagerContract {

    Observable<ArrayList<SalesReturnReasonBO>> fetchSalesReturnReasons();

    Observable<ArrayList<ReasonMaster>> fetchNonPlannedReasons();

    Single<NonproductivereasonBO> fetchNonProductiveReasons(String retailerID, String moduleName);

    Single<Boolean> isNpReasonPhotoAvailable(String retailerID, String moduleName);

    Observable<ArrayList<ReasonMaster>> fetPlaneDeviatedReasons(String listType);

    Observable<ArrayList<ReasonMaster>> fetchPlanedActivitiesReason(String listType);

    Observable<ArrayList<ReasonMaster>> fetchReasons();

    Observable<ArrayList<StandardListBO>> fetchSRCategoryReasons();

    Observable<ArrayList<ReasonMaster>> fetchReasonFromStdListMasterByListCode(String mReasonListCode);

    Observable<ArrayList<ReasonMaster>> fetchReasonsFromStdListMasterByListType(String listType);

    Observable<ArrayList<ReasonMaster>> fetchRetailerAddressFromSLM(String retailerID, String addressCode);
}
