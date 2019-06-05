package com.ivy.cpg.view.survey;

import java.util.ArrayList;

public class QuestionBO {


    private int questionID, brandID;
    private String questionDescription;
    private int surveyid;
    private String questionType;
    private int questionTypeId;
    private int isMandatory;
    private int questWeight;
    private int isScore;
    private int isPhotoReq;
    private int minPhoto;
    private int maxPhoto;
    private int isBonus;
    private float questScore;
    private boolean excludeQuestionWeight;
    private String groupName = "";
    private int isSubQuestion = 0;
    private boolean isMandatoryQuestNotAnswered;

    private final ArrayList<AnswerBO> answersList = new ArrayList<>();

    private ArrayList<Integer> selectedAnswerIDs = new ArrayList<>();
    private final ArrayList<String> imageNames = new ArrayList<>();
    private final ArrayList<String> selectedAnswer = new ArrayList<>();

    private String fromValue;
    private String toValue;
    private int precision;
    //private String selectedAnswer = "";

    private String image1Path, image2Path;
    private boolean image1Captured, image2Captured;
    private String tempImagePath;
    private String questionNo;
    private int minValue;
    private int maxValue;

    public String getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(String questionNo) {
        this.questionNo = questionNo;
    }

    public String getImage1Path() {
        return image1Path;
    }

    public void setImage1Path(String image1Path) {
        this.image1Path = image1Path;
    }

    public String getImage2Path() {
        return image2Path;
    }

    public void setImage2Path(String image2Path) {
        this.image2Path = image2Path;
    }

    public boolean isImage1Captured() {
        return image1Captured;
    }

    public void setImage1Captured(boolean image1Captured) {
        this.image1Captured = image1Captured;
    }

    public boolean isImage2Captured() {
        return image2Captured;
    }

    public void setImage2Captured(boolean image2Captured) {
        this.image2Captured = image2Captured;
    }

    public int getBrandID() {
        return brandID;
    }

    public void setBrandID(int brandID) {
        this.brandID = brandID;
    }

    public int getSurveyid() {
        return surveyid;
    }

    public void setSurveyid(int surveyid) {
        this.surveyid = surveyid;
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public void setQuestionDescription(String questionDescription) {
        this.questionDescription = questionDescription;
    }

    public String toString() {
        return questionDescription;
    }

    public int getQuestWeight() {
        return questWeight;
    }

    public void setQuestWeight(int questWeight) {
        this.questWeight = questWeight;
    }

    public int getIsScore() {
        return isScore;
    }

    public void setIsScore(int isScore) {
        this.isScore = isScore;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isExcludeQuestionWeight() {
        return excludeQuestionWeight;
    }

    public void setExcludeQuestionWeight(boolean excludeQuestionWeight) {
        this.excludeQuestionWeight = excludeQuestionWeight;
    }

    /**
     * Question Type A - Single Choice B - Multi Choice C - Text Input
     *
     * @return
     */
    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public ArrayList<Integer> getSelectedAnswerIDs() {
        return selectedAnswerIDs;
    }

    public void setSelectedAnswerIDs(ArrayList<Integer> answerIDs) {
        this.selectedAnswerIDs = answerIDs;
    }

    public void setSelectedAnswerID(int answerID) {
        if (selectedAnswerIDs != null && selectedAnswerIDs.size() > 0) {
            if (!selectedAnswerIDs.contains(answerID)) {
                this.selectedAnswerIDs.add(answerID);
            }

        } else {
            this.selectedAnswerIDs.add(answerID);
        }
    }

    public ArrayList<String> getImageNames() {
        return imageNames;
    }


    public ArrayList<String> getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String answer) {
        this.selectedAnswer.add(answer);
    }

   /* public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }*/

    public float getQuestScore() {
        return questScore;
    }

    public void setQuestScore(float questScore) {
        this.questScore = questScore;
    }

    public ArrayList<AnswerBO> getAnswersList() {
        return answersList;
    }

    public int getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(int questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public int getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(int isMandatory) {
        this.isMandatory = isMandatory;
    }

    public int getIsPhotoReq() {
        return isPhotoReq;
    }

    public void setIsPhotoReq(int isPhotoReq) {
        this.isPhotoReq = isPhotoReq;
    }

    public int getMinPhoto() {
        return minPhoto;
    }

    public void setMinPhoto(int minPhoto) {
        this.minPhoto = minPhoto;
    }

    public int getMaxPhoto() {
        return maxPhoto;
    }

    public void setMaxPhoto(int maxPhoto) {
        this.maxPhoto = maxPhoto;
    }

    public int getIsBonus() {
        return isBonus;
    }

    public void setIsBonus(int isBonus) {
        this.isBonus = isBonus;
    }

    public int getIsSubQuestion() {
        return isSubQuestion;
    }

    public void setIsSubQuestion(int isSubQuestion) {
        this.isSubQuestion = isSubQuestion;
    }

    public boolean isMandatoryQuestNotAnswered() {
        return isMandatoryQuestNotAnswered;
    }

    public void setIsMandatoryQuestNotAnswered(boolean isMandatoryQuestNotAnswered) {
        this.isMandatoryQuestNotAnswered = isMandatoryQuestNotAnswered;
    }

    public String getTempImagePath() {
        return tempImagePath;
    }

    public void setTempImagePath(String tempImagePath) {
        this.tempImagePath = tempImagePath;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    private double maxScore;

    public String getFromValue() {
        return fromValue;
    }

    public void setFromValue(String fromValue) {
        this.fromValue = fromValue;
    }

    public String getToValue() {
        return toValue;
    }

    public void setToValue(String toValue) {
        this.toValue = toValue;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    private String parentHierarchy;

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

}
