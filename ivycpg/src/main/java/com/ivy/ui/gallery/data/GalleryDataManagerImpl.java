package com.ivy.ui.gallery.data;

import android.database.Cursor;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.ivy.sd.png.util.DataMembers.tbl_AnswerDetail;
import static com.ivy.sd.png.util.DataMembers.tbl_AnswerHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_AnswerImageDetail;
import static com.ivy.sd.png.util.DataMembers.tbl_AssetHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_AssetImgInfo;
import static com.ivy.sd.png.util.DataMembers.tbl_CompetitorDetails;
import static com.ivy.sd.png.util.DataMembers.tbl_CompetitorHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_PlanogramHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_PosmMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_ProductMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_PromotionDetail;
import static com.ivy.sd.png.util.DataMembers.tbl_PromotionHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_QuestionMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_SOD_Asset_Tracking_Detail;
import static com.ivy.sd.png.util.DataMembers.tbl_SOD_Asset_Tracking_Header;
import static com.ivy.sd.png.util.DataMembers.tbl_SOSKU_Tracking_Detail;
import static com.ivy.sd.png.util.DataMembers.tbl_SOSKU_Tracking_Header;
import static com.ivy.sd.png.util.DataMembers.tbl_SOS_Tracking_Detail;
import static com.ivy.sd.png.util.DataMembers.tbl_SOS_Tracking_Header;
import static com.ivy.sd.png.util.DataMembers.tbl_SerializedAssetHeader;
import static com.ivy.sd.png.util.DataMembers.tbl_SerializedAssetImageDetail;
import static com.ivy.sd.png.util.DataMembers.tbl_SerializedAssetMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_SurveyMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_TaskConfigurationMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_TaskImageDetails;
import static com.ivy.sd.png.util.DataMembers.tbl_TaskMaster;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitPromotion;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitPromotionImage;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitSOS;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitSOSImage;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitSurvey;
import static com.ivy.sd.png.util.DataMembers.tbl_lastVisitSurveyImage;
import static com.ivy.sd.png.util.DataMembers.tbl_last_visit_planogram;
import static com.ivy.sd.png.util.DataMembers.tbl_last_visit_planogram_image_detail;
import static com.ivy.sd.png.util.DataMembers.tbl_planogram_image_detail;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_COMPETITOR;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_PERSUATION;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_PLANOGRAM;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_PROMO;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_QUALITY;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SERIALIZED_ASSET;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SOD;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SOS;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SOSKU;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SURVEY;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_SURVEY01;
import static com.ivy.sd.png.view.HomeScreenTwo.MENU_TASK;
import static com.ivy.ui.gallery.GalleryConstant.ASSET;
import static com.ivy.ui.gallery.GalleryConstant.COMPETITOR;
import static com.ivy.ui.gallery.GalleryConstant.PLANOGRAM;
import static com.ivy.ui.gallery.GalleryConstant.PROMOTION;
import static com.ivy.ui.gallery.GalleryConstant.SHARE_OF_DISPLAY;
import static com.ivy.ui.gallery.GalleryConstant.SHARE_OF_SELF;
import static com.ivy.ui.gallery.GalleryConstant.SHARE_OF_SKU;
import static com.ivy.ui.gallery.GalleryConstant.SURVEY;
import static com.ivy.ui.gallery.GalleryConstant.TASK;
import static com.ivy.ui.gallery.GalleryConstant.nFoldersList;

public class GalleryDataManagerImpl implements GalleryDataManager {

    private DBUtil mDbUtil;
    private DataManager dataManager;
    private HashMap<String, ArrayList<GalleryBo>> galleryListHashMap;

