package com.ivy.cpg.view.denomination;

/**
 * Created by murugan on 3/9/18.
 */

public class DenominationBO {

    private String denomintionId;
    private String denominationDisplayName;
    private String denominationDisplayNameValues;
    private String count;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }



    public String getDenomintionId() {
        return denomintionId;
    }

    public void setDenomintionId(String denomintionId) {
        this.denomintionId = denomintionId;
    }

    public String getDenominationDisplayName() {
        return denominationDisplayName;
    }

    public void setDenominationDisplayName(String denominationDisplayName) {
        this.denominationDisplayName = denominationDisplayName;
    }

    public String getDenominationDisplayNameValues() {
        return denominationDisplayNameValues;
    }

    public void setDenominationDisplayNameValues(String denominationDisplayNameValues) {
        this.denominationDisplayNameValues = denominationDisplayNameValues;
    }
}
