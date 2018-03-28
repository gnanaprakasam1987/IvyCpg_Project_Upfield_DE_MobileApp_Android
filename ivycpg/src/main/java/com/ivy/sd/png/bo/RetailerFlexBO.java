package com.ivy.sd.png.bo;

/**
 * Created by ramkumard on 20/3/18.
 */

public class RetailerFlexBO {

    private String id;
    private String name;

    public RetailerFlexBO(){

    }

    public RetailerFlexBO(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