    @Inject
    public GalleryDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, DataManager dataManager) {
        this.mDbUtil = mDbUtil;
        this.dataManager = dataManager;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }


    /**
     * Fetch file path method
     *
     * @param imgDirectory
     * @param startWitName
     * @param isLastVisit
     * @return Image file path
     */
    private String getFileListDirectory(String imgDirectory, String startWitName, boolean isLastVisit) {
        File[] list;
        File file;

        if (isLastVisit) {
            switch (startWitName) {
                case PLANOGRAM:
                    imgDirectory = imgDirectory + "/" + DataMembers.PLANOGRAM;
                    break;
                case PROMOTION:
                    imgDirectory = imgDirectory + "/" + DataMembers.PROMOTION;
                    break;
                case TASK:
                    imgDirectory = imgDirectory + "/" + DataMembers.TASK_DIGITAL_CONTENT;
                    break;
                case SURVEY:
                    imgDirectory = imgDirectory + "/" + DataMembers.SURVEY_DIGITAL_CONTENT;
                    break;
                case SHARE_OF_SELF:
                    imgDirectory = imgDirectory + "/" + DataMembers.SOS_DIGITAL_CONTENT;
                    break;
                case ASSET:
                    imgDirectory = imgDirectory + "/" + DataMembers.SERIALIZED_ASSET_DIG_CONTENT;
                    break;
            }
        }

        file = new File(imgDirectory);

        list = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                return fileName.startsWith(startWitName);
            }
        });

        return ((list != null && list.length > 0) ? imgDirectory : "");
    }


    /**
     * @param imgDirectory - Image file source path
     * @param isLastVisit  - Last visit Transaction
     * @return image hash map list
     */

    @Override
    public Observable<HashMap<String, ArrayList<GalleryBo>>> fetchImageData(String imgDirectory, boolean isLastVisit) {
        return Observable.fromCallable(() -> {
            try {
                initDb();
                String query = "";
                galleryListHashMap = new HashMap<>();
                for (String aNFoldersList : nFoldersList) {
                    String finalImgDirectory = getFileListDirectory(imgDirectory, aNFoldersList, isLastVisit);

                    if (!finalImgDirectory.isEmpty()) {
                        switch (aNFoldersList) {
                            case PROMOTION:

                                if (!isLastVisit)
                                    query = "Select Distinct IFNULL(PD.ImageName,''),PH.Date,PM.PName From " + tbl_PromotionHeader +
                                            " PH inner join " + tbl_PromotionDetail + " PD on PH.Uid=PD.Uid" +
                                            " Left join " + tbl_ProductMaster + " PM on PD.BrandID = PM.Pid" +
                                            " Where PH.RetailerID =" + dataManager.getRetailMaster().getRetailerID();
                                else
                                    query = "Select Distinct PD.ImageName,PH.Date,PM.PName From" + tbl_lastVisitPromotion +
                                            " PH inner join " + tbl_lastVisitPromotionImage + " PD on PH.RetailerID = PD.RetailerID" +
                                            " Left join " + tbl_ProductMaster + " PM on PD.BrandID = PM.Pid" +
                                            " Where PH.RetailerID =" + dataManager.getRetailMaster().getRetailerID();

                                prepareData(query, MENU_PROMO, finalImgDirectory);
                                break;

                            case SHARE_OF_SELF:

                                if (!isLastVisit)
                                    query = " Select Distinct IFNULL(SD.imgName,''),SH.Date,PM.PName From " + tbl_SOS_Tracking_Header +
                                            " SH inner join " + tbl_SOS_Tracking_Detail +
                                            " SD on SH.Uid = SD.Uid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on SD.pid = PM.Pid" +
                                            " where SH.RetailerId=" + dataManager.getRetailMaster().getRetailerID() +
                                            " and SD.imgName != ''";
                                else
                                    query = " Select Distinct SD.imgName,SH.Date,PM.PName From " + tbl_lastVisitSOS +
                                            " SH inner join " + tbl_lastVisitSOSImage +
                                            " SD on SH.pid = SD.pid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on SD.pid = PM.Pid" +
                                            " where SH.RetailerId=" + dataManager.getRetailMaster().getRetailerID() +
                                            " and SD.imgName != ''";

                                prepareData(query, MENU_SOS, finalImgDirectory);
                                break;

                            case SHARE_OF_DISPLAY:

                                if (!isLastVisit) {
                                    query = " Select Distinct IFNULL(SD.imgName,''),SH.Date,PM.PName From " + tbl_SOD_Asset_Tracking_Header +
                                            " SH inner join " + tbl_SOD_Asset_Tracking_Detail +
                                            " SD on SH.Uid = SD.Uid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on SD.pid = PM.Pid" +
                                            " where SH.RetailerId=" + dataManager.getRetailMaster().getRetailerID() +
                                            " and SD.imgName != ''";

                                    prepareData(query, MENU_SOD, finalImgDirectory);
                                }
                                break;


                            case SHARE_OF_SKU:

                                if (!isLastVisit) {
                                    query = " Select Distinct IFNULL(SD.imgName,''),SH.Date,PM.PName From " + tbl_SOSKU_Tracking_Header +
                                            " SH inner join " + tbl_SOSKU_Tracking_Detail +
                                            " SD on SH.Uid = SD.Uid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on SD.pid = PM.Pid" +
                                            " where SH.RetailerId=" + dataManager.getRetailMaster().getRetailerID() +
                                            " and SD.imgName != ''";

                                    prepareData(query, MENU_SOSKU, finalImgDirectory);
                                }
                                break;

                            case PLANOGRAM:

                                if (!isLastVisit)
                                    query = " Select Distinct PMD.imageName,PH.Date,PM.PName From " + tbl_PlanogramHeader +
                                            " PH inner join " + tbl_planogram_image_detail +
                                            " PMD on PH.Tid = PMD.Tid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on PMD.pid = PM.Pid" +
                                            " where PH.RetailerId=" + dataManager.getRetailMaster().getRetailerID();
                                else
                                    query = " Select Distinct PMD.imageName,PH.Date,PM.PName From " + tbl_last_visit_planogram +
                                            " PH inner join " + tbl_last_visit_planogram_image_detail +
                                            " PMD on PH.pid = PMD.pid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on PMD.pid = PM.Pid" +
                                            " where PH.RetailerId=" + dataManager.getRetailMaster().getRetailerID();

                                prepareData(query, MENU_PLANOGRAM, finalImgDirectory);

                                break;
                            case COMPETITOR:

                                if (!isLastVisit) {
                                    query = " Select Distinct IFNULL(CD.imageName,''),CH.Date,PM.PName From " + tbl_CompetitorHeader +
                                            " CH inner join " + tbl_CompetitorDetails +
                                            " CD on CH.pid =CD.pid" +
                                            " Left join " + tbl_ProductMaster +
                                            " PM on CD.pid = PM.Pid" +
                                            " where CH.RetailerId=" + dataManager.getRetailMaster().getRetailerID();

                                    prepareData(query, MENU_COMPETITOR, finalImgDirectory);
                                }

                                break;

                            case ASSET:

                                if (!isLastVisit) {
                                    query = " Select Distinct AID.imgName,AH.DateTime,AM.AssetName From " + tbl_SerializedAssetHeader +
                                            " AH inner join " + tbl_SerializedAssetImageDetail + " AID on AH.uid =AID.uid" +
                                            " inner join " + tbl_SerializedAssetMaster + " AM on AID.AssetId=AM.AssetId" +
                                            " where AH.RetailerId=" + dataManager.getRetailMaster().getRetailerID() +

                                            " UNION ALL " +

                                            " Select Distinct AID.ImageName,AH.Date,AM.Posmdesc From " + tbl_AssetHeader +
                                            " AH inner join " + tbl_AssetImgInfo + " AID on AH.uid =AID.uid" +
                                            " inner join " + tbl_PosmMaster + " AM on AID.AssetId=AM.PosmId" +
                                            " where AH.RetailerID=" + dataManager.getRetailMaster().getRetailerID() +
                                            " And AH.Date=" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

                                    prepareData(query, MENU_SERIALIZED_ASSET, finalImgDirectory);
                                }
                                break;

                            case TASK:

                                int serverTask = isLastVisit ? 1 : 0;

                                query = "SELECT Distinct TMD.TaskImageName,TM.Date,(TM.taskcode || '-' ||IFNULL(PM.PName,''))" +
                                        " FROM " + tbl_TaskImageDetails + " TMD" +
                                        " INNER JOIN " + tbl_TaskMaster + " TM ON TM.taskId = TMD.TaskId" +
                                        " INNER JOIN " + tbl_TaskConfigurationMaster + " TCM ON TCM.taskId = TM.taskID" +
                                        " LEFT JOIN " + tbl_ProductMaster + " PM on PM.pid=TM.CategoryId" +
                                        " WHERE (TMD.Status !='D' OR TMD.Status IS NULL) " +
                                        " AND TMD.IsServerTask=" + serverTask +
                                        " AND TCM.RetailerID = " + dataManager.getRetailMaster().getRetailerID();

                                prepareData(query, MENU_TASK, finalImgDirectory);
                                break;

                            case SURVEY:

                                if (!isLastVisit)
                                    query = "Select Distinct AID.ImgName,AH.Date,(SM.SurveyDesc || '-' || QM.QText) From " + tbl_AnswerHeader +
                                            " AH inner join " + tbl_AnswerDetail + " AD on AH.uid=AID.uid" +
                                            " inner join " + tbl_AnswerImageDetail + " AID on AID.uid=AH.uid" +
                                            " inner join " + tbl_QuestionMaster + " QM on QM.Qid=AID.qid" +
                                            " inner join " + tbl_SurveyMaster + " SM on SM.SurveyId=AH.SurveyId" +
                                            " Where AH.RetailerID=" + dataManager.getRetailMaster().getRetailerID();
                                else
                                    query = "Select Distinct LSI.ImageName,LS.Date,(SM.SurveyDesc || '-' || QM.QText) From" + tbl_lastVisitSurvey +
                                            " LS inner join " + tbl_lastVisitSurveyImage + " LSI on LSI.SurveyId = LS.SurveyId" +
                                            " inner join " + tbl_QuestionMaster + " QM on QM.Qid=LS.qid" +
                                            " inner join " + tbl_SurveyMaster + " SM on SM.SurveyId=LS.SurveyId" +
                                            " Where LS.RetailerID=" + dataManager.getRetailMaster().getRetailerID();

                                prepareData(query, MENU_SURVEY, finalImgDirectory);
                                break;

                        }

                    }
                }
                shutDownDb();
                return galleryListHashMap;
            } catch (Exception e) {
                Commons.printException(e);
                shutDownDb();
            }

            return new HashMap<>();
        });
    }

    /**
     * Prepare Data into hash map with section wise
     *
     * @param query
     * @param sectionName  - Module name
     * @param imgDirectory - File path
     */
    private void prepareData(String query, String menuCode, String imgDirectory) {
        try {
            String sectionName = getMenuName(menuCode);
            Cursor c = mDbUtil.selectSQL(query);
            ArrayList<GalleryBo> galleryBoArrayList = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    if (!c.getString(0).isEmpty()) {
                        GalleryBo galleryBo = new GalleryBo();
                        galleryBo.setImageName(getImageName(c.getString(0), menuCode));
                        galleryBo.setDate(c.getString(1));
                        galleryBo.setFilePath(imgDirectory + "/" + galleryBo.getImageName());

                        galleryBo.setName(c.getString(2));

                        galleryBo.setRetailerName(dataManager.getRetailMaster().getRetailerName());

                        galleryBoArrayList.add(galleryBo);
                    }

                    if (!galleryBoArrayList.isEmpty())
                        galleryListHashMap.put(sectionName, galleryBoArrayList);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    private String getImageName(String imageName, String menuCode) {

        if (menuCode.equals(MENU_SURVEY)
                || menuCode.equals(MENU_SURVEY01)
                || menuCode.equals(MENU_PERSUATION)
                || menuCode.equals(MENU_QUALITY) || menuCode.equals(MENU_TASK)) {
            String[] splitPath = imageName.split("/");
            imageName = splitPath[splitPath.length - 1];
            return imageName;
        } else
            return imageName;
    }


    private String getMenuName(String menuCode) {
        String menuName = "";
        try {

            String sql = "select MName from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode=" + StringUtils.getStringQueryParam(menuCode)
                    + " and lang=" + StringUtils.getStringQueryParam(dataManager.getPreferredLanguage());

            Cursor c = mDbUtil.selectSQL(sql);
            if (c != null) {
                if (c.moveToNext()) {
                    menuName = c.getString(0);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return menuName;
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
