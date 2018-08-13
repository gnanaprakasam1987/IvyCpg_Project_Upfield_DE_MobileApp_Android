package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

public class NewOutletAttributeHelper {

    private Context context;
    private ArrayList<NewOutletAttributeBO> attribList;
    private ArrayList<NewOutletAttributeBO> attributeParentList;
    private HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap;

    private ArrayList<Integer> mCommonAttributeList;

    static NewOutletAttributeHelper instance = null;

    public static NewOutletAttributeHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NewOutletAttributeHelper(context);
        }
        return instance;
    }

    private NewOutletAttributeHelper(Context context) {
        this.context = context;
    }

    public void downloadRetailerAttribute() {
        try {
            NewOutletAttributeBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            attribList = new ArrayList<>();
            Cursor c = db
                    .selectSQL("SELECT attributeid, attributename, parentid, levelid, allowmultiple, iscriteriamapped FROM entityattributemaster where parentid !=" + 0 + " order by attributeid");
            if (c != null) {
                while (c.moveToNext()) {
                    temp = new NewOutletAttributeBO();
                    temp.setAttrId(c.getInt(0));
                    temp.setAttrName(c.getString(1));
                    temp.setParentId(c.getInt(2));
                    temp.setLevelId(c.getInt(3));
                    temp.setAllowMultiple(c.getInt(4));
                    temp.setCriteriaMapped(c.getInt(5));

                    attribList.add(temp);
                }
                setAttributeList(attribList);
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void setAttributeList(ArrayList<NewOutletAttributeBO> attribList) {
        this.attribList = attribList;
    }

    public ArrayList<NewOutletAttributeBO> getAttributeList() {
        return attribList;
    }

    public void downloadAttributeParentList() {
        try {
            attributeParentList = new ArrayList<>();
            NewOutletAttributeBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT attributeid, attributename, isSystemComputed,IsMandatory FROM entityattributemaster where parentid =0 order by sequence");
            if (c != null) {
                downloadRetailerAttribute();
                while (c.moveToNext()) {
                    int attribId = c.getInt(0);
                    int levelId = 0;
                    temp = new NewOutletAttributeBO();
                    temp.setAttrId(attribId);
                    temp.setAttrName(c.getString(1));
                    temp.setIsMandatory(c.getInt(3));

                    for (int i = 0; i < attribList.size(); i++) {
                        int parentID = attribList.get(i).getParentId();
                        if (attribId == parentID) {
                            attribId = attribList.get(i).getAttrId();
                            levelId = attribList.get(i).getLevelId();
                        }
                    }

                    temp.setLevelId(levelId);
                    attributeParentList.add(temp);
                }
                setAttributeParentList(attributeParentList);
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public HashMap<Integer, ArrayList<NewOutletAttributeBO>> getmAttributeBOListByLocationID() {
        return mAttributeBOListByLocationID;
    }

    HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID = null;

    public HashMap<Integer, ArrayList<Integer>> downloadChannelWiseAttributeList() {
        HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT EAM.attributeid,CriteriaId,ECT.isMandatory,AttributeName FROM entityattributemaster EAM inner join EntityCriteriaType ECT ON EAM.attributeId=ECT.attributeId where parentid =0 and criteriaType='CHANNEL' and IsSystemComputed=0 order by sequence");
            if (c != null && c.getCount() > 0) {
                mAttributeBOListByLocationID = new HashMap<>();
                mAttributeListByLocationID = new HashMap<>();
                NewOutletAttributeBO newOutletAttributeBO;
                while (c.moveToNext()) {
                    newOutletAttributeBO = new NewOutletAttributeBO();
                    newOutletAttributeBO.setAttrId(c.getInt(0));
                    newOutletAttributeBO.setIsMandatory(c.getInt(2));
                    newOutletAttributeBO.setAttrName(c.getString(3));

                    if (mAttributeBOListByLocationID.get(c.getInt(1)) != null) {
                        mAttributeBOListByLocationID.get(c.getInt(1)).add(newOutletAttributeBO);
                        mAttributeListByLocationID.get(c.getInt(1)).add(c.getInt(0));
                    } else {
                        ArrayList<NewOutletAttributeBO> mAtrributeList = new ArrayList<>();
                        mAtrributeList.add(newOutletAttributeBO);

                        mAttributeBOListByLocationID.put(c.getInt(1), mAtrributeList);

                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(c.getInt(0));
                        mAttributeListByLocationID.put(c.getInt(1), list);
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return mAttributeListByLocationID;
    }


    public ArrayList<Integer> getmCommonAttributeList() {
        if (mCommonAttributeList == null)
            mCommonAttributeList = new ArrayList<>();
        return mCommonAttributeList;
    }

    public void downloadCommonAttributeList() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT attributeid FROM entityattributemaster where parentid =0 and attributeid not in(select attributeid from EntityCriteriaType) and IsSystemComputed=0 and IsCriteriaMapped=0 order by sequence");
            if (c != null && c.getCount() > 0) {
                mCommonAttributeList = new ArrayList<>();
                while (c.moveToNext()) {
                    mCommonAttributeList.add(c.getInt(0));
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setAttributeParentList(ArrayList<NewOutletAttributeBO> attributeParentList) {
        this.attributeParentList = attributeParentList;
    }

    public ArrayList<NewOutletAttributeBO> getAttributeParentList() {
        return attributeParentList;
    }

    public HashMap<String, ArrayList<NewOutletAttributeBO>> getAttribMap() {
        try {
            attribMap = new HashMap<>();
            ArrayList<NewOutletAttributeBO> tempList;
            for (NewOutletAttributeBO parent : getAttributeParentList()) {
                tempList = new ArrayList<>();
                for (NewOutletAttributeBO child : getAttributeList()) {
                    if (parent.getAttrId() == child.getParentId()) {
                        tempList.add(child);
                    }
                }
                attribMap.put(parent.getAttrName(), tempList);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return attribMap;
    }

    public ArrayList<NewOutletAttributeBO> getEditAttributeList(String retailerID) {
        ArrayList<NewOutletAttributeBO> attributeBOArrayList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            Cursor cursor = db.selectSQL("select attributeid, levelid, status from retailereditattribute where retailerid = " + retailerID + " and upload='N'");
            if (cursor != null) {
                NewOutletAttributeBO tempBO;
                while (cursor.moveToNext()) {
                    tempBO = new NewOutletAttributeBO();
                    tempBO.setAttrId(cursor.getInt(0));
                    tempBO.setLevelId(cursor.getInt(1));
                    tempBO.setStatus(cursor.getString(2));
                    attributeBOArrayList.add(tempBO);
                }
                cursor.close();
                db.closeDB();
            }
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
            return new ArrayList<>();
        }
        return attributeBOArrayList;
    }
}
