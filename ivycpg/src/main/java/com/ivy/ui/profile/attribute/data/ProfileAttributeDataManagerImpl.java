package com.ivy.ui.profile.attribute.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class ProfileAttributeDataManagerImpl implements IProfileAttributeDataManager {

    private DBUtil mDbUtil;

    @Inject
    ProfileAttributeDataManagerImpl(@DataBaseInfo DBUtil dbUtil){
        this.mDbUtil = dbUtil;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    @Override
    public Observable<ArrayList<AttributeBO>> prepareCommonAttributeList(boolean isProfileEdit){
        return Observable.fromCallable(new Callable<ArrayList<AttributeBO>>() {
            @Override
            public ArrayList<AttributeBO> call() throws Exception {
                ArrayList<AttributeBO> mapValues = new ArrayList<>();

                String whereCond = " ";
                if (isProfileEdit)
                    whereCond = " and EAM.isEditable = 1 ";

                String parentQry = "Select EAM.AttributeId,EAM.AttributeName,EAM.Sequence,EAM.ParentId,EAM.levels,EAM.isEditable,EAM.IsMandatory from EntityAttributeMaster EAM where EAM.parentId = 0 "
                        +whereCond+
                        " and EAM.Attributeid not in(select attributeid from EntityCriteriaType) and EAM.IsSystemComputed=0 and EAM.IsCriteriaMapped=0 order by EAM.Sequence,EAM.AttributeId ASC";

                initDb();

                Cursor c = mDbUtil.selectSQL(parentQry);
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {

                        AttributeBO attributeBO = new AttributeBO();
                        attributeBO.setAttributeId(c.getInt(0));
                        attributeBO.setAttributeName(c.getString(1));
                        attributeBO.setLevelCount(c.getInt(4));
                        attributeBO.setEditable(c.getInt(5) > 0);
                        attributeBO.setMandatory(c.getInt(6) > 0);

                        mapValues.add(attributeBO);

                    }
                }

                shutDownDb();

                return mapValues;
            }
        });
    }

    @Override
    public Observable<ArrayList<AttributeBO>> prepareChannelAttributeList(boolean isProfileEdit){
        return Observable.fromCallable(new Callable<ArrayList<AttributeBO>>() {
            @Override
            public ArrayList<AttributeBO> call() throws Exception {
                ArrayList<AttributeBO> mapValues = new ArrayList<>();

                String whereCond = " ";
                if (isProfileEdit)
                    whereCond = " and EAM.isEditable = 1 ";

                String parentQry = "Select EAM.AttributeId,EAM.AttributeName,EAM.Sequence,EAM.ParentId,EAM.levels,EAM.isEditable,ECT.CriteriaId,ECT.isMandatory from EntityAttributeMaster EAM " +
                        " inner join EntityCriteriaType ECT ON EAM.attributeId = ECT.attributeId " +
                        " where parentid = 0 "
                        +whereCond+
                        " and criteriaType = 'CHANNEL' and IsSystemComputed = 0 order by EAM.Sequence,EAM.AttributeId ASC";

                initDb();

                Cursor c = mDbUtil.selectSQL(parentQry);
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {

                        AttributeBO attributeBO = new AttributeBO();
                        attributeBO.setAttributeId(c.getInt(0));
                        attributeBO.setAttributeName(c.getString(1));
                        attributeBO.setLevelCount(c.getInt(4));
                        attributeBO.setChannelId(c.getInt(6));
                        attributeBO.setMandatory(c.getInt(7) > 0);
                        attributeBO.setEditable(c.getInt(5) > 0);

                        mapValues.add(attributeBO);

                    }
                }

                shutDownDb();

                return mapValues;
            }
        });
    }

    @Override
    public Observable<HashMap<String, ArrayList<AttributeBO>>> prepareChildAttributeList(String retailerId) {
        return Observable.fromCallable(new Callable<HashMap<String,ArrayList<AttributeBO>>>() {
            @Override
            public HashMap<String,ArrayList<AttributeBO>> call() throws Exception {
                HashMap<String,ArrayList<AttributeBO>> mapValues = new HashMap<>();

                String queryStr = "Select EAM.AttributeId as BaseAId,EAM.AttributeName as BaseName,EAM1.AttributeId,EAM1.AttributeName," +
                        " EAM.ParentId,ifnull(RA.AttributeId,-1) as retailAttribute,ifnull(REA.AttributeId,-1) as retailEditAttribute," +
                        " EAM1.LevelId,ifnull(REA.status,'') as status  from EntityAttributeMaster EAM " +
                        " inner join EntityAttributeMaster as EAM1 on EAM.AttributeId = EAM1.ParentId " +
                        " left join RetailerAttribute as RA on RA.AttributeId = EAM1.AttributeId  and RA.RetailerId = "+ StringUtils.getStringQueryParam(retailerId) +
                        " left join RetailerEditAttribute as REA on REA.AttributeId = EAM1.AttributeId  and REA.RetailerId = "+ StringUtils.getStringQueryParam(retailerId) +
                        " group by EAM1.AttributeId " +
                        " order by EAM.AttributeId ASC ";

                initDb();

                Cursor c = mDbUtil.selectSQL(queryStr);
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {

                        AttributeBO attributeBO = new AttributeBO();
                        attributeBO.setAttributeId(c.getInt(2));
                        attributeBO.setAttributeName(c.getString(3));
                        attributeBO.setLevelId(c.getInt(7));
                        attributeBO.setStatus(c.getString(8));
                        attributeBO.setParentId(c.getString(4));
                        attributeBO.setAttributeParentId(c.getInt(0));


                        attributeBO.setRetailerAttributeId(c.getInt(5) > -1);
                        attributeBO.setRetailerEditAttributeId(c.getInt(6) > -1 && !attributeBO.getStatus().equalsIgnoreCase("D"));

                        if (mapValues.get(c.getString(0)) == null) {
                            ArrayList<AttributeBO> attributeBOS = new ArrayList<AttributeBO>(){{add(attributeBO);}};
                            mapValues.put(c.getString(0), attributeBOS);
                        }
                        else {
                            ArrayList<AttributeBO> attributeBOVal = mapValues.get(c.getString(0));
                            attributeBOVal.add(attributeBO);
                        }

                        if (attributeBO.isRetailerAttributeId())
                            attributeBO.setMasterRecord(true);


                        if ((attributeBO.isRetailerAttributeId() && !attributeBO.getStatus().equalsIgnoreCase("D"))
                                || (attributeBO.isRetailerEditAttributeId() && !attributeBO.getStatus().equalsIgnoreCase("D"))) {
                            updateAttributeSelection(mapValues, attributeBO);
                        }
                    }
                }

                shutDownDb();

                return mapValues;
            }
        });
    }

    private void updateAttributeSelection(HashMap<String,ArrayList<AttributeBO>> mapValues, AttributeBO attributeBO) {

        if (mapValues.get(attributeBO.getParentId()) != null){

            AttributeBO matchedAttributeBo = null;

            for (AttributeBO bo : mapValues.get(attributeBO.getParentId())){
                if (attributeBO.getAttributeParentId() == bo.getAttributeId()){
                    bo.setRetailerAttributeId(attributeBO.isRetailerAttributeId());
                    bo.setRetailerEditAttributeId(attributeBO.isRetailerEditAttributeId());

                    matchedAttributeBo = bo;

                    break;
                }
            }

            if (matchedAttributeBo != null)
                updateAttributeSelection(mapValues,matchedAttributeBo);

        }

    }

    @Override
    public Single<Boolean> saveRetailerAttribute(final int userId, final String retailerId,
                                        final ArrayList<AttributeBO> selectedAttribList){

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                String tid = userId
                        + "" + retailerId
                        + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                Cursor headerCursor;
                try {
                    // delete Header if exist
                    headerCursor = mDbUtil.selectSQL("SELECT Tid FROM RetailerEditHeader"
                            + " WHERE RetailerId = "
                            + retailerId
                            + " AND Date = "
                            + StringUtils.getStringQueryParam(currentDate)
                            + " AND Upload = "
                            + StringUtils.getStringQueryParam("N"));

                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }

                updateRetailerMasterAttribute(tid,retailerId,selectedAttribList);

                return true;
            }
        });
    }

    public void updateRetailerMasterAttribute(final String mTid, final String RetailerID,
                                                         final ArrayList<AttributeBO> selectedAttribList) {

        try {
            mDbUtil.deleteSQL("RetailerEditAttribute", " tid =" + StringUtils.getStringQueryParam(mTid), false);

            for (AttributeBO id : selectedAttribList) {
                String Q = "insert into RetailerEditAttribute (tid,retailerid,attributeid,levelid,status,upload)" +
                        "values (" + StringUtils.getStringQueryParam(mTid)
                        + "," + RetailerID
                        + "," + id.getAttributeId()
                        + "," + id.getLevelId()
                        + "," + StringUtils.getStringQueryParam(id.getStatus()) + ",'N')";
                mDbUtil.executeQ(Q);
            }

            shutDownDb();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public boolean isChannelAvailable(){
        initDb();

        boolean isChannelAvail = false;

        try {
            // delete Header if exist
            Cursor cursor = mDbUtil.selectSQL("SELECT Rfield from hhtmenumaster where hhtcode='PROFILE06' and rfield = 1");

            if (cursor != null) {

                if (cursor.getCount() > 0)
                    isChannelAvail = true;

                cursor.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        shutDownDb();

        return isChannelAvail;

    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
