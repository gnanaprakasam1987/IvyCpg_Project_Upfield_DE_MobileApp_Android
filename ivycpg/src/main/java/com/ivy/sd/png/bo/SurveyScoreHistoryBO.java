package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class SurveyScoreHistoryBO {
    private int weight;
    private int groupID;
    private String surveyName;
    private ArrayList<Integer> score;

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public ArrayList<Integer> getScore() {
        return score;
    }

    public void setScore(ArrayList<Integer> score) {
        this.score = score;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }
}
