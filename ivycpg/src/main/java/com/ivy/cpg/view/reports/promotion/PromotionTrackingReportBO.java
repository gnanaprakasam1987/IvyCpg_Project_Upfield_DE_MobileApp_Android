package com.ivy.cpg.view.reports.promotion;

/**
 * Created by anandasir.v on 8/30/2017.
 */

public class PromotionTrackingReportBO {

    private String BrandName, PromoName, Reason;
    private String IsExecuted, HasAnnouncer;

    public String getBrandName() {
        return BrandName;
    }

    public void setBrandName(String brandName) {
        BrandName = brandName;
    }

    public String getPromoName() {
        return PromoName;
    }

    public void setPromoName(String promoName) {
        PromoName = promoName;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public String getIsExecuted() {
        return IsExecuted;
    }

    public void setIsExecuted(String isExecuted) {
        IsExecuted = isExecuted;
    }

    public String getHasAnnouncer() {
        return HasAnnouncer;
    }

    public void setHasAnnouncer(String hasAnnouncer) {
        HasAnnouncer = hasAnnouncer;
    }
}
