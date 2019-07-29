package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;

import java.util.ArrayList;
import java.util.HashMap;

public class NewOutletAttributeHelper {

    private Context context;
    private ArrayList<NewOutletAttributeBO> mAttributeBOArrayListChild;
    private ArrayList<NewOutletAttributeBO> mAttributeParentList;
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


    public void setAttributeList(ArrayList<NewOutletAttributeBO> attribList) {
        this.mAttributeBOArrayListChild = attribList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadRetailerChildAttribute()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletAttributeBO> getAttributeList() {
        return mAttributeBOArrayListChild;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadRetailerChildAttribute()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */

    public void downloadRetailerAttributeChildList() {
        try {
            NewOutletAttributeBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            mAttributeBOArrayListChild = new ArrayList<>();
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

                    mAttributeBOArrayListChild.add(temp);
                }
                setAttributeList(mAttributeBOArrayListChild);
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }



    private void setmAttributeParentList(ArrayList<NewOutletAttributeBO> mAttributeParentList) {
        this.mAttributeParentList = mAttributeParentList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadAttributeParentList(ArrayList<NewOutletAttributeBO> )}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletAttributeBO> getmAttributeParentList() {
        return mAttributeParentList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadAttributeParentList(ArrayList<NewOutletAttributeBO> )}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void downloadAttributeParentList() {
        try {
            mAttributeParentList = new ArrayList<>();
            NewOutletAttributeBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT attributeid, attributename, isSystemComputed,IsMandatory FROM entityattributemaster where parentid =0 order by sequence");
            if (c != null) {
                downloadRetailerAttributeChildList();
                while (c.moveToNext()) {
                    int attribId = c.getInt(0);
                    int levelId = 0;
                    temp = new NewOutletAttributeBO();
                    temp.setAttrId(attribId);
                    temp.setAttrName(c.getString(1));
                    temp.setIsMandatory(c.getInt(3));

                    for (int i = 0; i < mAttributeBOArrayListChild.size(); i++) {
                        int parentID = mAttributeBOArrayListChild.get(i).getParentId();
                        if (attribId == parentID) {
                            attribId = mAttributeBOArrayListChild.get(i).getAttrId();
                            levelId = mAttributeBOArrayListChild.get(i).getLevelId();
                        }
                    }
                    temp.setLevelId(levelId);
                    mAttributeParentList.add(temp);
                }
                setmAttributeParentList(mAttributeParentList);
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }




    HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID = null;

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadChannelWiseAttributeList()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public HashMap<Integer, ArrayList<NewOutletAttributeBO>> getmAttributeBOListByLocationID() {
        return mAttributeBOListByLocationID;
    }
    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadChannelWiseAttributeList()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public HashMap<Integer, ArrayList<Integer>> downloadChannelWiseAttributeList() {

        HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
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

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadCommonAttributeList()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<Integer> getmCommonAttributeList() {
        if (mCommonAttributeList == null)
            mCommonAttributeList = new ArrayList<>();
        return mCommonAttributeList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadCommonAttributeList()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void downloadCommonAttributeList() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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



    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp}
     * @since CPG131 replaced by {@link ProfileEditPresenterImp#getAttributeMap()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public HashMap<String, ArrayList<NewOutletAttributeBO>> getAttribMap() {
        try {
            attribMap = new HashMap<>();
            ArrayList<NewOutletAttributeBO> tempList;
            for (NewOutletAttributeBO parent : getmAttributeParentList()) {
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

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadEditAttributeList( String retailerID)()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletAttributeBO> getEditAttributeList(String retailerID) {
        ArrayList<NewOutletAttributeBO> attributeBOArrayList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
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
