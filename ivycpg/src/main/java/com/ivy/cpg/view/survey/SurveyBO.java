package com.ivy.cpg.view.survey;

import java.util.ArrayList;

public class SurveyBO {

	private int surveyID;

	private String surveyName;
	private String surveyFreq;

	private float maxBonusScore;

	private float achievedScore, targtScore, bonusScoreAchieved;
	private boolean isSignatureRequired;
	private String signaturePath;


	private final ArrayList<QuestionBO> questions = new ArrayList<>();


	public int getSurveyID() {
		return surveyID;
	}

	public void setSurveyID(int surveyID) {
		this.surveyID = surveyID;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public float getMaxBonusScore() {
		return maxBonusScore;
	}

	public void setMaxBonusScore(float maxBonusScore) {
		this.maxBonusScore = maxBonusScore;
	}

	public ArrayList<QuestionBO> getQuestions() {
		return questions;
	}

	public float getAchievedScore() {
		return achievedScore;
	}

	public void setAchievedScore(float achievedScore) {
		this.achievedScore = achievedScore;
	}

	public float getTargtScore() {
		return targtScore;
	}

	public void setTargtScore(float targtScore) {
		this.targtScore = targtScore;
	}

	public float getBonusScoreAchieved() {
		return bonusScoreAchieved;
	}

	public void setBonusScoreAchieved(float bonusScoreAchieved) {
		this.bonusScoreAchieved = bonusScoreAchieved;
	}

	public String getSurveyFreq() {
		return surveyFreq;
	}

	public void setSurveyFreq(String surveyFreq) {
		this.surveyFreq = surveyFreq;
	}

	public boolean isSignatureRequired() {
		return isSignatureRequired;
	}

	public void setSignatureRequired(boolean signatureRequired) {
		isSignatureRequired = signatureRequired;
	}

	public String getSignaturePath() {
		return signaturePath;
	}

	public void setSignaturePath(String signaturePath) {
		this.signaturePath = signaturePath;
	}
}
