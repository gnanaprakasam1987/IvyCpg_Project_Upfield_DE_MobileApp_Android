package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatEditText;
import android.util.SparseArray;

import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.AddressBO;
import com.ivy.sd.png.bo.CensusLocationBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;

public class NewOutletHelper {

    private final Context context;
    private int mSelectedChannelid = 0;
    private String mSelectedChannelname = "";

    public int getmSelectedChannelid() {
        return mSelectedChannelid;
    }

    public void setmSelectedChannelid(int mSelectedChannelid) {
        this.mSelectedChannelid = mSelectedChannelid;
    }


    private NewOutletBO imageType;
    private ArrayList<NewOutletBO> retailerTypeList;
    private ArrayList<NewOutletBO> contactTitleList;
    private ArrayList<NewOutletBO> contractStatusList;
    private Vector<RetailerMasterBO> mLinkRetailerList;
    private ArrayList<StandardListBO> selectedPrioProducts = new ArrayList<>();
    private ArrayList<ProductMasterBO> orderedProductList = new ArrayList<>();
    private ArrayList<ProductMasterBO> opprProductList = new ArrayList<>();
    private ArrayList<CensusLocationBO> censusLocationList = new ArrayList<>();

    public AppCompatEditText[] getEditText() {
        return editText;
    }

    public void setEditText(AppCompatEditText[] editText) {
        if (editText != null) {
            this.editText = editText.clone();
        } else {
            this.editText = null;
        }
    }

    private AppCompatEditText editText[] = null;

    public MaterialSpinner[] getMaterialSpinner() {
        return materialSpinner;
    }

    public void setMaterialSpinner(MaterialSpinner[] materialSpinner) {
        if (materialSpinner != null)
            this.materialSpinner = materialSpinner.clone();
        else
            this.materialSpinner = null;
    }

