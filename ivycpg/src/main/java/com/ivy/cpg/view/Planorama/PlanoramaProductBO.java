package com.ivy.cpg.view.Planorama;

public class PlanoramaProductBO {

    private String productId,productName;
    private int numberOfFacings;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getNumberOfFacings() {
        return numberOfFacings;
    }

    public void setNumberOfFacings(int numberOfFacings) {
        this.numberOfFacings = numberOfFacings;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    private boolean isAvailable;
    private String sosTarget;

    public String getSosTarget() {
        return sosTarget;
    }

    public void setSosTarget(String sosTarget) {
        this.sosTarget = sosTarget;
    }

    public String getSosActual() {
        return sosActual;
    }

    public void setSosActual(String sosActual) {
        this.sosActual = sosActual;
    }

    private String sosActual;


}
