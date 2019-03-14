package com.ivy.cpg.view.tradeCoverage.deviation;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

public class DeviationHelper {

    private BusinessModel bmodel;

    public DeviationHelper(BusinessModel businessModel) {
        this.bmodel = businessModel;
    }

    public void setDeviate(String retailerid, ReasonMaster reasonMaster,
                           int beatid, String remarks) {
        RetailerMasterBO retailer;
        int siz = bmodel.retailerMaster.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = bmodel.retailerMaster.get(i);
            if (retailer.getRetailerID().equals(retailerid)
                    && (retailer.getBeatID() == beatid || beatid == 0)) {
                retailer.setIsDeviated("Y");
                bmodel.retailerMaster.setElementAt(retailer, i);
                setDeviateinDB(retailerid, reasonMaster, beatid, remarks);
                return;
            }
        }
    }

    private void setDeviateinDB(String retailerid, ReasonMaster reasonMaster,
                                int beatid, String remarks) {
        try {
            DBUtil db = new DBUtil(bmodel.getContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = "update RetailerBeatMapping set isDeviated='Y' where retailerid=" + retailerid +
                    " AND BeatId=" + beatid;
            db.executeQ(sql);

            String uid = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            String values = QT(uid) + "," + retailerid + ","
                    + QT(bmodel.getAppDataProvider().getUser().getDownloadDate())
                    + "," + reasonMaster.getReasonID() + "," + beatid + "," + bmodel.getAppDataProvider().getRetailMaster().getDistributorId() + "," + QT(remarks)
                    + "," + QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF());
            sql = "insert into deviateReasontable (uid,retailerid,date,reasonid,beatid,distributorID,remarks,ridSF) values("
                    + values + ")";

            db.executeQ(sql);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private String QT(String data) {
        return "'" + data + "'";
    }
}