    private MaterialSpinner materialSpinner[] = null;


    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#getSelectedPrioProducts}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    private ArrayList<StandardListBO> getSelectedPrioProducts() {
        return selectedPrioProducts;
    }

    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#setSelectedPrioProducts}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void setSelectedPrioProducts(ArrayList<StandardListBO> selectedPrioProducts) {
        this.selectedPrioProducts = selectedPrioProducts;
    }

    private SparseArray<Vector<RetailerMasterBO>> mLinkRetailerListByDistributorId;

    public ArrayList<NewOutletBO> getRetailerTypeList() {
        return retailerTypeList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletBO> getContactTitleList() {
        return contactTitleList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletBO> getContractStatusList() {
        return contractStatusList;
    }

    public String getContractStatus(int statusLovid) {
        NewOutletBO beat;
        int siz = getContractStatusList().size();

        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            beat = getContractStatusList().get(i);
            if (statusLovid == beat.getListId()) {
                return beat.getListName();
            }
        }
        return "";
    }

    private LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevelId;

    public NewOutletBO getImageType() {
        return imageType;
    }

    private Vector<ConfigureBO> profileConfig = null;
    private Vector<ConfigureBO> profileEditConfig = null;

    private static NewOutletHelper instance = null;
    private final BusinessModel bmodel;
    private Vector<NewOutletBO> imageTypeList = new Vector<>();

    private String id;

    private NewOutletHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vector<NewOutletBO> getImageTypeList() {
        return imageTypeList;
    }

    private void setImageTypeList(Vector<NewOutletBO> imageTypeList) {
        this.imageTypeList = imageTypeList;
    }

    public ArrayList<ProductMasterBO> getOrderedProductList() {
        return orderedProductList;
    }

    public void setOrderedProductList(ArrayList<ProductMasterBO> orderedProductList) {
        this.orderedProductList = orderedProductList;
    }

    public ArrayList<ProductMasterBO> getOpprProductList() {
        return opprProductList;
    }

    public void setOpprProductList(ArrayList<ProductMasterBO> opprProductList) {
        this.opprProductList = opprProductList;
    }

    public static NewOutletHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NewOutletHelper(context);
        }
        return instance;
    }

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public void loadRetailerType() {
        NewOutletBO retailerType;
        retailerTypeList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    retailerType = new NewOutletBO();
                    retailerType.setListId(c.getInt(0));
                    retailerType.setListName(c.getString(2));
                    retailerTypeList.add(retailerType);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#getContactTitle()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void loadContactTitle() {
        NewOutletBO contactTitle;
        contactTitleList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTACT_TITLE_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactTitle = new NewOutletBO();
                    contactTitle.setListId(c.getInt(0));
                    contactTitle.setListName(c.getString(2));
                    contactTitleList.add(contactTitle);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#getContactStatus}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void loadContactStatus() {
        NewOutletBO contactStatus;
        contractStatusList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTRACT_STATUS'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactStatus = new NewOutletBO();
                    contactStatus.setListId(c.getInt(0));
                    contactStatus.setListName(c.getString(2));
                    contractStatusList.add(contactStatus);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Update retailer details.. in RetailerMaster and RetailerEdit Header and Detail
     */
    public void updateRetailer() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            boolean isData;
            String tid;
            String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            Cursor headerCursor;


            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + bmodel.getRetailerMasterBO().getRetailerID()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            // delete Header if exist
            headerCursor = db.selectSQL("SELECT Tid FROM RetailerEditHeader"
                    + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = "
                    + bmodel.QT(currentDate)
                    + " AND Upload = "
                    + bmodel.QT("N"));

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                tid = headerCursor.getString(0);
                headerCursor.close();
            }

            String insertHeader = "insert into RetailerEditHeader (tid,RetailerId,date)" +
                    "values (" + bmodel.QT(tid)
                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                    + "," + bmodel.QT(currentDate) + ")";

            String insertquery = "insert into RetailerEditDetail (tid,Code,value,RefId,RetailerId)" + "values (" + bmodel.QT(tid) + ",";

            String queryInsert = "";

            profileEditConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();

            Commons.print("Profile Fragment, " + " Update Retailer List size :" + profileEditConfig.size());

            isData = false;

            for (ConfigureBO configBO : profileEditConfig) {

                if (configBO.getConfigCode().equalsIgnoreCase("PROFILE02") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getRetailerName().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getRetailerName().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE03") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getAddress1().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getAddress1().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE04") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getAddress2().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getAddress2().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE05") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getAddress3().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getAddress3().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE39") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getCity().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getCity().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE40") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getState().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getState().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE41") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("-1")) {
                        if (bmodel.getRetailerMasterBO().getContact1_titlelovid().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getContact1_titlelovid().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp1id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE42") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("-1")) {
                        if (bmodel.getRetailerMasterBO().getContact2_titlelovid().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getContact2_titlelovid().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp2id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE43") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("0")) {
                        if ((bmodel.getRetailerMasterBO().getContractLovid() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContractLovid() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("CT1TITLE") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("0")) {
                        if (bmodel.getRetailerMasterBO().getContact1_title().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getContact1_title().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp1id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("CT2TITLE") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("0")) {
                        if (bmodel.getRetailerMasterBO().getContact2_title().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getContact2_title().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp2id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE38") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getPincode().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getPincode().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE30") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getContactnumber().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getContactnumber().equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE06") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("0")) {
                        if ((bmodel.getRetailerMasterBO().getChannelID() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getChannelID() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE07") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("0")) {
                        if ((bmodel.getRetailerMasterBO().getSubchannelid() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getSubchannelid() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE08")
                        && (configBO.getModule_Order() == 1 || bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE)) {
                    if (!configBO.getMenuNumber().equals("0.0")) {
                        if ((bmodel.getRetailerMasterBO().getLatitude() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getLatitude() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }

                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE31")
                        && (configBO.getModule_Order() == 1 || bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE)) {
                    if (!configBO.getMenuNumber().equals("0.0")) {
                        if ((bmodel.getRetailerMasterBO().getLongitude() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getLongitude() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE63") && configBO.getModule_Order() == 1) {
                    deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                    String imagePath = "Profile" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + "/" + configBO.getMenuNumber();
                    queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(imagePath) + "," + bmodel.getRetailerMasterBO().getAddressid() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                    isData = true;
                    bmodel.profilehelper.checkFileExist(bmodel.latlongImageFileName + "", bmodel.getRetailerMasterBO().getRetailerID(), true);
                }
                //
                else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE09") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactname() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactname() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp1id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE20") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRField1() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRField1() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE26") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRfield2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRfield2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE27") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getCredit_invoice_count() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getCredit_invoice_count() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE28") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRField4() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRField4() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE53") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRField5() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRField5() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE54") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRField6() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRField6() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE55") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getRField7() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getRField7() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("LNAME") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactLname() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactLname() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp1id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE10") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactnumber1() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactnumber1() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp1id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE11") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactname2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactname2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp2id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("LNAME2") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactLname2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactLname2() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp2id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;

                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE12") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getContactnumber2()).equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getContactnumber2()).equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getCp2id() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE13") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("0")) {
                        if ((bmodel.getRetailerMasterBO().getLocationId() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if (bmodel.getRetailerMasterBO().getLocationId() != 0 && ((!(bmodel.getRetailerMasterBO().getLocationId() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber()))))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber()) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE25") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("0")) {

                        if ((bmodel.getRetailerMasterBO().getCreditDays() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getCreditDays() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + configBO.getMenuNumber() + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE36") && configBO.getModule_Order() == 1) {

                    HashMap<String, String> temp = new HashMap<>();
                    if (bmodel.getNearByRetailers().size() > 0) {
                        isData = true;
                        for (RetailerMasterBO bo : bmodel.getNearByRetailers()) {
                            temp.put(bo.getRetailerID(), "N");
                        }
                    }

                    //
                    ArrayList<String> ids = getNearbyRetailerIds(bmodel.getRetailerMasterBO().getRetailerID());
                    if (temp.size() > 0) {
                        for (String id : ids) {
                            if (temp.get(id) != null) {
                                temp.remove(id);
                            } else {
                                temp.put(id, "D");
                            }
                        }
                    }

                    db.deleteSQL("RrtNearByEditRequest", " tid =" + bmodel.QT(tid), false);

                    for (String id : temp.keySet()) {
                        String Q = "insert into RrtNearByEditRequest (tid,rid,nearbyrid,status,upload)" +
                                "values (" + bmodel.QT(tid)
                                + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + id
                                + "," + bmodel.QT(temp.get(id)) + ",'N')";
                        db.executeQ(Q);
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE57") && configBO.getModule_Order() == 1) {


                    db.deleteSQL("RetailerEditPriorityProducts", " RetailerId =" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()), false);

                    for (StandardListBO bo : getSelectedPrioProducts()) {
                        isData = true;
                        String Q = "insert into RetailerEditPriorityProducts (tid,RetailerId,productId,levelid,status,upload)" +
                                "values (" + bmodel.QT(tid)
                                + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                                + "," + SDUtil.convertToInt(bo.getListID())
                                + "," + bmodel.QT(bo.getListCode())
                                + "," + bmodel.QT(bo.getStatus()) + ",'N')";
                        db.executeQ(Q);
                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE58") && configBO.getModule_Order() == 1) {

                    ArrayList<NewOutletAttributeBO> tempList = new ArrayList<>();
                    isData = true;

                    ArrayList<NewOutletAttributeBO> attributeList = updateRetailerMasterAttribute(bmodel.getRetailerAttribute());
                    //    ArrayList<NewOutletAttributeBO> ids = updateRetailerMasterAttribute(bmodel.newOutletAttributeHelper.downloadEditAttributeList(bmodel.getRetailerMasterBO().getRetailerID()));
                    ArrayList<NewOutletAttributeBO> attList = updateRetailerMasterAttribute(bmodel.getRetailerMasterBO().getAttributeBOArrayList());


                    NewOutletAttributeBO tempBO1;
                    NewOutletAttributeBO tempBO2 = null;
                    if (attributeList.size() > 0) {
                        for (int i = 0; i < attributeList.size(); i++) {
                            tempBO1 = attributeList.get(i);
                            if (attList.size() > 0) {
                                boolean isDiffParent = true;
                                ArrayList<Integer> porcessedAttributes = new ArrayList<>();
                                for (int j = 0; j < attList.size(); j++) {
                                    tempBO2 = attList.get(j);
                                    if (tempBO1.getParentId() == tempBO2.getParentId()) {
                                        if (tempBO1.getAttrId() != tempBO2.getAttrId()) {
                                            tempBO1.setStatus("N");
                                            tempList.add(tempBO1);
                                            tempBO2.setStatus("D");
                                            tempList.add(tempBO2);
                                            isDiffParent = false;
                                            porcessedAttributes.add(tempBO2.getAttrId());
                                        }
                                    }

                                }
                                /**
                                 * add attribute list while change parent id
                                 * isDiffParent
                                 * true - parentId is mismatched
                                 * false - parentId is matched
                                 * add previous attribute data
                                 * which is not available in processedAttribute list
                                 *
                                 */
                                if (isDiffParent) {
                                    tempBO1.setStatus("N");
                                    tempList.add(tempBO1);

                                    for (NewOutletAttributeBO bo : attList) {
                                        if (!porcessedAttributes.contains(bo.getAttrId())) {
                                            tempBO2.setStatus("D");
                                            tempList.add(tempBO2);
                                        }
                                    }
                                }

                            } else {
                                tempBO1.setStatus("N");
                                tempList.add(tempBO1);
                            }
                        }
                    } else {
                        for (int j = 0; j < attList.size(); j++) {
                            tempBO2 = attList.get(j);
                            tempBO2.setStatus("D");
                            tempList.add(tempBO2);
                        }
                    }

                    /*for (int i=0; i<tempList.size(); i++){
                        tempBO1 = tempList.get(i);
                        for (int j=0; j<ids.size(); j++){
                            tempBO2 = ids.get(j);
                            if (tempBO1.getParentId() == tempBO2.getParentId()) {
                                if (tempBO1.getAttrId() != tempBO2.getAttrId() && "D".equals(tempBO1.getStatus())) {
                                    tempBO2.setStatus("D");
                                    tempList.set(i,tempBO2);
                                }
                            }

                        }
                    }*/

                    db.deleteSQL("RetailerEditAttribute", " tid =" + bmodel.QT(tid), false);

                    for (NewOutletAttributeBO id : tempList) {
                        String Q = "insert into RetailerEditAttribute (tid,retailerid,attributeid,levelid,status,upload)" +
                                "values (" + bmodel.QT(tid)
                                + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + id.getAttrId()
                                + "," + id.getLevelId()
                                + "," + bmodel.QT(id.getStatus()) + ",'N')";
                        db.executeQ(Q);
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE60") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {
                        if ((bmodel.getRetailerMasterBO().getProfileImagePath() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                            bmodel.profilehelper.checkFileExist(configBO.getMenuNumber() + "", bmodel.getRetailerMasterBO().getRetailerID(), false);
                        } else if ((!(bmodel.getRetailerMasterBO().getProfileImagePath() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                            String imagePath = "Profile" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                                    + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "/" + configBO.getMenuNumber();
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(imagePath) + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                            bmodel.profilehelper.checkFileExist(configBO.getMenuNumber() + "", bmodel.getRetailerMasterBO().getRetailerID(), false);
                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE61") && configBO.getModule_Order() == 1) {

                    if (!configBO.getMenuNumber().equals("")) {

                        if ((bmodel.getRetailerMasterBO().getGSTNumber() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!(bmodel.getRetailerMasterBO().getGSTNumber() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + ",'" + configBO.getMenuNumber() + "'," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE62") && configBO.getModule_Order() == 1) {

                    if ((bmodel.getRetailerMasterBO().getIsSEZzone() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                        deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                        isData = true;
                    } else if ((!(bmodel.getRetailerMasterBO().getIsSEZzone() + "").equals(configBO.getMenuNumber()) && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                            || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                        deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                        queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + configBO.getMenuNumber()
                                + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                        isData = true;
                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE81") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getPanNumber().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getPanNumber().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode()) + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE82") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getFoodLicenceNo().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getFoodLicenceNo().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE84") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getDLNo().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getDLNo().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE83") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getFoodLicenceExpDate().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getFoodLicenceExpDate().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE85") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getDLNoExpDate().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getDLNoExpDate().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE78") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getEmail().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getEmail().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber())
                                    + "," + bmodel.getRetailerMasterBO().getAddressid()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE79") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getMobile().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getMobile().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber()) + ","
                                    + bmodel.getRetailerMasterBO().getAddressid()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE86") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getFax().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getFax().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber()) + ","
                                    + bmodel.getRetailerMasterBO().getAddressid() + ","
                                    + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE87") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getRegion().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getRegion().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber()) + ","
                                    + bmodel.getRetailerMasterBO().getAddressid()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }


                } else if (configBO.getConfigCode().equalsIgnoreCase("PROFILE88") && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getRetailerMasterBO().getCountry().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getRetailerMasterBO().getCountry().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getRetailerMasterBO().getRetailerID());
                            queryInsert = insertquery + bmodel.QT(configBO.getConfigCode())
                                    + "," + bmodel.QT(configBO.getMenuNumber()) + ","
                                    + bmodel.getRetailerMasterBO().getAddressid()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
                            isData = true;
                        }

                    }
                } else if (configBO.getConfigCode().equalsIgnoreCase(ProfileConstant.DISTRICT) && configBO.getModule_Order() == 1) {
                    if (!configBO.getMenuNumber().equals("")) {
                        if (bmodel.getAppDataProvider().getRetailMaster().getDistrict().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null) {
                            deleteQuery(configBO.getConfigCode(), bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                            isData = true;
                        } else if ((!bmodel.getAppDataProvider().getRetailMaster().getDistrict().equals(configBO.getMenuNumber())
                                && getmPreviousProfileChangesList().get(configBO.getConfigCode()) == null)
                                || (getmPreviousProfileChangesList().get(configBO.getConfigCode()) != null
                                && (!getmPreviousProfileChangesList().get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                            deleteQuery(configBO.getConfigCode(), bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                            queryInsert = insertquery + QT(configBO.getConfigCode())
                                    + "," + QT(configBO.getMenuNumber()) + ","
                                    + bmodel.getAppDataProvider().getRetailMaster().getAddressid()
                                    + "," + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + ")";
                            isData = true;
                        }

                    }
                }

                if (!queryInsert.equals(""))
                    db.executeQ(queryInsert);

                queryInsert = "";

            }


            if (isData) {

                db.deleteSQL(DataMembers.tbl_RetailerEditHeader, " Tid=" + bmodel.QT(tid), false);

                Cursor c = db.selectSQL("SELECT code FROM " + DataMembers.tbl_RetailerEditDetail + " where Tid=" + bmodel.QT(tid));

                Cursor c1 = db.selectSQL("SELECT status FROM " + DataMembers.tbl_nearbyEditRequest + " where Tid=" + bmodel.QT(tid));

                Cursor c2 = db.selectSQL("SELECT status FROM " + DataMembers.tbl_RetailerEditPriorityProducts + " where Tid=" + bmodel.QT(tid));

                Cursor c3 = db.selectSQL("SELECT status FROM RetailerEditAttribute" + " where Tid=" + bmodel.QT(tid));

                if (c.getCount() > 0 || c1.getCount() > 0 || c2.getCount() > 0 || c3.getCount() > 0) {
                    db.executeQ(insertHeader);
                    c.close();
                    c1.close();
                    c2.close();
                    c3.close();
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#updateRetailerMasterAttribute(ArrayList, ArrayList, ArrayList)}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */

    public ArrayList<NewOutletAttributeBO> updateRetailerMasterAttribute(ArrayList<NewOutletAttributeBO> list) {

        //Load Child Attribute list which parent is not zero
        ArrayList<NewOutletAttributeBO> childList = bmodel.newOutletAttributeHelper.getAttributeList();
        //Load Parent Attribute List which Parent id is zero
        ArrayList<NewOutletAttributeBO> parentList = bmodel.newOutletAttributeHelper.getmAttributeParentList();

        ArrayList<NewOutletAttributeBO> tempList = new ArrayList<>();
        int attribID;
        int tempAttribID;
        int parentID;
        int tempParentID = 0;
        String attribName = "";
        String attribHeader = "";
        int levelId;
        String status;
        NewOutletAttributeBO tempBO;
        for (NewOutletAttributeBO attributeBO : list) {
            tempBO = new NewOutletAttributeBO();
            attribID = attributeBO.getAttrId();
            status = attributeBO.getStatus();
            levelId = attributeBO.getLevelId();
            for (int i = childList.size() - 1; i >= 0; i--) {
                NewOutletAttributeBO attributeBO1 = childList.get(i);
                tempAttribID = attributeBO1.getAttrId();
                if (attribID == tempAttribID) {
                    attribName = attributeBO1.getAttrName();
                    tempParentID = attributeBO1.getParentId();
                    continue;
                }
                if (tempAttribID == tempParentID)
                    tempParentID = attributeBO1.getParentId();
            }

            for (NewOutletAttributeBO attributeBO2 : parentList) {
                parentID = attributeBO2.getAttrId();
                if (tempParentID == parentID)
                    attribHeader = attributeBO2.getAttrName();
            }
            tempBO.setAttrId(attribID);
            tempBO.setParentId(tempParentID);
            tempBO.setAttrName(attribName);
            tempBO.setAttrParent(attribHeader);
            tempBO.setStatus(status);
            tempBO.setLevelId(levelId);
            tempList.add(tempBO);
        }
        return tempList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public HashMap<String, String> getmPreviousProfileChangesList() {
        return mPreviousProfileChangesList;
    }

    private HashMap<String, String> mPreviousProfileChangesList;

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void getPreviousProfileChanges(String retailerid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        mPreviousProfileChangesList = new HashMap<>();
        try {
            db.openDataBase();
            Cursor c, headerCursor;

            String tid = "";
            String currentDate;
            currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);

            headerCursor = db
                    .selectSQL("SELECT Tid FROM RetailerEditHeader"
                            + " WHERE RetailerId = "
                            + bmodel.getRetailerMasterBO().getRetailerID() + " AND Date = "
                            + bmodel.QT(currentDate) + " AND Upload = " + bmodel.QT("N"));

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                tid = headerCursor.getString(0);
                headerCursor.close();

            }

            c = db.selectSQL("select code, value from RetailerEditDetail RED INNER JOIN RetailerEditHeader REH ON REH.tid=RED.tid where REH.retailerid=" + retailerid + " and REH.tid=" + bmodel.QT(tid));
            if (c != null) {
                while (c.moveToNext()) {
                    mPreviousProfileChangesList.put(c.getString(0), c.getString(1));
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public HashMap<String, NewOutletBO> getmNewRetailerById() {
        return mNewRetailerById;
    }

    private HashMap<String, NewOutletBO> mNewRetailerById;

    public ArrayList<NewOutletBO> getNewRetailers() {
        ArrayList<NewOutletBO> lst = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c;

            String query = "select distinct RM.RetailerID,RetailerName,subchannelid,beatid,visitDays ,locationid,creditlimit,RPTypeId,tinnumber,RField3," +
                    "distributorId,TaxTypeid,contractstatuslovid,classid,AccountId,RC1.contactname as contactName1,RC1.ContactName_LName as contactLName1,RC1.contactNumber as contactNumber1" +
                    ",RC1.contact_title as contact_title1,RC1.contact_title_lovid as contact_title_lovid1" +
                    ",RC2.contactname as contactName2,RC2.ContactName_LName as contactLName2,RC2.contactNumber as contactNumber2,RC2.contact_title as contact_title2,RC2.contact_title_lovid as contact_title_lovid2," +
                    "RA.address1,RA.address2,RA.address3,RA.City,RA.latitude,RA.longitude,RA.email,RA.FaxNo,RA.pincode,RA.State,RM.RField5,RM.RField6,RM.TinExpDate," +
                    "RM.pan_number,RM.food_licence_number,RM.food_licence_exp_date,RM.DLNo,RM.DLNoExpDate,RM.RField4,RM.RField7,RA.Mobile,RA.Region,RA.Country,RM.userid,RM.GSTNumber,RA.District" +
                    " from RetailerMaster RM LEFT JOIN RetailerContact RC1 ON Rm.retailerid=RC1.retailerId AND RC1.isprimary=1" +
                    " LEFT JOIN RetailerContact RC2 ON Rm.retailerid=RC2.retailerId AND RC2.isprimary=0" +
                    " LEFT JOIN RetailerAddress RA ON RA.RetailerId=RM.retailerId" +
                    " where RM.is_new='Y' and RM.upload='N' ";
            c = db.selectSQL(query);
            if (c != null) {
                if (c.getCount() > 0) {
                    mNewRetailerById = new HashMap<>();
                    NewOutletBO retailer;
                    while (c.moveToNext()) {
                        retailer = new NewOutletBO();
                        retailer.setRetailerId(c.getString(c.getColumnIndex("RetailerID")));
                        retailer.setOutletName(c.getString(c.getColumnIndex("RetailerName")));
                        retailer.setSubChannel(c.getInt(c.getColumnIndex("subchannelid")));
                        retailer.setRouteid(c.getInt(c.getColumnIndex("beatid")));
                        retailer.setVisitDays(c.getString(c.getColumnIndex("VisitDays")));
                        retailer.setLocid(c.getInt(c.getColumnIndex("locationid")));
                        retailer.setCreditLimit(c.getString(c.getColumnIndex("creditlimit")));
                        retailer.setPayment(c.getString(c.getColumnIndex("RPTypeId")));
                        retailer.setTinno(c.getString(c.getColumnIndex("tinnumber")));
                        retailer.setRfield3(c.getString(c.getColumnIndex("RField3")));
                        retailer.setDistid(c.getString(c.getColumnIndex("distributorid")));
                        retailer.setTaxTypeId(c.getString(c.getColumnIndex("TaxTypeId")));
                        retailer.setContractStatuslovid(c.getInt(c.getColumnIndex("contractstatuslovid")));
                        retailer.setClassTypeId(c.getString(c.getColumnIndex("classid")));

                        //from retailer contact
                        retailer.setContactpersonname(c.getString(c.getColumnIndex("contactName1")));
                        retailer.setContactpersonnameLastName(c.getString(c.getColumnIndex("contactLName1")));
                        retailer.setPhone(c.getString(c.getColumnIndex("contactNumber1")));
                        retailer.setContact1title(c.getString(c.getColumnIndex("contact_title1")));
                        retailer.setContact1titlelovid(c.getString(c.getColumnIndex("contact_title_lovid1")));

                        retailer.setContactpersonname2(c.getString(c.getColumnIndex("contactName2")));
                        retailer.setContactpersonname2LastName(c.getString(c.getColumnIndex("contactLName2")));
                        retailer.setPhone2(c.getString(c.getColumnIndex("contactNumber2")));
                        retailer.setContact2title(c.getString(c.getColumnIndex("contact_title2")));
                        retailer.setContact2titlelovid(c.getString(c.getColumnIndex("contact_title_lovid2")));

                        // from Retailer Address
                        retailer.setAddress(c.getString(c.getColumnIndex("Address1")));
                        retailer.setAddress2(c.getString(c.getColumnIndex("Address2")));
                        retailer.setAddress3(c.getString(c.getColumnIndex("Address3")));
                        retailer.setCity(c.getString(c.getColumnIndex("City")));
                        retailer.setNewOutletlattitude(c.getDouble(c.getColumnIndex("Latitude")));
                        retailer.setNewOutletLongitude(c.getDouble(c.getColumnIndex("Longitude")));
                        retailer.setEmail(c.getString(c.getColumnIndex("Email")));
                        retailer.setFax(c.getString(c.getColumnIndex("FaxNo")));
                        retailer.setPincode(c.getString(c.getColumnIndex("pincode")));
                        retailer.setState(c.getString(c.getColumnIndex("State")));
                        retailer.setRfield5(c.getString(c.getColumnIndex("RField5")));
                        retailer.setRfield6(c.getString(c.getColumnIndex("RField6")));
                        retailer.setTinExpDate(c.getString(c.getColumnIndex("TinExpDate")));
                        retailer.setPanNo(c.getString(c.getColumnIndex("pan_number")));
                        retailer.setFoodLicenseNo(c.getString(c.getColumnIndex("food_licence_number")));
                        retailer.setFlExpDate(c.getString(c.getColumnIndex("food_licence_exp_date")));
                        retailer.setDrugLicenseNo(c.getString(c.getColumnIndex("DLNo")));
                        retailer.setDlExpDate(c.getString(c.getColumnIndex("DLNoExpDate")));
                        retailer.setrField7(c.getString(c.getColumnIndex("RField7")));
                        retailer.setrField4(c.getString(c.getColumnIndex("RField4")));
                        retailer.setRegion(c.getString(c.getColumnIndex("Region")));
                        retailer.setCountry(c.getString(c.getColumnIndex("Country")));
                        retailer.setMobile(c.getString(c.getColumnIndex("Mobile")));
                        retailer.setUserId(c.getInt(c.getColumnIndex("userid")));
                        retailer.setGstNum(c.getString(c.getColumnIndex("GSTNumber")));
                        retailer.setDistrict(c.getString(c.getColumnIndex("District")));
                        retailer.setImageName(loadImgList(retailer.getRetailerId(), db));
                        retailer.setEditAttributeList(loadEditAttributes(retailer.getRetailerId(), db));
                        lst.add(retailer);
                        mNewRetailerById.put(retailer.getRetailerId(), retailer);
                    }
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
        return lst;
    }


    private ArrayList<String> loadImgList(String retailerID, DBUtil db) {
        ArrayList<String> imgList = new ArrayList<>();
        try {
            Cursor c1;
            String query = "Select ImageName from NewOutletImage where RetailerId=" + bmodel.QT(retailerID);
            c1 = db.selectSQL(query);
            if (c1 != null) {
                if (c1.getCount() > 0) {
                    String attrName = "";
                    while (c1.moveToNext()) {

                        attrName = c1.getString(0)
                                .substring(c1.getString(0)
                                        .indexOf("/NO") + 1, c1.getString(0).indexOf(".jpg"));
                        imgList.add(attrName);

                    }
                }
                c1.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imgList;
    }


    /*
    Load Edit Attributes list
     */
    private ArrayList<String> loadEditAttributes(String retailerID, DBUtil db) {
        ArrayList<String> attributeList = new ArrayList<>();
        try {
            Cursor c1;
            String query = "Select AttributeId from RetailerAttribute where RetailerId=" + bmodel.QT(retailerID);
            c1 = db.selectSQL(query);
            if (c1 != null) {
                if (c1.getCount() > 0) {
                    NewOutletAttributeBO attrBo;
                    while (c1.moveToNext()) {
                        attrBo = new NewOutletAttributeBO();
                        attrBo.setAttrId(c1.getInt(0));
                        attributeList.add(String.valueOf(attrBo.getAttrId()));
                    }
                }
                c1.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return attributeList;
    }


    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#deleteQuery(String, String)}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    private void deleteQuery(String query, String rid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_RetailerEditDetail, " Code =" + bmodel.QT(query) + "and RetailerId=" + rid, false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void deleteRetailerEdit(String rid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_retailerMaster, " RetailerId=" + bmodel.QT(rid), false);
            db.deleteSQL("RetailerContact", " RetailerId=" + bmodel.QT(rid), false);
            db.deleteSQL("RetailerAddress", " RetailerId=" + bmodel.QT(rid), false);

            if (selectQuery("NewOutletImage", rid))
                db.deleteSQL("NewOutletImage", " RetailerId=" + bmodel.QT(rid), false);
//                db.selectSQL("delete from NewOutletImage where RetailerId='" + rid+"'");

            else if (selectQuery("NearByRetailers", rid))
                db.deleteSQL("NearByRetailers", " rid=" + bmodel.QT(rid), false);
                //db.selectSQL("delete from NearByRetailers where RetailerId='" + rid+"'");

            else if (selectQuery("RetailerPriorityProducts", rid))
                db.deleteSQL("RetailerPriorityProducts", " RetailerId=" + bmodel.QT(rid), false);
            // db.selectSQL("delete from RetailerPriorityProducts where rid='" + rid+"'");

            if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER) {

                if (selectQuery("OrderHeaderRequest", rid))
                    db.deleteSQL("OrderHeaderRequest", " RetailerId=" + bmodel.QT(rid), false);

                if (selectQuery("OrderDetailRequest", rid))
                    db.deleteSQL("OrderDetailRequest", " RetailerId=" + bmodel.QT(rid), false);
            }

            if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR) {

                if (selectQuery("RetailerPotential", rid))
                    db.deleteSQL("RetailerPotential", " Rid=" + bmodel.QT(rid), false);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private boolean selectQuery(String tblName, String retId) {

        try {
            String rid = tblName.equals("NearByRetailers") ? "rid" : "RetailerId";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL("Select * from " + tblName
                    + " where " + rid + "='" + retId + "'");

            if (c.getCount() > 0) {
                c.close();
                return true;
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            return false;
        }
        return false;
    }


    /**
     * Download configuration fields for new retailer
     * <p>
     * return
     */
    public void loadNewOutletConfiguration(int channelid) {
        ConfigureBO ConfigureBO;

        try {
            profileConfig = new Vector<>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c;
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            StringBuilder sb = new StringBuilder();
            sb.append("select HHTCode, flag , MName , MNumber ,RField,haslink,RField6,Regex from HhtMenuMaster where MenuType= 'MENU_NEW_RET'");
            sb.append("  and Flag = 1 and lang=");
            sb.append(bmodel.QT(language));
            if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                sb.append(" and subchannelid=");
                sb.append(channelid);
                sb.append(" ");
            }
            sb.append(" order by Mnumber");
            c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    ConfigureBO = new ConfigureBO();
                    ConfigureBO.setConfigCode(c.getString(0));
                    ConfigureBO.setFlag(c.getInt(1));
                    ConfigureBO.setMenuName(c.getString(2));
                    ConfigureBO.setMenuNumber((c.getString(3)));
                    ConfigureBO.setMandatory((c.getInt(4)));
                    ConfigureBO.setHasLink(c.getInt(5));
                    ConfigureBO.setRField6(String.valueOf(c.getInt(6)));
                    String str = c.getString(7);
                    if (str != null && !str.isEmpty()) {
                        if (str.contains("<") && str.contains(">")) {

                            String minlen = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                            if (!minlen.isEmpty()) {
                                try {
                                    ConfigureBO.setMaxLengthNo(SDUtil.convertToInt(minlen));
                                } catch (Exception ex) {
                                    Commons.printException("min len in new outlet helper", ex);
                                }
                            }
                        }
                    }
                    ConfigureBO.setRegex(c.getString(7));
                    profileConfig.add(ConfigureBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public Vector<ConfigureBO> getProfileConfiguraion() {
        return profileConfig;

    }

    public void loadImageType() {
        imageTypeList.clear();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_IMAGE_TYPE'");
            if (c.getCount() > 0) {
                setImageTypeList(new Vector<NewOutletBO>());
                while (c.moveToNext()) {
                    imageType = new NewOutletBO();
                    imageType.setListId(c.getInt(0));
                    imageType.setListName(c.getString(2));
                    getImageTypeList().add(imageType);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @author rajesh.k
     * Method to use download location for new retailer creation
     */
    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void downloadLocationMaster() {

        mLocationListByLevelId = new LinkedHashMap<>();

        LocationBO locationBo;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select LM.LocId,LM.LocCode,LM.LocName,LM.LocParentId,LL.id  from LocationMaster LM " +
                    "inner join  (select distinct id from LocationLevel LL1 inner join LocationMaster LM1" +
                    " on  LL1.id=LM1.LocLevelId order by Sequence desc  limit 3) LL " +
                    "on LL.id=LM.LocLevelId";
            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    ArrayList<LocationBO> locationList = new ArrayList<>();
                    int levelId = 0;
                    while (c.moveToNext()) {
                        locationBo = new LocationBO();
                        locationBo.setLocId(c.getInt(0));
                        locationBo.setLocCode(c.getString(1));
                        locationBo.setLocName(c.getString(2));
                        locationBo.setParentId(c.getInt(3));

                        if (levelId != c.getInt(4)) {
                            if (levelId != 0) {
                                mLocationListByLevelId.put(levelId, locationList);
                                locationList = new ArrayList<>();
                                locationList.add(locationBo);
                                levelId = c.getInt(4);

                            } else {
                                locationList.add(locationBo);
                                levelId = c.getInt(4);
                            }
                        } else {
                            locationList.add(locationBo);
                        }
                    }
                    if (locationList.size() > 0) {
                        mLocationListByLevelId.put(levelId, locationList);
                    }
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException("" + e);

        } finally {
            db.closeDB();
        }

    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public LinkedHashMap<Integer, ArrayList<LocationBO>> getLocationListByLevId() {
        return mLocationListByLevelId;
    }

    public boolean isRetailerAlreadyAvailable(String retailerName, String pincode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT A.pincode FROM  RetailerAddress A"
                            + " WHERE A.RetailerId IN (SELECT RetailerId FROM RetailerMaster WHERE RetailerName = '" + retailerName + "')"
                            + " AND A.pincode = '" + pincode + "'");
            if (c.getCount() > 0) {
                c.close();
                return true;
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            return false;
        }
        return false;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#getNearbyRetailerIds(String)}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<String> getNearbyRetailerIds(String retailerId) {
        ArrayList<String> lst = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT nearbyrid from NearByRetailers where rid='" + retailerId + "' and upload='Y'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    lst.add(c.getString(0));
                }

            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);

        }
        return lst;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#getNearbyRetailersEditRequest}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public HashMap<String, String> getLstEditRequests() {
        return lstEditRequests;
    }

    private final HashMap<String, String> lstEditRequests = new HashMap<>();

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#getNearbyRetailersEditRequest}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void getNearbyRetailersEditRequest(int retailerId) {
        lstEditRequests.clear();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT nearbyrid,status from RrtNearByEditRequest where rid=" + retailerId + " and upload='N'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    lstEditRequests.put(c.getString(0), c.getString(1));
                }

            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public ArrayList<StandardListBO> downloadTaxType() {
        ArrayList<StandardListBO> taxTypeList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sb = "select listid,listname from standardlistmaster where listtype='CERTIFICATE_TYPE'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    taxTypeList.add(standardListBO);
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return taxTypeList;

    }

    public ArrayList<StandardListBO> downloadClaasType() {
        ArrayList<StandardListBO> classTypeList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sb = "select listid,listname from standardlistmaster where listtype='CLASS_TYPE'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    classTypeList.add(standardListBO);
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return classTypeList;

    }

    public ArrayList<StandardListBO> downloadPriorityProducts() {
        ArrayList<StandardListBO> priorityproductList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sb = "select  priorityproductid,pname,ProductLevelId from PriorityProducts  pp inner join productmaster pm " +
                    " on pm.pid=pp.priorityproductid  and pm.plid=pp.productlevelid";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                priorityproductList = new ArrayList<>();
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    standardListBO.setListCode(c.getString(2));
                    priorityproductList.add(standardListBO);
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return priorityproductList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadPriorityProductsForRetailerUpdate}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<String> downloadPriorityProductsForRetailer(String retailerId) {
        ArrayList<String> priorityproductList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + bmodel.QT(retailerId);
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                priorityproductList = new ArrayList<>();
                while (c.moveToNext()) {
                    priorityproductList.add(c.getString(0));
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return priorityproductList;
    }

    public ArrayList<String> downloadPriorityProductsForRetailerEdit(String retailerId) {
        ArrayList<String> priorityproductList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sql = "select ProductId from RetailerEditPriorityProducts where status = 'N' and retailerId=" + bmodel.QT(retailerId);
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                priorityproductList = new ArrayList<>();
                while (c.moveToNext()) {
                    priorityproductList.add(c.getString(0));
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return priorityproductList;
    }

    public ArrayList<String> downloadNearbyRetailers(String retailerId) {
        ArrayList<String> nearByRetailers = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sql = "select  nearbyrid from NearByRetailers where rid=" + bmodel.QT(retailerId);
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                nearByRetailers = new ArrayList<>();
                while (c.moveToNext()) {
                    nearByRetailers.add(c.getString(0));
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return nearByRetailers;
    }


    public String getListName(int listid, String type) {
        String name = "";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sb = "select listname from standardlistmaster " +
                    "where listtype=" + bmodel.QT(type) + " and listid=" + listid;
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    name = c.getString(0);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return name;
    }

    public String getLevelame() {
        return levelame;
    }

    private String levelame = "";

    public ArrayList<ChannelBO> getChannelList() {
        ArrayList<ChannelBO> channelList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String sql = "SELECT ChName,ChId,cl.levelname FROM ChannelHierarchy ch"
                    + " inner join channellevel cl on cl.levelid=ch.levelid WHERE ch.LevelId = (SELECT LevelId FROM ChannelLevel WHERE Sequence = (SELECT rfield FROM HhtModuleMaster WHERE hhtCode= 'FUN36' AND flag = 1 AND ForSwitchSeller = 0))";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                ChannelBO channelBO;
                while (c.moveToNext()) {
                    channelBO = new ChannelBO();
                    channelBO.setChannelId(c.getInt(1));
                    channelBO.setChannelName(c.getString(0));
                    levelame = c.getString(2);
                    channelList.add(channelBO);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return channelList;
    }

    private void savePriorityProducts(NewOutletBO newOutletBO) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String columns = "RetailerId,ProductId,LevelId,upload";
            StringBuffer sb;
            final ArrayList<StandardListBO> productIdList = newOutletBO.getPriorityProductList();
            if (productIdList != null) {
                for (StandardListBO bo : productIdList) {
                    sb = new StringBuffer();
                    sb.append(bmodel.QT(id));
                    sb.append(",");
                    sb.append(bo.getListID());
                    sb.append(",");
                    sb.append(bo.getListCode());
                    sb.append(",'N'");
                    db.insertSQL("RetailerPriorityProducts", columns, sb.toString());

                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    private NewOutletBO newoutlet;

    public NewOutletBO getNewoutlet() {
        return newoutlet;
    }

    public void setNewoutlet(NewOutletBO newoutlet) {
        this.newoutlet = newoutlet;
    }

    public boolean saveNewOutlet(boolean isEdit) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String column, value;

            NewOutletBO outlet = getNewoutlet();

            if (isEdit) {

                Cursor getCpidCursor = db.selectSQL("Select CPId from RetailerContact where retailerId=" + StringUtils.QT(outlet.getRetailerId()));

                if (getCpidCursor != null && getCpidCursor.getCount() > 0) {
                    while (getCpidCursor.moveToNext()) {
                        db.deleteSQL("ContactAvailability", "CPId=" + getCpidCursor.getString(0), false);
                    }
                }
                db.deleteSQL("RetailerMaster", "retailerId=" + StringUtils.QT(outlet.getRetailerId()), false);
                db.deleteSQL("RetailerContact", "retailerId=" + StringUtils.QT(outlet.getRetailerId()), false);
                db.deleteSQL("RetailerAddress", "retailerId=" + StringUtils.QT(outlet.getRetailerId()), false);
                db.deleteSQL("RetailerAttribute", "retailerId=" + StringUtils.QT(outlet.getRetailerId()), false);
                db.updateSQL("Update NewRetailerSurveyResultHeader set retailerID = '" + getId() + "' where retailerID = '" + outlet.getRetailerId() + "'");
                db.updateSQL("Update NewRetailerSurveyResultDetail set retailerID = '" + getId() + "' where retailerID = '" + outlet.getRetailerId() + "'");
            } else {
                // edit option not allowed for image
                for (int i = 0; i < outlet.ImageId.size(); i++) {

                    column = "RetailerID,ListId,ImageName,upload";

                    value = QT(getId())
                            + ","
                            + outlet.ImageId.get(i)
                            + ","
                            + QT("/RetailerImages/"
                            + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "/"
                            + (outlet.ImageName.get(i))) + "," + QT("N");

                    db.insertSQL("NewOutletImage", column, value);
                }
            }

            if (bmodel.getNearByRetailers() != null && bmodel.getNearByRetailers().size() > 0) {
                bmodel.saveNearByRetailers(getId());
            }

            if (outlet.getPriorityProductList() != null
                    && outlet.getPriorityProductList().size() > 0) {
                savePriorityProducts(outlet);
            }

            column = "RetailerID,RetailerName,channelID,subchannelid,beatid,"
                    + DataMembers.VISIT_DAYS_COLUMN_NAME + ",LocationId," +
                    "creditlimit,RPTypeId,tinnumber,RField3,distributorId,TaxTypeid," +
                    "contractstatuslovid,classid,AccountId,is_new,Upload,creditPeriod,inSEZ,GSTnumber,RField5,RField6,TinExpDate," +
                    "pan_number,food_licence_number,food_licence_exp_date,DLNo,DLNoExpDate,RField4,RField7,userid";

            int userid = getNewoutlet().getUserId();
            if (userid == 0)
                userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            value = QT(getId())
                    + "," + QT(outlet.getOutletName())
                    + "," + outlet.getChannel()
                    + "," + outlet.getSubChannel()
                    + "," + QT(outlet.getRouteid() + "")
                    + "," + QT(outlet.getVisitDays())
                    + "," + getNewoutlet().getLocid()
                    + "," + getNewoutlet().getCreditLimit()
                    + "," + getNewoutlet().getPayment()
                    + "," + QT(getNewoutlet().getTinno())
                    + "," + QT(getNewoutlet().getRfield3())
                    + "," + QT(getNewoutlet().getDistid())
                    + "," + QT(getNewoutlet().getTaxTypeId())
                    + "," + getNewoutlet().getContractStatuslovid()
                    + "," + getNewoutlet().getClassTypeId()
                    + "," + 0
                    + "," + QT("Y")
                    + "," + QT("N")
                    + "," + getNewoutlet().getCreditDays()
                    + "," + getNewoutlet().getIsSEZ()
                    + "," + QT(getNewoutlet().getGstNum())
                    + "," + QT(getNewoutlet().getRfield5())
                    + "," + QT(getNewoutlet().getRfield6())
                    + "," + (getNewoutlet().getTinExpDate().isEmpty() ? null : QT(getNewoutlet().getTinExpDate()))
                    + "," + QT(getNewoutlet().getPanNo())
                    + "," + QT(getNewoutlet().getFoodLicenseNo())
                    + "," + (getNewoutlet().getFlExpDate().isEmpty() ? null : QT(getNewoutlet().getFlExpDate()))
                    + "," + QT(getNewoutlet().getDrugLicenseNo())
                    + "," + (getNewoutlet().getDlExpDate().isEmpty() ? null : QT(getNewoutlet().getDlExpDate()))
                    + "," + QT(getNewoutlet().getrField4())
                    + "," + QT(getNewoutlet().getrField7())
                    + "," + QT(userid + "");


            db.insertSQL("RetailerMaster", column, value);
            bmodel.setNewlyaddedRetailer(getId());

            column = "RetailerID,contactname,ContactName_LName,contactNumber," +
                    "contact_title,contact_title_lovid,IsPrimary,Email,Upload,salutationLovId,IsEmailNotificationReq,CPID";

            if (bmodel.configurationMasterHelper.IS_CONTACT_TAB) {
                if (retailerContactList != null && retailerContactList.size() > 0) {
                    for (RetailerContactBo retailerContactBo : retailerContactList) {

                        value = QT(getId())
                                + "," + QT(retailerContactBo.getFistname())
                                + "," + QT(retailerContactBo.getLastname())
                                + "," + QT(retailerContactBo.getContactNumber())
                                + "," + QT(retailerContactBo.getTitle())
                                + "," + QT(retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1") ? "0" : retailerContactBo.getContactTitleLovId())
                                + "," + retailerContactBo.getIsPrimary()
                                + "," + QT(retailerContactBo.getContactMail())
                                + "," + QT("N")
                                + "," + QT(retailerContactBo.getContactSalutationId())
                                + "," + QT(retailerContactBo.getIsEmailPrimary()+"")
                                + "," +QT(retailerContactBo.getCpId());
                        db.insertSQL("RetailerContact", column, value);

                        if (retailerContactBo.getContactAvailList().size() > 0)
                            addContactAvail(db,retailerContactBo);
                    }
                    retailerContactList.clear();
                }
            }else{
                if (outlet.getContactpersonname() != null && !outlet.getContactpersonname().trim().equals("")) {
                    value = QT(getId())
                            + "," + QT(outlet.getContactpersonname())
                            + "," + QT(getNewoutlet().getContactpersonnameLastName())
                            + "," + QT(outlet.getPhone())
                            + "," + QT(getNewoutlet().getContact1title())
                            + "," + getNewoutlet().getContact1titlelovid()
                            + "," + 1
                            + "," + QT("")
                            + "," + QT("N")
                            + "," + QT("")
                            + "," + QT("")
                            + "," +QT("");
                    db.insertSQL("RetailerContact", column, value);
                }
                if (outlet.getContactpersonname2() != null && !outlet.getContactpersonname2().trim().equals("")) {
                    value = QT(getId())
                            + "," + QT(outlet.getContactpersonname2())
                            + "," + QT(getNewoutlet().getContactpersonname2LastName())
                            + "," + QT(outlet.getPhone2())
                            + "," + QT(getNewoutlet().getContact2title())
                            + "," + getNewoutlet().getContact2titlelovid()
                            + "," + 0
                            + "," + QT("")
                            + "," + QT("N")
                            + "," + QT("")
                            + "," + QT("")
                            + "," +QT("");
                    db.insertSQL("RetailerContact", column, value);
                }
            }


            column = "RetailerID,Address1,Address2,Address3,ContactNumber,City,latitude,longitude,"
                    + "email,FaxNo,pincode,State,Upload,IsPrimary,AddressTypeID,Region,Country,Mobile,District";

            //converting big decimal value while Exponential value occur
            String lattitude = (outlet.getNewOutletlattitude() + "").contains("E")
                    ? (SDUtil.truncateDecimal(outlet.getNewOutletlattitude(), -1) + "").substring(0, 20)
                    : ((outlet.getNewOutletlattitude() + "").length() > 20
                    ? (outlet.getNewOutletlattitude() + "").substring(0, 20)
                    : (outlet.getNewOutletlattitude() + ""));

            String longitude = (outlet.getNewOutletLongitude() + "").contains("E")
                    ? (SDUtil.truncateDecimal(outlet.getNewOutletLongitude(), -1) + "").substring(0, 20)
                    : ((outlet.getNewOutletLongitude() + "").length() > 20
                    ? (outlet.getNewOutletLongitude() + "").substring(0, 20)
                    : (outlet.getNewOutletLongitude() + ""));

            if (outlet.getmAddressByTag() != null) {
                for (String addressType : outlet.getmAddressByTag().keySet()) {
                    AddressBO addressBO = outlet.getmAddressByTag().get(addressType);
                    value = QT(getId())
                            + "," + QT(addressBO.getAddress1())
                            + "," + QT(addressBO.getAddress2())
                            + "," + QT(addressBO.getAddress3())
                            + "," + QT(addressBO.getPhone())
                            + "," + QT(addressBO.getCity())
                            + "," + QT(lattitude)
                            + "," + QT(longitude)
                            + "," + QT(addressBO.getEmail())
                            + "," + QT(addressBO.getFax())
                            + "," + QT(addressBO.getPincode())
                            + "," + QT(addressBO.getState())
                            + "," + QT("N")
                            + "," + 1
                            + "," + addressType
                            + "," + QT(getNewoutlet().getRegion())
                            + "," + QT(getNewoutlet().getCountry())
                            + "," + QT(getNewoutlet().getMobile())
                            + "," + QT(getNewoutlet().getDistrict());


                    db.insertSQL("RetailerAddress", column, value);
                }
            } else {

                value = QT(getId())
                        + "," + QT(outlet.getAddress())
                        + "," + QT(outlet.getAddress2())
                        + "," + QT(outlet.getAddress3())
                        + "," + QT(outlet.getPhone())
                        + "," + QT(outlet.getCity())
                        + "," + QT(lattitude)
                        + "," + QT(longitude)
                        + "," + QT(getNewoutlet().getEmail())
                        + "," + QT(getNewoutlet().getFax())
                        + "," + QT(getNewoutlet().getPincode())
                        + "," + QT(getNewoutlet().getState())
                        + "," + QT("N")
                        + "," + 1
                        + "," + 0
                        + "," + QT(getNewoutlet().getRegion())
                        + "," + QT(getNewoutlet().getCountry())
                        + "," + QT(getNewoutlet().getMobile())
                        + "," + QT(getNewoutlet().getDistrict());

                db.insertSQL("RetailerAddress", column, value);

            }

            column = "RetailerId, AttributeId, LevelId, Upload";
            for (NewOutletAttributeBO attributeBO : outlet.getAttributeList()) {
                value = QT(getId())
                        + "," + attributeBO.getAttrId()
                        + "," + attributeBO.getLevelId()
                        + "," + QT("N");
                db.insertSQL("RetailerAttribute", column, value);
            }

            if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER && getOrderedProductList().size() > 0) {
                String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
                String uid = QT(id);

                // Commented after discussing it with Abbas.
                // if (bmodel.configurationMasterHelper.SHOW_ORDER_SEQUENCE_NO) {
                //   bmodel.insertSeqNumber("ORD");
                //   uid = QT(bmodel.downloadSequenceNo("ORD"));
                // }

                column = "OrderID, OrderDate, RetailerID, DistributorId, OrderValue,LinesPerCall,TotalWeight,Remarks,OrderTime";
                value = uid
                        + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                        + "," + QT(getId())
                        + "," + bmodel.userMasterHelper.getUserMasterBO().getDistributorid()
                        + "," + bmodel.getOrderHeaderBO().getOrderValue()
                        + "," + bmodel.getOrderHeaderBO().getLinesPerCall()
                        + "," + bmodel.getOrderHeaderBO().getTotalWeight()
                        + "," + QT(bmodel.getOrderHeaderNote())
                        + "," + QT(DateTimeUtils.now(DateTimeUtils.TIME));
                db.insertSQL("OrderHeaderRequest", column, value);

                column = "OrderID, ProductID, Qty,uomid,Price,LineValue, Weight,uomcount,HsnCode,RetailerID";
                for (ProductMasterBO productMasterBO : getOrderedProductList()) {
                    if (productMasterBO.getOrderedPcsQty() > 0) {
                        value = uid
                                + "," + QT(productMasterBO.getProductID())
                                + "," + productMasterBO.getOrderedPcsQty()
                                + "," + productMasterBO.getPcUomid()
                                + "," + productMasterBO.getSrp()
                                + "," + productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                + "," + productMasterBO.getOrderedPcsQty() * productMasterBO.getWeight()
                                + ",1"
                                + "," + QT(productMasterBO.getHsnCode())
                                + "," + QT(getId());
                        db.insertSQL("OrderDetailRequest", column, value);
                    }
                    if (productMasterBO.getOrderedCaseQty() > 0) {
                        value = uid
                                + "," + QT(productMasterBO.getProductID())
                                + "," + productMasterBO.getOrderedCaseQty()
                                + "," + productMasterBO.getCaseUomId()
                                + "," + productMasterBO.getCsrp()
                                + "," + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                + "," + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()) * productMasterBO.getWeight()
                                + "," + productMasterBO.getCaseSize()
                                + "," + QT(productMasterBO.getHsnCode())
                                + "," + QT(getId());
                        db.insertSQL("OrderDetailRequest", column, value);
                    }
                    if (productMasterBO.getOrderedOuterQty() > 0) {
                        value = uid
                                + "," + QT(productMasterBO.getProductID())
                                + "," + productMasterBO.getOrderedOuterQty()
                                + "," + productMasterBO.getOuUomid()
                                + "," + productMasterBO.getOsrp()
                                + "," + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp()
                                + "," + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize()) * productMasterBO.getWeight()
                                + "," + productMasterBO.getOutersize()
                                + "," + QT(productMasterBO.getHsnCode())
                                + "," + QT(getId());
                        db.insertSQL("OrderDetailRequest", column, value);
                    }
                }
            }
            if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR && getOpprProductList().size() > 0) {
                column = "rid, pid, facing,IsOwn,Price";
                for (ProductMasterBO productMasterBO : getOpprProductList()) {

                    value = QT(getId())
                            + "," + QT(productMasterBO.getProductID())
                            + "," + productMasterBO.getQty_klgs()
                            + "," + productMasterBO.getOwn()
                            + "," + productMasterBO.getOrderPricePiece();
                    db.insertSQL("RetailerPotential", column, value);
                }
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            return false;
        }

        return true;
    }

    private void addContactAvail(DBUtil db, RetailerContactBo retailerContactBo){
        String column = "CPAId,CPId,Day,StartTime,EndTime,isLocal,upload,retailerID";

        for (RetailerContactAvailBo retailerContactAvailBo : retailerContactBo.getContactAvailList()) {

            String value = QT(getId())
                    + "," + QT(retailerContactBo.getCpId())
                    + "," + QT(retailerContactAvailBo.getDay())
                    + "," + QT(retailerContactAvailBo.getFrom())
                    + "," + QT(retailerContactAvailBo.getTo())
                    + "," + QT("1")
                    + "," + QT("N")
                    + "," + QT(getId());

            db.insertSQL("ContactAvailability", column, value);
        }
    }

    public String getRetailerId_edit() {
        return retailerId_edit;
    }

    public void setRetailerId_edit(String retailerId_edit) {
        this.retailerId_edit = retailerId_edit;
    }

    private String retailerId_edit;

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#getLinkRetailer()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void downloadLinkRetailer() {
        try {
            mLinkRetailerList = new Vector<>();
            mLinkRetailerListByDistributorId = new SparseArray<>();

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sb = "select Distributorid ,retailerid,name,latitude,longitude,pincode from linkretailermaster " +
                    "order by Distributorid ";
            Cursor c = db.selectSQL(sb);

            Vector<RetailerMasterBO> linkRetailerList = new Vector<>();
            int distributorId = 0;
            RetailerMasterBO linkRetailerBO;

            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    linkRetailerBO = new RetailerMasterBO();
                    linkRetailerBO.setDistributorId(c.getInt(0));
                    linkRetailerBO.setRetailerID(c.getString(1));
                    linkRetailerBO.setRetailerName(c.getString(2));
                    linkRetailerBO.setLatitude(c.getDouble(3));
                    linkRetailerBO.setLongitude(c.getDouble(4));
                    linkRetailerBO.setPincode(c.getString(5));
                    mLinkRetailerList.add(linkRetailerBO);

                    if (distributorId != linkRetailerBO.getDistributorId()) {
                        if (distributorId != 0) {
                            mLinkRetailerListByDistributorId.put(distributorId, linkRetailerList);
                            linkRetailerList = new Vector<>();
                            linkRetailerList.add(linkRetailerBO);
                            distributorId = linkRetailerBO.getDistributorId();

                        } else {
                            linkRetailerList.add(linkRetailerBO);
                            distributorId = linkRetailerBO.getDistributorId();
                        }
                    } else {
                        linkRetailerList.add(linkRetailerBO);
                    }
                }


                if (linkRetailerList.size() > 0) {
                    mLinkRetailerListByDistributorId.put(distributorId, linkRetailerList);
                }
            }
            c.close();
            db.closeDB();


        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#getLinkRetailer()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public SparseArray<Vector<RetailerMasterBO>> getLinkRetailerListByDistributorId() {
        return mLinkRetailerListByDistributorId;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#getLinkRetailer()}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public Vector<RetailerMasterBO> getLinkRetailerList() {
        return mLinkRetailerList;
    }

    public String getmSelectedChannelname() {
        return mSelectedChannelname;
    }

    public void setmSelectedChannelname(String mSelectedChannelname) {
        this.mSelectedChannelname = mSelectedChannelname;
    }

    public ArrayList<String> getRetialerIds(String suppilerID) {
        ArrayList<String> mRetailerIds = new ArrayList<>();
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb = "select distinct rid from Suppliermaster " +
                    "where sid=" + QT(suppilerID);

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mRetailerIds.add(c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
            return new ArrayList<>();
        }

        return mRetailerIds;
    }

    public ArrayList<StandardListBO> getAddressTypes() {
        ArrayList<StandardListBO> mLst = new ArrayList<>();
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb = "select listid,listname from StandardListMaster " +
                    "where listtype='ADDRESS_TYPE'";

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                StandardListBO bo;
                while (c.moveToNext()) {
                    bo = new StandardListBO();
                    bo.setListID(c.getString(0));
                    bo.setListName(c.getString(1));
                    mLst.add(bo);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
            return new ArrayList<>();
        }

        return mLst;
    }

    public HashMap<String, AddressBO> downloadRetailerAddress() {
        HashMap<String, AddressBO> lst = new HashMap<>();
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            String sb = "select distinct Address1,Address2,Address3,ContactNumber,City,latitude,longitude,email,FaxNo,pincode,State,AddressTypeID from RetailerAddress";
         /*   sb.append("LEFT JOIN StandardListMaster SM ON RA.AddressTypeID=SM.listid");
            sb.append("where listtype='ADDRESS_TYPE'");*/

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                AddressBO bo;
                while (c.moveToNext()) {
                    bo = new AddressBO();
                    bo.setAddress1(c.getString(0));
                    bo.setAddress2(c.getString(1));
                    bo.setAddress3(c.getString(2));
                    bo.setPhone(c.getString(3));
                    bo.setCity(c.getString(4));
                    bo.setNewOutletlattitude(c.getDouble(5));
                    bo.setNewOutletLongitude(c.getDouble(6));
                    bo.setEmail(c.getString(7));
                    bo.setFax(c.getString(8));
                    bo.setPincode(c.getString(9));
                    bo.setState(c.getString(10));
                    bo.setAddressTypeId(c.getInt(11));

                    lst.put(bo.getAddressTypeId() + "", bo);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
            return new HashMap<>();
        }

        return lst;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#downloadRetailerFlexValues}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<RetailerFlexBO> downloadRetailerFlexValues(String type) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        ArrayList<RetailerFlexBO> flexValues = new ArrayList<>();
        try {
            db.openDataBase();
            String sql = "select id,name from RetailerFlexValues where type = " + bmodel.QT(type);
            Cursor c = db.selectSQL(sql);
            RetailerFlexBO retailerFlexBO;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    retailerFlexBO = new RetailerFlexBO();
                    retailerFlexBO.setId(c.getString(0));
                    retailerFlexBO.setName(c.getString(1));
                    flexValues.add(retailerFlexBO);
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
        return flexValues;
    }

    private ArrayList<RetailerContactBo> retailerContactList;

    public ArrayList<RetailerContactBo> getRetailerContactList() {
        if (retailerContactList == null)
            return new ArrayList<>();
        return retailerContactList;
    }

    public void setRetailerContactList(ArrayList<RetailerContactBo> retailerContactList) {
        this.retailerContactList = retailerContactList;
    }

    public ArrayList<StandardListBO> downlaodContactTitle() {
        StandardListBO contactTitle;
        ArrayList<StandardListBO> contactTitleList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTACT_TITLE_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactTitle = new StandardListBO();
                    contactTitle.setListID(c.getString(0));
                    contactTitle.setListName(c.getString(2));
                    contactTitleList.add(contactTitle);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return contactTitleList;
    }

    public ArrayList<StandardListBO> downlaodContactSalutation() {
        StandardListBO contactTitle;
        ArrayList<StandardListBO> contactTitleList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='CONTACT_SALUTATION'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactTitle = new StandardListBO();
                    contactTitle.setListID(c.getString(0));
                    contactTitle.setListName(c.getString(2));
                    contactTitleList.add(contactTitle);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return contactTitleList;
    }

    public ArrayList<CensusLocationBO> getCensusLocationList() {
        return censusLocationList;
    }

    public void setCensusLocationList(ArrayList<CensusLocationBO> censusLocationList) {
        this.censusLocationList = censusLocationList;
    }

    public void downloadLocationLevels() {
        int pincodeLevel = 0;
        ArrayList<CensusLocationBO> pincodeList = new ArrayList<>();
        ArrayList<CensusLocationBO> pincodeTempList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor cursor = db.selectSQL("select LevelId from CensusLocationLevel  where sequence = (select MAX(Sequence) from censuslocationlevel)");
            if (cursor.getCount() > 0) {
                if (cursor.moveToNext())
                    pincodeLevel = cursor.getInt(0);
            }
            cursor = db.selectSQL("select id,name,levelid,parentid,pincode from CensusLocationMaster where levelid =" + pincodeLevel);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CensusLocationBO censusBO = new CensusLocationBO();
                    censusBO.setId(cursor.getString(0));
                    censusBO.setLocationName(cursor.getString(1));
                    censusBO.setLevelId(cursor.getString(2));
                    censusBO.setParentId(cursor.getString(3));
                    censusBO.setPincode(cursor.getString(4));
                    pincodeList.add(censusBO);
                }
            }
            cursor = db.selectSQL("select id,name,levelid,parentid,(select sequence from CensusLocationLevel where levelid = A.levelid)" +
                    " as sequence from CensusLocationMaster A where levelid !=" + pincodeLevel + " order by sequence desc");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CensusLocationBO censusBO = new CensusLocationBO();
                    censusBO.setId(cursor.getString(0));
                    censusBO.setLocationName(cursor.getString(1));
                    censusBO.setLevelId(cursor.getString(2));
                    censusBO.setParentId(cursor.getString(3));
                    pincodeTempList.add(censusBO);
                }
            }

            prepareCensusLocationData(pincodeList, pincodeTempList);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void prepareCensusLocationData(ArrayList<CensusLocationBO> pincodeList, ArrayList<CensusLocationBO> tempList) {
        String districtId;
        String stateId;
        String countryId;
        for (CensusLocationBO pincodeBO : pincodeList) {
            stateId = "";
            countryId = "";
            districtId = pincodeBO.getParentId();
            for (CensusLocationBO tempBO : tempList) {
                if (districtId.equals(tempBO.getId())) {
                    pincodeBO.setDistrict(tempBO.getLocationName());
                    stateId = tempBO.getParentId();
                } else if (stateId.equals(tempBO.getId())) {
                    pincodeBO.setState(tempBO.getLocationName());
                    countryId = tempBO.getParentId();
                } else if (countryId.equals(tempBO.getId())) {
                    pincodeBO.setCountry(tempBO.getLocationName());
                    break;
                }
            }
        }

        setCensusLocationList(pincodeList);
    }
}
