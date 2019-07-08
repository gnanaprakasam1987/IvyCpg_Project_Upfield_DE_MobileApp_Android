package com.ivy.cpg.view.van.vanstockapply;

import java.util.Vector;

public class VanLoadMasterBO {

    private int pid, bid, duomqty, caseUomId, caseqty, pieceUomId,
            pieceqty, sih, odameteruid, outerQty, dOuomQty, outerUomId;

    private float msqqty;
    private String pname, psname, barcode, pcode, rfield1, odameterdate, startdatetime, starttime, endtime;
    private double odameterstart, odameterend;
    private int isended, isstarted;
    private String batchno, batchId, mfgDate,
            expDate;


    private String startTripImg,endTripImg;
    private Vector<VanLoadMasterBO> batchlist;

    public VanLoadMasterBO() {

    }

    public VanLoadMasterBO(VanLoadMasterBO vanloadbo) {
        this.pid = vanloadbo.pid;
        this.bid = vanloadbo.bid;
        this.duomqty = vanloadbo.duomqty;
        this.caseUomId = vanloadbo.caseUomId;
        this.caseqty = vanloadbo.caseqty;
        this.pieceUomId = vanloadbo.pieceUomId;
        this.pieceqty = vanloadbo.pieceqty;
        this.sih = vanloadbo.sih;
        this.odameteruid = vanloadbo.odameteruid;
        this.outerQty = vanloadbo.outerQty;
        this.dOuomQty = vanloadbo.dOuomQty;
        this.outerUomId = vanloadbo.outerUomId;
        this.msqqty = vanloadbo.msqqty;
        this.pname = vanloadbo.pname;
        this.psname = vanloadbo.psname;
        this.barcode = vanloadbo.barcode;
        this.pcode = vanloadbo.pcode;
        this.rfield1 = vanloadbo.rfield1;
        this.odameterdate = vanloadbo.odameterdate;
        this.odameterstart = vanloadbo.odameterstart;
        this.odameterend = vanloadbo.odameterend;
        this.isended = vanloadbo.isended;
        this.isstarted = vanloadbo.isstarted;
        this.batchno = vanloadbo.batchno;
        this.batchId = vanloadbo.batchId;
        this.mfgDate = vanloadbo.mfgDate;
        this.expDate = vanloadbo.expDate;
        this.batchlist = vanloadbo.batchlist;
        this.startdatetime = vanloadbo.startdatetime;
        this.starttime = vanloadbo.starttime;
        this.endtime = vanloadbo.endtime;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public Vector<VanLoadMasterBO> getBatchlist() {
        return batchlist;
    }

    public void setBatchlist(Vector<VanLoadMasterBO> batchno) {
        this.batchlist = batchno;
    }

    public String getBatchno() {
        return batchno;
    }

    public void setBatchno(String batchno) {
        this.batchno = batchno;
    }

    public int getIsstarted() {
        return isstarted;
    }

    public void setIsstarted(int isstarted) {
        this.isstarted = isstarted;
    }

    public int getIsended() {
        return isended;
    }

    public void setIsended(int isended) {
        this.isended = isended;
    }

    public int getOuterUomId() {
        return outerUomId;
    }

    public void setOuterUomId(int outerUomId) {
        this.outerUomId = outerUomId;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }


    public int getCasesize() {
        return duomqty;
    }

    public void setCasesize(int duomqty) {
        this.duomqty = duomqty;
    }

    public int getDuomqty() {
        return duomqty;
    }

    public void setDuomqty(int duomqty) {
        this.duomqty = duomqty;
    }

    public int getCaseUomId() {
        return caseUomId;
    }

    public void setCaseUomId(int caseUomId) {
        this.caseUomId = caseUomId;
    }

    public float getMsqqty() {
        return msqqty;
    }

    public void setMsqqty(float msqqty) {
        this.msqqty = msqqty;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPsname() {
        return psname;
    }

    public void setPsname(String psname) {
        this.psname = psname;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public String getRfield1() {
        return rfield1;
    }

    public void setRfield1(String rfield1) {
        this.rfield1 = rfield1;
    }

    public int getCaseqty() {
        return caseqty;
    }

    public void setCaseqty(int caseqty) {
        this.caseqty = caseqty;
    }

    public int getPieceqty() {
        return pieceqty;
    }

    public void setPieceqty(int pieceqty) {
        this.pieceqty = pieceqty;

    }

    public int getSih() {
        return sih;
    }

    public void setSih(int sih) {
        this.sih = sih;
    }

    public int getOdameteruid() {
        return odameteruid;
    }

    public void setOdameteruid(int odameteruid) {
        this.odameteruid = odameteruid;

    }

    public double getOdameterstart() {
        return odameterstart;

    }

    public void setOdameterstart(double odameterstart) {
        this.odameterstart = odameterstart;
    }

    public double getOdameterend() {
        return odameterend;

    }

    public void setOdameterend(double odameterend) {
        this.odameterend = odameterend;

    }

    public String getodameterdate() {
        return odameterdate;
    }

    public void setOdameterdate(String odameterdate) {
        this.odameterdate = odameterdate;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public int getOuterSize() {
        return dOuomQty;
    }

    public void setOuterSize(int dOuomQty) {
        this.dOuomQty = dOuomQty;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public int getPieceUomId() {
        return pieceUomId;
    }

    public void setPieceUomId(int pieceUomId) {
        this.pieceUomId = pieceUomId;
    }

    public String getStartdatetime() {
        return startdatetime;
    }

    public void setStartdatetime(String startdatetime) {
        this.startdatetime = startdatetime;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }


    public String getStartTripImg() {
        return startTripImg;
    }

    public void setStartTripImg(String startTripImg) {
        this.startTripImg = startTripImg;
    }

    public String getEndTripImg() {
        return endTripImg;
    }

    public void setEndTripImg(String endTripImg) {
        this.endTripImg = endTripImg;
    }
}
