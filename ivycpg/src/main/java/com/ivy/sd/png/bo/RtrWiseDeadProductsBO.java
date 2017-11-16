package com.ivy.sd.png.bo;

/**
 * Created by dharmapriya.k on 11/1/2017,2:33 PM.
 */
public class RtrWiseDeadProductsBO {
    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    private int rid;
    private int pid;
    private String flag;
}
