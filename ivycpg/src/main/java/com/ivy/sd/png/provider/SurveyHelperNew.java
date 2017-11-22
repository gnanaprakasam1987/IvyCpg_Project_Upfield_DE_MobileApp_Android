package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.survey.AnswerBO;
import com.ivy.sd.png.survey.QuestionBO;
import com.ivy.sd.png.survey.SurveyBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.util.ArrayList;
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

    ArrayList<String> mGroupIDList = new ArrayList<>();

    public String remarkDone = "N";

    public final static String cs_feedback_menucode = "MENU_SURVEY_CS";

    public static final String SURVEY_SL_TYPE = "SURVEY_TYPE";


    private SurveyHelperNew(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
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

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public ArrayList<SurveyBO> getSurvey() {
        return survey;
    }

    private ArrayList<QuestionBO> getDependentQuestions() {
        return subQuestions;
    }


    /**
     * Downlaod the surveyType standard list id.
     *
     * @param surveyType STANDARD|SPECIAL|NEW_RETAILER
     */
    public void downloadModuleId(String surveyType) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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


    public void downloadValidSurveyGroups(DBUtil db) {

        StringBuilder sb = new StringBuilder();

        ArrayList<String> retailerAttributes = bmodel.getAttributeParentListForCurrentRetailer();

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
                            if (!mGroupIDList.contains(c.getString(1) + c.getString(0))) {
                                mGroupIDList.add(c.getString(1) + c.getString(0));
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
                if (!mGroupIDList.contains(lastGroupId + "" + lastSurveyId)) {
                    mGroupIDList.add(lastGroupId + "" + lastSurveyId);
                }
            }

        }
        c.close();
    }

    private boolean isSurveyApplicable(int surveyid, int groupId, int parentId) {


        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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

    private ArrayList<Integer> mValidSurveyIds;

    private String getValidSurveyIds() {

        DBUtil db = null;
        String mSurveyIds = "";
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            downloadValidSurveyGroups(db);

            StringBuilder sb = new StringBuilder();
            String locIdScheme = "";
            String channelId = "";
            if (!"".equals(bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme()) &&
                    bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() != null) {
                locIdScheme = "," + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme();
            }

            if (!"".equals(getChannelidForSurvey()) &&
                    getChannelidForSurvey() != null) {
                channelId = "," + getChannelidForSurvey();
            }

            sb.append("SELECT Distinct Survey.SurveyId,Survey.GroupId,IfNull(LocationId,0) AS LocationId,IfNull(ChannelId,0) AS ChannelId," +
                    "Case  IFNULL(AttributeID ,-1) when -1  then '0' else '1' END as flag" +
                    ",IfNull(PriorityBiD,0) AS PriorityBiD,IfNull(RetailerID,0) AS RetailerID" +
                    " FROM (SELECT  DISTINCT SurveyId,GroupId FROM SurveyCriteriaMapping) AS Survey" +
                    " LEFT JOIN  (SELECT DISTINCT SurveyId,GroupId,CriteriaId LocationId  FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode='LOCATION' and listtype='SURVEY_CRITERIA_TYPE')  LS" +
                    " ON Survey.SurveyId=Ls.SurveyId and Survey.GroupId=LS.GroupId" +
                    " LEFT JOIN (SELECT SurveyId,GroupId,CriteriaId ChannelId FROM SurveyCriteriaMapping" +
                    " INNER JOIN StandardListMaster on ListId=CriteriaType" +
                    " WHERE ListCode in ('CHANNEL','SUBCHANNEL') and listtype='SURVEY_CRITERIA_TYPE') CS" +
                    " ON  Survey.SurveyId=CS.SurveyId and Survey.GroupId=CS.GroupId" +
                    " LEFT JOIN (" +
                    " SELECT SurveyId,GroupId,CriteriaId AttributeID FROM SurveyCriteriaMapping" +
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
                    " where ifNull(locationid,0) in(0" + locIdScheme + "," + bmodel.getRetailerMasterBO().getLocationId() + ")" +
                    " And ifnull(channelid,0) in (0" + channelId + "," + bmodel.getRetailerMasterBO().getSubchannelid() + ") And ifnull(PriorityBiD,0) in (0," + bmodel.getRetailerMasterBO().getPrioriryProductId() + ") "
                    + "And ifnull(RetailerID,0) in (0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (c.getInt(4) == 0 || (c.getInt(4) == 1 && mGroupIDList.contains(c.getInt(1) + "" + c.getInt(0)))) {

                        if (mSurveyIds.equals("")) {
                            mSurveyIds = c.getString(0);
                        } else {
                            mSurveyIds += "," + c.getString(0);
                        }
                    }

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception ex) {

            Commons.printException(ex);
        }
        return mSurveyIds;
    }


    public void downloadQuestionDetails(String moduleCode) {
        try {

            survey = new ArrayList<>();

            int tempSurveyId = -1;
            int tempQuestionId = -1;
            int tempOptionId = -1;
            int surveyIndex = -1;
            int questionIndex = -1;
            int optionIndex = -1;
            String mtempGName = "";
            String locationQuery = "";
            String channelQuery = "";
            String retailerid = "0";


            if (!fromHomeScreen) {
                locationQuery = "SCM.locid=" + bmodel.getRetailerMasterBO().getLocationId() + "  OR SCM.locid in (" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ")";
                channelQuery = "SCM.ChannelId=" + bmodel.getRetailerMasterBO().getSubchannelid() + "  OR SCM.ChannelId in (" + getChannelidForSurvey() + ")";
                if (bmodel.getRetailerMasterBO() != null)
                    retailerid = bmodel.getRetailerMasterBO().getRetailerID();
            }


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct SM.SurveyId, SM.SurveyDesc, SM.BonusPoint,");
            sb.append(" A.QId, A.QText, A.QType, IFNULL(C.ListCode, ''), A.BrandID, A.IsMand,");
            sb.append(" SMP.Weight,ifnull(SMP.GroupName,''), SMP.isScore, A.isPhotoReq, A.minPhoto,");
            sb.append(" A.maxPhoto,A.isBonus, IFNULL(OM.OptionId,0), OM.OptionText, OSM.Score,");
            sb.append(" CASE OSM.isExcluded WHEN '1' THEN 'true' ELSE 'false' END as isExcluded,");
            sb.append(" IFNULL(OD.DQID,0),IFNULL(SLM.listname,'NO FREQ') as freq,SMP.maxScore FROM SurveyCriteriaMapping SCM");
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
                sb.append(" AND SM.surveyId in(" + getValidSurveyIds() + ")");

            } else {
                if (moduleCode.equalsIgnoreCase("MENU_NEW_RET") && bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER)
                    sb.append(" AND SCM.CriteriaID=" + bmodel.newOutletHelper.getmSelectedChannelid() + "  and SL.listcode='CHANNEL' OR SL.listcode='SUBCHANNEL'");
            }

            sb.append(" and SM.SurveyId not in (select AH.surveyid from answerheader AH ");
            sb.append("Where retailerid = " + bmodel.QT(retailerid) + " and AH.frequency='DAILY_PIRAMAL')");
            sb.append(" ORDER BY SM.Sequence, SM.SurveyId, SMP.GroupName, SMP.Sequence, SMP.QID, OM.OptionId");
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

                        questionIndex = 0;
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
                                    questionBO.setImage1Path(HomeScreenFragment.folder
                                            .getPath() + imgName);
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
                                    questionBO.setImage2Path(HomeScreenFragment.folder
                                            .getPath() + imgName);
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
                                        questionBO.setImage1Path(HomeScreenFragment.folder
                                                .getPath() + "/" + imgName);
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
                                        questionBO.setImage2Path(HomeScreenFragment.folder
                                                .getPath() + imgName);
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


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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


        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {

                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();

                for (QuestionBO qus : mParentQuestions) {
                    if (qus.getSelectedAnswer().isEmpty() && qus.getSelectedAnswerIDs().isEmpty()) {
                        return false;
                    }
                }
            }

        }

        return true;
    }


    /**
     * Check whether all mandatory questions are answered
     *
     * @return true if all mandatory question answered else false
     */
    public boolean isMandatoryQuestionAnswered() {
        boolean returnFlag = true;
        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();

                for (QuestionBO qus : mParentQuestions) {
                    qus.setIsMandatoryQuestNotAnswered(false);
                    if (qus.getIsSubQuestion() == 0) {
                        if (qus.getIsMandatory() == 1) {
                            if (qus.getSelectedAnswer().isEmpty()
                                    && qus.getSelectedAnswerIDs().isEmpty()) {
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
                }
            }
        }

        return returnFlag;
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
                                        && subQBO.getSelectedAnswer().isEmpty()
                                        && subQBO.getSelectedAnswerIDs().isEmpty()) {
                                    subQBO.setIsMandatoryQuestNotAnswered(true);
                                    returnFlag = false;

                                }
                            }
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


        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();
                for (QuestionBO qus : mParentQuestions) {
                    if (!qus.getSelectedAnswer().isEmpty()
                            || !qus.getSelectedAnswerIDs().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasPhotoToSave() {


        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey || bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
                ArrayList<QuestionBO> mParentQuestions = sBO.getQuestions();
                for (QuestionBO qus : mParentQuestions) {
                    if (qus.getIsPhotoReq() > 0 && (!qus.getSelectedAnswer().isEmpty()
                            || !qus.getSelectedAnswerIDs().isEmpty())
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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String retailerid = "0";
            int distID = 0;
            String type = "RETAILER";
            int superwiserID;
            if ("MENU_SURVEY_SW".equalsIgnoreCase(menuCode)) {
                type = "SELLER";
                superwiserID = bmodel.mSurveyHelperNew.mSelectedSuperVisiorID;
            } else if (bmodel.configurationMasterHelper.IS_CNT01) {
                superwiserID = bmodel.getSelectedUserId();
            } else {
                superwiserID = 0;
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
                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    // delete transaction if exist

                    String sql = "SELECT uid FROM AnswerHeader WHERE"
                            + " surveyid = " + sBO.getSurveyID()
                            + " AND date = "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
                            + SDUtil.now(SDUtil.DATE_TIME_ID);
                    // update joint call
                    bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                    String headerColumns = "uid, surveyid, date, retailerid,distributorID, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,userid,frequency";

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

                                if ("TEXT".equals(questionBO.getQuestionType()) ||
                                        "NUM".equals(questionBO.getQuestionType()) ||
                                        "PERC".equals(questionBO.getQuestionType()) || "OPT".equals(questionBO.getQuestionType())
                                        && !questionBO
                                        .getSelectedAnswer().isEmpty()) {
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

                                } else {
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

                        for (SurveyBO qBO : bmodel.mSurveyHelperNew.getSurvey()) {
                            if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                String headerValues = QT(uid) + ","
                                        + sBO.getSurveyID() + ","
                                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                        + QT(retailerid) + "," + distID + "," + QT(surveyTypeStandardListId) + ","
                                        + superwiserID
                                        + "," + totalAchievedScore + "," + qBO.getTargtScore()
                                        + "," + QT(menuCode)
                                        + "," + qBO.getBonusScoreAchieved()
                                        + "," + qBO.getMaxBonusScore()
                                        + "," + QT(remarkDone) + "," + QT(type) + ",0,''"
                                        + "," + userid
                                        + "," + QT(sBO.getSurveyFreq());

                                db.insertSQL("AnswerHeader", headerColumns, headerValues);
                            }

                        }
                    }
                }


            } else {

                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    if (sBO.getSurveyID() == mSelectedSurvey) {
                        // delete transaction if exist
                        String sql = "SELECT uid FROM AnswerHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                + " AND retailerid = " + QT(retailerid)
                                + " AND distributorID = " + distID
                                + " AND ModuleID = " + QT(surveyTypeStandardListId)
                                + " AND menucode=" + QT(menuCode)
                                + " AND upload='N'" + " AND SupervisiorId = "
                                + superwiserID
                                + " AND userid=" + userid;
                        ;

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
                                + SDUtil.now(SDUtil.DATE_TIME_ID);

                        // update joint call
                        bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(menuCode, uid, oldUid);

                        String headerColumns = "uid, surveyid, date, retailerid,DistributorID, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,userid,frequency";

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

                                    if ("TEXT".equals(questionBO.getQuestionType()) ||
                                            "NUM".equals(questionBO.getQuestionType()) ||
                                            "PERC".equals(questionBO.getQuestionType()) && !questionBO
                                                    .getSelectedAnswer().isEmpty()) {
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

                                    } else {
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

                            for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
                                if (surBO.getSurveyID() == sBO.getSurveyID()) {

                                    String headerValues = QT(uid) + ","
                                            + surBO.getSurveyID() + ","
                                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                            + QT(retailerid) + "," + distID + "," + QT(surveyTypeStandardListId) + ","
                                            + superwiserID
                                            + "," + totalAchievedScore + "," + sBO.getTargtScore()
                                            + "," + QT(menuCode)
                                            + "," + sBO.getBonusScoreAchieved()
                                            + "," + sBO.getMaxBonusScore()
                                            + "," + QT(remarkDone) + "," + QT(type) + ",0,''"
                                            + "," + userid
                                            + "," + QT(sBO.getSurveyFreq());

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
            Commons.printException("" + e);
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

    public void saveCSSurveyAnswer(String flag) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String retailerid = "0";
            String type = "COUNTER";

            //cmd for SupervisiorId is not needed for customer wise feed back
           /* int superwiserID;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                superwiserID = bmodel.getSelectedUserId();
            } else {
                superwiserID = 0;
            }*/

            if (isFromCSsurvey()) {
                retailerid = bmodel.getRetailerMasterBO().getRetailerID();
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
                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    // delete transaction if exist

                    if (bmodel.getCounterSaleBO().isDraft()) {
                        String sql = "SELECT uid FROM AnswerHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                + " AND retailerid = " + QT(retailerid)
                                + " AND ModuleID = " + QT(surveyTypeStandardListId)
                                + " AND menucode=" + QT(cs_feedback_menucode)
                                + " AND SupervisiorId = 0"
                                + " AND upload='I' AND refid='" + bmodel.getCounterSaleBO().getLastUid() + "'";


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
                            headerCursor.close();
                        }
                    }
                    isData = false;

                    String uid = sBO.getSurveyID()
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + ""
                            + SDUtil.now(SDUtil.DATE_TIME_ID);
                    // update joint call
                    bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(cs_feedback_menucode, uid, oldUid);

                    String headerColumns = "uid, surveyid, date, retailerid, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,upload,userid";

                    mAllQuestions.addAll(sBO.getQuestions());
                    questionSize = mAllQuestions.size();
                    for (int ii = 0; ii < questionSize; ii++) {

                        questionBO = mAllQuestions.get(ii);
                        if (sBO.getSurveyID() == questionBO.getSurveyid()) {

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
                            for (int j = 0; j < answerSize; j++) {
                                String detailvalues = values1
                                        + ","
                                        + questionBO.getSelectedAnswerIDs()
                                        .get(j)
                                        + ","
                                        + DatabaseUtils
                                        .sqlEscapeString(questionBO
                                                .getSelectedAnswer().get(j))
                                        + "," + questionBO.getQuestScore()
                                        + "," + weight
                                        + "," + questionBO.getSurveyid()
                                        + "," + questionBO.getIsSubQuestion();

                                db.insertSQL("AnswerDetail", detailColumns,
                                        detailvalues);
                                isData = true;

                            }
                            for (int k = 0; k < questionBO.getImageNames().size(); k++) {
                                String detailImageValues = values2
                                        + ","
                                        + bmodel.QT(questionBO.getImageNames()
                                        .get(k));

                                db.insertSQL("AnswerImageDetail", detailImageColumns,
                                        detailImageValues);
                            }
                        }

                    }
                    if (isData) {

                        Commons.print("In Survey Save," + "" + remarkDone);

                        for (SurveyBO qBO : bmodel.mSurveyHelperNew.getSurvey()) {
                            if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                String headerValues = QT(uid) + ","
                                        + sBO.getSurveyID() + ","
                                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                        + QT(retailerid) + "," + QT(surveyTypeStandardListId) + ","
                                        + "0"
                                        + "," + qBO.getAchievedScore() + "," + qBO.getTargtScore()
                                        + "," + QT(cs_feedback_menucode)
                                        + "," + qBO.getBonusScoreAchieved()
                                        + "," + qBO.getMaxBonusScore()
                                        + "," + QT(remarkDone)
                                        + "," + QT(type)
                                        + "," + bmodel.getCounterId()
                                        + "," + QT(bmodel.mCounterSalesHelper.getUid())
                                        + "," + bmodel.QT(flag)
                                        + "," + bmodel.userMasterHelper.getUserMasterBO().getUserid();

                                db.insertSQL("AnswerHeader", headerColumns, headerValues);
                            }

                        }
                    }
                }


            } else {

                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    if (sBO.getSurveyID() == mSelectedSurvey) {
                        // delete transaction if exist
                        String sql = "SELECT uid FROM AnswerHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                + " AND retailerid = " + QT(retailerid)
                                + " AND ModuleID = " + QT(surveyTypeStandardListId)
                                + " AND menucode=" + QT(cs_feedback_menucode)
                                + " AND upload='I'" + " AND SupervisiorId =" + "0" + " AND refid='" + bmodel.getCounterSaleBO().getLastUid() + "'";

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
                            headerCursor.close();
                        }

                        isData = false;
                        String uid = sBO.getSurveyID()
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid() + ""
                                + SDUtil.now(SDUtil.DATE_TIME_ID);

                        // update joint call
                        bmodel.outletTimeStampHelper.updateJointCallDetailsByModuleWise(cs_feedback_menucode, uid, oldUid);

                        String headerColumns = "uid, surveyid, date, retailerid, ModuleID,SupervisiorId,achScore,tgtScore,menucode,AchBonusPoint,MaxBonusPoint,Remark,type,counterid,refid,upload,userid";

                        mAllQuestions.addAll(sBO.getQuestions());
                        questionSize = mAllQuestions.size();

                        for (int ii = 0; ii < questionSize; ii++) {

                            questionBO = mAllQuestions.get(ii);

                            if (sBO.getSurveyID() ==
                                    questionBO.getSurveyid()) {

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
                                for (int j = 0; j < answerSize; j++) {
                                    String detailvalues = values1
                                            + ","
                                            + questionBO.getSelectedAnswerIDs()
                                            .get(j)
                                            + ","
                                            + DatabaseUtils
                                            .sqlEscapeString(questionBO
                                                    .getSelectedAnswer().get(j))
                                            + "," + questionBO.getQuestScore()
                                            + "," + weight
                                            + "," + questionBO.getSurveyid()
                                            + "," + questionBO.getIsSubQuestion();

                                    db.insertSQL("AnswerDetail", detailColumns,
                                            detailvalues);
                                    isData = true;

                                }
                                for (int k = 0; k < questionBO.getImageNames().size(); k++) {
                                    String detailImageValues = values2
                                            + ","
                                            + bmodel.QT(questionBO.getImageNames()
                                            .get(k));

                                    db.insertSQL("AnswerImageDetail", detailImageColumns,
                                            detailImageValues);
                                }
                            }
                        }
                        if (isData) {

                            Commons.print("In Survey Save," + "" + remarkDone);

                            for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
                                if (surBO.getSurveyID() == sBO.getSurveyID()) {

                                    String headerValues = QT(uid) + ","
                                            + surBO.getSurveyID() + ","
                                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                            + QT(retailerid) + "," + QT(surveyTypeStandardListId) + ","
                                            + "0"
                                            + "," + sBO.getAchievedScore() + "," + sBO.getTargtScore()
                                            + "," + QT(cs_feedback_menucode)
                                            + "," + sBO.getBonusScoreAchieved()
                                            + "," + sBO.getMaxBonusScore()
                                            + "," + QT(remarkDone)
                                            + "," + QT(type)
                                            + "," + bmodel.getCounterId()
                                            + "," + QT(bmodel.mCounterSalesHelper.getUid())
                                            + "," + bmodel.QT(flag)
                                            + "," + bmodel.userMasterHelper.getUserMasterBO().getUserid();

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
            Commons.printException("" + e);
        }
    }

    public void deleteUnusedImages() {

        Vector<QuestionBO> mAllQuestions = new Vector<>();

        int questionSize;

        if (bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE) {
            for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {

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
            for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {

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
        File folder = new File(HomeScreenFragment.photoPath + "/");

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
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
        if (!isFromCSsurvey() && bmodel.configurationMasterHelper.IS_CNT01) {
            supervisiorId = bmodel.getSelectedUserId();
        }

        boolean isLocalData = false;// to check whether transaction record is there or not

        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {

            surveyId = sBO.getSurveyID();
            Vector<QuestionBO> mAllQuestions = new Vector<>();

            uid = "0";

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT uid FROM AnswerHeader  ");
            sb.append(" WHERE retailerid = " + QT(retailerid) + " AND distributorID = " + distID + " AND surveyid = ");
            sb.append(+surveyId + " AND date = ");
            sb.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            sb.append(" and upload='N' and supervisiorId = " + supervisiorId + " AND userid = " + userid);
            sb.append(" and frequency!='MULTIPLE'");

            Cursor answerHeaderCursor = db.selectSQL(sb.toString());
            if (answerHeaderCursor != null) {
                if (answerHeaderCursor.moveToNext()) {
                    uid = answerHeaderCursor.getString(0);
                    isLocalData = true;
                }
                answerHeaderCursor.close();
            }
            qsize = sBO.getQuestions().size();
            mAllQuestions.addAll(sBO.getQuestions());

            if (!isFromCSsurvey() && bmodel.configurationMasterHelper.IS_CNT01) {
                for (int ii = 0; ii < qsize; ii++) {
                    questionBO = mAllQuestions.get(ii);
                    if (surveyId == questionBO.getSurveyid()) {
                        questionBO.getSelectedAnswer().clear();
                        questionBO.getSelectedAnswerIDs().clear();

                    }
                }
            }

            if (!"0".equals(uid)) {
//                qsize = sBO.getQuestions().size();
//                mAllQuestions.addAll(sBO.getQuestions());

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

                                    if (subqBO.getQuestionID() == c.getInt(0))
                                        subqBO.setQuestScore(subqBO.getQuestScore() + c.getFloat(3));
                                    else
                                        subqBO.setQuestScore(c.getFloat(3));

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
                                                subqBO.setImage1Path(HomeScreenFragment.folder
                                                        .getPath() + imgName);
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
                                                subqBO.setImage2Path(HomeScreenFragment.folder
                                                        .getPath() + imgName);
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

            String query = "SELECT surveyId,qid, answerid, Answer FROM LastVisitSurvey WHERE"
                    + " retailerID = " + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {

                    for (SurveyBO surveyBO : bmodel.mSurveyHelperNew.getSurvey()) {
                        if (surveyBO.getSurveyID() == c.getInt(0)) {

                            for (QuestionBO questionBO : surveyBO.getQuestions()) {
                                if (questionBO.getQuestionID() == c.getInt(1)) {

                                    questionBO.setSelectedAnswerID(c.getInt(2));
                                    questionBO.setSelectedAnswer(c.getString(3));

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
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String uid = "0";

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
            if (c != null) {

                while (c.moveToNext()) {
                    if (c.getInt(4) == 0) {
                        for (SurveyBO surveyBO : bmodel.mSurveyHelperNew.getSurvey()) {
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

        db.closeDB();
    }

    public void loadCSSurveyAnswers(int supervisiorId, String referenceId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String retailerid;
        retailerid = bmodel.getRetailerMasterBO().getRetailerID();

        if (!isFromCSsurvey() && bmodel.configurationMasterHelper.IS_CNT01) {
            supervisiorId = bmodel.getSelectedUserId();
        }
        int qsize = 0;


        int surveyId;
        String uid;

        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {

            surveyId = sBO.getSurveyID();
            Vector<QuestionBO> mAllQuestions = new Vector<>();

            uid = "0";

            String sql = "SELECT uid FROM AnswerHeader WHERE"
                    + " retailerid = " + QT(retailerid) + " AND surveyid = "
                    + surveyId + " AND date = "
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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


        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {

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
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " and upload='N' and supervisiorId = " + supervisiorId;

            Cursor answerHeaderCursor = db.selectSQL(sql);
            if (answerHeaderCursor != null) {
                if (answerHeaderCursor.moveToNext()) {
                    uid = answerHeaderCursor.getString(0);
                }
                answerHeaderCursor.close();
            }

            if (!"0".equals(uid)) {

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


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    // delete transaction if exist

                    String sql = "SELECT uid FROM NewRetailerSurveyResultHeader WHERE"
                            + " surveyid = " + sBO.getSurveyID()
                            + " AND date = "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
                            + SDUtil.now(SDUtil.DATE_TIME_ID);
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

                        for (SurveyBO qBO : bmodel.mSurveyHelperNew.getSurvey()) {
                            if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                String headerValues = QT(uid) + ","
                                        + sBO.getSurveyID() + ","
                                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
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

                for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
                    if (sBO.getSurveyID() == mSelectedSurvey) {
                        // delete transaction if exist
                        String sql = "SELECT uid FROM NewRetailerSurveyResultHeader WHERE"
                                + " surveyid = " + sBO.getSurveyID()
                                + " AND date = "
                                + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
                                + SDUtil.now(SDUtil.DATE_TIME_ID);

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

                            for (SurveyBO qBO : bmodel.mSurveyHelperNew.getSurvey()) {
                                if (qBO.getSurveyID() == sBO.getSurveyID()) {

                                    String headerValues = QT(uid) + ","
                                            + sBO.getSurveyID() + ","
                                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

}
