package com.ivy.sd.png.bo;

import java.util.HashMap;

public class MvpBO {

    private int userID;
    private int parentID;
    private int rank;
    private int totalRank;
    private int totalScore;
    private int userPosID;
    private int parentPosID;
    private String username;
    private String entitylevel;

    private HashMap<Integer,Integer> badgeList=new HashMap<>();

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTotalRank() {
        return totalRank;
    }

    public void setTotalRank(int totalRank) {
        this.totalRank = totalRank;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public HashMap<Integer, Integer> getBadgeList() {
        return badgeList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEntitylevel() {
        return entitylevel;
    }

    public void setEntitylevel(String entitylevel) {
        this.entitylevel = entitylevel;
    }

    public int getUserPosID() {
        return userPosID;
    }

    public void setUserPosID(int userPosID) {
        this.userPosID = userPosID;
    }

    public int getParentPosID() {
        return parentPosID;
    }

    public void setParentPosID(int parentPosID) {
        this.parentPosID = parentPosID;
    }
}

