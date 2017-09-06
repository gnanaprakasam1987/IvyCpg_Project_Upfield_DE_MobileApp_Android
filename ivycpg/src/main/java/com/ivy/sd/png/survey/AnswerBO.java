package com.ivy.sd.png.survey;

import java.util.ArrayList;

public class AnswerBO {

    private int answerID;
    private float score;
    private boolean excluded;
    private String answer;

    private final ArrayList<Integer> questionList = new ArrayList<>();

    public int getAnswerID() {
        return answerID;
    }

    public void setAnswerID(int answerID) {
        this.answerID = answerID;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public ArrayList<Integer> getQuestionList() {
        return questionList;
    }

	public String toString() {
		return answer;
	}
}
