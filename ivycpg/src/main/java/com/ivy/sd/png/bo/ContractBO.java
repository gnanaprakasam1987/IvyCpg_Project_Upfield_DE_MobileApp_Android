package com.ivy.sd.png.bo;


import java.util.Comparator;

public class ContractBO {
    private String outletCode;
    private String outletName;
    private String subChannel;
    private String tradeID;
    private String tradeName;
    private String startDate;
    private String endDate;
    private int daysToExp;
    private String contractID;

    public static final Comparator<ContractBO> DayToExpiryComparator = new Comparator<ContractBO>() {

        public int compare(ContractBO bo1, ContractBO bo2) {

            int mExpiry1 = bo1.getDaysToExp();
            int mExpiry2 = bo2.getDaysToExp();

            if (mExpiry1 == 0 || mExpiry2 == 0) {
                return bo1.getOutletName().compareToIgnoreCase(bo2.getOutletName());
            }
            return mExpiry1 - mExpiry2;
        }

    };


    public String getOutletCode() {
        return outletCode;
    }

    public void setOutletCode(String outletCode) {
        this.outletCode = outletCode;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getSubChannel() {
        return subChannel;
    }

    public void setSubChannel(String subChannel) {
        this.subChannel = subChannel;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getDaysToExp() {
        return daysToExp;
    }

    public void setDaysToExp(int daysToExp) {
        this.daysToExp = daysToExp;
    }
    public String getContractID() {
        return contractID;
    }

    public void setContractID(String contractID) {
        this.contractID = contractID;
    }
}
