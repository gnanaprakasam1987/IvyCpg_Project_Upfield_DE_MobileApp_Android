package com.ivy.countersales.bo;

/**
 * Created by rajkumar.s on 6/23/2017.
 */

public class CS_StockReasonBO {

    public  CS_StockReasonBO(CS_StockReasonBO bo){
        this.reasonID=bo.getReasonID();
        this.reasonDesc=bo.getReasonDesc();
        this.reasonCategory=bo.getReasonCategory();
        this.pieceQty=bo.getPieceQty();
    }
    public  CS_StockReasonBO(){

    }


    public String getReasonID() {
        return reasonID;
    }

    public void setReasonID(String reasonID) {
        this.reasonID = reasonID;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public int getPieceQty() {
        return pieceQty;
    }

    public void setPieceQty(int pieceQty) {
        this.pieceQty = pieceQty;
    }

    private String reasonID;
    private String reasonDesc;
    private String reasonCategory;
    private int pieceQty;

    @Override
    public String toString() {
        return reasonDesc;
    }
}
