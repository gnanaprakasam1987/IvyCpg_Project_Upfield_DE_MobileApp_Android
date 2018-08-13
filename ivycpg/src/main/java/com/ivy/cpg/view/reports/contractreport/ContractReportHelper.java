package com.ivy.cpg.view.reports.contractreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by ivyuser on 2/8/18.
 */

public class ContractReportHelper {
    private static ContractReportHelper instance = null;

    private ContractReportHelper() {
    }

    public static ContractReportHelper getInstance() {
        if (instance == null) {
            instance = new ContractReportHelper();
        }
        return instance;
    }


    public Observable<ArrayList<ContractBO>> downloadContractReport(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<ContractBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<ContractBO>> subscribe) throws Exception {
                ContractBO contractBO;
                ArrayList<ContractBO> contractBOArrayList = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    Cursor c = db
                            .selectSQL("Select Distinct RM.RetailerID, RM.RetailerCode, RM.RetailerName, (select ch.ChName from channelhierarchy ch where ch.chid = RM.subchannelid) AS SubChannel, RC.ContractDesc, RC.StartDate, RC.EndDate,RC.ContractId  from RetailerMaster RM inner join  RetailerContract RC where RM.RetailerID = RC.RetailerId");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            contractBO = new ContractBO();
                            contractBO.setOutletCode(c.getString(1));
                            contractBO.setOutletName(c.getString(2));
                            contractBO.setSubChannel(c.getString(3));
                            contractBO.setTradeName(c.getString(4));
                            contractBO.setStartDate(DateUtil.convertFromServerDateToRequestedFormat(
                                    c.getString(5),
                                    ConfigurationMasterHelper.outDateFormat));
                            contractBO.setEndDate(DateUtil.convertFromServerDateToRequestedFormat(
                                    c.getString(6),
                                    ConfigurationMasterHelper.outDateFormat));
                            contractBO.setDaysToExp(Utils.getDaysDifference(SDUtil.now(SDUtil.DATE_GLOBAL), c.getString(6), "yyyy/MM/dd"));
                            contractBO.setContractID(c.getString(7));
                            if (contractBO.getDaysToExp() <= 45)
                                contractBOArrayList.add(contractBO);
                        }

                        c.close();
                    }
                    subscribe.onNext(contractBOArrayList);
                    subscribe.onComplete();
                } catch (Exception e) {
                    Commons.printException(e);
                    subscribe.onError(e);
                    subscribe.onComplete();
                } finally {
                    if (db != null)
                        db.closeDB();
                }
            }
        });
    }

}
