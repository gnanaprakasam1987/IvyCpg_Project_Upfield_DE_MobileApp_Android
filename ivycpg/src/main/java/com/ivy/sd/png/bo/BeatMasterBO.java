package com.ivy.sd.png.bo;

public class BeatMasterBO {

    private int beatId;
    private String beatDescription;



    private String beatCode;
    private int today;
    private int userId;
    public BeatMasterBO() {

        // Empty Constructor
    }

    public BeatMasterBO(int beatId, String beatDescription, int today) {
        super();
        this.beatId = beatId;
        this.beatDescription = beatDescription;
        this.today = today;
    }

    public String toString() {
        return beatDescription;
    }

    public int getBeatId() {
        return beatId;
    }

    public void setBeatId(int beatId) {
        this.beatId = beatId;
    }

    public String getBeatDescription() {
        return beatDescription;
    }

    public void setBeatDescription(String beatDescription) {
        this.beatDescription = beatDescription;
    }

    public int getToday() {
        return today;
    }

    public void setToday(int today) {
        this.today = today;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBeatCode() {
        return beatCode;
    }

    public void setBeatCode(String beatCode) {
        this.beatCode = beatCode;
    }
}
