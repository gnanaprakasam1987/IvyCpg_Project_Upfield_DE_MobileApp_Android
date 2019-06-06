package com.ivy.cpg.view.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

public class SurveyHelperNew {

    private final Context context;
    private final BusinessModel bmodel;
    private static SurveyHelperNew instance = null;

    private String surveyTypeStandardListId;
    private ArrayList<SurveyBO> survey;
    private ArrayList<QuestionBO> questions;
    private ArrayList<QuestionBO> subQuestions;
    private ArrayList<QuestionBO> mQuestionData;
    private QuestionBO questionBODragDrop;
    private QuestionBO questionBO;
    private AnswerBO answerBO;
    private boolean fromHomeScreen;


    private boolean fromCSsurvey;
    public boolean highlightQuest;

    public int mSelectedFilter = -1;
    public int mSelectedSurvey = -1;
    public int mSelectedSuperVisiorID;


    public String remarkDone = "N";

    public final static String cs_feedback_menucode = "MENU_SURVEY_CS";

    public static final String SURVEY_SL_TYPE = "SURVEY_TYPE";
    public boolean SHOW_SMS_IN_SURVEY;
    public boolean SHOW_PHOTOCAPTURE_IN_SURVEY;
    public boolean SHOW_DRAGDROP_IN_SURVEY;
    public boolean ENABLE_MULTIPLE_PHOTO;
    public boolean SHOW_TOTAL_SCORE_IN_SURVEY;
    public boolean IS_SURVEY_ANSWER_ALL;
    public boolean IS_SURVEY_ANSWER_MANDATORY;
    public boolean SHOW_SCORE_IN_SURVEY;

    private String CODE_SHOW_TOTAL_SCORE_IN_SURVEY = "SURVEY10";
    public String CODE_SHOW_SCORE_IN_SURVEY = "SURVEY09";
    private String CODE_SURVEY_ANSWER_ALL = "SURVEY02";
    private String CODE_SURVEY_ANSWER_MANDATORY = "SURVEY03";
    public String smsmenutype;
    public String photocapturemenutype;
    public String multiplePhotoCapture;

    private String SignaturePath = "";
    private int accountGroupId;

    private SurveyHelperNew(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static SurveyHelperNew getInstance(Context context) {
        if (instance == null) {
            instance = new SurveyHelperNew(context);
        }
        return instance;
    }

    private boolean isFromHomeScreen() {
        return fromHomeScreen;
    }

    public void setFromHomeScreen(boolean fromHomeScreen) {
        this.fromHomeScreen = fromHomeScreen;
    }


    private boolean isFromCSsurvey() {
        return fromCSsurvey;
    }

    public void setFromCSsurvey(boolean fromCSsurvey) {
        this.fromCSsurvey = fromCSsurvey;
    }

    public String getSignaturePath() {
        return SignaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        SignaturePath = signaturePath;
    }

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public ArrayList<SurveyBO> getSurvey() {
        if (survey == null)
            survey = new ArrayList<>();
        return survey;
    }

    private ArrayList<QuestionBO> getDependentQuestions() {
        return subQuestions;
    }

    /**
     * To check whether the retailer mapped in account group for survey
     * @return - true if account grouping enabled or else false
     */
    private boolean hasAccountGroup() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();
        try {
            Cursor c = db.selectSQL("select groupid from AccountGroupDetail where retailerid=" + bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
            if (c != null) {
                if (c.moveToNext()) {
                    accountGroupId = c.getInt(0);
                    return accountGroupId > 0;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return false;
    }

    /**
     * Retrieve survey id's mapped to the account group.
     * @return - Group of survey id's concatenated with comma separator.
     */
    private String getAccountGroupSurveys() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();
        StringBuilder surveyIds = new StringBuilder();
        try {
            Cursor c = db.selectSQL("select surveyid from SurveyCriteriaMapping where criteriaid=" + accountGroupId);
            if (c != null) {
                while (c.moveToNext()) {
                    surveyIds.append(c.getInt(0));
                    surveyIds.append(",");
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return String.valueOf(surveyIds).substring(0, surveyIds.length()-1);
    }


    /**
     * Downlaod the surveyType standard list id.
     *
     * @param surveyType STANDARD|SPECIAL|NEW_RETAILER
     */
    public void downloadModuleId(String surveyType) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        Cursor c = db.selectSQL("SELECT ListId FROM StandardListMaster"
                + " WHERE ListCode = " + QT(surveyType)
                + " AND ListType = 'SURVEY_TYPE'");
        if (c != null) {
            if (c.moveToNext()) {
                surveyTypeStandardListId = c.getString(0);
            }
            c.close();
        }
        db.closeDB();
    }


    /**
     * Get valid survey id and groupid by comparing retailer attributes
     *
     * @param db
     */
    private ArrayList<String> getValidGroupIdByAttributeCriteriaMapping(DBUtil db) {

        StringBuilder sb = new StringBuilder();
        ArrayList<String> groupIDList = new ArrayList<>();

        ArrayList<String> retailerAttributes = bmodel.getAttributeParentListForCurrentRetailer(bmodel.getRetailerMasterBO().getRetailerID());

        sb.append("select Distinct surveyid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SurveyCriteriaMapping  SAM" +
                " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.criteriaid" +
                " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID order by surveyid,groupid");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            int lastSurveyId = 0, lastGroupId = 0;
            boolean isGroupSatisfied = false;
            while (c.moveToNext()) {
                if (retailerAttributes != null && retailerAttributes.contains(c.getString(3))) {

                    if (lastSurveyId != c.getInt(0) || lastGroupId != c.getInt(1)) {

                        if (isGroupSatisfied) {
                            if (!groupIDList.contains(c.getString(1) + c.getString(0))) {
                                groupIDList.add(c.getString(1) + c.getString(0));
                            }
                        }

                    }

                    if (isSurveyApplicable(c.getInt(0), c.getInt(1), c.getInt(3)))
                        isGroupSatisfied = true;
                    else
                        isGroupSatisfied = false;

                    lastSurveyId = c.getInt(0);
                    lastGroupId = c.getInt(1);


                }

            }
            if (isGroupSatisfied) {
                if (!groupIDList.contains(lastGroupId + "" + lastSurveyId)) {
                    groupIDList.add(lastGroupId + "" + lastSurveyId);
                }
            }

        }
        c.close();
        return groupIDList;
    }

    private boolean isSurveyApplicable(int surveyid, int groupId, int parentId) {


        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();


            StringBuilder sb = new StringBuilder();

            sb.append("select Distinct surveyid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SurveyCriteriaMapping  SAM" +
                    " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.criteriaId" +
                    " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID ");
            sb.append("where surveyid=" + surveyid + " and groupid=" + groupId + " and SAM.criteriaId in(select RA.attributeid from RetailerAttribute RA" +
                    " inner join EntityAttributeMaster EA on EA.Attributeid = RA.Attributeid and EA.PArentid=" + parentId +
                    " where retailerid = " + bmodel.getRetailerMasterBO().getRetailerID() + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                return true;
            }
            c.close();
            db.closeDB();

        } catch (Exception ex) {

            Commons.printException(ex);
        }
        return false;
    }


    /**
     * This method will check criteria mapping to return surveyids.
     * Criteria types like Location, Channel , Account, Retailer Attribute, Priority Product and Retailer will be considered.
     *
     * @return surveyids as comma separated string.
     */
    private String getMappedSurveyIds() {

        DBUtil db = null;
        String surveyIds = "";
        try {

            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            // Attribute Groupid will be validated seprately after validating other citeria type.
            ArrayList<String> mGroupIDList = getValidGroupIdByAttributeCriteriaMapping(db);

            StringBuilder sb = new StringBuilder();
            String locIdScheme = "";
            String channelId = "";

            /* Get location id and its parent id */
            locIdScheme = "," + bmodel.channelMasterHelper.getLocationHierarchy(context);


            /* Get channel id and its parent id */
            if (!"".equals(getChannelidForSurvey()) &&
                    getChannelidForSurvey() != null) {
                channelId = "," + getChannelidForSurvey();
            }

            sb.append("SELECT Distinct Survey.SurveyId,Survey.GroupId," +
                    "IfNull(LocationId,0) AS LocationId," +
                    "IfNull(ChannelId,0) AS ChannelId," +
                    "Case  IFNULL(AttributeID ,-1) when -1  then '0' else '1' END as flag" +
                    ",IfNull(PriorityBiD,0) AS PriorityBiD," +
                    "IfNull(RetailerID,0) AS RetailerID, " +
                    "IfNull(AccountID,0) AS AccountID" +
                    " FROM (SELECT  DISTINCT SurveyId,GroupId FROM SurveyCriteriaMapping) AS Survey" +

                    " LEFT JOIN  (SELECT DISTINCT SurveyId,GroupId,CriteriaId LocationId  FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='LOCATION' and listtype='SURVEY_CRITERIA_TYPE')  LS" +
                    " ON Survey.SurveyId=Ls.SurveyId and Survey.GroupId=LS.GroupId" +

                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId ChannelId FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode in ('CHANNEL','SUBCHANNEL') and listtype='SURVEY_CRITERIA_TYPE') CS" +
                    " ON  Survey.SurveyId=CS.SurveyId and Survey.GroupId=CS.GroupId" +

                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId AttributeID FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='RTR_ATTRIBUTES' and listtype='SURVEY_CRITERIA_TYPE')" +
                    " AT ON  Survey.SurveyId=AT.SurveyId and Survey.GroupId=AT.GroupId" +

                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId PriorityBiD FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='PRIORITY_PRD' and listtype='SURVEY_CRITERIA_TYPE')" +
                    " PR ON  Survey.SurveyId=PR.SurveyId and Survey.GroupId=PR.GroupId" +

                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId RetailerID FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='RETAILER')" +
                    " RTR ON  Survey.SurveyId=RTR.SurveyId and Survey.GroupId=RTR.GroupId" +

                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId AccountID FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='ACCOUNT')" +
                    " ACC ON  Survey.SurveyId=ACC.SurveyId and Survey.GroupId=ACC.GroupId" +

                    " where ifNull(locationid,0) in(0" + locIdScheme + "," + bmodel.getRetailerMasterBO().getLocationId() + ")" +
                    " And ifnull(channelid,0) in (0" + channelId + "," + bmodel.getRetailerMasterBO().getSubchannelid() + ")" +
                    " And ifnull(PriorityBiD,0) in (0," + bmodel.getRetailerMasterBO().getPrioriryProductId() + ")" +
                    " And ifnull(RetailerID,0) in (0," + bmodel.getRetailerMasterBO().getRetailerID() + ")" +
                    " And ifnull(AccountID,0) in (0," + bmodel.getRetailerMasterBO().getAccountid() + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    // If attribute id is 0 or attribute id !=0 and match with critera then add
                    if (c.getInt(4) == 0 || (c.getInt(4) == 1 && mGroupIDList.contains(c.getInt(1) + "" + c.getInt(0)))) {

                        if (surveyIds.equals("")) {
                            surveyIds = c.getString(0);
                        } else {
                            surveyIds += "," + c.getString(0);
                        }
                    }

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return surveyIds;
    }

    public HashMap<Integer, QuestionBO> getValidateQuestions() {
        return validateQuestions;
    }

    private HashMap<Integer, QuestionBO> validateQuestions = new HashMap<>();

    public void getSurveyValidateOptions() {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();


            StringBuilder sb = new StringBuilder();

            sb.append("select Qid,QType,FromValue,ToValue,ifnull(Precision,0) as Precision from SurveyQuestionValidations");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    QuestionBO questionBO = new QuestionBO();
                    questionBO.setQuestionID(c.getInt(c.getColumnIndex("Qid")));
                    questionBO.setQuestionTypeId(c.getInt(c.getColumnIndex("QType")));
                    questionBO.setFromValue(c.getString(c.getColumnIndex("FromValue")));
                    questionBO.setToValue(c.getString(c.getColumnIndex("ToValue")));
                    questionBO.setPrecision(c.getInt(c.getColumnIndex("Precision")));
                    validateQuestions.put(questionBO.getQuestionID(), questionBO);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception ex) {

            Commons.printException(ex);
        }
    }


    /**
     * Download survey and its questions along with option and score matching criteria.
     *
     * @param moduleCode
     */
    public void downloadQuestionDetails(String moduleCode) {
        try {
            getSurveyValidateOptions();
            survey = new ArrayList<>();

            int tempSurveyId = -1;
            int tempQuestionId = -1;
            int tempOptionId = -1;
            int surveyIndex = -1;
            int questionIndex = -1;
            int optionIndex = -1;
            String mtempGName = "";
            String retailerid = "0";


            if (!fromHomeScreen) {
                if (bmodel.getRetailerMasterBO() != null)
                    retailerid = bmodel.getRetailerMasterBO().getRetailerID();
            }


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct SM.SurveyId, SM.SurveyDesc, SM.BonusPoint,");
            sb.append(" A.QId, A.QText, A.QType, IFNULL(C.ListCode, ''), A.BrandID, A.IsMand,");
            sb.append(" SMP.Weight,ifnull(SMP.GroupName,''), SMP.isScore, A.isPhotoReq, A.minPhoto,");
            sb.append(" A.maxPhoto,A.isBonus, IFNULL(OM.OptionId,0), OM.OptionText, OSM.Score,");
            sb.append(" CASE OSM.isExcluded WHEN '1' THEN 'true' ELSE 'false' END as isExcluded,");
            sb.append(" IFNULL(OD.DQID,0),IFNULL(SLM.listname,'NO FREQ') as freq,SMP.maxScore,SM.IsSignatureRequired,A.MinValue,A.MaxValue,SMP.DefaultOptionId FROM SurveyCriteriaMapping SCM");
            sb.append(" INNER JOIN StandardListMaster SL On SL.Listid=SCM.CriteriaType and SL.listtype='SURVEY_CRITERIA_TYPE'");
            sb.append(" INNER JOIN SurveyMapping SMP ON SMP.SurveyId = SCM.SurveyId");
            sb.append(" INNER JOIN SurveyMaster SM ON SM.SurveyId = SCM.SurveyId");
            sb.append(" LEFT JOIN StandardListMaster SLM On SLM.Listid=SM.frequencyType and SLM.listtype='SURVEY_FREQUENCY_TYPE'");
            sb.append(" INNER JOIN QuestionMaster A ON A.QID = SMP.QID");
            sb.append(" LEFT JOIN OptionMaster OM ON OM.QId = A.QId");
            sb.append(" LEFT JOIN StandardListMaster C ON C.ListId =  A.QType");
            sb.append(" LEFT JOIN OptionScoreMapping OSM ON OSM.optionid = OM.optionid AND OSM.SurveyId = SM.SurveyId");
            sb.append(" LEFT JOIN OptionDQM OD ON OD.OptionId = OM.OptionId");
            sb.append(" LEFT JOIN SurveyCriteriaMapping SCM1 ON SCM1.SurveyId = SCM.SurveyId AND SCM1.Groupid = SCM.Groupid");
            sb.append(" WHERE Module=");
            sb.append(QT(surveyTypeStandardListId));
            sb.append(" AND SM.menuCode=");
            sb.append(QT(moduleCode));
            if (!fromHomeScreen) {
                if (hasAccountGroup())
                    sb.append(" AND SM.surveyId in(" + getAccountGroupSurveys() + ")");
                else
                    sb.append(" AND SM.surveyId in(" + getMappedSurveyIds() + ")");


            } else {
                if (moduleCode.equalsIgnoreCase("MENU_NEW_RET") && bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER)
                    sb.append(" AND SCM.CriteriaID=" + bmodel.newOutletHelper.getmSelectedChannelid() + "  and SL.listcode='CHANNEL' OR SL.listcode='SUBCHANNEL'");
            }

            sb.append(" and SM.SurveyId not in (select AH.surveyid from answerheader AH ");
            sb.append("Where retailerid = '" + retailerid + "' and AH.frequency='DAILY_PIRAMAL') ");
            sb.append("AND SM.SurveyId NOT IN (SELECT SH.surveyid FROM SurveyHistroy SH WHERE SH.retailerid = '" + retailerid + "' and " +
                    "(case when lower(freq) = 'daily' then Date = '" + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL) + "' " +
                    "when lower(freq) = 'monthly' then Date like '%/" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "/%' " +
                    "when lower(freq) = 'yearly' then Date like '" + Calendar.getInstance().get(Calendar.YEAR) + "/%' end)) ");
            sb.append("ORDER BY SM.Sequence, SM.SurveyId, SMP.GroupName, SMP.Sequence, SMP.QID, OM.OptionId");
            Cursor c = db.selectSQL(sb.toString());


            StringBuilder sb1 = new StringBuilder();
            int counter = 0;
            if (c != null) {

                while (c.moveToNext()) {
                    if (tempSurveyId != c.getInt(0)) {
                        mtempGName = "";
                        surveyIndex = surveyIndex + 1;

                        tempSurveyId = c.getInt(0);
                        SurveyBO surveyBO = new SurveyBO();
                        surveyBO.setSurveyID(c.getInt(0));
                        surveyBO.setSurveyName(c.getString(1));
                        surveyBO.setMaxBonusScore(c.getFloat(2));
                        surveyBO.setSurveyFreq(c.getString(c.getColumnIndex("freq")));
                        surveyBO.setSignatureRequired(c.getInt(c.getColumnIndex("IsSignatureRequired")) == 1);

                        questionIndex = 0;
                        tempQuestionId = c.getInt(3);
                        questionBO = new QuestionBO();
                        questionBO.setSurveyid(c.getInt(0));
                        questionBO.setQuestionID(c.getInt(3));
                        if (getValidateQuestions().containsKey(questionBO.getQuestionID())) {
                            QuestionBO validateQns = getValidateQuestions().get(questionBO.getQuestionID());
                            questionBO.setPrecision(validateQns.getPrecision());
                            questionBO.setFromValue(validateQns.getFromValue());
                            questionBO.setToValue(validateQns.getToValue());
                        }
                        questionBO.setQuestionDescription(c.getString(4));
                        questionBO.setQuestionTypeId(c.getInt(5));
                        questionBO.setQuestionType(c.getString(6));
                        questionBO.setBrandID(c.getInt(7));
                        questionBO.setIsMandatory(c.getInt(8));
                        questionBO.setQuestWeight(c.getInt(9));
                        if (mtempGName.equals(c.getString(10)))
                            questionBO.setGroupName("");
                        else {
                            questionBO.setGroupName(c.getString(10));
                            mtempGName = questionBO.getGroupName();
                        }
                        questionBO.setIsScore(c.getInt(11));
                        questionBO.setIsPhotoReq(c.getInt(12));
                        questionBO.setMinPhoto(c.getInt(13));
                        questionBO.setMaxPhoto(c.getInt(14));
                        questionBO.setIsBonus(c.getInt(15));
                        questionBO.setIsSubQuestion(0);
                        questionBO.setMaxScore(c.getDouble(c.getColumnIndex("MaxScore")));
                        questionBO.setMinValue(c.getInt(c.getColumnIndex("MinValue")));
                        questionBO.setMaxValue(c.getInt(c.getColumnIndex("MaxValue")));
                        questionBO.setSelectedAnswerID(c.getInt(c.getColumnIndex("DefaultOptionId")));
                        if (questionBO.getBrandID() > 0)
                            questionBO.setParentHierarchy(getParentHiearchy(questionBO.getBrandID()));


                        sb1.append("Select IFNULL(AID.ImgName,'') FROM AnswerImageDetail AID INNER JOIN AnswerHeader AH  ON AH.uid=AID.Uid " +
                                "AND AH.surveyid='" + questionBO.getSurveyid() + "' " +
                                "WHERE AID.qid = '");
                        sb1.append(questionBO.getQuestionID());
                        sb1.append("' AND AID.RetailerId ='");
                        sb1.append(retailerid);
                        sb1.append("'");
                        Cursor c1 = db.selectSQL(sb1.toString());

                        if (c1 != null) {
                            while (c1.moveToNext()) {
                                if (counter == 0) {
                                    questionBO.setImage1Captured(true);
                                    String imgName = null;
                                    try {
                                        imgName = c1.getString(0).substring(c1.getString(0).lastIndexOf("/"));
                                    } catch (Exception e) {
                                        imgName = c1.getString(0);
                                    }
                                    questionBO.setImage1Captured(true);
                                    questionBO.setImage1Path(FileUtils.photoFolderPath + imgName);
                                    questionBO.setImage2Captured(false);
                                    questionBO.setImage2Path("");
                                    counter++;
                                } else if (counter == 1) {
                                    String imgName = null;
                                    try {
                                        imgName = c1.getString(0).substring(c1.getString(0).lastIndexOf("/"));
                                    } catch (Exception e) {
                                        imgName = c1.getString(0);
                                    }
                                    questionBO.setImage2Captured(true);
                                    questionBO.setImage2Path(FileUtils.photoFolderPath + imgName);
                                    break;
                                }
                            }
                            c1.close();
                        } else {
                            questionBO.setImage1Captured(false);
                            questionBO.setImage1Path("");
                            questionBO.setImage2Captured(false);
                            questionBO.setImage2Path("");
                        }
                        sb1.setLength(0);
                        counter = 0;
                        optionIndex = 0;
                        tempOptionId = c.getInt(16);
                        answerBO = new AnswerBO();
                        answerBO.setAnswerID(c.getInt(16));
                        answerBO.setAnswer(c.getString(17));
                        answerBO.setScore(c.getFloat(18));
                        answerBO.setExcluded(Boolean.valueOf(c.getString(19)));
                        answerBO.getQuestionList().add(c.getInt(20));

                        questionBO.getAnswersList().add(answerBO);

                        surveyBO.getQuestions().add(questionBO);

                        survey.add(surveyBO);

                    } else {
                        if (tempQuestionId != c.getInt(3)) {

                            questionIndex = questionIndex + 1;

                            tempQuestionId = c.getInt(3);
                            questionBO = new QuestionBO();
                            questionBO.setSurveyid(c.getInt(0));
                            questionBO.setQuestionID(c.getInt(3));
                            questionBO.setQuestionDescription(c.getString(4));
                            questionBO.setQuestionTypeId(c.getInt(5));
                            questionBO.setQuestionType(c.getString(6));
                            questionBO.setBrandID(c.getInt(7));
                            questionBO.setIsMandatory(c.getInt(8));
                            questionBO.setQuestWeight(c.getInt(9));
                            if (getValidateQuestions().containsKey(questionBO.getQuestionID())) {
                                QuestionBO validateQns = getValidateQuestions().get(questionBO.getQuestionID());
                                questionBO.setPrecision(validateQns.getPrecision());
                                questionBO.setFromValue(validateQns.getFromValue());
                                questionBO.setToValue(validateQns.getToValue());
                            }
                            if (mtempGName.equals(c.getString(10)))
                                questionBO.setGroupName("");
                            else {
                                questionBO.setGroupName(c.getString(10));
                                mtempGName = questionBO.getGroupName();
                            }
                            questionBO.setIsScore(c.getInt(11));
                            questionBO.setIsPhotoReq(c.getInt(12));
                            questionBO.setMinPhoto(c.getInt(13));
                            questionBO.setMaxPhoto(c.getInt(14));
                            questionBO.setIsBonus(c.getInt(15));
                            questionBO.setIsSubQuestion(0);
                            questionBO.setMaxScore(c.getDouble(c.getColumnIndex("MaxScore")));
                            questionBO.setMinValue(c.getInt(c.getColumnIndex("MinValue")));
                            questionBO.setMaxValue(c.getInt(c.getColumnIndex("MaxValue")));
                            questionBO.setSelectedAnswerID(c.getInt(c.getColumnIndex("DefaultOptionId")));
                            if (questionBO.getBrandID() > 0)
                                questionBO.setParentHierarchy(getParentHiearchy(questionBO.getBrandID()));

                            sb1.append("Select IFNULL(AID.ImgName,'') FROM AnswerImageDetail AID INNER JOIN AnswerHeader AH  ON AH.uid=AID.Uid " +
                                    "AND AH.surveyid='" + questionBO.getSurveyid() + "' " +
                                    "WHERE AID.qid = '");
                            sb1.append(questionBO.getQuestionID());
                            sb1.append("' AND AID.RetailerId ='");
                            sb1.append(retailerid);
                            sb1.append("'");
                            Cursor c1 = db.selectSQL(sb1.toString());
                            if (c1 != null) {
                                while (c1.moveToNext()) {
                                    if (counter == 0) {
                                        String imgName = null;
                                        try {
                                            imgName = c1.getString(0).substring(c1.getString(0).lastIndexOf("/"));
                                        } catch (Exception e) {
                                            imgName = c1.getString(0);
                                        }
                                        questionBO.setImage1Captured(true);
                                        questionBO.setImage1Path(FileUtils.photoFolderPath + "/" + imgName);
                                        questionBO.setImage2Captured(false);
                                        questionBO.setImage2Path("");
                                        counter++;
                                    } else if (counter == 1) {
                                        String imgName = null;
                                        try {
                                            imgName = c1.getString(0).substring(c1.getString(0).lastIndexOf("/"));
                                        } catch (Exception e) {
                                            imgName = c1.getString(0);
                                        }
                                        questionBO.setImage2Captured(true);
                                        questionBO.setImage2Path(FileUtils.photoFolderPath + imgName);
                                        break;
                                    }
                                }
                                c1.close();
                            } else {
                                questionBO.setImage1Captured(false);
                                questionBO.setImage1Path("");
                                questionBO.setImage2Captured(false);
                                questionBO.setImage2Path("");
                            }
                            sb1.setLength(0);
                            counter = 0;
                            optionIndex = 0;
                            tempOptionId = c.getInt(16);
                            answerBO = new AnswerBO();
                            answerBO.setAnswerID(c.getInt(16));
                            answerBO.setAnswer(c.getString(17));
                            answerBO.setScore(c.getFloat(18));
                            answerBO.setExcluded(Boolean.valueOf(c.getString(19)));
                            answerBO.getQuestionList().add(c.getInt(20));

                            questionBO.getAnswersList().add(answerBO);

                            survey.get(surveyIndex).getQuestions().add(questionBO);
                        } else {
                            if (tempOptionId != c.getInt(16)) {
                                optionIndex = optionIndex + 1;
                                tempOptionId = c.getInt(16);
                                answerBO = new AnswerBO();
                                answerBO.setAnswerID(c.getInt(16));
                                answerBO.setAnswer(c.getString(17));
                                answerBO.setScore(c.getFloat(18));
                                answerBO.setExcluded(Boolean.valueOf(c.getString(19)));
                                answerBO.getQuestionList().add(c.getInt(20));
                                survey.get(surveyIndex).getQuestions().get(questionIndex).getAnswersList().add(answerBO);
                            } else {
                                survey.get(surveyIndex).getQuestions().get(questionIndex).getAnswersList().get(optionIndex).getQuestionList().add(c.getInt(20));
                            }
                        }
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        downloadDepQuestionDetails();
    }


    /**
     * Get the dependent questions. OptionDQM Table Used to get dependent
     * questions for each option QuestionMaster table Used to get question
     * details OptionMaster and SurveyMapping Used to categorize the question as
     * survey wise
     */
    private void downloadDepQuestionDetails() {
        try {
            subQuestions = new ArrayList<>();

            int tempQuestionId = -1;
            int tempOptionId = -1;
            int questionIndex = -1;
            int optionIndex = -1;


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct A.QId, A.QText, A.QType, IFNULL(E.ListCode,''), A.IsMand,"
                            + " OM.OptionId, OM.OptionText, OSM.Score,"
                            + " CASE OSM.isExcluded WHEN '1' THEN 'true' ELSE 'false' END as isExcluded,"
                            + " IFNULL(OD.DQID,0) FROM QuestionMaster A"
                            + " INNER JOIN OptionDQM B ON B.DQId = A.QId"
                            + " LEFT JOIN OptionMaster OM ON OM.QId = A.QId"
                            + " LEFT JOIN StandardListMaster C ON C.ListId = A.QType"
                            + " LEFT JOIN OptionScoreMapping OSM ON OSM.optionid = OM.optionid"
                            + " LEFT JOIN OptionDQM OD ON OD.OptionId = OM.OptionId"
                            + " LEFT JOIN StandardListMaster E ON E.ListId =  A.QType"
                            + " WHERE Module = "
                            + QT(surveyTypeStandardListId));
            if (c != null) {
                while (c.moveToNext()) {
                    if (tempQuestionId != c.getInt(0)) {

                        questionIndex = questionIndex + 1;

                        tempQuestionId = c.getInt(0);
                        questionBO = new QuestionBO();
                        questionBO.setQuestionID(c.getInt(0));
                        questionBO.setQuestionDescription(c.getString(1));
                        questionBO.setQuestionTypeId(c.getInt(2));
                        questionBO.setQuestionType(c.getString(3));
                        questionBO.setIsMandatory(c.getInt(4));
                        if (getValidateQuestions().containsKey(questionBO.getQuestionID())) {
                            QuestionBO validateQns = getValidateQuestions().get(questionBO.getQuestionID());
                            questionBO.setPrecision(validateQns.getPrecision());
                            questionBO.setFromValue(validateQns.getFromValue());
                            questionBO.setToValue(validateQns.getToValue());
                        }
                        optionIndex = 0;
                        tempOptionId = c.getInt(5);
                        answerBO = new AnswerBO();
                        answerBO.setAnswerID(c.getInt(5));
                        answerBO.setAnswer(c.getString(6));
                        answerBO.setScore(c.getFloat(7));
                        answerBO.setExcluded(Boolean.valueOf(c.getString(8)));
                        answerBO.getQuestionList().add(c.getInt(9));
                        questionBO.getAnswersList().add(answerBO);
                        subQuestions.add(questionBO);
                    } else {


                        if (tempOptionId != c.getInt(5)) {
                            optionIndex = optionIndex + 1;
                            tempOptionId = c.getInt(5);
                            answerBO = new AnswerBO();
                            answerBO.setAnswerID(c.getInt(5));
                            answerBO.setAnswer(c.getString(6));
                            answerBO.setScore(c.getFloat(7));
                            answerBO.setExcluded(Boolean.valueOf(c.getString(8)));
                            answerBO.getQuestionList().add(c.getInt(9));
                            subQuestions.get(questionIndex).getAnswersList().add(answerBO);
                        } else {
                            subQuestions.get(questionIndex).getAnswersList().get(optionIndex).getQuestionList().add(c.getInt(9));
                        }
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * Get the dependent questions. OptionDQM Table Used to get dependent
     * questions for each option QuestionMaster table Used to get question
     * details OptionMaster and SurveyMapping Used to categorize the question as
     * survey wise
     */
    public ArrayList<QuestionBO> addDepQuestionDetailsToSurvey(int surveyID, int qID) {
        try {
            questions = new ArrayList<>();

            for (QuestionBO subqBO : getDependentQuestions()) {
                if (subqBO.getQuestionID() == qID) {
                    subqBO.setSurveyid(surveyID);
                    subqBO.setIsSubQuestion(1);
                    questions.add(subqBO);
                }


            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return questions;
    }


    /**
     * Check whether all the questions are answered
     *
     * @return true if all question answered else false
     */
    public boolean isAllAnswered() {

        invalidEmails = new StringBuilder();
        notInRange = new StringBuilder();
        for (SurveyBO sBO : getSurvey()) {
            if (sBO.getSurveyID() == mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {

                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();

                for (QuestionBO qus : mParentQuestions) {
                    if ((qus.getSelectedAnswer().isEmpty() || qus.getSelectedAnswer().contains(context.getResources().
                            getString(R.string.plain_select)))
                            && (qus.getSelectedAnswerIDs().isEmpty() || qus.getSelectedAnswerIDs().contains(-1))) {
                        return false;
                    }

                    if (qus.getQuestionType().equals("EMAIL")) {
                        if (qus.getSelectedAnswer().size() > 0 && !isValidEmail(qus.getSelectedAnswer().get(0))) {
                            invalidEmails.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                            invalidEmails.append("\n");
                        }
                    }

                    if (qus.getFromValue() != null && qus.getToValue() != null && qus.getQuestionType().equals("NUM")) {
                        if (!qus.getSelectedAnswer().get(0).equalsIgnoreCase("")) {
                            if (!qus.getFromValue().isEmpty() && !qus.getToValue().isEmpty() && qus.getSelectedAnswer().size() > 0) {
                                if (!isInRange(SDUtil.convertToFloat(qus.getFromValue()), SDUtil.convertToFloat(qus.getToValue()),
                                        SDUtil.convertToFloat(qus.getSelectedAnswer().get(0)))) {
                                    notInRange.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                                    notInRange.append("\n");
                                }
                            }
                        }
                    }
                }
            }

        }

        if (invalidEmails.toString().length() > 0) {
            return false;
        }

        if (notInRange.toString().length() > 0) {
            return false;
        }

        return true;
    }


    public String getInvalidEmails() {
        return invalidEmails.toString();
    }

    public String getNotInRange() {
        return notInRange.toString();
    }

    StringBuilder invalidEmails;
    StringBuilder notInRange;


    /**
     * Check whether all mandatory questions are answered
     *
     * @return true if all mandatory question answered else false
     */
    public boolean isMandatoryQuestionAnswered() {
        boolean returnFlag = true;
        invalidEmails = new StringBuilder();
        notInRange = new StringBuilder();
        for (SurveyBO sBO : getSurvey()) {
            if (sBO.getSurveyID() == mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();

                for (QuestionBO qus : mParentQuestions) {
                    qus.setIsMandatoryQuestNotAnswered(false);
                    if (qus.getIsSubQuestion() == 0) {
                        if (qus.getIsMandatory() == 1) {
                            if ((qus.getSelectedAnswer().isEmpty() || qus.getSelectedAnswer().contains(context.getResources().
                                    getString(R.string.plain_select)))
                                    && (qus.getSelectedAnswerIDs().isEmpty() || qus.getSelectedAnswerIDs().contains(-1))) {
                                //parent question itself not answered
                                qus.setIsMandatoryQuestNotAnswered(true);
                                returnFlag = false;
                            } else {
                                //Parent question answered,then checking sub question
                                if (!isMandatorySubQuestionsAnswered(qus, sBO)) {
                                    returnFlag = false;
                                }
                            }


                        } else {
                            //Parent question not mandatory,then checking for sub question if mandatory
                            if (!isMandatorySubQuestionsAnswered(qus, sBO)) {
                                returnFlag = false;
                            }
                        }
                    }


                    if (qus.getQuestionType().equals("EMAIL")) {
                        if (qus.getSelectedAnswer().size() > 0 && !isValidEmail(qus.getSelectedAnswer().get(0))) {
                            invalidEmails.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                            invalidEmails.append("\n");
                        }
                    }

                    if (qus.getFromValue() != null && qus.getToValue() != null && qus.getQuestionType().equals("NUM")) {
                        if (!qus.getFromValue().isEmpty() && !qus.getToValue().isEmpty() && qus.getSelectedAnswer().size() > 0) {
                            if (!qus.getSelectedAnswer().get(0).equalsIgnoreCase("")) {
                                if (!isInRange(SDUtil.convertToFloat(qus.getFromValue()),
                                        SDUtil.convertToFloat(qus.getToValue()),
                                        SDUtil.convertToFloat(qus.getSelectedAnswer().get(0)))) {
                                    notInRange.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                                    notInRange.append("\n");
                                }
                            }
                        }
                    }
                }
            }
        }
        if (invalidEmails.toString().length() > 0) {
            return false;
        }
        if (notInRange.toString().length() > 0) {
            return false;
        }
        return returnFlag;
    }

    public boolean isAnsweredTypeEmail() {
        invalidEmails = new StringBuilder();
        for (SurveyBO sBO : getSurvey()) {
            if (sBO.getSurveyID() == mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();
                for (QuestionBO qus : mParentQuestions) {
                    if (qus.getQuestionType().equals("EMAIL")) {
                            if (qus.getSelectedAnswer().size() > 0
                                    && qus.getSelectedAnswer().get(0).length()!=0
                                    && !isValidEmail(qus.getSelectedAnswer().get(0))) {
                                invalidEmails.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                                invalidEmails.append("\n");
                            }
                    }
                }
            }
        }
        return invalidEmails.toString().length() <= 0;
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;

    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isMandatorySubQuestionsAnswered(QuestionBO qus, SurveyBO surveyBO) {
        boolean returnFlag = true;
        final ArrayList<AnswerBO> answers = qus.getAnswersList();
        for (AnswerBO answerBO : answers) {
            if (qus.getSelectedAnswerIDs().contains(answerBO.getAnswerID())) {

                for (int subQid : answerBO.getQuestionList()) {

                    for (QuestionBO subQBO : surveyBO.getQuestions()) {
                        if (subQBO.getQuestionID() == subQid) {
                            subQBO.setIsMandatoryQuestNotAnswered(false);
                            if (subQBO.getIsMandatory() == 1) {
                                if (subQBO.getIsMandatory() == 1
                                        && (subQBO.getSelectedAnswer().isEmpty() || qus.getSelectedAnswer().contains(context.getResources().
                                        getString(R.string.plain_select)))
                                        && (subQBO.getSelectedAnswerIDs().isEmpty() || qus.getSelectedAnswerIDs().contains(-1))) {
                                    subQBO.setIsMandatoryQuestNotAnswered(true);
                                    returnFlag = false;

                                }
                            }
                            //To Check all the subquestions
                            if (returnFlag)
                                returnFlag = isMandatorySubQuestionsAnswered(subQBO, surveyBO);
                        }
                    }
                }
            }
        }
        return returnFlag;
    }


    /**
     * Check Whether any one question is answered
     *
     * @return true if any one question answered else false
     */
    public boolean hasDataToSave() {

        invalidEmails = new StringBuilder();
        notInRange = new StringBuilder();
        boolean isEmail = false;
        boolean isNum = false;
        for (SurveyBO sBO : getSurvey()) {
            if (sBO.getSurveyID() == mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();
                for (QuestionBO qus : mParentQuestions) {
                    if ((qus.getSelectedAnswer() != null && !qus.getSelectedAnswer().isEmpty()
                            && !qus.getSelectedAnswer().contains(context.getResources().
                            getString(R.string.plain_select)))
                            || (qus.getSelectedAnswerIDs() != null && !qus.getSelectedAnswerIDs().isEmpty() &&
                            !qus.getSelectedAnswerIDs().contains(-1)) && !qus.getQuestionType().equals("EMAIL")) {
                        return true;
                    }
                    if (qus.getQuestionType().equals("EMAIL") && qus.getSelectedAnswer().size() > 0) {
                        isEmail = true;
                        if (!isValidEmail(qus.getSelectedAnswer().get(0))) {
                            invalidEmails.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                            invalidEmails.append("\n");
                        }
                    }
                    if (qus.getFromValue() != null && qus.getToValue() != null && qus.getQuestionType().equals("NUM")
                            && qus.getSelectedAnswer().size() > 0) {
                        isNum = true;
                        if (!qus.getFromValue().isEmpty() && !qus.getToValue().isEmpty()) {
                            if (!qus.getSelectedAnswer().get(0).equalsIgnoreCase("")) {
                                if (!isInRange(SDUtil.convertToFloat(qus.getFromValue()), SDUtil.convertToFloat(qus.getToValue()),
                                        SDUtil.convertToFloat(qus.getSelectedAnswer().get(0)))) {
                                    notInRange.append(sBO.getSurveyName() + "-" + "Q.No " + qus.getQuestionNo());
                                    notInRange.append("\n");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (invalidEmails.toString().isEmpty() && isEmail) return true;
        return notInRange.toString().isEmpty() && isNum;
    }

    public boolean hasPhotoToSave() {
        for (SurveyBO sBO : getSurvey()) {
            if (sBO.getSurveyID() == mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();
                for (QuestionBO qus : mParentQuestions) {
                    if (qus.getIsPhotoReq() > 0 && ((!qus.getSelectedAnswer().isEmpty() && !qus.getSelectedAnswer().contains(context.getResources().
                            getString(R.string.plain_select)))
                            || (!qus.getSelectedAnswerIDs().isEmpty() && !qus.getSelectedAnswerIDs().contains(-1)))
                            && qus.getMinPhoto() > qus.getImageNames().size()) {
                        highlightQuest = true;
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Save the Transaction as Survey Wise. AnswerHeader - used to hold the
     * survey detail. AnswerDetail - used to hold the question and anser detail.
     */
    public void saveAnswer(String menuCode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String retailerid = "0";
            int distID = 0;
            String type = "RETAILER";
            int superwiserID = 0;
            if ("MENU_SURVEY_SW".equalsIgnoreCase(menuCode)) {
                type = "SELLER";
                superwiserID = mSelectedSuperVisiorID;
            }

            if (!isFromHomeScreen()) {
                retailerid = bmodel.getRetailerMasterBO().getRetailerID();
                distID = bmodel.getRetailerMasterBO().getDistributorId();
            }

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                bmodel.userMasterHelper.downloadUserDetails();
                bmodel.userMasterHelper.downloadDistributionDetails();
            }


            Vector<QuestionBO> mAllQuestions = new Vector<>();


            int questionSize;
            boolean isData;
            String oldUid = "";


            if (bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                for (SurveyBO sBO : getSurvey()) {
                    // delete transaction if exist

                    String sql = "SELECT uid FROM AnswerHeader WHERE"
                            + " surveyid = " + sBO.getSurveyID()
                            + " AND date = "
                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + " AND retailerid = " + QT(retailerid)
                            + " AND distributorID = " + distID
                            + " AND ModuleID = " + QT(surveyTypeStandardListId)
                            + " AND menucode=" + QT(menuCode)
                            + " AND SupervisiorId = "
                            + superwiserID
                            + " AND upload='N'"
                            + " AND userid=" + userid
                            + " AND frequency!='MULTIPLE'";

                    Cursor headerCursor = db.selectSQL(sql);

                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        oldUid = headerCursor.getString(0);
                        db.deleteSQL("AnswerHeader",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        db.deleteSQL("AnswerDetail",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        db.deleteSQL("AnswerImageDetail",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        db.deleteSQL("AnswerScoreDetail",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        headerCursor.close();
                    }
                    isData = false;

                    String uid = sBO.getSurveyID()
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + ""
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
                    // update joint call
                    bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                    String headerColumns = "uid, surveyid, date, retailerid,distributorID, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,userid,frequency,SignaturePath";

                    mAllQuestions.addAll(sBO.getQuestions());
                    questionSize = mAllQuestions.size();
                    double totalAchievedScore = 0;
                    for (int ii = 0; ii < questionSize; ii++) {

                        questionBO = mAllQuestions.get(ii);
                        if (sBO.getSurveyID() == questionBO.getSurveyid()) {

                            boolean isAnswered = false;

                            String detailColumns = "uid, retailerid, qid, qtype, answerid, answer,score,isExcluded,surveyid,isSubQuest";
                            String detailImageColumns = "uid, retailerid, qid,imgName";

                            String values1 = QT(uid) + "," + QT(retailerid)
                                    + "," + questionBO.getQuestionID() + ","
                                    + QT(questionBO.getQuestionTypeId() + "");

                            String values2 = QT(uid) + "," + QT(retailerid)
                                    + "," + questionBO.getQuestionID();
                            int answerSize = questionBO.getSelectedAnswerIDs()
                                    .size();

                            int weight;
                            if (questionBO.isExcludeQuestionWeight())
                                weight = 1;
                            else
                                weight = 0;

                            double optionScore = 0;
                            for (int j = 0; j < answerSize; j++) {

                                //option wise score needed here for MULTI SELECTION
                                // other questions single selection only so updated in questionBO
                                if ("MULTISELECT".equals(questionBO.getQuestionType()))
                                    optionScore = getScoreForAnswerId(questionBO, questionBO.getSelectedAnswerIDs().get(j));
                                else
                                    optionScore = questionBO.getQuestScore();

                                if ("TEXT".equals(questionBO.getQuestionType())
                                        || "FREE_TEXT".equals(questionBO.getQuestionType())
                                        || "NUM".equals(questionBO.getQuestionType())
                                        || "PERC".equals(questionBO.getQuestionType())
                                        || "EMAIL".equals(questionBO.getQuestionType())
                                        || "DATE".equals(questionBO.getQuestionType())
                                        || "PH_NO".equals(questionBO.getQuestionType())
                                        || "DECIMAL".equals(questionBO.getQuestionType())
                                        && !questionBO.getSelectedAnswer().isEmpty()) {
                                    String detailvalues = values1
                                            + ","
                                            + questionBO.getSelectedAnswerIDs()
                                            .get(j)
                                            + ","
                                            + DatabaseUtils
                                            .sqlEscapeString(questionBO
                                                    .getSelectedAnswer().get(j))
                                            + "," + optionScore
                                            + "," + weight
                                            + "," + questionBO.getSurveyid()
                                            + "," + questionBO.getIsSubQuestion();

                                    db.insertSQL("AnswerDetail", detailColumns,
                                            detailvalues);
                                    isData = true;
                                    isAnswered = true;

                                } else if (questionBO.getSelectedAnswerIDs().get(j) > 0) {
                                    String detailvalues = values1
                                            + ","
                                            + questionBO.getSelectedAnswerIDs()
                                            .get(j)
                                            + ","
                                            + DatabaseUtils
                                            .sqlEscapeString(questionBO
                                                    .getSelectedAnswer().get(j))
                                            + "," + optionScore
                                            + "," + weight
                                            + "," + questionBO.getSurveyid()
                                            + "," + questionBO.getIsSubQuestion();

                                    db.insertSQL("AnswerDetail", detailColumns,
                                            detailvalues);
                                    isData = true;
                                    isAnswered = true;
                                }

                            }
                            for (int k = 0; k < questionBO.getImageNames().size(); k++) {
                                String detailImageValues = values2
                                        + ","
                                        + bmodel.QT(questionBO.getImageNames()
                                        .get(k));

                                db.insertSQL("AnswerImageDetail", detailImageColumns,
                                        detailImageValues);
                            }

                            if (isAnswered) {
                                if ("OPT".equals(questionBO.getQuestionType())
                                        || "MULTISELECT".equals(questionBO.getQuestionType())) {
                                    //Insert Answer score detail
                                    double score = ((questionBO.getMaxScore() > 0 && questionBO.getQuestScore() > questionBO.getMaxScore()) ? questionBO.getMaxScore() : questionBO.getQuestScore());
                                    totalAchievedScore += score;
                                    String detailvalues = QT(uid) + "," + questionBO.getSurveyid()
                                            + "," + questionBO.getQuestionID() + ","
                                            + score;
                                    db.insertSQL("AnswerScoreDetail", "uid,surveyid,qid,score",
                                            detailvalues);
                                }
                            }

                        }

                    }
                    if (isData) {

                        Commons.print("In Survey Save," + "" + remarkDone);

                        for (SurveyBO qBO : getSurvey()) {
                            if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                String headerValues = QT(uid) + ","
                                        + sBO.getSurveyID() + ","
                                        + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                        + QT(retailerid) + "," + distID + "," + QT(surveyTypeStandardListId) + ","
                                        + superwiserID
                                        + "," + totalAchievedScore + "," + qBO.getTargtScore()
                                        + "," + QT(menuCode)
                                        + "," + qBO.getBonusScoreAchieved()
                                        + "," + qBO.getMaxBonusScore()
                                        + "," + QT(remarkDone) + "," + QT(type) + ",0,''"
                                        + "," + userid
                                        + "," + QT(sBO.getSurveyFreq())
                                        + "," + QT(getSignaturePath());
                                qBO.setSignaturePath(getSignaturePath());
                                db.insertSQL("AnswerHeader", headerColumns, headerValues);
                            }

                        }
                    }
                }


            } else {

                for (SurveyBO sBO : getSurvey()) {
                    if (sBO.getSurveyID() == mSelectedSurvey) {
                        // delete transaction if exist
                        String sql = "SELECT uid FROM AnswerHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                                + " AND retailerid = " + QT(retailerid)
                                + " AND distributorID = " + distID
                                + " AND ModuleID = " + QT(surveyTypeStandardListId)
                                + " AND menucode=" + QT(menuCode)
                                + " AND upload='N'" + " AND SupervisiorId = "
                                + superwiserID
                                + " AND userid=" + userid
                                + " AND frequency!='MULTIPLE'";


                        Cursor headerCursor = db.selectSQL(sql);

                        if (headerCursor.getCount() > 0) {

                            headerCursor.moveToNext();
                            oldUid = headerCursor.getString(0);
                            headerCursor.getString(0);
                            db.deleteSQL("AnswerHeader",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            db.deleteSQL("AnswerDetail",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            db.deleteSQL("AnswerImageDetail",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            db.deleteSQL("AnswerScoreDetail",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            headerCursor.close();
                        }

                        isData = false;
                        String uid = sBO.getSurveyID()
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid() + ""
                                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                        // update joint call
                        bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                        String headerColumns = "uid, surveyid, date, retailerid,DistributorID, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,userid,frequency,ridSF,VisitId,SignaturePath";

                        mAllQuestions.addAll(sBO.getQuestions());
                        questionSize = mAllQuestions.size();

                        double totalAchievedScore = 0;
                        for (int ii = 0; ii < questionSize; ii++) {

                            questionBO = mAllQuestions.get(ii);

                            if (sBO.getSurveyID() ==
                                    questionBO.getSurveyid()) {

                                boolean isAnswered = false;

                                String detailColumns = "uid, retailerid, qid, qtype, answerid, answer,score,isExcluded,surveyid,isSubQuest";
                                String detailImageColumns = "uid, retailerid, qid,imgName";

                                String values1 = QT(uid) + "," + QT(retailerid)
                                        + "," + questionBO.getQuestionID() + ","
                                        + QT(questionBO.getQuestionTypeId() + "");

                                String values2 = QT(uid) + "," + QT(retailerid)
                                        + "," + questionBO.getQuestionID();
                                int answerSize = questionBO.getSelectedAnswerIDs()
                                        .size();

                                int weight;
                                if (questionBO.isExcludeQuestionWeight())
                                    weight = 1;
                                else
                                    weight = 0;

                                double optionScore = 0;
                                for (int j = 0; j < answerSize; j++) {

                                    //option wise score needed here for MULTI SELECTION
                                    // other questions single selection only so updated in questionBO
                                    if ("MULTISELECT".equals(questionBO.getQuestionType()))
                                        optionScore = getScoreForAnswerId(questionBO, questionBO.getSelectedAnswerIDs().get(j));
                                    else
                                        optionScore = questionBO.getQuestScore();

                                    if ("TEXT".equals(questionBO.getQuestionType())
                                            || "FREE_TEXT".equals(questionBO.getQuestionType())
                                            || "NUM".equals(questionBO.getQuestionType())
                                            || "PERC".equals(questionBO.getQuestionType())
                                            || "EMAIL".equals(questionBO.getQuestionType())
                                            || "DATE".equals(questionBO.getQuestionType())
                                            || "PH_NO".equals(questionBO.getQuestionType())
                                            || "DECIMAL".equals(questionBO.getQuestionType())
                                            && !questionBO.getSelectedAnswer().isEmpty()) {
                                        String detailvalues = values1
                                                + ","
                                                + questionBO.getSelectedAnswerIDs()
                                                .get(j)
                                                + ","
                                                + DatabaseUtils
                                                .sqlEscapeString(questionBO
                                                        .getSelectedAnswer().get(j))
                                                + "," + optionScore
                                                + "," + weight
                                                + "," + questionBO.getSurveyid()
                                                + "," + questionBO.getIsSubQuestion();

                                        db.insertSQL("AnswerDetail", detailColumns,
                                                detailvalues);
                                        isData = true;
                                        isAnswered = true;

                                    } else if (questionBO.getSelectedAnswerIDs().get(j) > 0) {
                                        String detailvalues = values1
                                                + ","
                                                + questionBO.getSelectedAnswerIDs()
                                                .get(j)
                                                + ","
                                                + DatabaseUtils
                                                .sqlEscapeString(questionBO
                                                        .getSelectedAnswer().get(j))
                                                + "," + optionScore
                                                + "," + weight
                                                + "," + questionBO.getSurveyid()
                                                + "," + questionBO.getIsSubQuestion();

                                        db.insertSQL("AnswerDetail", detailColumns,
                                                detailvalues);
                                        isData = true;
                                        isAnswered = true;
                                    }

                                }
                                for (int k = 0; k < questionBO.getImageNames().size(); k++) {
                                    String detailImageValues = values2
                                            + ","
                                            + bmodel.QT(questionBO.getImageNames()
                                            .get(k));

                                    db.insertSQL("AnswerImageDetail", detailImageColumns,
                                            detailImageValues);
                                }

                                if (isAnswered) {
                                    //Insert Answer score detail
                                    if ("OPT".equals(questionBO.getQuestionType())
                                            || "MULTISELECT".equals(questionBO.getQuestionType())) {
                                        double score = ((questionBO.getMaxScore() > 0 && questionBO.getQuestScore() > questionBO.getMaxScore()) ? questionBO.getMaxScore() : questionBO.getQuestScore());
                                        totalAchievedScore += score;
                                        String detailvalues = QT(uid) + "," + questionBO.getSurveyid()
                                                + "," + questionBO.getQuestionID() + ","
                                                + score;
                                        db.insertSQL("AnswerScoreDetail", "uid,surveyid,qid,score",
                                                detailvalues);
                                    }
                                }
                            }
                        }
                        if (isData) {

                            Commons.print("In Survey Save," + "" + remarkDone);

                            for (SurveyBO surBO : getSurvey()) {
                                if (surBO.getSurveyID() == sBO.getSurveyID()) {

                                    String headerValues = QT(uid) + ","
                                            + surBO.getSurveyID() + ","
                                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                            + QT(retailerid) + "," + distID + "," + QT(surveyTypeStandardListId) + ","
                                            + superwiserID
                                            + "," + totalAchievedScore + "," + sBO.getTargtScore()
                                            + "," + QT(menuCode)
                                            + "," + sBO.getBonusScoreAchieved()
                                            + "," + sBO.getMaxBonusScore()
                                            + "," + QT(remarkDone) + "," + QT(type) + ",0,''"
                                            + "," + userid
                                            + "," + QT(sBO.getSurveyFreq())
                                            + "," + QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF())
                                            + "," + bmodel.getAppDataProvider().getUniqueId()
                                            + "," + QT(getSignaturePath());

                                    surBO.setSignaturePath(getSignaturePath());
                                    db.insertSQL("AnswerHeader", headerColumns, headerValues);
                                }

                            }

                        }

                        break;
                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private double getScoreForAnswerId(QuestionBO questionBO, int answerId) {
        try {
            for (AnswerBO answerBO : questionBO.getAnswersList()) {
                if (answerBO.getAnswerID() == answerId) {
                    return answerBO.getScore();
                }
            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return 0;
    }


    public void deleteUnusedImages() {

        Vector<QuestionBO> mAllQuestions = new Vector<>();

        int questionSize;

        if (bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
            for (SurveyBO sBO : getSurvey()) {

                mAllQuestions.addAll(sBO.getQuestions());
                questionSize = mAllQuestions.size();
                for (int ii = 0; ii < questionSize; ii++) {

                    questionBO = mAllQuestions.get(ii);

                    if (sBO.getSurveyID() == questionBO.getSurveyid()) {

                        if ("TEXT".equals(questionBO.getQuestionType()) ||
                                "NUM".equals(questionBO.getQuestionType()) ||
                                "PERC".equals(questionBO.getQuestionType())) {
                            if (questionBO.getSelectedAnswer().isEmpty()
                                    && !questionBO.getImageNames().isEmpty()) {

                                for (int k = 0; k < questionBO.getImageNames().size(); k++) {

                                    String fileName = questionBO.getImageNames()
                                            .get(k).substring(questionBO.getImageNames()
                                                    .get(k).lastIndexOf("/") + 1);

                                    deleteFiles(fileName);
                                    questionBO.getImageNames().remove(k);
                                    k--;
                                }
                            }
                        } else {
                            if (questionBO.getSelectedAnswerIDs().size() == 0 && questionBO.getImageNames().size() > 0) {

                                for (int k = 0; k < questionBO.getImageNames().size(); k++) {

                                    String fileName = questionBO.getImageNames()
                                            .get(k).substring(questionBO.getImageNames()
                                                    .get(k).lastIndexOf("/") + 1);

                                    deleteFiles(fileName);
                                    questionBO.getImageNames().remove(k);
                                    k--;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (SurveyBO sBO : getSurvey()) {

                if (sBO.getSurveyID() == mSelectedSurvey) {
                    mAllQuestions.addAll(sBO.getQuestions());
                    questionSize = mAllQuestions.size();

                    for (int ii = 0; ii < questionSize; ii++) {

                        questionBO = mAllQuestions.get(ii);

                        if (sBO.getSurveyID() == questionBO.getSurveyid()) {

                            if ("TEXT".equals(questionBO.getQuestionType()) ||
                                    "NUM".equals(questionBO.getQuestionType()) ||
                                    "PERC".equals(questionBO.getQuestionType())) {
                                if (questionBO.getSelectedAnswer().isEmpty()
                                        && !questionBO.getImageNames().isEmpty()) {

                                    for (int k = 0; k < questionBO.getImageNames().size(); k++) {

                                        String fileName = questionBO.getImageNames()
                                                .get(k).substring(questionBO.getImageNames()
                                                        .get(k).lastIndexOf("/") + 1);

                                        deleteFiles(fileName);
                                        questionBO.getImageNames().remove(k);
                                        k--;

                                    }
                                }
                            } else {
                                if (questionBO.getSelectedAnswerIDs().size() == 0 && questionBO.getImageNames().size() > 0) {

                                    for (int k = 0; k < questionBO.getImageNames().size(); k++) {

                                        String fileName = questionBO.getImageNames()
                                                .get(k).substring(questionBO.getImageNames()
                                                        .get(k).lastIndexOf("/") + 1);

                                        deleteFiles(fileName);
                                        questionBO.getImageNames().remove(k);
                                        k--;
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    }


    private void deleteFiles(String filename) {
        File folder = new File(FileUtils.photoFolderPath + "/");

        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Sucess");
            }
        }
    }


    public void loadSurveyAnswers(int supervisiorId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        String retailerid = "0";
        int distID = 0;
        if (!isFromHomeScreen()) {
            retailerid = bmodel.getRetailerMasterBO().getRetailerID();
            distID = bmodel.getRetailerMasterBO().getDistributorId();
        }


        int qsize = 0;


        int surveyId;
        String uid;
        int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

        boolean isLocalData = false;// to check whether transaction record is there or not

        if (getSurvey() != null)
            for (SurveyBO sBO : getSurvey()) {

                surveyId = sBO.getSurveyID();
                Vector<QuestionBO> mAllQuestions = new Vector<>();

                uid = "0";

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT uid,SignaturePath FROM AnswerHeader  ");
                sb.append(" WHERE retailerid = " + QT(retailerid) + " AND distributorID = " + distID + " AND surveyid = ");
                sb.append(+surveyId + " AND date = ");
                sb.append(QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                sb.append(" and upload='N' and supervisiorId = " + supervisiorId + " AND userid = " + userid);
                sb.append(" and frequency!='MULTIPLE'");

                Cursor answerHeaderCursor = db.selectSQL(sb.toString());
                if (answerHeaderCursor != null) {
                    if (answerHeaderCursor.moveToNext()) {
                        uid = answerHeaderCursor.getString(0);
                        sBO.setSignaturePath(answerHeaderCursor.getString(1));
                        isLocalData = true;
                    }
                    answerHeaderCursor.close();
                }
                qsize = sBO.getQuestions().size();
                mAllQuestions.addAll(sBO.getQuestions());

                if (!"0".equals(uid)) {
//                qsize = sBO.getQuestions().size();
//                mAllQuestions.addAll(sBO.getQuestions());

                    //To clear default option id
                    for (QuestionBO questionBO : sBO.getQuestions())
                        questionBO.setSelectedAnswerIDs(new ArrayList<>());

                    boolean check = false;
                    String sql1 = "SELECT qid, answerid, Answer,score FROM AnswerDetail WHERE"
                            + " uid = " + QT(uid) + " and isSubQuest=0";
                    Cursor c = db.selectSQL(sql1);
                    if (c != null) {

                        while (c.moveToNext()) {


                            for (int ii = 0; ii < qsize; ii++) {
                                questionBO = mAllQuestions.get(ii);
                                if (surveyId == questionBO.getSurveyid()
                                        && questionBO.getQuestionID() == c
                                        .getInt(0)) {
                                    questionBO.setSelectedAnswerID(c.getInt(1));
                                    questionBO.setSelectedAnswer(c.getString(2));
                                    if (questionBO.getQuestionID() == c.getInt(0))
                                        questionBO.setQuestScore(questionBO.getQuestScore() + c.getFloat(3));
                                    else
                                        questionBO.setQuestScore(c.getFloat(3));
                                    check = true;
                                    break;
                                }
                            }
                        }
                        c.close();
                    }
                    if (check) {
                        String sql2 = "SELECT qid, answerid, Answer,score FROM AnswerDetail WHERE"
                                + " uid = " + QT(uid) + " and isSubQuest=1";

                        StringBuilder sb1 = new StringBuilder();
                        int counter = 0;
                        Cursor c1 = db.selectSQL(sql2);
                        if (c1 != null) {

                            while (c1.moveToNext()) {
                                for (QuestionBO subqBO : getDependentQuestions()) {
                                    questions = new ArrayList<>();
                                    if (subqBO.getQuestionID() == c1.getInt(0)) {
                                        subqBO.setSurveyid(surveyId);
                                        subqBO.setIsSubQuestion(1);
                                        subqBO.setSelectedAnswerID(c1.getInt(1));
                                        subqBO.setSelectedAnswer(c1.getString(2));

                                        if (subqBO.getQuestionID() == c1.getInt(0))
                                            subqBO.setQuestScore(subqBO.getQuestScore() + c1.getFloat(3));
                                        else
                                            subqBO.setQuestScore(c1.getFloat(3));

                                        sb1.append("Select IFNULL(ImgName,'') FROM AnswerImageDetail WHERE uid = ");
                                        sb1.append(QT(uid));
                                        sb1.append(" and qid=");
                                        sb1.append(c1.getInt(0));
                                        Cursor c3 = db.selectSQL(sb1.toString());

                                        if (c3 != null) {
                                            while (c3.moveToNext()) {
                                                if (counter == 0) {
                                                    subqBO.setImage1Captured(true);
                                                    String imgName = null;
                                                    try {
                                                        imgName = c3.getString(0).substring(c3.getString(0).lastIndexOf("/"));
                                                    } catch (Exception e) {
                                                        imgName = c3.getString(0);
                                                    }
                                                    subqBO.setImage1Path(FileUtils.photoFolderPath + imgName);
                                                    subqBO.setImage2Captured(false);
                                                    subqBO.setImage2Path("");
                                                    counter++;
                                                } else if (counter == 1) {
                                                    String imgName = null;
                                                    try {
                                                        imgName = c3.getString(0).substring(c3.getString(0).lastIndexOf("/"));
                                                    } catch (Exception e) {
                                                        imgName = c3.getString(0);
                                                    }
                                                    subqBO.setImage2Captured(true);
                                                    subqBO.setImage2Path(FileUtils.photoFolderPath + imgName);
                                                    break;
                                                }
                                            }
                                            c3.close();
                                        } else {
                                            questionBO.setImage1Captured(false);
                                            questionBO.setImage1Path("");
                                            questionBO.setImage2Captured(false);
                                            questionBO.setImage2Path("");
                                        }
                                        sb1.setLength(0);
                                        counter = 0;

                                        questions.add(subqBO);
                                        sBO.getQuestions().addAll(questions);
                                    }


                                }
                            }
                            c1.close();
                        }

                    }


                }

                if (!"0".equals(uid)) {


                    String sql1 = "SELECT qid, imgName FROM AnswerImageDetail WHERE"
                            + " uid = " + QT(uid);
                    Cursor c = db.selectSQL(sql1);
                    if (c != null) {
                        while (c.moveToNext()) {
                            for (int ii = 0; ii < qsize; ii++) {
                                questionBO = mAllQuestions.get(ii);
                                if (surveyId == questionBO.getSurveyid()
                                        && questionBO.getQuestionID() == c
                                        .getInt(0)) {
                                    if (questionBO.getImageNames() != null && questionBO.getImageNames().size() > 0) {
                                        for (int i = 0; i < questionBO.getImageNames().size(); i++) {
                                            if (!questionBO.getImageNames().get(i).equals(c.getString(1))) {
                                                questionBO.getImageNames().add(c.getString(1));
                                            }
                                        }
                                    } else {
                                        questionBO.getImageNames().add(c.getString(1));
                                    }
                                    break;
                                }
                            }
                        }
                        c.close();
                    }


                }
            }

        //Load last visit data
        if (!isLocalData && bmodel.configurationMasterHelper.IS_SURVEY_RETAIN_LAST_VISIT_TRAN) {

            String query = "SELECT surveyId,qid, answerid, Answer,isSubQuest FROM LastVisitSurvey WHERE"
                    + " retailerID = " + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {

                    if (getSurvey() != null)
                        for (SurveyBO surveyBO : getSurvey()) {
                            if (surveyBO.getSurveyID() == c.getInt(0)) {

                                //Load Main Question Last transaction data
                                for (QuestionBO questionBO : surveyBO.getQuestions()) {
                                    if (questionBO.getQuestionID() == c.getInt(1)) {

                                        questionBO.setSelectedAnswerID(c.getInt(2));
                                        questionBO.setSelectedAnswer(c.getString(3));

                                    }
                                }


                                //Load sub question Last transaction data
                                for (QuestionBO subQuestioBo : getDependentQuestions()) {
                                    if (subQuestioBo.getQuestionID() == c.getInt(1)) {
                                        subQuestioBo.setIsSubQuestion(1);
                                        subQuestioBo.setSelectedAnswerID(c.getInt(2));
                                        subQuestioBo.setSelectedAnswer(c.getString(3));
                                        surveyBO.getQuestions().add(subQuestioBo);
                                    }
                                }


                            }

                        }

                }
                c.close();
            }
        }


        db.closeDB();
    }

    public void loadNewRetailerSurveyAnswers(String retailerId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        String uid = "0";
        for (SurveyBO bo : getSurvey()) {

            String sql = "SELECT uid FROM NewRetailerSurveyResultHeader WHERE"
                    + " retailerid = " + QT(retailerId);

            Cursor answerHeaderCursor = db.selectSQL(sql);
            if (answerHeaderCursor != null) {
                if (answerHeaderCursor.moveToNext()) {
                    uid = answerHeaderCursor.getString(0);
                }
                answerHeaderCursor.close();
            }

            if (!uid.equals("0")) {
                String sql1 = "SELECT qid, answerid, Answer,surveyid,isSubQuest FROM NewRetailerSurveyResultDetail WHERE"
                        + " uid = " + QT(uid);
                Cursor c = db.selectSQL(sql1);
                //To clear default option id
                for (QuestionBO questionBO : bo.getQuestions())
                    questionBO.setSelectedAnswerIDs(new ArrayList<>());
                if (c != null) {

                    while (c.moveToNext()) {
                        if (c.getInt(4) == 0) {
                            for (SurveyBO surveyBO : getSurvey()) {
                                if (surveyBO.getSurveyID() == c.getInt(3)) {

                                    for (QuestionBO questionBO : surveyBO.getQuestions()) {
                                        if (questionBO.getQuestionID() == c.getInt(0)) {

                                            questionBO.setSelectedAnswerID(c.getInt(1));
                                            questionBO.setSelectedAnswer(c.getString(2));

                                        }
                                    }
                                }

                            }
                        } else {

                            for (QuestionBO subqBO : getDependentQuestions()) {
                                if (subqBO.getQuestionID() == c.getInt(0)) {

                                    subqBO.setSelectedAnswerID(c.getInt(1));
                                    subqBO.setSelectedAnswer(c.getString(2));

                                }
                            }
                        }

                    }
                    c.close();
                }
            }

        }

        db.closeDB();
    }

    public void loadCSSurveyAnswers(int supervisiorId, String referenceId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        String retailerid;
        retailerid = bmodel.getRetailerMasterBO().getRetailerID();
        int qsize = 0;


        int surveyId;
        String uid;

        for (SurveyBO sBO : getSurvey()) {

            surveyId = sBO.getSurveyID();
            Vector<QuestionBO> mAllQuestions = new Vector<>();

            uid = "0";

            String sql = "SELECT uid FROM AnswerHeader WHERE"
                    + " retailerid = " + QT(retailerid) + " AND surveyid = "
                    + surveyId + " AND date = "
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and upload='I' and supervisiorId = " + supervisiorId
                    + " and refid='" + referenceId + "'";

            Cursor answerHeaderCursor = db.selectSQL(sql);
            if (answerHeaderCursor != null) {
                if (answerHeaderCursor.moveToNext()) {
                    uid = answerHeaderCursor.getString(0);
                }
                answerHeaderCursor.close();
            }

            if (!"0".equals(uid)) {
                qsize = sBO.getQuestions().size();
                mAllQuestions.addAll(sBO.getQuestions());
                boolean check = false;
                String sql1 = "SELECT qid, answerid, Answer FROM AnswerDetail WHERE"
                        + " uid = " + QT(uid) + " and isSubQuest=0";
                Cursor c = db.selectSQL(sql1);
                if (c != null) {

                    while (c.moveToNext()) {


                        for (int ii = 0; ii < qsize; ii++) {
                            questionBO = mAllQuestions.get(ii);
                            if (surveyId == questionBO.getSurveyid()
                                    && questionBO.getQuestionID() == c
                                    .getInt(0)) {
                                questionBO.setSelectedAnswerID(c.getInt(1));
                                questionBO.setSelectedAnswer(c.getString(2));
                                check = true;
                                break;
                            }
                        }
                    }
                    c.close();
                }
                if (check) {
                    String sql2 = "SELECT qid, answerid, Answer FROM AnswerDetail WHERE"
                            + " uid = " + QT(uid) + " and isSubQuest=1";
                    Cursor c1 = db.selectSQL(sql2);
                    if (c1 != null) {

                        while (c1.moveToNext()) {
                            for (QuestionBO subqBO : getDependentQuestions()) {
                                questions = new ArrayList<>();
                                if (subqBO.getQuestionID() == c1.getInt(0)) {
                                    subqBO.setSurveyid(surveyId);
                                    subqBO.setIsSubQuestion(1);
                                    subqBO.setSelectedAnswerID(c1.getInt(1));
                                    subqBO.setSelectedAnswer(c1.getString(2));
                                    questions.add(subqBO);
                                    sBO.getQuestions().addAll(questions);
                                }


                            }
                        }
                        c1.close();
                    }
                }


            }

            if (!"0".equals(uid)) {

                String sql1 = "SELECT qid, imgName FROM AnswerImageDetail WHERE"
                        + " uid = " + QT(uid);
                Cursor c = db.selectSQL(sql1);
                if (c != null) {
                    while (c.moveToNext()) {
                        for (int ii = 0; ii < qsize; ii++) {
                            questionBO = mAllQuestions.get(ii);
                            if (surveyId == questionBO.getSurveyid()
                                    && questionBO.getQuestionID() == c
                                    .getInt(0)) {
                                if (questionBO.getImageNames() != null && questionBO.getImageNames().size() > 0) {
                                    for (int i = 0; i < questionBO.getImageNames().size(); i++) {
                                        if (!questionBO.getImageNames().get(i).equals(c.getString(1))) {
                                            questionBO.getImageNames().add(c.getString(1));
                                        }
                                    }
                                } else {
                                    questionBO.getImageNames().add(c.getString(1));
                                }
                                break;
                            }
                        }
                    }
                    c.close();
                }
            }
        }

        db.closeDB();
    }

    public void loadSuperVisorSurveyAnswers(int supervisiorId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        String retailerid = "0";
        int distID = 0;

        if (!isFromHomeScreen()) {
            retailerid = bmodel.getRetailerMasterBO().getRetailerID();
            distID = bmodel.getRetailerMasterBO().getDistributorId();
        }


        int surveyId;
        String uid;

        int qsize;


        for (SurveyBO sBO : getSurvey()) {

            surveyId = sBO.getSurveyID();
            Vector<QuestionBO> mAllQuestions = new Vector<>();
            qsize = sBO.getQuestions().size();
            mAllQuestions.addAll(sBO.getQuestions());

            for (int ii = 0; ii < qsize; ii++) {
                questionBO = mAllQuestions.get(ii);
                if (surveyId == questionBO.getSurveyid()) {
                    questionBO.getSelectedAnswer().clear();
                    questionBO.getSelectedAnswerIDs().clear();

                }
            }

            uid = "0";

            String sql = "SELECT uid FROM AnswerHeader WHERE"
                    + " retailerid = " + QT(retailerid) + " AND distributorID = " + distID + " AND surveyid = "
                    + surveyId + " AND date = "
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and upload='N' and supervisiorId = " + supervisiorId;

            Cursor answerHeaderCursor = db.selectSQL(sql);
            if (answerHeaderCursor != null) {
                if (answerHeaderCursor.moveToNext()) {
                    uid = answerHeaderCursor.getString(0);
                }
                answerHeaderCursor.close();
            }

            if (!"0".equals(uid)) {

                //To clear default option id
                for (QuestionBO questionBO : sBO.getQuestions())
                    questionBO.setSelectedAnswerIDs(new ArrayList<>());

                boolean check = false;
                String sql1 = "SELECT qid, answerid, Answer FROM AnswerDetail WHERE"
                        + " uid = " + QT(uid) + " and isSubQuest=0";
                Cursor c = db.selectSQL(sql1);
                if (c != null) {

                    while (c.moveToNext()) {
                        check = false;
                        for (int ii = 0; ii < qsize; ii++) {
                            questionBO = mAllQuestions.get(ii);
                            if (surveyId == questionBO.getSurveyid()
                                    && questionBO.getQuestionID() == c
                                    .getInt(0)) {
                                questionBO.setSelectedAnswerID(c.getInt(1));
                                questionBO.setSelectedAnswer(c.getString(2));
                                check = true;
                                break;
                            }
                        }
                    }
                    c.close();
                }
                if (check) {
                    String sql2 = "SELECT qid, answerid, Answer FROM AnswerDetail WHERE"
                            + " uid = " + QT(uid) + " and isSubQuest=1";
                    Cursor c1 = db.selectSQL(sql2);
                    if (c1 != null) {

                        while (c1.moveToNext()) {
                            for (QuestionBO subqBO : getDependentQuestions()) {
                                questions = new ArrayList<>();
                                if (subqBO.getQuestionID() == c1.getInt(0)) {
                                    subqBO.setSurveyid(surveyId);
                                    subqBO.setIsSubQuestion(1);
                                    subqBO.setSelectedAnswerID(c1.getInt(1));
                                    subqBO.setSelectedAnswer(c1.getString(2));
                                    questions.add(subqBO);
                                    sBO.getQuestions().addAll(questions);
                                }


                            }
                        }
                        c1.close();
                    }
                }
            }
        }

        db.closeDB();
    }

    public int getSuperVisiroID() {
        int supervisiorID = 0;
        if (bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList() != null)
            for (UserMasterBO user : bmodel.userMasterHelper.getUserMasterBO()
                    .getJoinCallUserList()) {
                if (user.getIsJointCall() == 1) {
                    supervisiorID = user.getUserid();
                    break;
                }

            }
        return supervisiorID;
    }

    public String getChannelidForSurvey() {
        String sql;
        String sql1 = "";
        String str = "";
        int channelid = 0;
        try {
            if (!isFromHomeScreen() && bmodel.getRetailerMasterBO() != null)
                channelid = bmodel.getRetailerMasterBO().getSubchannelid();


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelid + ") as contentlevel  from ChannelLevel");
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".ChId";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select LM1.ChId," + sql1 + "  from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + channelid;
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                    if (str.endsWith(","))
                        str = str.substring(0, str.length() - 1);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return str;
    }


    /**
     * Save the Transaction as Survey Wise. NewRetailerSurveyResultHeader - used to hold the
     * survey detail.
     * NewRetailerSurveyResultDetail - used to hold the question and anser detail.
     */
    public void saveAnswerNewRetailer(String menuCode, int screenMode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            if (screenMode == 2) {
                //Edit mode, then deleting old one
                db.deleteSQL("NewRetailerSurveyResultHeader", "retailerId=" + bmodel.QT(bmodel.newOutletHelper.getRetailerId_edit()), false);
                db.deleteSQL("NewRetailerSurveyResultDetail", "retailerId=" + bmodel.QT(bmodel.newOutletHelper.getRetailerId_edit()), false);
            }

            String retailerid;
            String type = "RETAILER";

            retailerid = bmodel.newOutletHelper.getId();

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                bmodel.userMasterHelper.downloadUserDetails();
                bmodel.userMasterHelper.downloadDistributionDetails();
            }


            Vector<QuestionBO> mAllQuestions = new Vector<>();


            int questionSize;
            boolean isData;
            String oldUid = "";


            if (bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                for (SurveyBO sBO : getSurvey()) {
                    // delete transaction if exist

                    String sql = "SELECT uid FROM NewRetailerSurveyResultHeader WHERE"
                            + " surveyid = " + sBO.getSurveyID()
                            + " AND date = "
                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + " AND retailerid = " + QT(retailerid)
                            + " AND menucode=" + QT(menuCode)
                            + " AND userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + " AND upload='N'";

                    Cursor headerCursor = db.selectSQL(sql);

                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        oldUid = headerCursor.getString(0);
                        db.deleteSQL("NewRetailerSurveyResultHeader",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        db.deleteSQL("NewRetailerSurveyResultDetail",
                                "uid = " + QT(headerCursor.getString(0)), false);
                        headerCursor.close();
                    }
                    isData = false;

                    String uid = sBO.getSurveyID()
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + ""
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
                    // update joint call
                    bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                    String headerColumns = "uid,surveyid,date,retailerid,Remark,userid,MenuCode,type,refid";

                    mAllQuestions.addAll(sBO.getQuestions());
                    questionSize = mAllQuestions.size();
                    for (int ii = 0; ii < questionSize; ii++) {

                        questionBO = mAllQuestions.get(ii);
                        if (sBO.getSurveyID() == questionBO.getSurveyid()) {

                            String detailColumns = "uid, retailerid, qid, qtype, answerid, answer, surveyid,score,NoReply,isSubQuest";

                            String values1 = QT(uid) + "," + QT(retailerid)
                                    + "," + questionBO.getQuestionID() + ","
                                    + QT(questionBO.getQuestionTypeId() + "");


                            int answerSize = questionBO.getSelectedAnswerIDs()
                                    .size();

                            int weight;
                            if (questionBO.isExcludeQuestionWeight())
                                weight = 1;
                            else
                                weight = 0;

                            for (int j = 0; j < answerSize; j++) {
                                String detailvalues = values1
                                        + ","
                                        + questionBO.getSelectedAnswerIDs()
                                        .get(j)
                                        + ","
                                        + DatabaseUtils
                                        .sqlEscapeString(questionBO
                                                .getSelectedAnswer().get(j))
                                        + "," + questionBO.getSurveyid()
                                        + "," + questionBO.getQuestScore() + "," + weight + "," + questionBO.getIsSubQuestion();

                                db.insertSQL("NewRetailerSurveyResultDetail", detailColumns,
                                        detailvalues);
                                isData = true;

                            }
                        }

                    }
                    if (isData) {

                        Commons.print("In Survey Save," + "" + remarkDone);

                        for (SurveyBO qBO : getSurvey()) {
                            if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                String headerValues = QT(uid) + ","
                                        + sBO.getSurveyID() + ","
                                        + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                        + QT(retailerid) + ","
                                        + QT(remarkDone) + ","
                                        + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ","
                                        + QT(menuCode)
                                        + "," + QT(type) + ",''";

                                db.insertSQL("NewRetailerSurveyResultHeader", headerColumns, headerValues);
                            }

                        }
                    }
                }


            } else {

                for (SurveyBO sBO : getSurvey()) {
                    if (sBO.getSurveyID() == mSelectedSurvey) {
                        // delete transaction if exist
                        String sql = "SELECT uid FROM NewRetailerSurveyResultHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                                + " AND retailerid = " + QT(retailerid)
                                + " AND menucode=" + QT(menuCode)
                                + " AND userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + " AND upload='N'";
                        Cursor headerCursor = db.selectSQL(sql);

                        if (headerCursor.getCount() > 0) {

                            headerCursor.moveToNext();
                            oldUid = headerCursor.getString(0);
                            headerCursor.getString(0);
                            db.deleteSQL("NewRetailerSurveyResultHeader",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            db.deleteSQL("NewRetailerSurveyResultDetail",
                                    "uid = " + QT(headerCursor.getString(0)), false);
                            headerCursor.close();
                        }

                        isData = false;
                        String uid = sBO.getSurveyID()
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid() + ""
                                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                        // update joint call
                        bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                        String headerColumns = "uid,surveyid,date,retailerid,Remark,userid,MenuCode,type,refid";

                        mAllQuestions.addAll(sBO.getQuestions());
                        questionSize = mAllQuestions.size();

                        for (int ii = 0; ii < questionSize; ii++) {

                            questionBO = mAllQuestions.get(ii);
                            if (sBO.getSurveyID() == questionBO.getSurveyid()) {

                                String detailColumns = "uid, retailerid, qid, qtype, answerid, answer, surveyid,isSubQuest";

                                String values1 = QT(uid) + "," + QT(retailerid)
                                        + "," + questionBO.getQuestionID() + ","
                                        + QT(questionBO.getQuestionTypeId() + "");


                                int answerSize = questionBO.getSelectedAnswerIDs()
                                        .size();

                                for (int j = 0; j < answerSize; j++) {
                                    String detailvalues = values1
                                            + ","
                                            + questionBO.getSelectedAnswerIDs()
                                            .get(j)
                                            + ","
                                            + DatabaseUtils
                                            .sqlEscapeString(questionBO
                                                    .getSelectedAnswer().get(j))
                                            + "," + questionBO.getSurveyid() + "," + questionBO.getIsSubQuestion();

                                    db.insertSQL("NewRetailerSurveyResultDetail", detailColumns,
                                            detailvalues);
                                    isData = true;

                                }
                            }

                        }
                        if (isData) {

                            Commons.print("In Survey Save," + "" + remarkDone);

                            for (SurveyBO qBO : getSurvey()) {
                                if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                    String headerValues = QT(uid) + ","
                                            + sBO.getSurveyID() + ","
                                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                            + QT(retailerid) + ","
                                            + QT(remarkDone) + ","
                                            + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ","
                                            + QT(menuCode)
                                            + "," + QT(type) + ",''";

                                    db.insertSQL("NewRetailerSurveyResultHeader", headerColumns, headerValues);
                                }

                            }
                        }

                        break;
                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    //used for new retailer survey
    public boolean isSurveyAvaliable(String retailerId) {
        boolean isavailable = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select uid from NewRetailerSurveyResultHeader "
                    + "where retailerid = " + QT(retailerId)
                    + "AND Upload = " + QT("N");
            c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                isavailable = true;
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        return isavailable;
    }

    public void deleteNewRetailerSurvey(String retailerID) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            db.deleteSQL("NewRetailerSurveyResultDetail", "retailerid ="
                    + QT(retailerID), false);
            db.deleteSQL("NewRetailerSurveyResultHeader", "retailerid ="
                    + QT(retailerID), false);


            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public ArrayList<QuestionBO> getmQuestionData() {
        return mQuestionData;
    }

    public void setmQuestionData(ArrayList<QuestionBO> mQuestionData) {
        this.mQuestionData = mQuestionData;
    }

    public QuestionBO getQuestionBODragDrop() {
        return questionBODragDrop;
    }

    public void setQuestionBODragDrop(QuestionBO questionBODragDrop) {
        this.questionBODragDrop = questionBODragDrop;
    }

    public void loadSurveyConfig(String menucode) {
        try {
            this.SHOW_SMS_IN_SURVEY = false;
            this.SHOW_PHOTOCAPTURE_IN_SURVEY = false;
            this.SHOW_DRAGDROP_IN_SURVEY = false;
            this.ENABLE_MULTIPLE_PHOTO = false;
            this.SHOW_TOTAL_SCORE_IN_SURVEY = false;
            this.IS_SURVEY_ANSWER_ALL = false;
            this.IS_SURVEY_ANSWER_MANDATORY = false;

            DBUtil db;
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select menu_type from HhtModuleMaster where flag=1 and hhtcode='SURVEY07'and menu_type="
                            + bmodel.QT(menucode) + " and  ForSwitchSeller = 0");
            if (c != null) {
                while (c.moveToNext()) {
                    this.SHOW_SMS_IN_SURVEY = true;
                    this.smsmenutype = c.getString(0);
                }
                c.close();
            }

            c = db.selectSQL("select menu_type,RField from HhtModuleMaster where flag=1 and hhtcode='SURVEY06'and menu_type="
                    + bmodel.QT(menucode) + " and  ForSwitchSeller = 0");
            if (c != null) {
                while (c.moveToNext()) {
                    this.SHOW_PHOTOCAPTURE_IN_SURVEY = true;
                    this.photocapturemenutype = c.getString(0);
                }
                c.close();
            }
            // Survey12 to enable multiple photo capture
            c = db.selectSQL("select menu_type from HhtModuleMaster where flag=1 and hhtcode='SURVEY12'and menu_type="
                    + bmodel.QT(menucode) + " and  ForSwitchSeller = 0");
            if (c != null) {
                while (c.moveToNext()) {
                    this.ENABLE_MULTIPLE_PHOTO = true;
                    this.multiplePhotoCapture = c.getString(0);
                }
                c.close();
            }

            c = db.selectSQL("select * from HhtModuleMaster where flag=1 and hhtcode='SURVEY13'and menu_type="
                    + bmodel.QT(menucode) + " and  ForSwitchSeller = 0");
            if (c != null) {
                while (c.moveToNext()) {
                    this.SHOW_DRAGDROP_IN_SURVEY = true;
                }
                c.close();
            }

            c = db.selectSQL("select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_TOTAL_SCORE_IN_SURVEY) + " and Flag=1 and ForSwitchSeller = 0");
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_TOTAL_SCORE_IN_SURVEY = true;
                }
                c.close();
            }

            c = db.selectSQL("select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SURVEY_ANSWER_ALL) + " and Flag=1 and ForSwitchSeller = 0");
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.IS_SURVEY_ANSWER_ALL = true;
                }
                c.close();
            }

            c = db.selectSQL("select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SURVEY_ANSWER_MANDATORY) + " and Flag=1 and ForSwitchSeller = 0");
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.IS_SURVEY_ANSWER_MANDATORY = true;
                }
                c.close();
            }

            c = db.selectSQL("select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_SCORE_IN_SURVEY) + " and Flag=1 and ForSwitchSeller = 0");
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_SCORE_IN_SURVEY = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private String getParentHiearchy(int productId) {
        String parentHiearchy = "";

        try {
            DBUtil db;
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ParentHierarchy from ProductMaster where PID=" + productId);
            if (c != null && c.getCount() > 0) {
                if (c.moveToNext()) {
                    parentHiearchy = c.getString(0);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

        return parentHiearchy;
    }
}
